package eu.halyid.drone.activity;

import android.Manifest;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.common.OwnCloudCredentialsFactory;
import com.owncloud.android.lib.common.network.OnDatatransferProgressListener;
import com.owncloud.android.lib.common.operations.OnRemoteOperationListener;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.CreateRemoteFolderOperation;
import com.owncloud.android.lib.resources.files.UploadRemoteFileOperation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import dji.common.camera.SettingsDefinitions;
import dji.common.error.DJICameraError;
import dji.common.error.DJIError;
import dji.common.util.CommonCallbacks;
import dji.sdk.media.DownloadListener;
import dji.sdk.media.MediaFile;
import dji.sdk.media.MediaManager;
import eu.halyid.drone.R;
import eu.halyid.drone.util.DJIProcedures;
import eu.halyid.drone.util.ToastUtils;
import okhttp3.ResponseBody;

public class MediaActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback, OnRemoteOperationListener, OnDatatransferProgressListener {

    private OkHttpClient client;

    private ListView listCamera;
    private ListView listLocal;
    private ListView listCloud;
    private Button buttonDownload;
    private Button buttonDeleteDrone;
    private Button buttonUpload;
    private Button buttonDetect;
    private Button buttonDeleteLocal;
    private TextView textviewLogMedia;
    private Spinner spinnerDataSet;
    private MediaManager mediaManager;
    private OwnCloudClient mClient;
    private Handler mHandler = new Handler();
    int totalFilesToDownload = 0;
    int downloadedFilesToDownload = 0;
    int totalFilesToUpload = 0;
    int uploadedFilesToUpload = 0;
    List<String> cloudFiles;
    int totalFiles = 0;
    int totalBugDetected = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

         client = new OkHttpClient.Builder()
                 .connectTimeout(30, TimeUnit.SECONDS)
                 .writeTimeout(30, TimeUnit.SECONDS)
                 .readTimeout(30, TimeUnit.SECONDS)
                 .build();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_media);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            mediaManager = DJIProcedures.getProductInstance().getCamera().getMediaManager();
        } catch (Exception e) {

        }

        initUI();
    }

    private void initUI() {
        listCamera = findViewById(R.id.list_camera);
        listLocal = findViewById(R.id.list_local);
        listCloud = findViewById(R.id.list_cloud);

        buttonDownload = findViewById(R.id.download_files);
        buttonDeleteDrone = findViewById(R.id.delete_drone_files);
        buttonUpload = findViewById(R.id.upload_files);
        buttonDeleteLocal = findViewById(R.id.delete_local_files);
        buttonDetect = findViewById(R.id.detect_files);

        textviewLogMedia = findViewById(R.id.media_log);

        buttonDownload.setOnClickListener(this);
        buttonDeleteDrone.setOnClickListener(this);
        buttonUpload.setOnClickListener(this);
        buttonDeleteLocal.setOnClickListener(this);
        buttonDetect.setOnClickListener(this);

        spinnerDataSet = findViewById(R.id.spinner_data_set);

        loadCameraFiles();
        loadLocalFiles();
        loadCloudFiles();
        loadSpinner();
    }

    private void loadSpinner() {
        ArrayList<String> datasetList = new ArrayList<>();
        datasetList.add("M-2023");
        datasetList.add("L-2023");
        datasetList.add("XL-2023");

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, datasetList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerDataSet.setAdapter(adapter);
    }

    private void loadCameraFiles() {
        if (mediaManager != null) {
            mediaManager.refreshFileListOfStorageLocation(SettingsDefinitions.StorageLocation.SDCARD, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError == null) {
                        List<MediaFile> mediaFiles = mediaManager.getSDCardFileListSnapshot();
                        int count = mediaFiles != null ? mediaFiles.size() : 0;
                        if (count > 0) {
                            String[] cameraFilesArray = new String[count];

                            for (int i = 0; i < count; i++) {
                                cameraFilesArray[i] = mediaFiles.get(i).getFileName();
                            }

                            ArrayAdapter adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.custom_list_item, cameraFilesArray);

                            mHandler.post(new Runnable() {
                                @Override
                                public void run() {
                                    listCamera.setAdapter(adapter);
                                }
                            });
                        }
                    }
                }
            });
        } else {
            ToastUtils.setResultToToast("Unable to load drone camera media");
        }
    }

    private void loadLocalFiles() {
        String finalDir = "/HalyID/";
        File destDir = new File(Environment.getExternalStorageDirectory().getPath() + finalDir);
        destDir.mkdir();
        File[] files = destDir.listFiles();
        Arrays.sort(files, Collections.reverseOrder());

        int count = files != null ? files.length : 0;
        String[] cameraFilesArray = new String[count];

        for (int i = 0; i < count; i++) {
            cameraFilesArray[i] = files[i].getName();
        }

        ArrayAdapter adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.custom_list_item, cameraFilesArray);

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                listLocal.setAdapter(adapter);
            }
        });
    }

    private void loadCloudFiles() {

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.download_files) {
            downloadFiles();
        } else if (id == R.id.delete_drone_files) {
            deleteDroneFiles();
        } else if (id == R.id.upload_files) {
            uploadFiles();
        } else if (id == R.id.delete_local_files) {
            deleteLocalFiles();
        } else if (id == R.id.detect_files) {
            try {
                detectFiles();
            } catch (IOException e) {
                throw new RuntimeException(e);
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private void detectFiles() throws IOException, JSONException  {
        String finalDir = "/HalyID/";
        File destDir = new File(Environment.getExternalStorageDirectory().getPath() + finalDir);
        destDir.mkdir();
        File[] files = destDir.listFiles();

        ToastUtils.setResultToText(textviewLogMedia, "Files to evaluate=" + files.length);

        totalFiles = 0;
        totalBugDetected = 0;

        for (File file : files) {

            RequestBody requestBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("image", file.getName(),
                            RequestBody.create(MediaType.parse("image/jpeg"), file))
                    .build();

            TextView input_ip = findViewById(R.id.input_ip);

            String selectedDataset = spinnerDataSet.getSelectedItem().toString();

            String url = "http://" + input_ip.getText().toString() + ":5000" + "/android/" + selectedDataset;

            Request request = new Request.Builder()
                    .url(url)
                    .post(requestBody)
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                public void onResponse(Call call, Response response) throws IOException {
                    try (ResponseBody responseBody = response.body()) {
                        String jsonArrayString = response.body().string();
                        JSONArray jsonArray = new JSONArray(jsonArrayString);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            totalBugDetected++;

                            // access the elements of the individual JSONObjects
//                            float x_min = Float.parseFloat(jsonObject.getString("xmin"));
//                            float y_min = Float.parseFloat(jsonObject.getString("ymin"));
//                            float x_max = Float.parseFloat(jsonObject.getString("xmax"));
//                            float y_max = Float.parseFloat(jsonObject.getString("ymax"));
//                            String confidence = jsonObject.getString("confidence");
//                            String objClass = jsonObject.getString("class");
//
//                            String res = "(" + x_min + "," + x_max + ") ";
//                            res += "(" + y_min + "," + y_max + ") ";
//                            res += "confidence=" + confidence + ", class=" + objClass;

//                            Toast.makeText(MediaActivity.this, res, Toast.LENGTH_SHORT).show();
                        }
                        totalFiles++;
                        ToastUtils.setResultToText(textviewLogMedia, "Evaluated=" + totalFiles + "/" + files.length + " Detected=" + totalBugDetected);
//                        if (jsonArray.length() == 0) {
//                            Toast.makeText(MediaActivity.this, "Nothing found!", Toast.LENGTH_SHORT).show();
//                        }
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                }

                public void onFailure(Call call, IOException e) {
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            ToastUtils.setResultToText(textviewLogMedia, e.getMessage().toString());
                        }
                    });
                }
            });


//            try (Response response = client.newCall(request).execute()) {
//                if (!response.isSuccessful()) {
//                    throw new IOException("Unexpected code " + response);
//                }
//
//                String jsonArrayString = response.body().string();
//                JSONArray jsonArray = new JSONArray(jsonArrayString);
//                for (int i = 0; i < jsonArray.length(); i++) {
//                    JSONObject jsonObject = jsonArray.getJSONObject(i);
//
//                    // access the elements of the individual JSONObjects
//                    float x_min = Float.parseFloat(jsonObject.getString("xmin"));
//                    float y_min = Float.parseFloat(jsonObject.getString("ymin"));
//                    float x_max = Float.parseFloat(jsonObject.getString("xmax"));
//                    float y_max = Float.parseFloat(jsonObject.getString("ymax"));
//                    String confidence = jsonObject.getString("confidence");
//                    String objClass = jsonObject.getString("class");
//
//                    String res = "(" + x_min + "," + x_max + ") ";
//                    res += "(" + y_min + "," + y_max + ") ";
//                    res += "confidence=" + confidence + ", class=" + objClass;
//
//                    Toast.makeText(MediaActivity.this, res, Toast.LENGTH_SHORT).show();
//                }
//
//                if (jsonArray.length() == 0) {
//                    Toast.makeText(MediaActivity.this, "Nothing found!", Toast.LENGTH_SHORT).show();
//                }
//            }
        }
    }

    private void downloadFiles() {
        if (mediaManager != null) {
            mediaManager.refreshFileListOfStorageLocation(SettingsDefinitions.StorageLocation.SDCARD, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError == null) {
                        List<MediaFile> mediaFiles = mediaManager.getSDCardFileListSnapshot();
                        totalFilesToDownload = mediaFiles != null ? mediaFiles.size() : 0;
//                        ToastUtils.setResultToToast("Total files to Download=" + totalFilesToDownload);

                        if (totalFilesToDownload > 0) {
                            String finalDir = "/HalyID/";
                            File destDir = new File(Environment.getExternalStorageDirectory().getPath() + finalDir);

                            downloadedFilesToDownload = 0;
                            for (MediaFile media : mediaFiles) {
                                media.fetchFileData(destDir, media.getFileName(), new DownloadListener<String>() {
                                    @Override
                                    public void onStart() {

                                    }

                                    @Override
                                    public void onRateUpdate(long l, long l1, long l2) {

                                    }

                                    @Override
                                    public void onRealtimeDataUpdate(byte[] bytes, long l, boolean b) {

                                    }

                                    @Override
                                    public void onProgress(long l, long l1) {

                                    }

                                    @Override
                                    public void onSuccess(String s) {
                                        String res = "Downloaded: " + ++downloadedFilesToDownload + "/" + totalFilesToDownload;
                                        ToastUtils.setResultToText(textviewLogMedia, res);

//                                        ToastUtils.setResultToToast("File " + downloadedFilesToDownload++ + "/" + totalFilesToDownload + " saved");

                                        mHandler.post(new Runnable() {
                                            @Override
                                            public void run() {
                                                loadLocalFiles();
                                            }
                                        });
                                    }

                                    @Override
                                    public void onFailure(DJIError djiError) {
                                    }
                                });
                            }
                        } else {
                            ToastUtils.setResultToToast("No files to download");
                        }
                    } else {
                        ToastUtils.setResultToToast(djiError.toString());
                    }
                }
            });
        }
    }

    private void deleteDroneFiles() {
        if (mediaManager != null) {
            mediaManager.refreshFileListOfStorageLocation(SettingsDefinitions.StorageLocation.SDCARD, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError == null) {
                        List<MediaFile> mediaFiles = mediaManager.getSDCardFileListSnapshot();
                        int count = mediaFiles != null ? mediaFiles.size() : 0;
//                        ToastUtils.setResultToToast("Total files=" + count);

                        if (count > 0) {
                            mediaManager.deleteFiles(mediaFiles, new CommonCallbacks.CompletionCallbackWithTwoParam<List<MediaFile>, DJICameraError>() {
                                @Override
                                public void onSuccess(List<MediaFile> mediaFiles, DJICameraError djiCameraError) {
                                    ToastUtils.setResultToToast("Deleted " + count + " files from drone");
                                    mHandler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            loadCameraFiles();
                                        }
                                    });
                                }

                                @Override
                                public void onFailure(DJIError djiError) {
                                    ToastUtils.setResultToToast(djiError.toString());
                                }
                            });
                        } else {
                            ToastUtils.setResultToToast("No drone files to delete");
                        }
                    } else {
                        ToastUtils.setResultToToast(djiError.toString());
                    }
                }
            });

            loadCameraFiles();
        }
    }

    private void deleteLocalFiles() {
        String localBaseFolderPath = "/HalyID/";
        File foldersToPurge = new File(Environment.getExternalStorageDirectory().getPath() + localBaseFolderPath);
        int count = foldersToPurge.listFiles().length;

        if (count > 0) {
            for (File file : foldersToPurge.listFiles()) {
                file.delete();
            }

            ToastUtils.setResultToToast("Deleted " + count + " local files");

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    loadLocalFiles();
                }
            });
        } else {
            ToastUtils.setResultToToast("No local files to delete");
        }
    }

    private void uploadFiles() {
        Uri serverUri = Uri.parse("https://nextcloud.ibr.cs.tu-bs.de");
        String username = "sorbelli";
        String password = "wKpS4kY6FH5L7ra";

        mClient = OwnCloudClientFactory.createOwnCloudClient(serverUri, this, true);
        mClient.setCredentials(OwnCloudCredentialsFactory.newBasicCredentials(username, password));

        String remoteBaseFolderPath = "HALY.ID/Bug_Images/Photos-by-date/";

        String localBaseFolderPath = "/HalyID/";
        File filesToUpload = new File(Environment.getExternalStorageDirectory().getPath() + localBaseFolderPath);
        File[] files = filesToUpload.listFiles();
        Arrays.sort(files, Collections.reverseOrder());

        List<List<File>> fileMissions = new ArrayList<>();
        List<String> folderMissions = new ArrayList<>();
        List<File> fileMission = new ArrayList<>();

        for (File file : files) {
            String fileName = file.getName();
            fileMission.add(file);
            int count = Integer.parseInt(fileName.split("_")[2]);
            String name = fileName.split("_")[1];
            if (count == 1) {
                try {
                    SimpleDateFormat originalFormat = new SimpleDateFormat("yyyyMMddHHmmss");
                    Date date = originalFormat.parse(name);
                    SimpleDateFormat targetFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
                    name = targetFormat.format(date);
                } catch (Exception e) {

                }

                folderMissions.add(name);
                fileMissions.add(fileMission);
                fileMission = new ArrayList<>();

                ToastUtils.setResultToToast("Created folder: " + name);
                String newFolderPath = remoteBaseFolderPath + name;
                CreateRemoteFolderOperation createOperation = new CreateRemoteFolderOperation(newFolderPath, false);
                createOperation.execute(mClient, this, mHandler);
            }
        }

        if (files.length == 0) {
            ToastUtils.setResultToToast("Nothing to upload");
            return;
        }

        ToastUtils.setResultToToast("Folders creation on NextCloud. Please wait...");

        mHandler.postDelayed(new Runnable() {
            public void run() {
                if (files.length > 0) {
                    ToastUtils.setResultToToast("Remote folders properly created");
                    doActualUpload(folderMissions, fileMissions);
                }
            }
        }, 5000);
    }

    private void doActualUpload(List<String> folderMissions, List<List<File>> fileMissions) {
        String remoteBaseFolderPath = "HALY.ID/Bug_Images/Photos-by-date/";

        totalFilesToUpload = uploadedFilesToUpload = 0;

        for (int i = 0; i < folderMissions.size(); i++) {
            String newFolderPath = remoteBaseFolderPath + folderMissions.get(i);

            for (File file : fileMissions.get(i)) {
                totalFilesToUpload += fileMissions.size();

                String absoluteLocalFile = file.getAbsolutePath();
                String remoteLocalFile = newFolderPath + "/" + file.getName();

                String fileTimestamp = Long.toString(new File(absoluteLocalFile).lastModified() / 1000);
                UploadRemoteFileOperation uploadOperation = new UploadRemoteFileOperation(absoluteLocalFile, remoteLocalFile, "image/jpeg", fileTimestamp);
                uploadOperation.addDatatransferProgressListener(this);
                uploadOperation.execute(mClient, this, mHandler);
            }
        }

        cloudFiles = new ArrayList<>();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

    }

    @Override
    public void onTransferProgress(long progressRate, long totalTransferredSoFar, long totalToTransfer, String fileName) {
        if (totalToTransfer == totalTransferredSoFar) {
            uploadedFilesToUpload++;

            cloudFiles.add(fileName.split("/")[fileName.split("/").length-1]);
            String[] cloudFilesArray = cloudFiles.toArray(new String[0]);
            Arrays.sort(cloudFilesArray, Collections.reverseOrder());

            ArrayAdapter adapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.custom_list_item, cloudFilesArray);

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    listCloud.setAdapter(adapter);
                }
            });
        }

        mHandler.post(new Runnable() {
            @Override
            public void run() {
                String res = "Uploaded: " + uploadedFilesToUpload + "/" + totalFilesToUpload;
                ToastUtils.setResultToText(textviewLogMedia, res);
            }
        });
    }

    @Override
    public void onRemoteOperationFinish(RemoteOperation remoteOperation, RemoteOperationResult remoteOperationResult) {

    }
}

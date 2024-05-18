package eu.halyid.drone.activity;

import static eu.halyid.drone.util.Utilities.checkGpsCoordination;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.gson.Gson;
import com.owncloud.android.lib.common.OwnCloudClient;
import com.owncloud.android.lib.common.OwnCloudClientFactory;
import com.owncloud.android.lib.common.OwnCloudCredentialsFactory;
import com.owncloud.android.lib.common.network.OnDatatransferProgressListener;
import com.owncloud.android.lib.common.operations.OnRemoteOperationListener;
import com.owncloud.android.lib.common.operations.RemoteOperation;
import com.owncloud.android.lib.common.operations.RemoteOperationResult;
import com.owncloud.android.lib.resources.files.CreateRemoteFolderOperation;
import com.owncloud.android.lib.resources.files.DownloadRemoteFileOperation;
import com.owncloud.android.lib.resources.files.ReadRemoteFolderOperation;
import com.owncloud.android.lib.resources.files.RemoteFile;
import com.owncloud.android.lib.resources.files.UploadRemoteFileOperation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

import dji.common.airlink.PhysicalSource;
import dji.common.battery.BatteryState;
import dji.common.camera.CameraStreamSettings;
import dji.common.camera.CameraVideoStreamSource;
import dji.common.camera.SettingsDefinitions;
import dji.common.camera.StorageState;
import dji.common.error.DJICameraError;
import dji.common.error.DJIError;
import dji.common.error.DJIWaypointV2Error;
import dji.common.flightcontroller.FlightControllerState;
import dji.common.flightcontroller.RTKState;
import dji.common.flightcontroller.flightassistant.PerceptionInformation;
import dji.common.flightcontroller.rtk.NetworkServiceSettings;
import dji.common.flightcontroller.rtk.NetworkServiceState;
import dji.common.flightcontroller.rtk.ReferenceStationSource;
import dji.common.mission.waypoint.Waypoint;
import dji.common.mission.waypoint.WaypointAction;
import dji.common.mission.waypoint.WaypointActionType;
import dji.common.mission.waypoint.WaypointMissionDownloadEvent;
import dji.common.mission.waypoint.WaypointMissionExecutionEvent;
import dji.common.mission.waypoint.WaypointMissionFinishedAction;
import dji.common.mission.waypoint.WaypointMissionFlightPathMode;
import dji.common.mission.waypoint.WaypointMissionUploadEvent;
import dji.common.mission.waypointv2.Action.ActionDownloadEvent;
import dji.common.mission.waypointv2.Action.ActionExecutionEvent;
import dji.common.mission.waypointv2.Action.ActionState;
import dji.common.mission.waypointv2.Action.ActionUploadEvent;
import dji.common.mission.waypointv2.Action.WaypointV2Action;
import dji.common.mission.waypointv2.WaypointV2;
import dji.common.mission.waypointv2.WaypointV2Mission;
import dji.common.mission.waypointv2.WaypointV2MissionDownloadEvent;
import dji.common.mission.waypointv2.WaypointV2MissionExecutionEvent;
import dji.common.mission.waypointv2.WaypointV2MissionState;
import dji.common.mission.waypointv2.WaypointV2MissionTypes;
import dji.common.mission.waypointv2.WaypointV2MissionUploadEvent;
import dji.common.model.LocationCoordinate2D;
import dji.common.useraccount.UserAccountState;
import dji.common.util.CommonCallbacks;
import dji.keysdk.AirLinkKey;
import dji.keysdk.CameraKey;
import dji.keysdk.KeyManager;
import dji.keysdk.callback.SetCallback;
import dji.sdk.airlink.OcuSyncLink;
import dji.sdk.base.BaseProduct;
import dji.sdk.camera.Camera;
import dji.sdk.camera.VideoFeeder;
import dji.sdk.flightcontroller.Compass;
import dji.sdk.flightcontroller.FlightAssistant;
import dji.sdk.flightcontroller.FlightController;
import dji.sdk.flightcontroller.RTK;
import dji.sdk.media.DownloadListener;
import dji.sdk.media.MediaFile;
import dji.sdk.media.MediaFileInfo;
import dji.sdk.media.MediaManager;
import dji.sdk.mission.MissionControl;
import dji.sdk.mission.timeline.TimelineElement;
import dji.sdk.mission.timeline.TimelineEvent;
import dji.sdk.mission.waypoint.WaypointMissionOperator;
import dji.sdk.mission.waypoint.WaypointMissionOperatorListener;
import dji.sdk.mission.waypoint.WaypointV2ActionListener;
import dji.sdk.mission.waypoint.WaypointV2MissionOperator;
import dji.sdk.mission.waypoint.WaypointV2MissionOperatorListener;
import dji.sdk.network.RTKNetworkServiceProvider;
import dji.sdk.products.Aircraft;
import dji.sdk.sdkmanager.DJISDKManager;
import dji.sdk.useraccount.UserAccountManager;
import dji.waypointv2.common.waypointv2.WaypointMission;
import eu.halyid.drone.OrchardMission;
import eu.halyid.drone.Parameters;
import eu.halyid.drone.R;
import eu.halyid.drone.util.DJIProcedures;
import eu.halyid.drone.util.ModuleVerificationUtil;
import eu.halyid.drone.util.ToastUtils;
import eu.halyid.drone.util.VideoFeedView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnMapReadyCallback, OnRemoteOperationListener {

    protected static final String TAG = "Waypoint2Activity";
    private final boolean useRTKLocation = true;
    boolean canUpload = false;
    boolean canStart = false;
    FlightAssistant flightAssistant;
    private GoogleMap gMap;
    private Marker droneMarker = null;
    private Button btn_select;
    private Button btn_start;
    private Button btn_stop;
    private Button btn_pause;
    private Button btn_resume;
    private Button btn_rth;
    private Button btn_media;
    private TextView tv_upload;
    private TextView tv_rtk;
    private TextView tv_height;
    private TextView tv_battery_1;
    private TextView tv_battery_2;
    private TextView tv_waypoint;
    private TextView tv_pictures;
    private TextView tv_duration;
    int reachedWaypoints = 0;
    int totalWaypoints = 0;
    int takenPictures = 0;
    int totalPictures = 0;
//    private Switch sw_obstacles;
    private FlightController flightController;
    protected BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            onProductConnectionChange();
        }
    };
    private OwnCloudClient mClient;
    private Handler mHandler = new Handler();
    private RTK mRtk;
    private RTKNetworkServiceProvider mRTKNetworkServiceProvider;
    private Camera camera = null;
    private View primaryCoverView;
    private VideoFeedView primaryVideoFeed;
    private AirLinkKey lbBandwidthKey;
    private AirLinkKey mainCameraBandwidthKey;
    private SetCallback setBandwidthCallback;
    private VideoFeeder.PhysicalSourceListener sourceListener;
    private double mHomeLat = 0;
    private double mHomeLng = 0;
    private double mAircraftLat = 0;
    private double mAircraftLng = 0;
    private float droneHeading;
    private float droneHeight;
    private OrchardMission mission = null;
    private WaypointV2MissionOperator waypointV2MissionOperator = null;
    private WaypointV2MissionOperatorListener waypointV2MissionOperatorListener;
    private WaypointV2ActionListener waypointV2ActionListener = null;
    private Parameters parameters;
    private List<String> listMissionFromCloud;
    private Timer timer;

    @Override
    protected void onResume() {
        super.onResume();
        initFlightController();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        unregisterReceiver(mReceiver);
        removeListener();
        super.onDestroy();
    }

    public void onReturn(View view) {
        Log.d(TAG, "onReturn");
        this.finish();
    }

    private void setResultToToast(final String string) {
        MainActivity.this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, string, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initUI() {
        btn_select = findViewById(R.id.select);
        btn_start = findViewById(R.id.start);
        btn_stop = findViewById(R.id.stop);
        btn_pause = findViewById(R.id.pause);
        btn_resume = findViewById(R.id.resume);
        btn_rth = findViewById(R.id.rth);
        btn_media = findViewById(R.id.media);
        tv_upload = findViewById(R.id.status_log);
        tv_rtk = findViewById(R.id.rtk_log);

        tv_height = findViewById(R.id.height_log);
        tv_battery_1 = findViewById(R.id.battery_1_log);
        tv_battery_2 = findViewById(R.id.battery_2_log);
        tv_waypoint = findViewById(R.id.waypoint_log);
        tv_pictures = findViewById(R.id.pictures_log);
        tv_duration = findViewById(R.id.duration_log);

//        sw_obstacles = findViewById(R.id.obstacles_avoid);
//        btn_pictures.setOnClickListener(this);

        btn_select.setOnClickListener(this);
        btn_start.setOnClickListener(this);
        btn_stop.setOnClickListener(this);
        btn_pause.setOnClickListener(this);
        btn_resume.setOnClickListener(this);
        btn_rth.setOnClickListener(this);
        btn_media.setOnClickListener(this);

//        sw_obstacles.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                getFlightAssistant();
//
//                if (isChecked) {
//                    // Obstacles avoidance system enabled
//                    flightAssistant.setHorizontalVisionObstacleAvoidanceEnabled(true, new CommonCallbacks.CompletionCallback() {
//                        @Override
//                        public void onResult(DJIError djiError) {
//                            if (djiError != null) {
//                                setResultToToast("Error: " + djiError.getDescription());
//                            } else {
//                                setResultToToast("Obstacle avoidance ON");
////                        getObstaclesAvoidanceDistance(var2);
//                            }
//                        }
//                    });
//
//                    setObstaclesAvoidanceDistance(0.5f, PerceptionInformation.DJIFlightAssistantObstacleSensingDirection.Downward);
//                    setObstaclesAvoidanceDistance(1.0f, PerceptionInformation.DJIFlightAssistantObstacleSensingDirection.Upward);
//                    setObstaclesAvoidanceDistance(1.0f, PerceptionInformation.DJIFlightAssistantObstacleSensingDirection.Horizontal);
//                } else {
//                    // Fuck!
//                    flightAssistant.setHorizontalVisionObstacleAvoidanceEnabled(false, new CommonCallbacks.CompletionCallback() {
//                        @Override
//                        public void onResult(DJIError djiError) {
//                            if (djiError != null) {
//                                setResultToToast("Error: " + djiError.getDescription());
//                            } else {
//                                setResultToToast("Obstacle avoidance OFF (Azz!)");
////                        getObstaclesAvoidanceDistance(var2);
//                            }
//                        }
//                    });
//                }
//
//                setObstacleAvoidanceSensorStateListener();
//            }
//        });

//        missionEndpoints = new ArrayList<LatLng>();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        primaryVideoFeed = (VideoFeedView) findViewById(R.id.primary_video_feed);
        primaryVideoFeed.setCoverView(primaryCoverView);
    }

    private void initMapView() {
        gMap.moveCamera(CameraUpdateFactory.zoomTo(22));
        gMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);

        updateDroneLocation();
        cameraUpdate();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.mymenu, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_main);

        IntentFilter filter = new IntentFilter();
        filter.addAction(DJIProcedures.FLAG_CONNECTION_CHANGE);
        registerReceiver(mReceiver, filter);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        if (getWaypointMissionOperator() == null) {
            setResultToToast("Not support Waypoint2.0");

            // For testing
            initUI();
            connectToCloud();
            return;
        }

        initCamera();
        initUI();
        initAllKeys();
        initCallbacks();
        setUpListeners();

        onProductConnectionChange();
        setDroneLocationListener();
        setUpListener();
        connectToCloud();
        connectToRTK();

//        setObstacleAvoidanceListener();
    }

    private void connectToRTK() {
        boolean simulator = true;
        configRTK(!simulator);
    }

    private void connectToCloud() {
        Uri serverUri = Uri.parse("https://nextcloud.ibr.cs.tu-bs.de");
        String username = "sorbelli";
        String password = "wKpS4kY6FH5L7ra";

        mClient = OwnCloudClientFactory.createOwnCloudClient(serverUri, this, true);
        mClient.setCredentials(OwnCloudCredentialsFactory.newBasicCredentials(username, password));

        String remoteBaseFolderPath = "HALY.ID/App/Missions/";

        ReadRemoteFolderOperation refreshOperation = new ReadRemoteFolderOperation(remoteBaseFolderPath);
        refreshOperation.execute(mClient, this, mHandler);
    }

    private void initCamera() {
        if (camera == null) {
            camera = DJIProcedures.getProductInstance().getCamera();

            camera.setCameraVideoStreamSource(CameraVideoStreamSource.ZOOM, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError == null) {
//                        ToastUtils.setResultToToast("setCameraVideoStreamSource");
                    } else {
                        ToastUtils.setResultToToast("Error: " + djiError.toString());
                    }
                }
            });

            CameraStreamSettings cameraStreamSettings = new CameraStreamSettings(false, Arrays.asList(CameraVideoStreamSource.ZOOM));
            camera.setCaptureCameraStreamSettings(cameraStreamSettings, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError == null) {
//                        ToastUtils.setResultToToast("Set ZOOM camera");
                    } else {
                        ToastUtils.setResultToToast("Error: " + djiError.toString());
                    }
                }
            });
        }
    }

    private void initAllKeys() {
        mainCameraBandwidthKey = AirLinkKey.createLightbridgeLinkKey(AirLinkKey.BANDWIDTH_ALLOCATION_FOR_PRIMARY_VIDEO);
        KeyManager.getInstance().setValue(mainCameraBandwidthKey, 1.0f, setBandwidthCallback);

        OcuSyncLink ocuSyncLink = DJIProcedures.getProductInstance().getAirLink().getOcuSyncLink();
        ocuSyncLink.assignSourceToPrimaryChannel(PhysicalSource.LEFT_CAM, PhysicalSource.UNKNOWN, new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError error) {
//                ToastUtils.setResultToText(tv_log, "Set Video Source : " + (error != null ? error.getDescription() : "Success"));
            }
        });
    }

    private void initCallbacks() {
        setBandwidthCallback = new SetCallback() {
            @Override
            public void onSuccess() {
                if (primaryVideoFeed != null) {
                    primaryVideoFeed.changeSourceResetKeyFrame();
                }
            }

            @Override
            public void onFailure(@NonNull DJIError error) {
                ToastUtils.setResultToToast("Failed to set: " + error.getDescription());
            }
        };
    }

    private void setUpListeners() {
        sourceListener = new VideoFeeder.PhysicalSourceListener() {
            @Override
            public void onChange(VideoFeeder.VideoFeed videoFeed, PhysicalSource newPhysicalSource) {
                if (videoFeed == VideoFeeder.getInstance().getPrimaryVideoFeed()) {
                    String newText = "Primary Source: " + newPhysicalSource.toString();
//                    setResultToToast(newText);
                }
            }
        };

        setVideoFeederListeners(true);
    }

    private void setVideoFeederListeners(boolean isOpen) {
        if (VideoFeeder.getInstance() == null) return;

        final BaseProduct product = DJISDKManager.getInstance().getProduct();

        if (product != null) {
            VideoFeeder.VideoDataListener primaryVideoDataListener =
                    primaryVideoFeed.registerLiveVideo(VideoFeeder.getInstance().getPrimaryVideoFeed(), true);
            if (isOpen) {
                VideoFeeder.getInstance().addPhysicalSourceListener(sourceListener);
                String newText = "Primary Source: " + VideoFeeder.getInstance().getPrimaryVideoFeed().getVideoSource().name();
//                setResultToToast(newText);
            } else {
                VideoFeeder.getInstance().removePhysicalSourceListener(sourceListener);
                VideoFeeder.getInstance().getPrimaryVideoFeed().removeVideoDataListener(primaryVideoDataListener);
            }
        }
    }

    private void setObstacleAvoidanceListener() {
//        getFlightAssistant();
//
//        flightAssistant.setHorizontalVisionObstacleAvoidanceEnabled(false, new CommonCallbacks.CompletionCallback() {
//            @Override
//            public void onResult(DJIError djiError) {
//                if (djiError != null) {
//                    setResultToToast("Error: " + djiError.getDescription());
//                } else {
//                    setResultToToast("Obstacle avoidance OFF (Azz!) ");
////                        getObstaclesAvoidanceDistance(var2);
//                }
//            }
//        });
//
////        setObstaclesAvoidanceDistancesetObstaclesAvoidanceDistance(0.5f, PerceptionInformation.DJIFlightAssistantObstacleSensingDirection.Downward);
////        setObstaclesAvoidanceDistance(1.0f, PerceptionInformation.DJIFlightAssistantObstacleSensingDirection.Upward);
////        setObstaclesAvoidanceDistance(1.0f, PerceptionInformation.DJIFlightAssistantObstacleSensingDirection.Horizontal);
//
//        setObstacleAvoidanceSensorStateListener();
    }

    private void setObstacleAvoidanceSensorStateListener() {
        if (flightAssistant != null) {

            flightAssistant.setVisualPerceptionInformationCallback(new CommonCallbacks.CompletionCallbackWith<PerceptionInformation>() {
                @Override
                public void onSuccess(PerceptionInformation perceptionInformation) {
                    int[] distances = perceptionInformation.getDistances();
                    int minHorizontalObstacleDistance = Integer.MAX_VALUE;
                    int minAngle = -1;
                    for (int i = 0; i < distances.length; i++) {
                        if (distances[i] < minHorizontalObstacleDistance) {
                            minHorizontalObstacleDistance = distances[i];
                            minAngle = i;
                        }
                    }

                    int downwardObstacleDistance = perceptionInformation.getDownwardObstacleDistance();
                    int upwardObstacleDistance = perceptionInformation.getUpwardObstacleDistance();

                    String distanceStr = "H: " + minHorizontalObstacleDistance + " (" + minAngle + "), ";
                    distanceStr += ("D: " + downwardObstacleDistance + ", ");
                    distanceStr += ("U: " + upwardObstacleDistance);

//                    updateLogListView(distanceStr);
                }

                @Override
                public void onFailure(DJIError djiError) {

                }
            });
        }
    }

    public void setObstaclesAvoidanceDistance(float var1, final PerceptionInformation.DJIFlightAssistantObstacleSensingDirection var2) {
        if (flightAssistant != null) {
            flightAssistant.setVisualObstaclesAvoidanceDistance(var1, var2, new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {

                    if (djiError != null) {
                        setResultToToast("Error: " + djiError.getDescription());
                    } else {
                        setResultToToast("Obstacle: " + var1 + " " + var2);
//                        getObstaclesAvoidanceDistance(var2);
                    }
                }
            });
        }
    }

//    public void getObstaclesAvoidanceDistance(final PerceptionInformation.DJIFlightAssistantObstacleSensingDirection var1){
//        if (flightAssistant!=null){
//            flightAssistant.getVisualObstaclesAvoidanceDistance(var1, new CommonCallbacks.CompletionCallbackWith<Float>() {
//                @Override
//                public void onSuccess(Float aFloat) {
//                    myAircraftInterface.getObstaclesAvoidanceDistance(aFloat,var1);
//                }
//
//                @Override
//                public void onFailure(DJIError djiError) {
//                    ToastUtils.setResultToToast("error:"+djiError.getDescription());
//                    myAircraftInterface.getObstaclesAvoidanceDistance(1.0f,var1);
//                }
//            });
//        }
//    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.select) {
            selectMission();
        } else if (id == R.id.start) {
            startWaypointMission();
        } else if (id == R.id.stop) {
            stopWaypointMission();
        } else if (id == R.id.pause) {
            pauseWaypointMission();
        } else if (id == R.id.resume) {
            resumeWaypointMission();
        } else if (id == R.id.rth) {
            loadRTH();
        } else if (id == R.id.media) {
            openMediaManager();
        }
//        else if (id == R.id.delete_files) {
//            deletePhotos();
//        } else if (id == R.id.download_files) {
//            downloadPhotos();
//        } else if (id == R.id.upload_files) {
//            uploadPhotos();
//        } else if (id == R.id.delete_local_files) {
//            purgeFiles();
//        }
    }

    private void openMediaManager() {
        try {
            initCamera();
            setPlaybackMode(false);
            setPlaybackMode(true);
        } catch (Exception e) {
            setResultToToast("Open Media Manager with disconnected drone");
        }

        Intent myIntent = new Intent(this, MediaActivity.class);
        startActivity(myIntent);
    }

    private void configRTK(boolean turnOn) {
        if (turnOn) {
            // Step 2: start_rtk
            if (ModuleVerificationUtil.isRtkAvailable()) {
                mRtk = ((Aircraft) DJIProcedures.getProductInstance()).getFlightController().getRTK();
                mRtk.setRtkEnabled(true, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                    }
                });

                mRtk.getRtkEnabled(new CommonCallbacks.CompletionCallbackWith<Boolean>() {

                    @Override
                    public void onSuccess(Boolean aBoolean) {
                        if (aBoolean)
                            ToastUtils.setResultToText(tv_rtk, "Started");
                        else
                            ToastUtils.setResultToText(tv_rtk, "Stopped");
                    }

                    @Override
                    public void onFailure(DJIError djiError) {
                    }
                });
            } else {
                setResultToToast("ERROR - Step 2: start_rtk");
            }


            // Step 3: set_rtk_source
            if (ModuleVerificationUtil.isRtkAvailable()) {
                mRtk = ((Aircraft) DJIProcedures.getProductInstance()).getFlightController().getRTK();
                mRtk.setReferenceStationSource(ReferenceStationSource.CUSTOM_NETWORK_SERVICE, new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {

                    }
                });

                mRtk.addReferenceStationSourceCallback(new ReferenceStationSource.Callback() {
                    @Override
                    public void onReferenceStationSourceUpdate(ReferenceStationSource referenceStationSource) {
                        String description3 = "" + referenceStationSource.toString();
                        ToastUtils.setResultToText(tv_rtk, description3);
                    }
                });
            } else {
                setResultToToast("ERROR - Step 3: set_rtk_source");
            }


            // Step 4: set_net
            if (ModuleVerificationUtil.isNetRtkAvailable()) {
                mRTKNetworkServiceProvider = DJISDKManager.getInstance().getRTKNetworkServiceProvider();
                mRTKNetworkServiceProvider.setCustomNetworkSettings(new NetworkServiceSettings.Builder()
                                .userName("UniMORE")
                                .password("5utapETu")
                                .ip("88.86.116.1")
                                .port(2101)
                                .mountPoint("NET_MSM5")
//                    .userName("pinotti_c")
//                    .password("crpi03r04")
//                    .ip("80.17.45.14")
//                    .port(2101)
//                    .mountPoint("NRT30")
                                .build()
                );

                String description4 = ""
                        + "server=" + mRTKNetworkServiceProvider.getCustomNetworkSettings().getServerAddress()
                        + ", port=" + mRTKNetworkServiceProvider.getCustomNetworkSettings().getPort()
                        + ", username=" + mRTKNetworkServiceProvider.getCustomNetworkSettings().getUserName()
                        + ", password=" + mRTKNetworkServiceProvider.getCustomNetworkSettings().getPassword()
                        + ", mountpoint=" + mRTKNetworkServiceProvider.getCustomNetworkSettings().getMountPoint();
                ToastUtils.setResultToText(tv_rtk, description4);
            } else {
                setResultToToast("ERROR - Step 4: set_net");
            }


            // Step 5: start_net_service
            if (ModuleVerificationUtil.isNetRtkAvailable()) {
                mRTKNetworkServiceProvider = DJISDKManager.getInstance().getRTKNetworkServiceProvider();
                mRTKNetworkServiceProvider.startNetworkService(new CommonCallbacks.CompletionCallback() {
                    @Override
                    public void onResult(DJIError djiError) {
                    }
                });
            } else {
                setResultToToast("ERROR - Step 5: start_net_service");
            }

            // Step 6: check_account
            if (ModuleVerificationUtil.isNetRtkAvailable()) {
                mRTKNetworkServiceProvider = DJISDKManager.getInstance().getRTKNetworkServiceProvider();

                mRTKNetworkServiceProvider.addNetworkServiceStateCallback(new NetworkServiceState.Callback() {
                    @Override
                    public void onNetworkServiceStateUpdate(NetworkServiceState networkServiceState) {
                        String description6 = "" + networkServiceState.getChannelState();
//                        updateLogListView(description6);
                        ToastUtils.setResultToText(tv_rtk, description6);
                    }
                });
            } else {
                setResultToToast("ERROR - Step 6: check_account");
            }
        } else {
            setResultToToast("To implement RTK=OFF");
        }
    }

    private void onProductConnectionChange() {
        initFlightController();
        loginAccount();
    }

    private void setDroneLocationListener() {

        if (flightController == null) {
            setResultToToast("FC is null, comeback later!");
            return;
        }

        Compass compass = flightController.getCompass();
        if (useRTKLocation) {
            if ((mRtk = flightController.getRTK()) == null) {
                setResultToToast("Not support RTK, use Flyc GPS!");
                flightController.setStateCallback(new FlightControllerState.Callback() {
                    @Override
                    public void onUpdate(FlightControllerState state) {
                        mHomeLat = state.getHomeLocation().getLatitude();
                        mHomeLng = state.getHomeLocation().getLongitude();
                        mAircraftLat = state.getAircraftLocation().getLatitude();
                        mAircraftLng = state.getAircraftLocation().getLongitude();
                        droneHeading = compass.getHeading();
                        droneHeight = state.getAircraftLocation().getAltitude();
                        DecimalFormat df = new DecimalFormat("#.#");
                        df.setRoundingMode(RoundingMode.CEILING);
                        ToastUtils.setResultToText(tv_height, df.format(droneHeight));
                        updateDroneLocation();
                    }
                });
            } else {
                flightController.setStateCallback((new FlightControllerState.Callback() {
                    @Override
                    public void onUpdate(FlightControllerState state) {
                        mHomeLat = state.getHomeLocation().getLatitude();
                        mHomeLng = state.getHomeLocation().getLongitude();
                        droneHeight = state.getAircraftLocation().getAltitude();
                        DecimalFormat df = new DecimalFormat("#.#");
                        df.setRoundingMode(RoundingMode.CEILING);
                        ToastUtils.setResultToText(tv_height, df.format(droneHeight));
                    }
                }));
                mRtk.setStateCallback(new RTKState.Callback() {
                    @Override
                    public void onUpdate(@NonNull RTKState state) {
                        if (state.isRTKBeingUsed()) {
//                            setResultToToast("RTK is using!");
                            ToastUtils.setResultToText(tv_rtk, "Ready!");
                        }

                        mAircraftLat = state.getFusionMobileStationLocation().getLatitude();
                        mAircraftLng = state.getFusionMobileStationLocation().getLongitude();
                        droneHeading = state.getFusionHeading();
                        droneHeight = state.getFusionMobileStationAltitude();

                        DecimalFormat df = new DecimalFormat("#.#");
                        df.setRoundingMode(RoundingMode.CEILING);
                        ToastUtils.setResultToText(tv_height, df.format(droneHeight));

                        df = new DecimalFormat("#.##");
                        df.setRoundingMode(RoundingMode.CEILING);
                        float std_lat = state.getMobileStationStandardDeviation().getStdLatitude();
                        float std_lon = state.getMobileStationStandardDeviation().getStdLongitude();
                        float std_alt = state.getMobileStationStandardDeviation().getStdAltitude();

                        String description7 = "Err: " +
                                "lat=" + df.format(std_lat) +
                                ", lon=" + df.format(std_lon) +
                                ", alt=" + df.format(std_alt);
//
                        ToastUtils.setResultToText(tv_rtk, description7);

                        updateDroneLocation();
                    }
                });
            }
        } else {
            if ((mRtk = flightController.getRTK()) != null) {
                flightController.getRTK().setStateCallback(null);
            }

            flightController.setStateCallback(new FlightControllerState.Callback() {
                @Override
                public void onUpdate(FlightControllerState state) {
                    mHomeLat = state.getHomeLocation().getLatitude();
                    mHomeLng = state.getHomeLocation().getLongitude();
                    mAircraftLat = state.getAircraftLocation().getLatitude();
                    mAircraftLng = state.getAircraftLocation().getLongitude();
                    droneHeading = compass.getHeading();
                    droneHeight = state.getAircraftLocation().getAltitude();
                    DecimalFormat df = new DecimalFormat("#.#");
                    df.setRoundingMode(RoundingMode.CEILING);
                    ToastUtils.setResultToText(tv_height, df.format(droneHeight));
                    updateDroneLocation();
                }
            });
        }
    }

    private void loginAccount() {
        UserAccountManager.getInstance().logIntoDJIUserAccount(this,
                new CommonCallbacks.CompletionCallbackWith<UserAccountState>() {
                    @Override
                    public void onSuccess(final UserAccountState userAccountState) {
                        Log.e(TAG, "Login Success");
                    }

                    @Override
                    public void onFailure(DJIError error) {
                        setResultToToast("Login Error: " + error.getDescription());
                    }
                });
    }

    private void initFlightController() {
        BaseProduct product = DJIProcedures.getProductInstance();
        if (product != null && product.isConnected()) {
            if (product instanceof Aircraft) {
                flightController = ((Aircraft) product).getFlightController();
            }
        }
    }

    private void removeListener() {
        if (getWaypointMissionOperator() != null) {
            getWaypointMissionOperator().removeWaypointListener(waypointV2MissionOperatorListener);
        }
    }

    private void setUpListener() {
        waypointV2MissionOperatorListener = new WaypointV2MissionOperatorListener() {

            @Override
            public void onDownloadUpdate(WaypointV2MissionDownloadEvent waypointV2MissionDownloadEvent) {

            }

            @Override
            public void onUploadUpdate(WaypointV2MissionUploadEvent uploadEvent) {
                if (uploadEvent.getCurrentState() == WaypointV2MissionState.UPLOADING
                        || (uploadEvent.getError() != null)) {
                    // deal with the progress or the error info
                }

                if (uploadEvent.getCurrentState() == WaypointV2MissionState.READY_TO_EXECUTE) {
                    // Can upload actions in it.
                }

                if (uploadEvent.getPreviousState() == WaypointV2MissionState.UPLOADING
                        && uploadEvent.getCurrentState() == WaypointV2MissionState.READY_TO_EXECUTE) {
                    // upload complete, can start mission
                }

                ToastUtils.setResultToText(tv_upload, uploadEvent.getCurrentState().name());

//                tv_status.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        tv_status.setText(uploadEvent.getCurrentState().name());
//                    }
//                });
            }

            @Override
            public void onExecutionUpdate(@NonNull WaypointV2MissionExecutionEvent waypointV2MissionExecutionEvent) {
                try {
                    boolean waypointReached = waypointV2MissionExecutionEvent.getProgress().isWaypointReached();
                    if (waypointReached) {
                        ToastUtils.setResultToText(tv_waypoint, ++reachedWaypoints + "/" + totalWaypoints);
                    }
                } catch (Exception e) {

                }
            }

            @Override
            public void onExecutionStart() {

            }

            @Override
            public void onExecutionFinish(DJIWaypointV2Error djiWaypointV2Error) {
                timer.cancel();
            }

            @Override
            public void onExecutionStopped() {

            }
        };

        waypointV2ActionListener = new WaypointV2ActionListener() {
            @Override
            public void onDownloadUpdate(@NonNull ActionDownloadEvent actionDownloadEvent) {
                int azz = 1;
            }

            @Override
            public void onUploadUpdate(@NonNull ActionUploadEvent actionUploadEvent) {
                if (actionUploadEvent.getCurrentState().equals(ActionState.READY_TO_UPLOAD)) {
                    uploadWaypointAction();
                }
                if (actionUploadEvent.getPreviousState() == ActionState.UPLOADING
                        && actionUploadEvent.getCurrentState() == ActionState.READY_TO_EXECUTE) {
                    setResultToToast("Actions are uploaded successfully");

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            canStart = true;
                            btn_start.setEnabled(true);

                            DJIProcedures.getAircraftInstance().getBatteries().get(0).setStateCallback(new BatteryState.Callback() {
                                @Override
                                public void onUpdate(BatteryState batteryState) {
                                    final float percent = batteryState.getChargeRemainingInPercent();
                                    ToastUtils.setResultToText(tv_battery_1, percent+"%");
                                }
                            });

                            DJIProcedures.getAircraftInstance().getBatteries().get(1).setStateCallback(new BatteryState.Callback() {
                                @Override
                                public void onUpdate(BatteryState batteryState) {
                                    final float percent = batteryState.getChargeRemainingInPercent();
                                    ToastUtils.setResultToText(tv_battery_2, percent+"%");
                                }
                            });
                        }
                    });
                }
            }

            @Override
            public void onExecutionUpdate(@NonNull ActionExecutionEvent actionExecutionEvent) {
                int azz = 1;
            }

            @Override
            public void onExecutionStart(int i) {
                int azz = 1;
            }

            @Override
            public void onExecutionFinish(int i, DJIWaypointV2Error djiWaypointV2Error) {
                int azz = 1;
            }
        };

        waypointV2MissionOperator.addWaypointEventListener(waypointV2MissionOperatorListener);
        waypointV2MissionOperator.addActionListener(waypointV2ActionListener);
    }

    private WaypointV2Mission createWaypointMission(Parameters parameters) {
        mission = new OrchardMission(parameters);
        WaypointV2Mission v2m = mission.createWaypointMission();

        totalWaypoints = mission.getWaypointV2List().size();
        totalPictures = totalWaypoints*20;
        ToastUtils.setResultToText(tv_waypoint, reachedWaypoints + "/" + totalWaypoints);
        ToastUtils.setResultToText(tv_waypoint, takenPictures + "/" + totalPictures);

        // Update map
        updateWaypointsGMap();

        return v2m;
    }

    private WaypointV2Mission createRTHMission() {
        LatLng home = new LatLng(mHomeLat, mHomeLng);
        waypointV2MissionOperator.removeActionListener(waypointV2ActionListener);
        waypointV2MissionOperator.removeWaypointListener(waypointV2MissionOperatorListener);

        WaypointV2Mission v2m = mission.createRTHMission(home);

        return v2m;
    }

    private void uploadWaypointAction() {
        List<WaypointV2Action> w2a = mission.createWaypointAction();

        waypointV2MissionOperator.uploadWaypointActions(w2a, new CommonCallbacks.CompletionCallback<DJIWaypointV2Error>() {
            @Override
            public void onResult(DJIWaypointV2Error djiWaypointV2Error) {
                if (djiWaypointV2Error != null) {
                    setResultToToast(djiWaypointV2Error.getDescription());
                }
            }
        });
    }

    private WaypointV2MissionOperator getWaypointMissionOperator() {
        if (waypointV2MissionOperator == null) {
            MissionControl missionControl = DJISDKManager.getInstance().getMissionControl();
            if (missionControl != null) {
                waypointV2MissionOperator = missionControl.getWaypointMissionV2Operator();
            }
        }
        return waypointV2MissionOperator;
    }

    // Update the drone location based on states from MCU.
    private void updateDroneLocation() {
        LatLng pos = new LatLng(mAircraftLat, mAircraftLng);
        //Create MarkerOptions object
        final MarkerOptions markerOptions = new MarkerOptions();
        markerOptions.position(pos);
        markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.drone));

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (droneMarker != null) {
                    droneMarker.remove();
                }

                if (checkGpsCoordination(mAircraftLat, mAircraftLng)) {
                    droneMarker = gMap.addMarker(markerOptions);
                    droneMarker.setRotation(droneHeading * -1.0f);
//                    cameraUpdate();
                }
            }
        });
    }

    private void cameraUpdate() {
        LatLng pos = new LatLng(mAircraftLat, mAircraftLng);
//        float zoomLevel = (float) 22.0;
        CameraUpdate cu = CameraUpdateFactory.newLatLng(pos);
//        CameraUpdateFactory.new
        gMap.moveCamera(cu);
    }

//    private void updateEndpointsGMap() {
//        gMap.clear();
//
//        for (LatLng pos : missionEndpoints) {
//            gMap.addMarker(new MarkerOptions()
//                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_VIOLET))
//                    .position(pos));
//        }
//
//        gMap.addPolyline(new PolylineOptions()
//                .color(0xff9400D3) // purple 0xff9400D3
//                .addAll(missionEndpoints));
//
//        LatLng home = new LatLng(mHomeLat, mHomeLng);
//        gMap.addMarker(new MarkerOptions()
//                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
//                .position(home));
//    }

    private void updateWaypointsGMap() {
        List<WaypointV2> waypointV2List = mission.getWaypointV2List();

        List<LatLng> list = new ArrayList<LatLng>();
        for (WaypointV2 wp : waypointV2List) {
            LatLng pos = new LatLng(wp.getCoordinate().getLatitude(), wp.getCoordinate().getLongitude());

            gMap.addMarker(new MarkerOptions()
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    .position(pos));

            list.add(pos);
        }

        gMap.addPolyline(new PolylineOptions()
                .color(0xff9400D3) // purple 0xff9400D3
                .addAll(list));

        LatLng home = new LatLng(mHomeLat, mHomeLng);

        gMap.addPolyline(new PolylineOptions()
                .color(0xff388E3C) // green 0xff388E3C
                .add(home, list.get(0)));

        gMap.addPolyline(new PolylineOptions()
                .color(0xff388E3C)
                .add(home, list.get(list.size() - 1)));
    }

    private void selectMission() {
        setPlaybackMode(false);

        View dialogSettings = (View) getLayoutInflater().inflate(R.layout.dialog_missions, null);

        TextView input_selected = (TextView) dialogSettings.findViewById(R.id.input_selected);
        ListView list_mission = (ListView) dialogSettings.findViewById(R.id.list_mission);

        String[] mobileArray = listMissionFromCloud.toArray(new String[0]);
        ArrayAdapter adapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, mobileArray);
        list_mission.setAdapter(adapter);

        list_mission.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                input_selected.setText(mobileArray[i]);
            }
        });

        new AlertDialog.Builder(this)
                .setTitle("Load Mission")
                .setView(dialogSettings)
                .setPositiveButton("Select", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        String selected = input_selected.getText().toString();
                        if (!selected.equals("")) {
                            try {
                                setResultToToast(selected);

                                File currentDirectory = getFilesDir();
                                File missionsDirectory = new File(currentDirectory, "missions/HALY.ID/App/Missions/");

                                File file = new File(missionsDirectory, selected);
                                FileInputStream fis = new FileInputStream(file);

                                InputStreamReader inputStreamReader = new InputStreamReader(fis, StandardCharsets.UTF_8);
                                StringBuilder stringBuilder = new StringBuilder();
                                try (BufferedReader reader = new BufferedReader(inputStreamReader)) {
                                    String line = reader.readLine();
                                    while (line != null) {
                                        stringBuilder.append(line).append('\n');
                                        line = reader.readLine();
                                    }
                                } catch (IOException e) {
                                    // Error occurred when opening raw file for reading.
                                } finally {
                                    String contents = stringBuilder.toString();

                                    Gson gson = new Gson();
                                    String json = contents;
                                    parameters = gson.fromJson(json, Parameters.class);

                                    loadMission(parameters);

                                    setResultToToast("Mission '" + selected + "' properly loaded!");

                                    dialog.cancel();
                                }
                            } catch (Exception e) {
                                // Handle error...
                                setResultToToast("ERROR - Read from file");
                            }
                        } else {
                            setResultToToast("Please, select a mission to load");
                        }
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }

    private void loadMission(Parameters parameters) {
        waypointV2MissionOperator.loadMission(createWaypointMission(parameters), new CommonCallbacks.CompletionCallback<DJIWaypointV2Error>() {
            @Override
            public void onResult(DJIWaypointV2Error error) {
                if (error == null) {
                    setResultToToast("Mission loaded!");
                    canUpload = true;

                    mHandler.postDelayed(new Runnable() {
                        public void run() {
                            uploadMission();
                        }
                    }, 7000);

                } else {
                    setResultToToast("Mission load failed, error: " + error.getDescription());
                }
            }
        });
    }

    private void loadRTH() {

        waypointV2MissionOperator.removeActionListener(waypointV2ActionListener);
        waypointV2MissionOperator.removeWaypointListener(waypointV2MissionOperatorListener);

        List<WaypointV2> waypointList = new ArrayList<>();

        WaypointV2 waypoint = Objects.requireNonNull(new WaypointV2.Builder()
                        .setAltitude(30.0)
                        .setCoordinate(new LocationCoordinate2D(mHomeLat, mHomeLng)))
//                .setFlightPathMode(WaypointV2MissionTypes.WaypointV2FlightPathMode.GOTO_POINT_STRAIGHT_LINE_AND_STOP)
//                .setHeadingMode(WaypointV2MissionTypes.WaypointV2HeadingMode.AUTO)
                .build();
        waypointList.add(waypoint);

        waypoint = Objects.requireNonNull(new WaypointV2.Builder()
                        .setAltitude(0.0)
                        .setCoordinate(new LocationCoordinate2D(mHomeLat, mHomeLng)))
//                .setFlightPathMode(WaypointV2MissionTypes.WaypointV2FlightPathMode.GOTO_POINT_STRAIGHT_LINE_AND_STOP)
//                .setHeadingMode(WaypointV2MissionTypes.WaypointV2HeadingMode.AUTO)
                .build();
        waypointList.add(waypoint);

        WaypointV2Mission.Builder waypointV2MissionBuilder = new WaypointV2Mission.Builder();
        waypointV2MissionBuilder.setMissionID(new Random().nextInt(65535))
                .setMaxFlightSpeed(3f)
                .setAutoFlightSpeed(3f)
                .setFinishedAction(WaypointV2MissionTypes.MissionFinishedAction.AUTO_LAND)
//                .setGotoFirstWaypointMode(WaypointV2MissionTypes.MissionGotoWaypointMode.SAFELY)
//                .setExitMissionOnRCSignalLostEnabled(true)
                .setRepeatTimes(1)
                .addwaypoints(waypointList);

        getWaypointMissionOperator().loadMission(waypointV2MissionBuilder.build(), new CommonCallbacks.CompletionCallback<DJIWaypointV2Error>() {
            @Override
            public void onResult(DJIWaypointV2Error djiWaypointV2Error) {
                setResultToToast("RTH loaded!");
                mHandler.postDelayed(new Runnable() {
                    public void run() {
                        getWaypointMissionOperator().uploadMission(new CommonCallbacks.CompletionCallback() {
                            @Override
                            public void onResult(DJIError error) {
                                if (error == null) {
                                    setResultToToast("RTH uploaded!");
                                    mHandler.postDelayed(new Runnable() {
                                        public void run() {
                                            getWaypointMissionOperator().startMission(new CommonCallbacks.CompletionCallback() {
                                                @Override
                                                public void onResult(DJIError error) {
                                                    setResultToToast("RTH Start: " + (error == null ? "successfully" : error.getDescription()));
                                                }
                                            });
                                        }
                                    }, 4000);

                                } else {
                                    setResultToToast("Mission upload failed, error: " + error.getDescription());
                                }
                            }
                        });
                    }
                }, 4000);
            }
        });




//        waypointV2MissionOperator.loadMission(createRTHMission(), new CommonCallbacks.CompletionCallback<DJIWaypointV2Error>() {
//            @Override
//            public void onResult(DJIWaypointV2Error error) {
//                if (error == null) {
//                    setResultToToast("RTH loaded!");
//                    canUpload = true;
//
//                    mHandler.postDelayed(new Runnable() {
//                        public void run() {
//                            uploadRTH();
//                        }
//                    }, 4000);
//
//                } else {
//                    setResultToToast("RTH load failed, error: " + error.getDescription());
//                }
//            }
//        });
    }

    private void uploadMission() {
        getWaypointMissionOperator().uploadMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError error) {
                if (error == null) {
                    setResultToToast("Mission uploaded!");
                } else {
                    setResultToToast("Mission upload failed, error: " + error.getDescription());
                }
            }
        });
    }

    private void uploadRTH() {
        getWaypointMissionOperator().uploadMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError error) {
                if (error == null) {
                    setResultToToast("RTH uploaded!");

                    mHandler.postDelayed(new Runnable() {
                        public void run() {
                            startRTHMission();
                        }
                    }, 5000);

                } else {
                    setResultToToast("Mission upload failed, error: " + error.getDescription());
                }
            }
        });
    }

    private void startWaypointMission() {
        if (canStart) {
            getWaypointMissionOperator().startMission(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError error) {
                    setResultToToast("Mission Start: " + (error == null ? "successfully" : error.getDescription()));
                }
            });
            btn_start.setEnabled(false);
            btn_stop.setEnabled(true);
            btn_pause.setEnabled(true);

            long startTime = System.currentTimeMillis();
            SimpleDateFormat dateFormat = new SimpleDateFormat("mm:ss");
            timer = new Timer();
            timer.scheduleAtFixedRate(new TimerTask() {
                public void run() {
                    long elapsedTime = System.currentTimeMillis() - startTime;
                    String formattedTime = dateFormat.format(new Date(elapsedTime));
                    ToastUtils.setResultToText(tv_duration, formattedTime);
                }
            }, 0, 1000); // 0ms initial delay, 1000ms period

            initCamera();

            camera.setNewGeneratedMediaFileInfoCallback(new MediaFile.NewFileInfoCallback() {
                @Override
                public void onNewFileInfo(@NonNull MediaFileInfo mediaFileInfo) {
                    ToastUtils.setResultToText(tv_pictures, ++takenPictures + "/" + totalPictures);
                }
            });

        } else {
            setResultToToast("Wait until the mission is completely uploaded");
        }
    }

    private void startRTHMission() {
        getWaypointMissionOperator().startMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError error) {
                setResultToToast("RTH Start: " + (error == null ? "successfully" : error.getDescription()));
            }
        });
    }

    private void stopWaypointMission() {
        getWaypointMissionOperator().stopMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError error) {
                setResultToToast("Mission Stop: " + (error == null ? "successfully" : error.getDescription()));

                timer.cancel();
            }
        });
        btn_rth.setEnabled(true);
    }

    private void pauseWaypointMission() {
        getWaypointMissionOperator().interruptMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError error) {
                setResultToToast("Mission Paused: " + (error == null ? "successfully" : error.getDescription()));
            }
        });
        btn_pause.setEnabled(false);
        btn_resume.setEnabled(true);
    }

    private void resumeWaypointMission() {
        getWaypointMissionOperator().recoverMission(new CommonCallbacks.CompletionCallback() {
            @Override
            public void onResult(DJIError error) {
                setResultToToast("Mission Resumed: " + (error == null ? "successfully" : error.getDescription()));
            }
        });
        btn_pause.setEnabled(true);
        btn_resume.setEnabled(false);
    }

    @Override
    public void onRemoteOperationFinish(RemoteOperation operation, RemoteOperationResult result) {
        if (operation instanceof ReadRemoteFolderOperation) {
            if (result.isSuccess()) {
                File currentDirectory = getFilesDir();
                File missionsDirectory = new File(currentDirectory, "missions");
                missionsDirectory.mkdir();

                ArrayList<Object> files = result.getData();
                listMissionFromCloud = new ArrayList<String>();

                int i = 0;
                for (Object o : files) {
                    String path = ((RemoteFile) o).getRemotePath();
                    DownloadRemoteFileOperation readOperation = new DownloadRemoteFileOperation(path, missionsDirectory.getAbsolutePath());
                    readOperation.execute(mClient, this, mHandler);

                    if (i == 0) {
                        i++;
                        continue;
                    }
                    listMissionFromCloud.add(path.split("/")[path.split("/").length-1]);
                }
            }
        }

        if (operation instanceof DownloadRemoteFileOperation) {
            if (result.isSuccess()) {
                int azz = 0;
            }
        }

        if (operation instanceof UploadRemoteFileOperation) {
            if (result.isSuccess()) {

            }
        }
    }

    private void setPlaybackMode(boolean on) {
        initCamera();

        if (on) {
            camera.enterPlayback(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError == null) {
                        ToastUtils.setResultToToast("Set Playback mode ON");
                    } else {
                        ToastUtils.setResultToToast(djiError.toString());
                    }
                }
            });
        } else {
            camera.exitPlayback(new CommonCallbacks.CompletionCallback() {
                @Override
                public void onResult(DJIError djiError) {
                    if (djiError == null) {
                        ToastUtils.setResultToToast("Set Playback mode OFF");
                    } else {
                        ToastUtils.setResultToToast(djiError.toString());
                    }
                }
            });
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        if (gMap == null) {
            gMap = googleMap;
            initMapView();
        }
    }
}

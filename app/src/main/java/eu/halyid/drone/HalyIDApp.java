package eu.halyid.drone;

import android.app.Application;
import android.content.Context;

import com.secneo.sdk.Helper;

import eu.halyid.drone.util.DJIProcedures;

public class HalyIDApp extends Application {

    private DJIProcedures app;

    @Override
    protected void attachBaseContext(Context paramContext) {
        super.attachBaseContext(paramContext);
        Helper.install(HalyIDApp.this);
        if (app == null) {
            app = new DJIProcedures();
            app.setContext(this);
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        app.onCreate();
    }

}

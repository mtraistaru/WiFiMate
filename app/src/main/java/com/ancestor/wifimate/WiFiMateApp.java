package com.ancestor.wifimate;

import android.app.Application;
import android.content.Context;

import com.ancestor.wifimate.module.WiFiMateModule;

/**
 * Created by Mihai.Traistaru on 22-Apr-16.
 */
public class WiFiMateApp extends Application {

    private WiFiMateComponent wiFiMateComponent;

    public static WiFiMateApp getApp(Context context) {
        return (WiFiMateApp) context.getApplicationContext();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        wiFiMateComponent = DaggerWiFiMateComponent
                .builder()
                .wiFiMateModule(new WiFiMateModule(this))
                .build();
    }

    public WiFiMateComponent getWiFiMateComponent() {
        return wiFiMateComponent;
    }

    public void setWiFiMateComponent(WiFiMateComponent wiFiMateComponent) {
        this.wiFiMateComponent = wiFiMateComponent;
    }
}

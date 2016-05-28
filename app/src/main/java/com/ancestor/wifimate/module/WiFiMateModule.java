package com.ancestor.wifimate.module;

import android.content.Context;

import com.ancestor.wifimate.utils.Utils;
import com.ancestor.wifimate.network.Router;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by Mihai.Traistaru on 22-Apr-16.
 */
@Module
public class WiFiMateModule {

    private Context context;

    public WiFiMateModule(Context context) {
        this.context = context;
    }

    @Singleton
    @Provides
    Context provideContext() {
        return context;
    }

    @Singleton
    @Provides
    Bus provideBus() {
        return new Bus(ThreadEnforcer.ANY);
    }

    @Singleton
    @Provides
    Router provideRouter(Context context) {
        return new Router(context);
    }

    @Singleton
    @Provides
    Utils provideUtils() {
        return new Utils();
    }
}

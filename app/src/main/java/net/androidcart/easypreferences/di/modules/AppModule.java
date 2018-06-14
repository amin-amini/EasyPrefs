package net.androidcart.easypreferences.di.modules;

/**
 * Created by Amin Amini on 6/14/18.
 */

import android.app.Application;

import net.androidcart.easypreferences.MyPreferences;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
@Module
public class AppModule {

    private Application mApplication;

    public AppModule(Application application) {
        mApplication = application;
    }

    @Provides
    @Singleton
    Application providesApplication() {
        return mApplication;
    }

    @Provides
    @Singleton
    // Application reference must come from AppModule.class
    MyPreferences providesSharedPreferences(Application application) {
        return new MyPreferences(application);
    }

}
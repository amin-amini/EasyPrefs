package net.androidcart.easypreferences.di.modules;

/**
 * Created by Amin Amini on 6/14/18.
 */

import android.app.Application;

import com.google.gson.reflect.TypeToken;

import net.androidcart.easypreferences.GenericPreferences;
import net.androidcart.easypreferences.MyPreferences;
import net.androidcart.easypreferences.Test;

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

    @Provides
    @Singleton
        // Application reference must come from AppModule.class
    GenericPreferences<Test,String> providesStringPreferences(Application application) {
        return new GenericPreferences<>(application,
                new TypeToken<Test>(){},
                new TypeToken<String>(){});
    }

}
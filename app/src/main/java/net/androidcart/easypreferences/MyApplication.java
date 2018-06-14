package net.androidcart.easypreferences;

import android.app.Application;

import net.androidcart.easypreferences.di.components.AppComponent;
import net.androidcart.easypreferences.di.components.DaggerAppComponent;
import net.androidcart.easypreferences.di.modules.AppModule;

/**
 * Created by Amin Amini on 6/14/18.
 */

public class MyApplication extends Application {

    private static AppComponent component;
    public static AppComponent getComponent() {
        return component;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        MyStaticPreferences.init(this);

        component = DaggerAppComponent.builder()
                .appModule(new AppModule(this))
                .build();

    }
}

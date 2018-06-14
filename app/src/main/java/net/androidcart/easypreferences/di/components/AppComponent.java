package net.androidcart.easypreferences.di.components;

import net.androidcart.easypreferences.MainActivity;
import net.androidcart.easypreferences.di.modules.AppModule;

import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by Amin Amini on 6/14/18.
 */

@Singleton
@Component(modules = {AppModule.class})
public interface AppComponent {
    void inject(MainActivity mainActivity);
}

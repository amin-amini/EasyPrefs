package net.androidcart.easypreferences;

import android.app.Application;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.google.gson.Gson;

import net.androidcart.easypreferences.di.components.AppComponent;

import java.util.ArrayList;
import java.util.HashMap;

import javax.inject.Inject;

public class MainActivity extends AppCompatActivity {

    @Inject MyPreferences prefs;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MyApplication.getComponent().inject(this);


        
        Log.d("DILog , str " , "\n" + prefs.getStr() );
        prefs.setStr("test");
        Log.d("DILog , str " , "" + prefs.getStr() );
        prefs.deleteStr();

        Log.d("DILog , test " , "\n" + prefs.getTest() );
        prefs.setTest(new Test());
        Log.d("DILog , test " , "" + prefs.getTest() );
        prefs.deleteTest();

        Log.d("DILog , double " , "\n" + prefs.getTestDouble() );
        prefs.setTestDouble(Double.MAX_VALUE);
        Log.d("DILog , double " , "" + prefs.getTestDouble() );
        prefs.deleteTestDouble();


        //NOTE: static version have been initialized in MyApplication -> onCreate
        MyStaticPreferences staticPreferences = MyStaticPreferences.i();

        Log.d("StaticLog , test " , "\n" + staticPreferences.getTest().toString() );
        staticPreferences.setTest(new Test(234,"static prefs" , new ArrayList<TestSimple>()));
        Log.d("StaticLog , double " , "" + staticPreferences.getTest().toString() );
        staticPreferences.deleteTest();





    }
}

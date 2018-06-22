package net.androidcart.easypreferences;

import net.androidcart.easyprefsschema.EasyPrefsSchema;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Amin Amini on 6/14/18.
 */

@EasyPrefsSchema("MyPreferences")
public abstract class MyPreferencesSchema {
    abstract String str();

    Test test(){
        return new Test(5, "default value" , new ArrayList<TestSimple>());
    }

    abstract ArrayList<Test> arrTest();

    abstract Map<String, ArrayList<Test> > hugeMap();

    abstract boolean testBool();
    int testInt(){return -10;}
    short testShort(){return -10;}
    long testLong(){return -1000;}
    float testFloat(){return -10000;}
    double testDouble(){return -10000;}
}

package net.androidcart.easypreferences;

import net.androidcart.easyprefsschema.EasyPrefsSchema;
import net.androidcart.easyprefsschema.EPItem;

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

    @EPItem(key = "myCustomTestingKey" , expiresIn = 1000L)
    int testInt(long helpId){return -10;}

    short testShort(){return -10;}
    long testLong(){return -1000;}
    float testFloat(){return -10000;}
    double testDouble(){return -10000;}

    @EPItem(exclude = true)
    void helperExcluding(){}
}

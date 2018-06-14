package net.androidcart.easypreferences;

import net.androidcart.easyprefs.EasyPrefsSchema;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by Amin Amini on 6/14/18.
 */

@EasyPrefsSchema(value = "MyStaticPreferences", useStaticReferences = true)
public abstract class StaticPreferencesSchema {
    Test test(){
        return new Test(50, "default static value" , new ArrayList<TestSimple>());
    }
}
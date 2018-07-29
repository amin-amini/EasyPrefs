package net.androidcart.easypreferences;

import net.androidcart.easyprefsschema.EasyPrefsSchema;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Amin Amini on 6/14/18.
 */

@EasyPrefsSchema("GenericPreferences")
public abstract class GenericPreferencesSchema<GT extends Test, GS extends String> {
    abstract GT generic(int keyInt, String keyStr);
    abstract GS genericString(int keyInt, String keyStr);
    abstract HashMap<GT, GS> hardGeneric();
}

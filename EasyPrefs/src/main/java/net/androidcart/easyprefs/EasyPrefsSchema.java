package net.androidcart.easyprefs;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.TYPE)
public @interface EasyPrefsSchema {
        String value();
        boolean useStaticReferences() default false;
}

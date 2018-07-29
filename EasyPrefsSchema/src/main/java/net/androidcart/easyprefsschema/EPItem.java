package net.androidcart.easyprefsschema;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface EPItem {
    String key() default "";
    long expiresIn() default -1;
    boolean exclude() default false;
}

package com.azure.core.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
public @interface ConfigurationDoc {
    String description();
    String defaultValue() default "null";
    /*String name() default "";
    Class<?> type() default Object.class;
    String[] aliases();
    String[] environmentVariables();
    Class converter();
    String defaultValue();
    Class type();
    Class sourceType();
    String description();
    boolean required();
    boolean global();
    boolean canLogValue();*/
}

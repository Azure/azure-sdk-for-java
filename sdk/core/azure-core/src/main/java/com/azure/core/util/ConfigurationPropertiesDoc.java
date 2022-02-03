package com.azure.core.util;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.SOURCE)
public @interface ConfigurationPropertiesDoc {
    String prefix() default "";
}

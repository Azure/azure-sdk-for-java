package com.azure.core.util;

public @interface ConfigurationPropertiesHint {
    Class<?> type() default Object.class;
}

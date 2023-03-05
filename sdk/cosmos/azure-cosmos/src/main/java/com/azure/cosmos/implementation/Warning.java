// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Warning annotation for APIs that are only supported for implementation details and not public usage
 */
@Retention(RetentionPolicy.SOURCE)
@Target({ ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.METHOD, ElementType.PACKAGE, ElementType.PARAMETER, ElementType.TYPE})
public @interface Warning {

    //  Common warnings
    String INTERNAL_USE_ONLY_WARNING = "Internal use only, not meant for public usage as this API may change in future";

    /**
     * Text of warning message
     * @return warning message value
     */
    String value();
}

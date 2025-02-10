// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.management.web;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * THis annotation can be added to any endpoint and will check if the feature is on.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface FeatureGate {

    /**
     * Return name of the feature that is checked.
     * @return Name of the feature that is checked.
     */
    String feature();

    /**
     * Return endpoint to be fall backed on if feature is off.
     * @return Endpoint to be fall backed on if feature is off.
     */
    String fallback() default "";

    /**
     * If true, feature will return the same value during the length of the request.
     * @return If true, feature will return the same value during the length of the request.
     */
    boolean snapshot() default false;
}

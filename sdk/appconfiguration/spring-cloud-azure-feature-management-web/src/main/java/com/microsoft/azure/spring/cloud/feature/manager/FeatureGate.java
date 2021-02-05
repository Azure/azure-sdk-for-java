/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.feature.manager;

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
     * Name of the feature that is checked.
     */
    public String feature();

    /**
     * Endpoint to be fall backed on if feature is off.
     */
    public String fallback() default "";

    /**
     * If true, feature will return the same value during the length of the request.
     */
    public boolean snapshot() default false;
}

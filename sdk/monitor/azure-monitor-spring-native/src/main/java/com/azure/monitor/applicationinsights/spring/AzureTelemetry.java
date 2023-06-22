// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.applicationinsights.spring;

/**
 *
 */
public final class AzureTelemetry {

    private static final String APPLICATIONINSIGHTS_NON_NATIVE_ENABLED = "applicationinsights.native.spring.non-native.enabled";

    private AzureTelemetry() {
    }

    /**
     *
     * @return return
     */
    public static boolean isEnabled() {
        return isNativeRuntimeExecution() || Boolean.getBoolean(APPLICATIONINSIGHTS_NON_NATIVE_ENABLED);
    }

    private static boolean isNativeRuntimeExecution() {
        String imageCode = System.getProperty("org.graalvm.nativeimage.imagecode");
        return imageCode != null;
    }

}

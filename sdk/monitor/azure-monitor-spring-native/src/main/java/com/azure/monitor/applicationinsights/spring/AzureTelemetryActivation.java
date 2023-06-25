// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.applicationinsights.spring;

/**
 *
 */
public final class AzureTelemetryActivation {

    private final boolean activated;

    /**
     *
     * @param enableEvenWithNonNative ..
     */
    public AzureTelemetryActivation(boolean enableEvenWithNonNative) {
        this.activated = isNativeRuntimeExecution() || enableEvenWithNonNative;
    }

    private static boolean isNativeRuntimeExecution() {
        String imageCode = System.getProperty("org.graalvm.nativeimage.imagecode");
        return imageCode != null;
    }

    /**
     *
     * @return ...
     */
    public boolean isTrue() {
        return activated;
    }

}

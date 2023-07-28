// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.statsbeat;

import javax.annotation.Nullable;

// Manual, StandaloneAuto, IntegratedAuto
public enum RpAttachType {
    Manual, // Manually writing code to start collecting telemetry, e.g. via azure-monitor-opentelemetry-exporter
    StandaloneAuto, // RP attach is enabled via a custom JAVA_OPTS
    IntegratedAuto; // RP attach is on by default

    private static volatile RpAttachType attachType;

    public static void setRpAttachType(RpAttachType type) {
        attachType = type;
    }

    static String getRpAttachType() {
        return attachType.name();
    }
}

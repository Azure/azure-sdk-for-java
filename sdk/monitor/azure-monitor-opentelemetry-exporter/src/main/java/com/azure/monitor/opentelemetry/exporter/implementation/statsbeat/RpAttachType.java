// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.statsbeat;

// Manual, StandaloneAuto, IntegratedAuto
public enum RpAttachType {
    MANUAL, // Manually writing code to start collecting telemetry, e.g. via azure-monitor-opentelemetry-exporter
    STANDALONE_AUTO, // RP attach is enabled via a custom JAVA_OPTS
    INTEGRATED_AUTO; // RP attach is on by default

    private static volatile RpAttachType attachType;

    public static void setRpAttachType(RpAttachType type) {
        attachType = type;
    }

    public static RpAttachType getRpAttachType() {
        return attachType;
    }
}

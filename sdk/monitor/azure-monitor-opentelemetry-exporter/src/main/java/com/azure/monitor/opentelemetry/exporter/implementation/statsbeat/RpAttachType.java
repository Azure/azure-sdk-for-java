// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.monitor.opentelemetry.exporter.implementation.statsbeat;

// Manual, StandaloneAuto, IntegratedAuto
public enum RpAttachType {
    MANUAL("Manual"), // Manually writing code to start collecting telemetry, e.g. via azure-monitor-opentelemetry-exporter
    STANDALONE_AUTO("StandaloneAuto"), // RP attach is enabled via a custom JAVA_OPTS or on premise resources
    INTEGRATED_AUTO("IntegratedAuto"); // RP attach is on by default

    private static volatile RpAttachType attachType;
    private final String label;

    private RpAttachType(String label) {
        this.label = label;
    }

    public static void setRpAttachType(RpAttachType type) {
        attachType = type;
    }

    public static String getRpAttachType() {
        return attachType != null ? attachType.label : null;
    }
}

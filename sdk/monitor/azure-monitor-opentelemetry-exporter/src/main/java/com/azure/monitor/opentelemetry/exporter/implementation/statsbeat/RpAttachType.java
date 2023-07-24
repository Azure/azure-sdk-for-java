package com.azure.monitor.opentelemetry.exporter.implementation.statsbeat;

public enum RpAttachType {
    AUTO,
    MANUAL;

    private static volatile RpAttachType attachType;

    public static void setRpAttachType(RpAttachType type) {
        attachType = type;
    }

    public static RpAttachType getRpAttachType() {
        return attachType;
    }
}

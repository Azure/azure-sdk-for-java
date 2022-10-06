package com.azure.core.tracing.opentelemetry;

import com.azure.core.util.ExpandableStringEnum;

/**
 * OTel schema version.
 */
public class OpenTelemetrySchemaVersion extends ExpandableStringEnum<OpenTelemetrySchemaVersion> {
    /**
     * 1.12.0 version.
     */
    public static final OpenTelemetrySchemaVersion V1_12_0 = fromString("1.12.0", OpenTelemetrySchemaVersion.class);

    /**
     * Gets latest version.
     * @return latest supported schema version.
     */
    static final OpenTelemetrySchemaVersion getLatest() {
        return V1_12_0;
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opentelemetry;

import com.azure.core.util.ExpandableStringEnum;

/**
 * OpenTelemetry schema version.
 */
public class OpenTelemetrySchemaVersion extends ExpandableStringEnum<OpenTelemetrySchemaVersion> {
    @SuppressWarnings("deprecation")
    OpenTelemetrySchemaVersion() {
        super();
    }

    /**
     * 1.12.0 version.
     */
    public static final OpenTelemetrySchemaVersion V1_17_0 = fromString("1.17.0", OpenTelemetrySchemaVersion.class);

    /**
     * Gets latest version.
     * @return latest supported schema version.
     */
    public static final OpenTelemetrySchemaVersion getLatest() {
        return V1_17_0;
    }
}

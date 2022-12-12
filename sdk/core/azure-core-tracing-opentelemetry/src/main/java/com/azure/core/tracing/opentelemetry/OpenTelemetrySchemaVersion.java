// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.tracing.opentelemetry;

import com.azure.core.util.ExpandableStringEnum;

/**
 * OTel schema version.
 */
public class OpenTelemetrySchemaVersion extends ExpandableStringEnum<OpenTelemetrySchemaVersion> {
    /**
     * Creates a new instance of {@link ExpandableStringEnum} without a {@link #toString()} value.
     * <p>
     * This constructor shouldn't be called as it will produce a {@link ExpandableStringEnum} which doesn't
     * have a String enum value.
     *
     * @deprecated Use the explicit pre-constracted version.
     */
    @Deprecated
    public OpenTelemetrySchemaVersion() {
    }

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

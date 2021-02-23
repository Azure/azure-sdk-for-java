// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import com.azure.core.annotation.Immutable;
import java.time.OffsetDateTime;

/** The AnomalyAlert model. */
@Immutable
public final class AnomalyAlert {
    /*
     * alert id
     */
    private String id;

    /*
     * anomaly time
     */
    private OffsetDateTime timestamp;

    /*
     * created time
     */
    private OffsetDateTime createdTime;

    /*
     * modified time
     */
    private OffsetDateTime modifiedTime;

    /**
     * Get the alertId property: alert id.
     *
     * @return the alertId value.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Get the timestamp property: anomaly time.
     *
     * @return the timestamp value.
     */
    public OffsetDateTime getTimestamp() {
        return this.timestamp;
    }

    /**
     * Get the createdTime property: created time.
     *
     * @return the createdTime value.
     */
    public OffsetDateTime getCreatedTime() {
        return this.createdTime;
    }

    /**
     * Get the modifiedTime property: modified time.
     *
     * @return the modifiedTime value.
     */
    public OffsetDateTime getModifiedTime() {
        return this.modifiedTime;
    }
}

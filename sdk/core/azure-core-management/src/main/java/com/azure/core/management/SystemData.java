// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management;

import java.time.OffsetDateTime;

/** Metadata pertaining to creation and last modification of the resource. */
public final class SystemData {

    private String createdBy;

    private CreatedByType createdByType;

    private OffsetDateTime createdAt;

    private String lastModifiedBy;

    private CreatedByType lastModifiedByType;

    private OffsetDateTime lastModifiedAt;

    /**
     * Get the identity that created the resource.
     *
     * @return the identity that created the resource.
     */
    public String createdBy() {
        return this.createdBy;
    }

    /**
     * Get the type of identity that created the resource.
     *
     * @return the type of identity that created the resource.
     */
    public CreatedByType createdByType() {
        return this.createdByType;
    }

    /**
     * Get the timestamp of resource creation (UTC).
     *
     * @return the timestamp of resource creation (UTC).
     */
    public OffsetDateTime createdAt() {
        return this.createdAt;
    }

    /**
     * Get the identity that last modified the resource.
     *
     * @return the identity that last modified the resource.
     */
    public String lastModifiedBy() {
        return this.lastModifiedBy;
    }

    /**
     * Get the type of identity that last modified the resource.
     *
     * @return the type of identity that last modified the resource.
     */
    public CreatedByType lastModifiedByType() {
        return this.lastModifiedByType;
    }

    /**
     * Get the type of identity that last modified the resource.
     *
     * @return the timestamp of resource modification (UTC).
     */
    public OffsetDateTime lastModifiedAt() {
        return this.lastModifiedAt;
    }
}

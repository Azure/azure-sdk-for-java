// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.compute.batch.models;

import java.time.OffsetDateTime;

/**
 * Optional parameters for getting the properties of a Batch Compute Node file.
 */
public class GetBatchNodeFilePropertiesOptions extends BatchBaseOptions {
    private OffsetDateTime ifModifiedSince;
    private OffsetDateTime ifUnmodifiedSince;

    /**
     * Gets a timestamp indicating the last modified time of the resource known to the client. The operation will be
     * performed only if the resource on the service has been modified since the specified time.
     *
     * @return A timestamp indicating the last modified time of the resource.
     */
    public OffsetDateTime getIfModifiedSince() {
        return ifModifiedSince;
    }

    /**
     * Sets a timestamp indicating the last modified time of the resource known to the client. The operation will be
     * performed only if the resource on the service has been modified since the specified time.
     *
     * @param ifModifiedSince A timestamp indicating the last modified time of the resource.
     * @return The {@link GetBatchNodeFilePropertiesOptions} object itself, allowing for method chaining.
     */
    public GetBatchNodeFilePropertiesOptions setIfModifiedSince(OffsetDateTime ifModifiedSince) {
        this.ifModifiedSince = ifModifiedSince;
        return this;
    }

    /**
     * Gets a timestamp indicating the last modified time of the resource known to the client. The operation will be
     * performed only if the resource on the service has not been modified since the specified time.
     *
     * @return A timestamp indicating the last modified time of the resource.
     */
    public OffsetDateTime getIfUnmodifiedSince() {
        return ifUnmodifiedSince;
    }

    /**
     * Sets a timestamp indicating the last modified time of the resource known to the client. The operation will be
     * performed only if the resource on the service has not been modified since the specified time.
     *
     * @param ifUnmodifiedSince A timestamp indicating the last modified time of the resource.
     * @return The {@link GetBatchNodeFilePropertiesOptions} object itself, allowing for method chaining.
     */
    public GetBatchNodeFilePropertiesOptions setIfUnmodifiedSince(OffsetDateTime ifUnmodifiedSince) {
        this.ifUnmodifiedSince = ifUnmodifiedSince;
        return this;
    }

}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import java.time.OffsetDateTime;

/**
 * Specifies HTTP options for conditional requests based on modification time.
 */
public class RequestConditions extends MatchConditions {
    private OffsetDateTime ifModifiedSince;
    private OffsetDateTime ifUnmodifiedSince;

    /**
     * Gets the {@link OffsetDateTime datetime} that resources must have been modified since.
     *
     * @return The datetime that resources must have been modified since.
     */
    public OffsetDateTime getIfModifiedSince() {
        return ifModifiedSince;
    }

    /**
     * Optionally limit requests to resources that have only been modified since the passed
     * {@link OffsetDateTime datetime}.
     *
     * @param ifModifiedSince The datetime that resources must have been modified since.
     * @return The updated ResourceConditions object.
     */
    public RequestConditions setIfModifiedSince(OffsetDateTime ifModifiedSince) {
        this.ifModifiedSince = ifModifiedSince;
        return this;
    }

    /**
     * Gets the {@link OffsetDateTime datetime} that resources must have remained unmodified since.
     *
     * @return The datetime that resources must have remained unmodified since.
     */
    public OffsetDateTime getIfUnmodifiedSince() {
        return ifUnmodifiedSince;
    }

    /**
     * Optionally limit requests to resources that have remained unmodified since the passed
     * {@link OffsetDateTime datetime}.
     *
     * @param ifUnmodifiedSince The datetime that resources must have remained unmodified since.
     * @return The updated ResourceConditions object.
     */
    public RequestConditions setIfUnmodifiedSince(OffsetDateTime ifUnmodifiedSince) {
        this.ifUnmodifiedSince = ifUnmodifiedSince;
        return this;
    }
}

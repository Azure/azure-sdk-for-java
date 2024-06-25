// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.http;

import com.azure.core.v2.annotation.Fluent;

import java.time.OffsetDateTime;

/**
 * <p>Specifies HTTP options for conditional requests based on modification time and ETag matching.</p>
 *
 * <p>This class extends {@link MatchConditions} and adds conditions based on the modification time of the resource.
 * It encapsulates conditions such as If-Modified-Since and If-Unmodified-Since, in addition to If-Match and
 * If-None-Match from {@link MatchConditions}.</p>
 *
 * <p>This class is useful when you want to create an HTTP request with conditional headers based on the modification
 * time of the resource and ETag matching. For example, you can use it to create a GET request that only retrieves the
 * resource if it has been modified since a specific time, or a PUT request that only updates the resource if it
 * has not been modified by another client since a specific time.</p>
 *
 * @see MatchConditions
 * @see OffsetDateTime
 */
@Fluent
public class RequestConditions extends MatchConditions {
    private OffsetDateTime ifModifiedSince;
    private OffsetDateTime ifUnmodifiedSince;

    /**
     * Creates a new instance of {@link RequestConditions}.
     */
    public RequestConditions() {
    }

    /**
     * Optionally limit requests to resources that match the passed ETag.
     *
     * @param ifMatch ETag that resources must match.
     * @return The updated ResourceConditions object.
     */
    @Override
    public RequestConditions setIfMatch(String ifMatch) {
        super.setIfMatch(ifMatch);
        return this;
    }

    /**
     * Optionally limit requests to resources that do not match the passed ETag.
     *
     * @param ifNoneMatch ETag that resources must not match.
     * @return The updated ResourceConditions object.
     */
    @Override
    public RequestConditions setIfNoneMatch(String ifNoneMatch) {
        super.setIfNoneMatch(ifNoneMatch);
        return this;
    }

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

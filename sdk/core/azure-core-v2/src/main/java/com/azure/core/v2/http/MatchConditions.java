// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.http;

import com.azure.core.v2.annotation.Fluent;

/**
 * <p>Specifies HTTP options for conditional requests based on ETag matching.</p>
 *
 * <p>This class encapsulates the ETag conditions that can be used in a request, such as If-Match and If-None-Match.</p>
 *
 * <p>This class is useful when you want to create an HTTP request with conditional headers based on ETag matching. For example,
 * you can use it to create a GET request that only retrieves the resource if it has not been modified (based on the ETag), or a
 * PUT request that only updates the resource if it has not been modified by another client (based on the ETag).</p>
 *
 * @see HttpRequest
 */
@Fluent
public class MatchConditions {
    private String ifMatch;
    private String ifNoneMatch;

    /**
     * Creates a new instance of {@link MatchConditions}.
     */
    public MatchConditions() {
    }

    /**
     * Gets the ETag that resources must match.
     *
     * @return The ETag that resources must match.
     */
    public String getIfMatch() {
        return ifMatch;
    }

    /**
     * Optionally limit requests to resources that match the passed ETag.
     *
     * @param ifMatch ETag that resources must match.
     * @return The updated MatchConditions object.
     */
    public MatchConditions setIfMatch(String ifMatch) {
        this.ifMatch = ifMatch;
        return this;
    }

    /**
     * Gets the ETag that resources must not match.
     *
     * @return The ETag that resources must not match.
     */
    public String getIfNoneMatch() {
        return ifNoneMatch;
    }

    /**
     * Optionally limit requests to resources that do not match the passed ETag.
     *
     * @param ifNoneMatch ETag that resources must not match.
     * @return The updated MatchConditions object.
     */
    public MatchConditions setIfNoneMatch(String ifNoneMatch) {
        this.ifNoneMatch = ifNoneMatch;
        return this;
    }
}

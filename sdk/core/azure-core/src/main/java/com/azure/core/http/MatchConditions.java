// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http;

import com.azure.core.annotation.Fluent;

/**
 * Specifies HTTP options for conditional requests.
 */
@Fluent
public class MatchConditions {
    private String ifMatch;
    private String ifNoneMatch;

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

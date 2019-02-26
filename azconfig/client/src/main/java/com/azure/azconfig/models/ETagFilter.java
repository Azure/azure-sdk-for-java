/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.azconfig.models;

/**
 * Contains etag values to be added to the request as headers for filtering.
 */
public class ETagFilter {
    private String ifMatch;
    private String ifNoneMatch;

    /**
     * @return 'If-Match' header value to be added
     */
    public String ifMatch() {
        return ifMatch;
    }

    /**
     * Set 'If-Match' header value to be added to the request.
     * @param ifMatch header value
     * @return ETagFilter itself
     */
    public ETagFilter withIfMatch(String ifMatch) {
        this.ifMatch = ifMatch;
        return this;
    }

    /**
     * @return 'If-None-Match' header value to be added
     */
    public String ifNoneMatch() {
        return ifNoneMatch;
    }

    /**
     * Set 'If-None-Match' header value to be added to the request.
     * @param ifNoneMatch header value
     * @return ETagFilter itself
     */
    public ETagFilter withIfNoneMatch(String ifNoneMatch) {
        this.ifNoneMatch = ifNoneMatch;
        return this;
    }
}

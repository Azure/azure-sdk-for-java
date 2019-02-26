// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.azconfig.models;

public class KeyValueFilter extends KeyValueGenericFilter<KeyValueFilter> {
    private String ifMatch;
    private String ifNoneMatch;

    /**
     * @param etag etag to set as If-Match header
     * @return KeyValueGenericFilter object itself
     */
    public KeyValueFilter withIfMatch(String etag) {
        this.ifMatch = etag;
        return this;
    }

    /**
     * @return 'If-Match' header value to be added
     */
    public String ifMatch() {
        return ifMatch;
    }

    /**
     * @param etag etag to set as If-None-Match header
     * @return KeyValueGenericFilter object itself
     */
    public KeyValueFilter withIfNoneMatch(String etag) {
        this.ifNoneMatch = etag;
        return this;
    }

    /**
     * @return 'If-None-Match' header value to be added
     */
    public String ifNoneMatch() {
        return ifNoneMatch;
    }
}

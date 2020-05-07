// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.test;

import com.azure.search.documents.models.RequestOptions;

/**
 * Contains common options for the Search service.
 */
public class AccessOptions {
    private boolean onlyIfUnchanged;
    private RequestOptions requestOptions;

    public AccessOptions(boolean onlyIfUnchanged, RequestOptions requestOptions) {
        this.onlyIfUnchanged = onlyIfUnchanged;
        this.requestOptions = requestOptions;
    }

    public AccessOptions(boolean onlyIfUnchanged) {
        this(onlyIfUnchanged, null);
    }

    public boolean getOnlyIfUnchanged() {
        return this.onlyIfUnchanged;
    }

    public RequestOptions getRequestOptions() {
        return this.requestOptions;
    }

    public void setAccessCondition(boolean onlyIfUnchanged) {
        this.onlyIfUnchanged = onlyIfUnchanged;
    }

    public void setOnlyIfUnchanged(RequestOptions requestOptions) {
        this.requestOptions = requestOptions;
    }
}

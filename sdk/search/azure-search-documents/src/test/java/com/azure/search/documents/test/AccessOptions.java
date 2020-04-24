// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.test;

import com.azure.core.http.MatchConditions;
import com.azure.search.documents.models.RequestOptions;

/**
 * Contains common options for the Search service.
 */
public class AccessOptions {
    private Boolean onlyIfUnchanged;
    private RequestOptions requestOptions;

    public AccessOptions(Boolean onlyIfUnchanged, RequestOptions requestOptions) {
        this.onlyIfUnchanged = onlyIfUnchanged;
        this.requestOptions = requestOptions;
    }

    public AccessOptions(Boolean onlyIfUnchanged) {
        this(onlyIfUnchanged, null);
    }

    public Boolean getOnlyIfUnchanged() {
        return this.onlyIfUnchanged;
    }

    public RequestOptions getRequestOptions() {
        return this.requestOptions;
    }

    public void setAccessCondition(Boolean onlyIfUnchanged) {
        this.onlyIfUnchanged = onlyIfUnchanged;
    }

    public void setOnlyIfUnchanged(RequestOptions requestOptions) {
        this.requestOptions = requestOptions;
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents.test;

import com.azure.core.http.MatchConditions;
import com.azure.search.documents.models.RequestOptions;

/**
 * Contains common options for the Search service.
 */
public class AccessOptions {
    private MatchConditions accessCondition;
    private RequestOptions requestOptions;

    public AccessOptions(MatchConditions accessCondition, RequestOptions requestOptions) {
        this.accessCondition = accessCondition;
        this.requestOptions = requestOptions;
    }

    public AccessOptions(MatchConditions accessCondition) {
        this(accessCondition, null);
    }

    public MatchConditions getAccessCondition() {
        return this.accessCondition;
    }

    public RequestOptions getRequestOptions() {
        return this.requestOptions;
    }

    public void setAccessCondition(MatchConditions accessCondition) {
        this.accessCondition = accessCondition;
    }

    public void setRequestOptions(RequestOptions requestOptions) {
        this.requestOptions = requestOptions;
    }
}

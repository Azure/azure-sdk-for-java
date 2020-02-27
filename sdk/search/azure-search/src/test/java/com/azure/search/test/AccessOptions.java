// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.test;

import com.azure.search.models.AccessCondition;
import com.azure.search.models.RequestOptions;

/**
 * Contains common options for the Search service.
 */
public class AccessOptions {
    private AccessCondition accessCondition;
    private RequestOptions requestOptions;

    public AccessOptions(AccessCondition accessCondition, RequestOptions requestOptions) {
        this.accessCondition = accessCondition;
        this.requestOptions = requestOptions;
    }

    public AccessOptions(AccessCondition accessCondition) {
        this(accessCondition, null);
    }

    public AccessCondition getAccessCondition() {
        return this.accessCondition;
    }

    public RequestOptions getRequestOptions() {
        return this.requestOptions;
    }

    public void setAccessCondition(AccessCondition accessCondition) {
        this.accessCondition = accessCondition;
    }

    public void setRequestOptions(RequestOptions requestOptions) {
        this.requestOptions = requestOptions;
    }
}

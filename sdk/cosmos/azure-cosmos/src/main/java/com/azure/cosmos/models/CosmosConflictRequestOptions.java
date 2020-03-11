// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.implementation.RequestOptions;

/**
 * The type Cosmos conflict request options.
 */
public final class CosmosConflictRequestOptions {
    private AccessCondition accessCondition;

    /**
     * Gets the conditions associated with the request.
     *
     * @return the access condition.
     */
    public AccessCondition getAccessCondition() {
        return accessCondition;
    }

    /**
     * Sets the conditions associated with the request.
     *
     * @param accessCondition the access condition.
     * @return the current request options
     */
    public CosmosConflictRequestOptions setAccessCondition(AccessCondition accessCondition) {
        this.accessCondition = accessCondition;
        return this;
    }

    RequestOptions toRequestOptions() {
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.setAccessCondition(accessCondition);
        return requestOptions;
    }
}

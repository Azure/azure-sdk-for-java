// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.RequestOptions;

/**
 * Contains the request options of CosmosPermission
 */
public class CosmosPermissionRequestOptions {
    //TODO: Need to add respective options
    private AccessCondition accessCondition;

    /**
     * Gets the conditions associated with the request.
     *
     * @return the access condition.
     */
    public AccessCondition accessCondition() {
        return accessCondition;
    }

    /**
     * Sets the conditions associated with the request.
     *
     * @param accessCondition the access condition.
     * @return the current request options
     */
    public CosmosPermissionRequestOptions accessCondition(AccessCondition accessCondition) {
        this.accessCondition = accessCondition;
        return this;
    }

    RequestOptions toRequestOptions() {
        //TODO: Should we set any default values instead of nulls?
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.setAccessCondition(accessCondition);
        return requestOptions;
    }
}

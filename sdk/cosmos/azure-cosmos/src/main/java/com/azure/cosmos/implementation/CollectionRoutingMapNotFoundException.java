// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosException;
import com.azure.cosmos.implementation.directconnectivity.WFConstants;

public class CollectionRoutingMapNotFoundException extends CosmosException {
    private static final long serialVersionUID = 1L;

    /**
     * Instantiates a new Invalid partition exception.
     *
     * @param msg the msg
     */
    public CollectionRoutingMapNotFoundException(String msg) {
        super(HttpConstants.StatusCodes.NOTFOUND, msg);
        setSubStatus();
    }

    private void setSubStatus() {
        this.getResponseHeaders().put(
            WFConstants.BackendHeaders.SUB_STATUS,
            Integer.toString(HttpConstants.SubStatusCodes.INCORRECT_CONTAINER_RID_SUB_STATUS));
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.implementation.Permission;
import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * The type Cosmos permission response.
 */
public class CosmosPermissionResponse extends CosmosResponse<CosmosPermissionProperties> {

    CosmosPermissionResponse(ResourceResponse<Permission> response) {
        super(response);
        ObjectNode bodyAsJson = (ObjectNode)response.getBody();
        if (bodyAsJson == null) {
            super.setProperties(null);
        } else {
            CosmosPermissionProperties props = new CosmosPermissionProperties(bodyAsJson);
            super.setProperties(props);
        }
    }

    /**
     * Get the permission properties
     *
     * @return the permission properties
     */
    public CosmosPermissionProperties getProperties() {
        return super.getProperties();
    }
}

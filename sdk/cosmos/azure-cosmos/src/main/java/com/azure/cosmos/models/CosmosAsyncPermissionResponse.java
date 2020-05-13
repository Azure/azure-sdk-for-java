// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.models;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.CosmosAsyncPermission;
import com.azure.cosmos.CosmosAsyncUser;
import com.azure.cosmos.implementation.Permission;
import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

/**
 * The type Cosmos async permission response.
 */
public class CosmosAsyncPermissionResponse extends CosmosResponse<CosmosPermissionProperties> {
    private final CosmosAsyncPermission permissionClient;

    CosmosAsyncPermissionResponse(ResourceResponse<Permission> response, CosmosAsyncUser cosmosUser) {
        super(response);
        String bodyAsString = response.getBodyAsString();
        if (StringUtils.isEmpty(bodyAsString)) {
            super.setProperties(null);
            permissionClient = null;
        } else {
            CosmosPermissionProperties props = new CosmosPermissionProperties(bodyAsString);
            super.setProperties(props);
            permissionClient = BridgeInternal.createCosmosAsyncPermission(ModelBridgeInternal.getResourceFromResourceWrapper(props).getId(), cosmosUser);
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

    /**
     * Gets the CosmosAsyncPermission
     *
     * @return the cosmos permission
     */
    public CosmosAsyncPermission getPermission() {
        return permissionClient;
    }
}

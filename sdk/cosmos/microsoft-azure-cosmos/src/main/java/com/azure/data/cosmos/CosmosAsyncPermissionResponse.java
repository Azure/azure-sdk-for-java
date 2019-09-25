// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.Permission;
import com.azure.data.cosmos.internal.ResourceResponse;

public class CosmosAsyncPermissionResponse extends CosmosResponse<CosmosPermissionProperties> {
    CosmosAsyncPermission permissionClient;
    
    CosmosAsyncPermissionResponse(ResourceResponse<Permission> response, CosmosAsyncUser cosmosUser) {
        super(response);
        if(response.getResource() == null){
            super.properties(null);
        }else{
            super.properties(new CosmosPermissionProperties(response.getResource().toJson()));
            permissionClient = new CosmosAsyncPermission(response.getResource().id(), cosmosUser);
        }
    }

    /**
     * Get the permission properties
     *
     * @return the permission properties
     */
    public CosmosPermissionProperties properties() {
        return super.properties();
    }

    /**
     * Gets the CosmosAsyncPermission
     *
     * @return the cosmos permission
     */
    public CosmosAsyncPermission permission() {
        return permissionClient;
    }
}

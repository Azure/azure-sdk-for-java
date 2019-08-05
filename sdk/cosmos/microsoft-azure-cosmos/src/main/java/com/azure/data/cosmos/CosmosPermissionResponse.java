// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.Permission;
import com.azure.data.cosmos.internal.ResourceResponse;

public class CosmosPermissionResponse extends CosmosResponse<CosmosPermissionProperties> {
    CosmosPermission permissionClient; 
    
    CosmosPermissionResponse(ResourceResponse<Permission> response, CosmosUser cosmosUser) {
        super(response);
        if(response.getResource() == null){
            super.resourceSettings(null);
        }else{
            super.resourceSettings(new CosmosPermissionProperties(response.getResource().toJson()));
            permissionClient = new CosmosPermission(response.getResource().id(), cosmosUser);
        }
    }

    /**
     * Get the permission properties
     *
     * @return the permission properties
     */
    public CosmosPermissionProperties properties() {
        return super.resourceSettings();
    }

    /**
     * Gets the CosmosPermission
     *
     * @return the cosmos permission
     */
    public CosmosPermission permission() {
        return permissionClient;
    }
}

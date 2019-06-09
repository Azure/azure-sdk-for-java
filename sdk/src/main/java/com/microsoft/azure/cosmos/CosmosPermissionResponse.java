/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.microsoft.azure.cosmos;

import com.microsoft.azure.cosmosdb.Permission;
import com.microsoft.azure.cosmosdb.ResourceResponse;

public class CosmosPermissionResponse extends CosmosResponse<CosmosPermissionSettings> {
    CosmosPermission permissionClient; 
    
    CosmosPermissionResponse(ResourceResponse<Permission> response, CosmosUser cosmosUser) {
        super(response);
        if(response.getResource() == null){
            super.setResourceSettings(null);
        }else{
            super.setResourceSettings(new CosmosPermissionSettings(response.getResource().toJson()));
            permissionClient = new CosmosPermission(response.getResource().getId(), cosmosUser);
        }
    }

    /**
     * Get the permission settings
     * @return the permission settings
     */
    public CosmosPermissionSettings getPermissionSettings(){
        return super.getResourceSettings();
    }

    /**
     * Gets the CosmosPermission
     * @return the cosmos permission
     */
    public CosmosPermission getPermission() {
        return permissionClient;
    }
}

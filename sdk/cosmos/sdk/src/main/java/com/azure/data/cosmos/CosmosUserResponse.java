// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.ResourceResponse;
import com.azure.data.cosmos.internal.User;

public class CosmosUserResponse extends CosmosResponse<CosmosUserProperties> {
    private CosmosUser user;
    
    CosmosUserResponse(ResourceResponse<User> response, CosmosDatabase database) {
        super(response);
        if(response.getResource() == null){
            super.resourceSettings(null);
        }else{
            super.resourceSettings(new CosmosUserProperties(response));
            this.user = new CosmosUser(resourceSettings().id(), database);
        }
    }

    /**
     * Get cosmos user
     *
     * @return {@link CosmosUser}
     */
    public CosmosUser user() {
        return user;
    }

    /**
     * Gets the cosmos user properties
     *
     * @return {@link CosmosUserProperties}
     */
    public CosmosUserProperties properties(){
        return resourceSettings();
    }
}

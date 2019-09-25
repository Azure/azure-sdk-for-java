// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.ResourceResponse;
import com.azure.data.cosmos.internal.User;

public class CosmosAsyncUserResponse extends CosmosResponse<CosmosUserProperties> {
    private CosmosAsyncUser user;
    
    CosmosAsyncUserResponse(ResourceResponse<User> response, CosmosAsyncDatabase database) {
        super(response);
        if(response.getResource() == null){
            super.properties(null);
        }else{
            super.properties(new CosmosUserProperties(response));
            this.user = new CosmosAsyncUser(this.properties().id(), database);
        }
    }

    /**
     * Get cosmos user
     *
     * @return {@link CosmosAsyncUser}
     */
    public CosmosAsyncUser user() {
        return user;
    }

    /**
     * Gets the cosmos user properties
     *
     * @return {@link CosmosUserProperties}
     */
    public CosmosUserProperties properties(){
        return this.properties();
    }
}

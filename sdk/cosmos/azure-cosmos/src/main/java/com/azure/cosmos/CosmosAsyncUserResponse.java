// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.User;

public class CosmosAsyncUserResponse extends CosmosResponse<CosmosUserProperties> {
    @SuppressWarnings("EnforceFinalFields")
    private final CosmosAsyncUser user;

    CosmosAsyncUserResponse(ResourceResponse<User> response, CosmosAsyncDatabase database) {
        super(response);
        if (response.getResource() == null) {
            super.setProperties(null);
            this.user = null;
        } else {
            super.setProperties(new CosmosUserProperties(response));
            this.user = new CosmosAsyncUser(this.getProperties().getId(), database);
        }
    }

    /**
     * Get cosmos user
     *
     * @return {@link CosmosAsyncUser}
     */
    public CosmosAsyncUser getUser() {
        return user;
    }

    /**
     * Gets the cosmos user properties
     *
     * @return {@link CosmosUserProperties}
     */
    public CosmosUserProperties getProperties() {
        return super.getProperties();
    }
}

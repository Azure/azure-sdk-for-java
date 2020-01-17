// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.ResourceResponse;
import com.azure.cosmos.implementation.User;
import org.apache.commons.lang3.StringUtils;

public class CosmosAsyncUserResponse extends CosmosResponse<CosmosUserProperties> {
    @SuppressWarnings("EnforceFinalFields")
    private final CosmosAsyncUser user;

    CosmosAsyncUserResponse(ResourceResponse<User> response, CosmosAsyncDatabase database) {
        super(response);
        String bodyAsString = response.getBodyAsString();
        if (StringUtils.isEmpty(bodyAsString)) {
            super.setProperties(null);
            user = null;
        } else {
            CosmosUserProperties props = new CosmosUserProperties(bodyAsString);
            super.setProperties(props);
            user = new CosmosAsyncUser(props.getId(), database);
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

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

/**
 * The type Cosmos sync user response.
 */
public class CosmosUserResponse extends CosmosResponse<CosmosUserProperties> {
    private final CosmosAsyncUserResponse asyncResponse;
    private final CosmosUser user;

    /**
     * Instantiates a new Cosmos sync user response.
     *
     * @param response the response
     * @param database the database
     */
    CosmosUserResponse(CosmosAsyncUserResponse response, CosmosDatabase database) {
        super(response.getProperties());
        this.asyncResponse = response;
        if (response.getUser() != null) {
            this.user = new CosmosUser(response.getUser(), database, response.getUser().getId());
        } else {
            // delete has null user client
            this.user = null;
        }
    }

    /**
     * Gets cosmos sync user.
     *
     * @return the cosmos sync user
     */
    public CosmosUser getUser() {
        return this.user;
    }

    /**
     * Gets cosmos user properties.
     *
     * @return the cosmos user properties
     */
    public CosmosUserProperties getProperties() {
        return asyncResponse.getProperties();
    }
}

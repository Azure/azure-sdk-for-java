// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.sync;

import com.azure.data.cosmos.CosmosUserProperties;
import com.azure.data.cosmos.CosmosUserResponse;

/**
 * The type Cosmos sync user response.
 */
public class CosmosSyncUserResponse extends CosmosSyncResponse {
    private final CosmosUserResponse asyncResponse;
    private final CosmosSyncUser user;

    /**
     * Instantiates a new Cosmos sync user response.
     *
     * @param response the response
     * @param database the database
     */
    CosmosSyncUserResponse(CosmosUserResponse response, CosmosSyncDatabase database) {
        super(response);
        this.asyncResponse = response;
        if (response.user() != null) {
            this.user = new CosmosSyncUser(response.user(), database, response.user().id());
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
    public CosmosSyncUser user() {
        return this.user;
    }

    /**
     * Gets cosmos user properties.
     *
     * @return the cosmos user properties
     */
    public CosmosUserProperties properties() {
        return asyncResponse.properties();
    }
}

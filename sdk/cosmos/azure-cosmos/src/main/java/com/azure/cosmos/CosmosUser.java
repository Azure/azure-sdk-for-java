// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.models.CosmosUserProperties;
import com.azure.cosmos.models.CosmosUserResponse;

/**
 * The type Cosmos sync user.
 */
public class CosmosUser {
    private final CosmosAsyncUser asyncUser;
    private final CosmosDatabase database;
    private final String id;

    /**
     * Instantiates a new Cosmos sync user.
     *
     * @param asyncUser the async user
     * @param database the database
     * @param id the id
     */
    CosmosUser(CosmosAsyncUser asyncUser, CosmosDatabase database, String id) {
        this.asyncUser = asyncUser;
        this.database = database;
        this.id = id;
    }

    /**
     * Id string.
     *
     * @return the string
     */
    public String getId() {
        return id;
    }

    /**
     * Read cosmos user
     *
     * @return the cosmos sync user response
     */
    public CosmosUserResponse read() {
        return database.mapUserResponseAndBlock(asyncUser.read());
    }

    /**
     * Replace cosmos user.
     *
     * @param userProperties the user properties
     * @return the cosmos sync user response
     */
    public CosmosUserResponse replace(CosmosUserProperties userProperties) {
        return database.mapUserResponseAndBlock(asyncUser.replace(userProperties));
    }

    /**
     * Delete cosmos user.
     *
     * @return the cosmos sync user response
     */
    public CosmosUserResponse delete() {
        return database.mapUserResponseAndBlock(asyncUser.delete());
    }

}

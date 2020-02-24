// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.sync;

import com.azure.data.cosmos.CosmosClientException;
import com.azure.data.cosmos.CosmosUser;
import com.azure.data.cosmos.CosmosUserProperties;

/**
 * The type Cosmos sync user.
 */
public class CosmosSyncUser {
    private final CosmosUser asyncUser;
    private final CosmosSyncDatabase database;
    private final String id;

    /**
     * Instantiates a new Cosmos sync user.
     *
     * @param asyncUser the async user
     * @param database the database
     * @param id the id
     */
    CosmosSyncUser(CosmosUser asyncUser, CosmosSyncDatabase database, String id) {
        this.asyncUser = asyncUser;
        this.database = database;
        this.id = id;
    }

    /**
     * Id string.
     *
     * @return the string
     */
    public String id() {
        return id;
    }

    /**
     * Read cosmos user
     *
     * @return the cosmos sync user response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosSyncUserResponse read() throws CosmosClientException {
        return database.mapUserResponseAndBlock(asyncUser.read());
    }

    /**
     * Replace cosmos user.
     *
     * @param userProperties the user properties
     * @return the cosmos sync user response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosSyncUserResponse replace(CosmosUserProperties userProperties) throws CosmosClientException {
        return database.mapUserResponseAndBlock(asyncUser.replace(userProperties));
    }

    /**
     * Delete cosmos user.
     *
     * @return the cosmos sync user response
     * @throws CosmosClientException the cosmos client exception
     */
    public CosmosSyncUserResponse delete() throws CosmosClientException {
        return database.mapUserResponseAndBlock(asyncUser.delete());
    }


}

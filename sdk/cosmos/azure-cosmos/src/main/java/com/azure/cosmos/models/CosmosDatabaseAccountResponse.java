// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;

import java.util.List;

/**
 * Represents the Cosmos DB account. A Cosmos DB account is a container for databases.
 * */
public class CosmosDatabaseAccountResponse {

    private final String id;

    private final List<String> readRegions;

    private final List<String> writeRegions;

    private final boolean isMultiWriteAccount;

    private final ConsistencyLevel accountLevelConsistency;

    CosmosDatabaseAccountResponse(String id, List<String> readRegions, List<String> writeRegions, boolean isMultiWriteAccount, ConsistencyLevel accountLevelConsistency) {
        this.id = id;
        this.readRegions = readRegions;
        this.writeRegions = writeRegions;
        this.isMultiWriteAccount = isMultiWriteAccount;
        this.accountLevelConsistency = accountLevelConsistency;
    }

    /**
     * Returns the database account name
     *
     * @return The database account name.
     * */
    public String getId() {
        return this.id;
    }

    /**
     * Returns the list of read regions for the Cosmos DB account.
     *
     * @return List of read regions.
     */
    public List<String> getReadRegions() {
        return this.readRegions;
    }

    /**
     * Returns the list of write regions for the Cosmos DB account.
     *
     * @return List of write regions.
     */
    public List<String> getWriteRegions() {
        return this.writeRegions;
    }

    /**
     * Indicates whether the Cosmos DB account is configured with multiple write regions.
     *
     * @return true if the account supports multiple write regions, false otherwise.
     */
    public Boolean isMultiWriteAccount() {
        return this.isMultiWriteAccount;
    }

    /**
     * Returns the consistency level configured for the Cosmos DB account.
     *
     * @return The consistency level of the account.
     */
    public ConsistencyLevel getAccountLevelConsistency() {
        return this.accountLevelConsistency;
    }

    static void initialize() {
        ImplementationBridgeHelpers.CosmosDatabaseAccountResponseHelper.setCosmosDatabaseAccountResponseAccessor(CosmosDatabaseAccountResponse::new);
    }

    static { initialize(); }
}

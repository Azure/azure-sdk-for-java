package com.azure.cosmos.models;

import com.azure.cosmos.ConsistencyLevel;

import java.util.List;

/***
 * Represents the configuration of a Cosmos DB database account, including its read and write regions,
 * multi-write capabilities, and consistency level.
 */
public class CosmosDatabaseAccount {

    private final List<String> readRegions;

    private final List<String> writeRegions;

    private final boolean isMultiWriteAccount;

    private final ConsistencyLevel accountLevelConsistency;

    public CosmosDatabaseAccount(List<String> readRegions, List<String> writeRegions, boolean isMultiWriteAccount, ConsistencyLevel accountLevelConsistency) {
        this.readRegions = readRegions;
        this.writeRegions = writeRegions;
        this.isMultiWriteAccount = isMultiWriteAccount;
        this.accountLevelConsistency = accountLevelConsistency;
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
     * @return true if the account supports multi-write, false otherwise.
     */
    public boolean isMultiWriteAccount() {
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
}

package com.azure.cosmos.models;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosContainerProactiveInitConfig;
import com.azure.cosmos.implementation.ImplementationBridgeHelpers;
import com.azure.cosmos.implementation.directconnectivity.ContainerDirectConnectionMetadata;

import java.util.List;
import java.util.Map;

/***
 * Represents the configuration of a Cosmos DB database account, including its read and write regions,
 * multi-write capabilities, and consistency level.
 */
public class CosmosDatabaseAccount {

    private final String id;

    private final String eTag;

    private final List<String> readRegions;

    private final List<String> writeRegions;

    private final boolean isMultiWriteAccount;

    private final ConsistencyLevel accountLevelConsistency;

    CosmosDatabaseAccount(String id, String eTag, List<String> readRegions, List<String> writeRegions, boolean isMultiWriteAccount, ConsistencyLevel accountLevelConsistency) {
        this.id = id;
        this.eTag = eTag;
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
     * Returns the entity tag associated with the resource from the Azure Cosmos DB service.
     *
     * @return The eTag associated with the database account resource.
     * */
    public String getETag() {
        return this.eTag;
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

    static void initialize() {
        ImplementationBridgeHelpers.CosmosDatabaseAccountHelper.setCosmosDatabaseAccountAccessor(CosmosDatabaseAccount::new);
    }

    static { initialize(); }
}

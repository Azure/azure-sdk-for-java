// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management;

import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ResourceTests {

    @Test
    public void testSerialization() throws IOException {
        String cosmosAccountJson = "{\"id\":\"/subscriptions/ec0aa5f7-9e78-40c9-85cd-535c6305b380/resourceGroups/rg-weidxu/providers/Microsoft.DocumentDB/databaseAccounts/c1weidxu\",\"name\":\"c1weidxu\",\"location\":\"West US\",\"type\":\"Microsoft.DocumentDB/databaseAccounts\",\"kind\":\"GlobalDocumentDB\",\"tags\":{\"defaultExperience\":\"Core (SQL)\",\"hidden-cosmos-mmspecial\":\"\",\"CosmosAccountType\":\"Non-Production\"},\"systemData\":{\"createdAt\":\"2020-12-22T04:18:30.6715763Z\",\"createdBy\":\"johndoe\",\"createdByType\":\"User\"},\"properties\":{\"provisioningState\":\"Succeeded\",\"documentEndpoint\":\"https://c1weidxu.documents.azure.com:443/\",\"publicNetworkAccess\":\"Enabled\",\"enableAutomaticFailover\":false,\"enableMultipleWriteLocations\":false,\"enablePartitionKeyMonitor\":false,\"isVirtualNetworkFilterEnabled\":false,\"virtualNetworkRules\":[],\"EnabledApiTypes\":\"Sql\",\"disableKeyBasedMetadataWriteAccess\":false,\"enableFreeTier\":false,\"enableAnalyticalStorage\":false,\"instanceId\":\"f5a124e6-988e-4936-8c9b-38e011c80ef4\",\"createMode\":\"Default\",\"databaseAccountOfferType\":\"Standard\",\"enableCassandraConnector\":false,\"connectorOffer\":\"\",\"consistencyPolicy\":{\"defaultConsistencyLevel\":\"Session\",\"maxIntervalInSeconds\":5,\"maxStalenessPrefix\":100},\"configurationOverrides\":{},\"writeLocations\":[{\"id\":\"c1weidxu-westus\",\"locationName\":\"West US\",\"documentEndpoint\":\"https://c1weidxu-westus.documents.azure.com:443/\",\"provisioningState\":\"Succeeded\",\"failoverPriority\":0,\"isZoneRedundant\":false}],\"readLocations\":[{\"id\":\"c1weidxu-westus\",\"locationName\":\"West US\",\"documentEndpoint\":\"https://c1weidxu-westus.documents.azure.com:443/\",\"provisioningState\":\"Succeeded\",\"failoverPriority\":0,\"isZoneRedundant\":false}],\"locations\":[{\"id\":\"c1weidxu-westus\",\"locationName\":\"West US\",\"documentEndpoint\":\"https://c1weidxu-westus.documents.azure.com:443/\",\"provisioningState\":\"Succeeded\",\"failoverPriority\":0,\"isZoneRedundant\":false}],\"failoverPolicies\":[{\"id\":\"c1weidxu-westus\",\"locationName\":\"West US\",\"failoverPriority\":0}],\"cors\":[],\"capabilities\":[],\"ipRules\":[],\"backupPolicy\":{\"type\":\"Periodic\",\"periodicModeProperties\":{\"backupIntervalInMinutes\":240,\"backupRetentionIntervalInHours\":8}}}}";

        SerializerAdapter serializerAdapter = SerializerFactory.createDefaultManagementSerializerAdapter();
        Resource cosmosAccountResource = serializerAdapter.deserialize(cosmosAccountJson, Resource.class, SerializerEncoding.JSON);
        Assertions.assertEquals("/subscriptions/ec0aa5f7-9e78-40c9-85cd-535c6305b380/resourceGroups/rg-weidxu/providers/Microsoft.DocumentDB/databaseAccounts/c1weidxu", cosmosAccountResource.id());
        Assertions.assertEquals(Region.US_WEST, Region.fromName(cosmosAccountResource.location()));
        Assertions.assertEquals("Microsoft.DocumentDB/databaseAccounts", cosmosAccountResource.type());
        Assertions.assertEquals(3, cosmosAccountResource.tags().size());
        Assertions.assertNotNull(cosmosAccountResource.systemData());
        Assertions.assertNotNull(cosmosAccountResource.systemData().createdAt());
        Assertions.assertEquals("johndoe", cosmosAccountResource.systemData().createdBy());
        Assertions.assertEquals(CreatedByType.USER, cosmosAccountResource.systemData().createdByType());
        Assertions.assertNull(cosmosAccountResource.systemData().lastModifiedAt());
        Assertions.assertNull(cosmosAccountResource.systemData().lastModifiedBy());
        Assertions.assertNull(cosmosAccountResource.systemData().lastModifiedByType());

        ProxyResource proxyResource = serializerAdapter.deserialize(cosmosAccountJson, Resource.class, SerializerEncoding.JSON);
        Assertions.assertNotNull(proxyResource.systemData());
        Assertions.assertNotNull(proxyResource.systemData().createdAt());
        Assertions.assertEquals("johndoe", proxyResource.systemData().createdBy());
        Assertions.assertEquals(CreatedByType.USER, proxyResource.systemData().createdByType());
        Assertions.assertNull(proxyResource.systemData().lastModifiedAt());
        Assertions.assertNull(proxyResource.systemData().lastModifiedBy());
        Assertions.assertNull(proxyResource.systemData().lastModifiedByType());

        String vaultJson = "{\"id\":\"/subscriptions/###/resourceGroups/rg-weidxu/providers/Microsoft.KeyVault/vaults/v1weidxu\",\"name\":\"v1weidxu\",\"type\":\"Microsoft.KeyVault/vaults\",\"location\":\"centralus\",\"tags\":{},\"properties\":{\"sku\":{\"family\":\"A\",\"name\":\"standard\"},\"tenantId\":\"###\",\"accessPolicies\":[],\"enabledForDeployment\":false,\"vaultUri\":\"https://v1weidxu.vault.azure.net/\",\"provisioningState\":\"Succeeded\"}}";
        Resource vaultResource = serializerAdapter.deserialize(vaultJson, Resource.class, SerializerEncoding.JSON);
        Assertions.assertEquals("/subscriptions/###/resourceGroups/rg-weidxu/providers/Microsoft.KeyVault/vaults/v1weidxu", vaultResource.id());
        Assertions.assertEquals(Region.US_CENTRAL, Region.fromName(vaultResource.location()));
        Assertions.assertEquals("Microsoft.KeyVault/vaults", vaultResource.type());
        Assertions.assertEquals(0, vaultResource.tags().size());
        Assertions.assertNull(vaultResource.systemData());
    }
}

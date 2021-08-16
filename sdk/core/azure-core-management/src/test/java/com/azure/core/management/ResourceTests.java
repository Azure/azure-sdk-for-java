// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management;

import com.azure.core.management.serializer.SerializerFactory;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import wiremock.com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;

public class ResourceTests {

    private static class ProxyResourceWithSystemData extends ProxyResource {
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        private SystemData systemData;

        /**
         * Get the systemData value.
         *
         * @return the metadata pertaining to creation and last modification of the resource.
         * */
        public SystemData systemData() {
            return this.systemData;
        }
    }

    private static class ResourceWithSystemData extends Resource {
        @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
        private SystemData systemData;

        /**
         * Get the systemData value.
         *
         * @return the metadata pertaining to creation and last modification of the resource.
         * */
        public SystemData systemData() {
            return this.systemData;
        }
    }

    @Test
    public void testSerialization() throws IOException {
        String cosmosAccountJson = "{\"id\":\"/subscriptions/###/resourceGroups/rg-weidxu/providers/Microsoft.DocumentDB/databaseAccounts/c1weidxu\",\"name\":\"c1weidxu\",\"location\":\"West US\",\"type\":\"Microsoft.DocumentDB/databaseAccounts\",\"kind\":\"GlobalDocumentDB\",\"tags\":{\"defaultExperience\":\"Core (SQL)\",\"hidden-cosmos-mmspecial\":\"\",\"CosmosAccountType\":\"Non-Production\"},\"systemData\":{\"createdBy\":\"00000000-0000-0000-0000-000000000000\",\"createdByType\":\"Application\",\"createdAt\":\"2021-03-03T02:03:46.3387771Z\",\"lastModifiedBy\":\"johndoe@microsoft.com\",\"lastModifiedByType\":\"User\",\"lastModifiedAt\":\"2021-03-03T02:04:42.8252362Z\"},\"properties\":{\"provisioningState\":\"Succeeded\",\"documentEndpoint\":\"https://c1weidxu.documents.azure.com:443/\",\"publicNetworkAccess\":\"Enabled\",\"enableAutomaticFailover\":false,\"enableMultipleWriteLocations\":false,\"enablePartitionKeyMonitor\":false,\"isVirtualNetworkFilterEnabled\":false,\"virtualNetworkRules\":[],\"EnabledApiTypes\":\"Sql\",\"disableKeyBasedMetadataWriteAccess\":false,\"enableFreeTier\":false,\"enableAnalyticalStorage\":false,\"instanceId\":\"f5a124e6-988e-4936-8c9b-38e011c80ef4\",\"createMode\":\"Default\",\"databaseAccountOfferType\":\"Standard\",\"enableCassandraConnector\":false,\"connectorOffer\":\"\",\"consistencyPolicy\":{\"defaultConsistencyLevel\":\"Session\",\"maxIntervalInSeconds\":5,\"maxStalenessPrefix\":100},\"configurationOverrides\":{},\"writeLocations\":[{\"id\":\"c1weidxu-westus\",\"locationName\":\"West US\",\"documentEndpoint\":\"https://c1weidxu-westus.documents.azure.com:443/\",\"provisioningState\":\"Succeeded\",\"failoverPriority\":0,\"isZoneRedundant\":false}],\"readLocations\":[{\"id\":\"c1weidxu-westus\",\"locationName\":\"West US\",\"documentEndpoint\":\"https://c1weidxu-westus.documents.azure.com:443/\",\"provisioningState\":\"Succeeded\",\"failoverPriority\":0,\"isZoneRedundant\":false}],\"locations\":[{\"id\":\"c1weidxu-westus\",\"locationName\":\"West US\",\"documentEndpoint\":\"https://c1weidxu-westus.documents.azure.com:443/\",\"provisioningState\":\"Succeeded\",\"failoverPriority\":0,\"isZoneRedundant\":false}],\"failoverPolicies\":[{\"id\":\"c1weidxu-westus\",\"locationName\":\"West US\",\"failoverPriority\":0}],\"cors\":[],\"capabilities\":[],\"ipRules\":[],\"backupPolicy\":{\"type\":\"Periodic\",\"periodicModeProperties\":{\"backupIntervalInMinutes\":240,\"backupRetentionIntervalInHours\":8}}}}";

        SerializerAdapter serializerAdapter = SerializerFactory.createDefaultManagementSerializerAdapter();
        ResourceWithSystemData cosmosAccountResource = serializerAdapter.deserialize(cosmosAccountJson, ResourceWithSystemData.class, SerializerEncoding.JSON);
        Assertions.assertEquals("/subscriptions/###/resourceGroups/rg-weidxu/providers/Microsoft.DocumentDB/databaseAccounts/c1weidxu", cosmosAccountResource.id());
        Assertions.assertEquals(Region.US_WEST, Region.fromName(cosmosAccountResource.location()));
        Assertions.assertEquals("Microsoft.DocumentDB/databaseAccounts", cosmosAccountResource.type());
        Assertions.assertEquals(3, cosmosAccountResource.tags().size());
        Assertions.assertNotNull(cosmosAccountResource.systemData());
        Assertions.assertNotNull(cosmosAccountResource.systemData().createdAt());
        Assertions.assertEquals("00000000-0000-0000-0000-000000000000", cosmosAccountResource.systemData().createdBy());
        Assertions.assertEquals(ResourceAuthorIdentityType.APPLICATION, cosmosAccountResource.systemData().createdByType());
        Assertions.assertNotNull(cosmosAccountResource.systemData().createdAt());
        Assertions.assertEquals("johndoe@microsoft.com", cosmosAccountResource.systemData().lastModifiedBy());
        Assertions.assertEquals(ResourceAuthorIdentityType.USER, cosmosAccountResource.systemData().lastModifiedByType());
        Assertions.assertNotNull(cosmosAccountResource.systemData().lastModifiedAt());

        ProxyResourceWithSystemData proxyResource = serializerAdapter.deserialize(cosmosAccountJson, ProxyResourceWithSystemData.class, SerializerEncoding.JSON);
        Assertions.assertNotNull(proxyResource.systemData());
        Assertions.assertNotNull(proxyResource.systemData().createdAt());
        Assertions.assertEquals("00000000-0000-0000-0000-000000000000", proxyResource.systemData().createdBy());
        Assertions.assertEquals(ResourceAuthorIdentityType.APPLICATION, proxyResource.systemData().createdByType());
        Assertions.assertNotNull(cosmosAccountResource.systemData().createdAt());
        Assertions.assertEquals("johndoe@microsoft.com", cosmosAccountResource.systemData().lastModifiedBy());
        Assertions.assertEquals(ResourceAuthorIdentityType.USER, cosmosAccountResource.systemData().lastModifiedByType());
        Assertions.assertNotNull(cosmosAccountResource.systemData().lastModifiedAt());

        String vaultJson = "{\"id\":\"/subscriptions/###/resourceGroups/rg-weidxu/providers/Microsoft.KeyVault/vaults/v1weidxu\",\"name\":\"v1weidxu\",\"type\":\"Microsoft.KeyVault/vaults\",\"location\":\"centralus\",\"tags\":{},\"properties\":{\"sku\":{\"family\":\"A\",\"name\":\"standard\"},\"tenantId\":\"###\",\"accessPolicies\":[],\"enabledForDeployment\":false,\"vaultUri\":\"https://v1weidxu.vault.azure.net/\",\"provisioningState\":\"Succeeded\"}}";
        ResourceWithSystemData vaultResource = serializerAdapter.deserialize(vaultJson, ResourceWithSystemData.class, SerializerEncoding.JSON);
        Assertions.assertEquals("/subscriptions/###/resourceGroups/rg-weidxu/providers/Microsoft.KeyVault/vaults/v1weidxu", vaultResource.id());
        Assertions.assertEquals(Region.US_CENTRAL, Region.fromName(vaultResource.location()));
        Assertions.assertEquals("Microsoft.KeyVault/vaults", vaultResource.type());
        Assertions.assertEquals(0, vaultResource.tags().size());
        Assertions.assertNull(vaultResource.systemData());
    }
}

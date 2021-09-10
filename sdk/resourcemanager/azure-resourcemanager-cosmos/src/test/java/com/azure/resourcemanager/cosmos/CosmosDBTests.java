// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cosmos;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.resourcemanager.cosmos.models.ConnectorOffer;
import com.azure.resourcemanager.cosmos.models.CosmosDBAccount;
import com.azure.resourcemanager.cosmos.models.DatabaseAccountKind;
import com.azure.resourcemanager.cosmos.models.DefaultConsistencyLevel;
import com.azure.resourcemanager.cosmos.models.PrivateEndpointConnection;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.ServiceEndpointType;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.core.management.Region;
import com.azure.core.management.profile.AzureProfile;
import com.azure.resourcemanager.resources.ResourceManager;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.test.ResourceManagerTestBase;
import com.azure.resourcemanager.test.utils.TestDelayProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

public class CosmosDBTests extends ResourceManagerTestBase {

    private String rgName = "";
    protected ResourceManager resourceManager;
    protected CosmosManager cosmosManager;
    protected NetworkManager networkManager;

    @Override
    protected HttpPipeline buildHttpPipeline(
        TokenCredential credential,
        AzureProfile profile,
        HttpLogOptions httpLogOptions,
        List<HttpPipelinePolicy> policies,
        HttpClient httpClient) {
        return HttpPipelineProvider.buildHttpPipeline(
            credential,
            profile,
            null,
            httpLogOptions,
            null,
            new RetryPolicy("Retry-After", ChronoUnit.SECONDS),
            policies,
            httpClient);
    }

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        ResourceManagerUtils.InternalRuntimeContext.setDelayProvider(new TestDelayProvider(!isPlaybackMode()));
        rgName = generateRandomResourceName("rgcosmosdb", 20);
        cosmosManager = buildManager(CosmosManager.class, httpPipeline, profile);
        networkManager = buildManager(NetworkManager.class, httpPipeline, profile);
        resourceManager = cosmosManager.resourceManager();
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().beginDeleteByName(rgName);
    }

    @Test
    public void canCreateCosmosDbSqlAccount() {
        final String cosmosDbAccountName = generateRandomResourceName("cosmosdb", 22);

        CosmosDBAccount cosmosDBAccount =
            cosmosManager
                .databaseAccounts()
                .define(cosmosDbAccountName)
                .withRegion(Region.US_WEST_CENTRAL)
                .withNewResourceGroup(rgName)
                .withDataModelSql()
                .withEventualConsistency()
                .withWriteReplication(Region.US_EAST)
                .withReadReplication(Region.US_CENTRAL)
                .withMultipleWriteLocationsEnabled(true)
                .withTag("tag1", "value1")
                .create();

        Assertions.assertEquals(cosmosDBAccount.name(), cosmosDbAccountName.toLowerCase());
        Assertions.assertEquals(cosmosDBAccount.kind(), DatabaseAccountKind.GLOBAL_DOCUMENT_DB);
        Assertions.assertEquals(cosmosDBAccount.writableReplications().size(), 2);
        Assertions.assertEquals(cosmosDBAccount.readableReplications().size(), 2);
        Assertions.assertEquals(cosmosDBAccount.defaultConsistencyLevel(), DefaultConsistencyLevel.EVENTUAL);
        Assertions.assertTrue(cosmosDBAccount.multipleWriteLocationsEnabled());
    }

    @Test
    public void canCreateSqlPrivateEndpoint() {
        final String cosmosDbAccountName = generateRandomResourceName("cosmosdb", 22);
        final String networkName = generateRandomResourceName("network", 22);
        final String subnetName = generateRandomResourceName("subnet", 22);
        final String plsConnectionName = generateRandomResourceName("plsconnect", 22);
        final String pedName = generateRandomResourceName("ped", 22);
        final Region region = Region.US_WEST;

        cosmosManager.resourceManager().resourceGroups().define(rgName).withRegion(region).create();

        Network network =
            networkManager
                .networks()
                .define(networkName)
                .withRegion(region)
                .withExistingResourceGroup(rgName)
                .withAddressSpace("10.0.0.0/16")
                .defineSubnet(subnetName)
                    .withAddressPrefix("10.0.0.0/24")
                    .withAccessFromService(ServiceEndpointType.MICROSOFT_AZURECOSMOSDB)
                    .disableNetworkPoliciesOnPrivateEndpoint()
                    .attach()
                .create();

        CosmosDBAccount cosmosDBAccount =
            cosmosManager
                .databaseAccounts()
                .define(cosmosDbAccountName)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(rgName)
                .withDataModelSql()
                .withStrongConsistency()
                .withDisableKeyBaseMetadataWriteAccess(true)
                .create();

        Assertions.assertTrue(cosmosDBAccount.keyBasedMetadataWriteAccessDisabled());

        // create network private endpoint.
        PrivateEndpoint privateEndpoint = networkManager.privateEndpoints().define(pedName)
            .withRegion(region)
            .withExistingResourceGroup(rgName)
            .withSubnet(network.subnets().get(subnetName))
            .definePrivateLinkServiceConnection(plsConnectionName)
                .withResource(cosmosDBAccount)
                .withSubResource(PrivateLinkSubResourceName.COSMOS_SQL)
                .attach()
            .create();

        Assertions.assertEquals("Approved", privateEndpoint.privateLinkServiceConnections().get(plsConnectionName).state().status());

        cosmosDBAccount
            .update()
            .defineNewPrivateEndpointConnection(pedName)
            .withStatus("Rejected")
            .withDescription("Rej")
            .attach()
            .apply();

        Map<String, PrivateEndpointConnection> connections = cosmosDBAccount.listPrivateEndpointConnection();
        Assertions.assertTrue(connections.containsKey(pedName));
        Assertions.assertEquals("Rejected", connections.get(pedName).privateLinkServiceConnectionState().status());

        Assertions.assertEquals(1, cosmosDBAccount.listPrivateLinkResources().size());

        cosmosDBAccount
            .update()
            .updatePrivateEndpointConnection(pedName)
            .withDescription("Test Update")
            .parent()
            .apply();
        Assertions
            .assertEquals(
                "Test Update",
                cosmosDBAccount
                    .getPrivateEndpointConnection(pedName)
                    .privateLinkServiceConnectionState()
                    .description());
    }

    @Test
    public void canCreateCosmosDbMongoDBAccount() {
        final String cosmosDbAccountName = generateRandomResourceName("cosmosdb", 22);

        CosmosDBAccount cosmosDBAccount =
            cosmosManager
                .databaseAccounts()
                .define(cosmosDbAccountName)
                .withRegion(Region.US_WEST_CENTRAL)
                .withNewResourceGroup(rgName)
                .withDataModelMongoDB()
                .withEventualConsistency()
                .withWriteReplication(Region.US_EAST)
                .withReadReplication(Region.US_CENTRAL)
                .withTag("tag1", "value1")
                .create();

        Assertions.assertEquals(cosmosDBAccount.name(), cosmosDbAccountName.toLowerCase());
        Assertions.assertEquals(cosmosDBAccount.kind(), DatabaseAccountKind.MONGO_DB);
        Assertions.assertEquals(cosmosDBAccount.writableReplications().size(), 1);
        Assertions.assertEquals(cosmosDBAccount.readableReplications().size(), 2);
        Assertions.assertEquals(cosmosDBAccount.defaultConsistencyLevel(), DefaultConsistencyLevel.EVENTUAL);
    }

    @Test
    public void canCreateCosmosDbCassandraAccount() {
        final String cosmosDbAccountName = generateRandomResourceName("cosmosdb", 22);

        CosmosDBAccount cosmosDBAccount =
            cosmosManager
                .databaseAccounts()
                .define(cosmosDbAccountName)
                .withRegion(Region.US_WEST_CENTRAL)
                .withNewResourceGroup(rgName)
                .withDataModelCassandra()
                .withEventualConsistency()
                .withWriteReplication(Region.US_EAST)
                .withReadReplication(Region.US_WEST)
                .withTag("tag1", "value1")
                .create();

        Assertions.assertEquals(cosmosDBAccount.name(), cosmosDbAccountName.toLowerCase());
        Assertions.assertEquals(cosmosDBAccount.kind(), DatabaseAccountKind.GLOBAL_DOCUMENT_DB);
        Assertions.assertEquals(cosmosDBAccount.capabilities().get(0).name(), "EnableCassandra");
        Assertions.assertEquals(cosmosDBAccount.writableReplications().size(), 1);
        Assertions.assertEquals(cosmosDBAccount.readableReplications().size(), 2);
        Assertions.assertEquals(cosmosDBAccount.defaultConsistencyLevel(), DefaultConsistencyLevel.EVENTUAL);
    }

    @Test
    public void canUpdateCosmosDbCassandraConnector() {
        final String cosmosDbAccountName = generateRandomResourceName("cosmosdb", 22);

        // CassandraConnector could only be used in West US and South Central US.
        CosmosDBAccount cosmosDBAccount =
            cosmosManager
                .databaseAccounts()
                .define(cosmosDbAccountName)
                .withRegion(Region.US_WEST)
                .withNewResourceGroup(rgName)
                .withDataModelCassandra()
                .withStrongConsistency()
                .withCassandraConnector(ConnectorOffer.SMALL)
                .withTag("tag1", "value1")
                .create();

        Assertions.assertEquals("value1", cosmosDBAccount.tags().get("tag1"));
        Assertions.assertTrue(cosmosDBAccount.cassandraConnectorEnabled());
        Assertions.assertEquals(ConnectorOffer.SMALL, cosmosDBAccount.cassandraConnectorOffer());

        cosmosDBAccount = cosmosDBAccount.update().withoutCassandraConnector().apply();

        Assertions.assertFalse(cosmosDBAccount.cassandraConnectorEnabled());
    }

    @Test
    public void canCreateCosmosDbAzureTableAccount() {
        final String cosmosDbAccountName = generateRandomResourceName("cosmosdb", 22);

        CosmosDBAccount cosmosDBAccount =
            cosmosManager
                .databaseAccounts()
                .define(cosmosDbAccountName)
                .withRegion(Region.US_WEST_CENTRAL)
                .withNewResourceGroup(rgName)
                .withDataModelAzureTable()
                .withEventualConsistency()
                .withWriteReplication(Region.US_EAST)
                .withReadReplication(Region.US_EAST2)
                .withTag("tag1", "value1")
                .create();

        Assertions.assertEquals(cosmosDBAccount.name(), cosmosDbAccountName.toLowerCase());
        Assertions.assertEquals(cosmosDBAccount.kind(), DatabaseAccountKind.GLOBAL_DOCUMENT_DB);
        Assertions.assertEquals(cosmosDBAccount.capabilities().get(0).name(), "EnableTable");
        Assertions.assertEquals(cosmosDBAccount.writableReplications().size(), 1);
        Assertions.assertEquals(cosmosDBAccount.readableReplications().size(), 2);
        Assertions.assertEquals(cosmosDBAccount.defaultConsistencyLevel(), DefaultConsistencyLevel.EVENTUAL);
    }
}

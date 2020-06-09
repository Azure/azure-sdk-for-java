// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cosmos;

import com.azure.core.http.HttpPipeline;
import com.azure.resourcemanager.cosmos.models.ConnectorOffer;
import com.azure.resourcemanager.cosmos.models.CosmosDBAccount;
import com.azure.resourcemanager.cosmos.models.DatabaseAccountKind;
import com.azure.resourcemanager.cosmos.models.DefaultConsistencyLevel;
import com.azure.resourcemanager.cosmos.models.PrivateEndpointConnection;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.PrivateLinkServiceConnection;
import com.azure.resourcemanager.network.models.PrivateLinkServiceConnectionState;
import com.azure.resourcemanager.network.models.ServiceEndpointType;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.inner.PrivateEndpointInner;
import com.azure.resourcemanager.resources.core.TestBase;
import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.profile.AzureProfile;
import com.azure.resourcemanager.resources.ResourceManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

public class CosmosDBTests extends TestBase {

    private String rgName = "";
    protected ResourceManager resourceManager;
    protected CosmosManager cosmosManager;
    protected NetworkManager networkManager;
    //    final String sqlPrimaryServerName = sdkContext.randomResourceName("sqlpri", 22);

    public CosmosDBTests() {
        super(TestBase.RunCondition.BOTH);
    }

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile)
        throws IOException {
        rgName = generateRandomResourceName("rgcosmosdb", 20);
        resourceManager = ResourceManager.authenticate(httpPipeline, profile).withDefaultSubscription();

        cosmosManager = CosmosManager.authenticate(httpPipeline, profile);

        networkManager = NetworkManager.authenticate(httpPipeline, profile);
    }

    @Override
    protected void cleanUpResources() {
        resourceManager.resourceGroups().beginDeleteByName(rgName);
    }

    @Test
    public void canCreateCosmosDbSqlAccount() {
        final String cosmosDbAccountName = sdkContext.randomResourceName("cosmosdb", 22);

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
                .withIpRangeFilter("")
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
        final String cosmosDbAccountName = sdkContext.randomResourceName("cosmosdb", 22);
        final String networkName = sdkContext.randomResourceName("network", 22);
        final String subnetName = sdkContext.randomResourceName("subnet", 22);
        final String plsConnectionName = sdkContext.randomResourceName("plsconnect", 22);
        final String pedName = sdkContext.randomResourceName("ped", 22);
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
                .attach()
                .create();

        network.subnets().get(subnetName).inner().withPrivateEndpointNetworkPolicies("Disabled");
        network.subnets().get(subnetName).inner().withPrivateLinkServiceNetworkPolicies("Disabled");

        network.update().updateSubnet(subnetName).parent().apply();

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
        PrivateLinkServiceConnection privateLinkServiceConnection =
            new PrivateLinkServiceConnection()
                .withName(plsConnectionName)
                .withPrivateLinkServiceId(cosmosDBAccount.id())
                .withPrivateLinkServiceConnectionState(new PrivateLinkServiceConnectionState().withStatus("Approved"))
                .withGroupIds(Arrays.asList("Sql"));

        PrivateEndpointInner privateEndpoint =
            new PrivateEndpointInner()
                .withPrivateLinkServiceConnections(Arrays.asList(privateLinkServiceConnection))
                .withSubnet(network.subnets().get(subnetName).inner());

        privateEndpoint.withLocation(region.toString());
        privateEndpoint = networkManager.inner().getPrivateEndpoints().createOrUpdate(rgName, pedName, privateEndpoint);

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
        final String cosmosDbAccountName = sdkContext.randomResourceName("cosmosdb", 22);

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
                .withIpRangeFilter("")
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
        final String cosmosDbAccountName = sdkContext.randomResourceName("cosmosdb", 22);

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
                .withIpRangeFilter("")
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
        final String cosmosDbAccountName = sdkContext.randomResourceName("cosmosdb", 22);

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
        final String cosmosDbAccountName = sdkContext.randomResourceName("cosmosdb", 22);

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
                .withIpRangeFilter("")
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

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.InternalObjectNode;
import com.azure.cosmos.models.ComputedProperty;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.CosmosContainerResponse;
import com.azure.cosmos.models.CosmosItemResponse;
import com.azure.cosmos.models.CosmosQueryRequestOptions;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.SqlQuerySpec;
import com.azure.cosmos.rx.TestSuiteBase;
import com.azure.cosmos.util.CosmosPagedIterable;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Ignore;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ContainerPropertiesTest extends TestSuiteBase {

    private String preExistingDatabaseId = CosmosDatabaseForTest.generateId();
    private CosmosClient client;
    private CosmosDatabase createdDatabase;
    private CosmosContainer container;

    @Factory(dataProvider = "clientBuilders")
    public ContainerPropertiesTest(CosmosClientBuilder clientBuilder) {
        super(clientBuilder);
    }

    @BeforeClass(groups = {"fast"}, timeOut = SETUP_TIMEOUT)
    public void before_CosmosContainerTest() {
        client = getClientBuilder().buildClient();
        createdDatabase = createSyncDatabase(client, preExistingDatabaseId);
    }

    @AfterClass(groups = {"fast"}, timeOut = 3 * SHUTDOWN_TIMEOUT, alwaysRun = true)
    public void afterClass() {
        logger.info("starting ....");
        safeDeleteSyncDatabase(createdDatabase);
        safeCloseSyncClient(client);
    }

    @Test(groups = { "fast" }, timeOut = TIMEOUT)
    public void testComputedProperties() throws Exception {
        String collectionName = UUID.randomUUID().toString();
        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);

        List<ComputedProperty> computedProperties = new ArrayList<>(
            Arrays.asList(
                new ComputedProperty("lowerName", "SELECT VALUE LOWER(IS_DEFINED(c.lastName) ? c.lastName : c.parents[0].familyName) FROM c"),
                new ComputedProperty("upperName", "SELECT VALUE UPPER(IS_DEFINED(c.lastName) ? c.lastName : c.parents[0].familyName) FROM c")
            )
        );

        containerProperties.setComputedProperties(computedProperties);

        // create container with container properties
        CosmosContainerResponse containerResponse = createdDatabase.createContainer(containerProperties);
        container = createdDatabase.getContainer(collectionName);

        List<ComputedProperty> responseComputedProperties = new ArrayList<>(containerResponse.getProperties().getComputedProperties());
        validateContainerProperties(computedProperties, responseComputedProperties);

        // replace container
        List<ComputedProperty> newComputedProperties = new ArrayList<>(
            Arrays.asList(
                new ComputedProperty("lowerName", "SELECT VALUE LOWER(c.firstName) FROM c")
            )
        );
        CosmosContainerResponse replaceContainerResponse = container.replace(
            containerResponse.getProperties().setComputedProperties(newComputedProperties)
        );
        container = createdDatabase.getContainer(collectionName);

        assertThat(replaceContainerResponse.getProperties()).isNotNull();

        responseComputedProperties = new ArrayList<>(replaceContainerResponse.getProperties().getComputedProperties());
        validateContainerProperties(newComputedProperties, responseComputedProperties);

        // append container properties
        CosmosContainerProperties modifiedProperites = container.read().getProperties();
        Collection<ComputedProperty> modifiedComputedProperites = modifiedProperites.getComputedProperties();
        modifiedComputedProperites.add(new ComputedProperty("upperName", "SELECT VALUE UPPER(c.firstName) FROM c"));
        modifiedProperites.setComputedProperties(modifiedComputedProperites);

        CosmosContainerResponse appendResponse = container.replace(modifiedProperites);

        container = createdDatabase.getContainer(collectionName);

        assertThat(appendResponse.getProperties()).isNotNull();
        responseComputedProperties = new ArrayList<>(appendResponse.getProperties().getComputedProperties());
        validateContainerProperties(modifiedProperites.getComputedProperties(), responseComputedProperties);

        // create documents
        InternalObjectNode properties = getDocumentDefinition("Mike", "Andersen");
        CosmosItemResponse<InternalObjectNode> itemResponse = container.createItem(properties);
        assertThat(itemResponse.getRequestCharge()).isGreaterThan(0);

        // query documents
        CosmosPagedIterable<InternalObjectNode> familiesPagedIterable = container.queryItems(
            new SqlQuerySpec("SELECT c.lowerName, c.upperName FROM c"), new CosmosQueryRequestOptions(), InternalObjectNode.class);

        familiesPagedIterable.forEach(cosmosItemPropertiesFeedResponse -> {
            assertThat(cosmosItemPropertiesFeedResponse.get("lowerName").toString()).isEqualTo("mike");
            assertThat(cosmosItemPropertiesFeedResponse.get("upperName").toString()).isEqualTo("MIKE");
        });
    }

    @Test(groups = { "unit" }, timeOut = TIMEOUT)
    public void testComputedProperty_SerializationAndDeserialization() {
        ComputedProperty computedProperty = new ComputedProperty("lowerName", "SELECT VALUE LOWER(IS_DEFINED(c.lastName) ? c.lastName : c.parents[0].familyName) FROM c");
        String collectionName = UUID.randomUUID().toString();

        CosmosContainerProperties containerProperties = getCollectionDefinition(collectionName);
        containerProperties.setComputedProperties(Arrays.asList(computedProperty));
        DocumentCollection documentCollection = ModelBridgeInternal.getV2Collection(containerProperties);

        String json = documentCollection.toJson();
        DocumentCollection documentCollectionPostSerialization = new DocumentCollection(json);

        Collection<ComputedProperty> computedPropertiesPostSerialization = documentCollectionPostSerialization.getComputedProperties();
        assertThat(computedPropertiesPostSerialization).isNotNull();

        validateContainerProperties(Arrays.asList(computedProperty), computedPropertiesPostSerialization);
    }

    private InternalObjectNode getDocumentDefinition(String firstName, String lastName) {
        final String uuid = UUID.randomUUID().toString();
        final InternalObjectNode properties =
            new InternalObjectNode(String.format("{ "
                    + "\"id\": \"%s\", "
                    + "\"mypk\": \"%s\", "
                    + "\"firstName\": \"%s\", "
                    + "\"lastName\": \"%s\" "
                    + "}"
                , uuid, uuid, firstName, lastName));
        return properties;
    }

    private static void validateContainerProperties(Collection<ComputedProperty> computedProperties, Collection<ComputedProperty> responseComputedProperties) {
        assertThat(responseComputedProperties.size()).isEqualTo(computedProperties.size());

        for (ComputedProperty computedProperty : responseComputedProperties) {
            assertThat(computedProperties.stream().anyMatch(x ->
                x.getName().equals(computedProperty.getName()) && x.getQuery().equals(computedProperty.getQuery())
            )).isTrue();
        }
    }
}

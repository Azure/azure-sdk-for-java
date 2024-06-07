package com.azure.cosmos;

import com.azure.cosmos.implementation.TestConfigurations;
import com.azure.cosmos.models.ComputedProperty;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.PartitionKeyDefinition;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public class ComputedPropertiesCodeSnippet {
    private CosmosAsyncClient client;
    private CosmosAsyncDatabase database;
    private CosmosAsyncContainer container;
    private String containerName = "TestContainer";

    public ComputedPropertiesCodeSnippet() {
        this.client = new CosmosClientBuilder()
                .endpoint(TestConfigurations.HOST)
                .key(TestConfigurations.MASTER_KEY)
                .contentResponseOnWriteEnabled(true)
                .consistencyLevel(ConsistencyLevel.SESSION)
                .buildAsyncClient();

        this.database = this.client.getDatabase("TestDB");
    }

    public void createContainerWithComputedProperties() {
        CosmosContainerProperties containerProperties = getCollectionDefinition(containerName);
        // BEGIN: com.azure.cosmos.computedProperty.createContainer
        List<ComputedProperty> computedProperties = new ArrayList<>(
                Arrays.asList(
                        new ComputedProperty("lowerName", "SELECT VALUE LOWER(c.name) FROM c")
                )
        );
        containerProperties.setComputedProperties(computedProperties);
        database.createContainer(containerProperties).subscribe();
        // END: com.azure.cosmos.computedProperty.createContainer
    }

    public void replaceContainerWithComputedProperties() {
        // BEGIN: com.azure.cosmos.computedProperty.replaceContainer
        CosmosContainerProperties containerProperties = getCollectionDefinition(containerName);
        List<ComputedProperty> computedProperties = new ArrayList<>(
                Arrays.asList(
                        new ComputedProperty("upperName", "SELECT VALUE UPPER(c.name) FROM c")
                )
        );
        containerProperties.setComputedProperties(computedProperties);
        container = database.getContainer(containerName);
        container.replace(containerProperties).subscribe();
        // END: com.azure.cosmos.computedProperty.replaceContainer
    }

    public void replaceContainerWithExistingComputedProperties() {
        // BEGIN: com.azure.cosmos.computedProperty.replaceContainer.existingProperties
        container = database.getContainer(containerName);

        CosmosContainerProperties modifiedProperties = container.read().block().getProperties();
        Collection<ComputedProperty> modifiedComputedProperties = modifiedProperties.getComputedProperties();
        modifiedComputedProperties.add(new ComputedProperty("upperName", "SELECT VALUE UPPER(c.firstName) FROM c"));
        modifiedProperties.setComputedProperties(modifiedComputedProperties);

        container.replace(modifiedProperties).subscribe();
        // END: com.azure.cosmos.computedProperty.replaceContainer.existingProperties
    }

    static protected CosmosContainerProperties getCollectionDefinition(String collectionId) {
        PartitionKeyDefinition partitionKeyDef = new PartitionKeyDefinition();
        ArrayList<String> paths = new ArrayList<>();
        paths.add("/mypk");
        partitionKeyDef.setPaths(paths);

        CosmosContainerProperties collectionDefinition = new CosmosContainerProperties(collectionId, partitionKeyDef);

        return collectionDefinition;
    }
}

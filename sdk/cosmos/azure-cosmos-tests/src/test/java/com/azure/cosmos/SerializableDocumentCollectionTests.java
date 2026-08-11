// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.PartitionKeyDefinitionVersion;
import com.azure.cosmos.models.SpatialSpec;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SerializableDocumentCollectionTests {

    @Test(groups = { "unit" })
    public void serialize_Deserialize_ViaJson() throws Exception {
        DocumentCollection collection = new DocumentCollection();
        PartitionKeyDefinition partitionKeyDefinition = new PartitionKeyDefinition();
        partitionKeyDefinition.setPaths(ImmutableList.of("/mypk"));
        partitionKeyDefinition.setVersion(PartitionKeyDefinitionVersion.V2);
        collection.setPartitionKey(partitionKeyDefinition);
        SpatialSpec spatialSpec = new SpatialSpec();
        List<SpatialSpec> spatialSpecList = new ArrayList<>();
        spatialSpecList.add(spatialSpec);
        IndexingPolicy indexingPolicy = new IndexingPolicy();
        indexingPolicy.setSpatialIndexes(spatialSpecList);
        collection.setIndexingPolicy(indexingPolicy);
        String altLink = UUID.randomUUID().toString();
        collection.setAltLink(altLink);

        // Serialize to JSON ObjectNode
        ObjectNode serialized = collection.toSerializableObjectNode();

        // Deserialize from JSON ObjectNode
        DocumentCollection deserialized = DocumentCollection.fromSerializableObjectNode(serialized);

        // Compare
        assertThat(deserialized.getAltLink())
            .isEqualTo(collection.getAltLink())
            .isEqualTo(altLink);
        assertThat(deserialized.toJson()).isEqualTo(collection.toJson());
    }

    @Test(groups = { "unit" })
    public void fromSerializableObjectNode_NullNode_ThrowsException() {
        assertThatThrownBy(() -> DocumentCollection.fromSerializableObjectNode(null))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("must not be null");
    }

    @Test(groups = { "unit" })
    public void fromSerializableObjectNode_MissingFields_ThrowsException() {
        com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
        ObjectNode emptyNode = mapper.createObjectNode();

        assertThatThrownBy(() -> DocumentCollection.fromSerializableObjectNode(emptyNode))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("col");
    }
}

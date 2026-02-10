// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import com.azure.cosmos.models.IndexingPolicy;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.models.PartitionKeyDefinitionVersion;
import com.azure.cosmos.models.SpatialSpec;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.azure.cosmos.implementation.DocumentCollection.SerializableDocumentCollection;
import static org.assertj.core.api.Assertions.assertThat;

public class SerializableDocumentCollectionTests {

    @Test(groups = { "unit" })
    public void serialize_Deserialize() throws Exception {
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

        SerializableDocumentCollection serializableDocumentCollection = SerializableDocumentCollection.from(collection);

        // serialize
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(baos);
        objectOutputStream.writeObject(serializableDocumentCollection);
        objectOutputStream.flush();
        objectOutputStream.close();

        // deserialize
        byte[] bytes = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(bais);
        SerializableDocumentCollection deserializedDocumentCollection = (SerializableDocumentCollection) ois.readObject();

        // compare
        assertThat(deserializedDocumentCollection.getWrappedItem().getAltLink())
            .isEqualTo(collection.getAltLink())
            .isEqualTo(altLink);
        assertThat(deserializedDocumentCollection.getWrappedItem().toJson()).isEqualTo(collection.toJson());
    }

    @Test(groups = { "unit" })
    public void deserializeWithInvalidClassType_shouldFail() throws Exception {
        // Create a malicious payload with a different class type instead of ObjectNode
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(baos);
        
        // Write a malicious object instead of ObjectNode
        objectOutputStream.writeObject("MaliciousString");
        objectOutputStream.flush();
        objectOutputStream.close();

        // Attempt to deserialize - should fail with InvalidClassException
        byte[] bytes = baos.toByteArray();
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
            ObjectInputStream ois = new ObjectInputStream(bais);
            SerializableDocumentCollection deserializedDocumentCollection = (SerializableDocumentCollection) ois.readObject();
            
            // Should not reach here
            assertThat(false).as("Expected InvalidClassException to be thrown").isTrue();
        } catch (java.io.InvalidClassException e) {
            // Expected - the malicious class type was rejected
            assertThat(e.getMessage()).contains("Expected ObjectNode");
        }
    }
}

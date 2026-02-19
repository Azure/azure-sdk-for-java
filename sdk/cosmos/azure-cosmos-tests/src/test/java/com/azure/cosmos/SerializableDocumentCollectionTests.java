// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.implementation.caches.SafeObjectInputStream;
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
        // Serialize an unauthorized class (ArrayList) - this will trigger resolveClass()
        // unlike String which uses a special TC_STRING type code and bypasses resolveClass()
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream = new ObjectOutputStream(baos);
        objectOutputStream.writeObject(new ArrayList<>());
        objectOutputStream.flush();
        objectOutputStream.close();

        // Try to deserialize with SafeObjectInputStream that only allows SerializableDocumentCollection
        byte[] bytes = baos.toByteArray();
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
        try (SafeObjectInputStream sois = new SafeObjectInputStream(bais,
                SerializableDocumentCollection.class.getName())) {
            sois.readObject();
            org.testng.Assert.fail("Expected InvalidClassException to be thrown");
        } catch (java.io.InvalidClassException e) {
            // Expected - the unauthorized class type was rejected by SafeObjectInputStream
            assertThat(e.getMessage()).contains("Unauthorized deserialization attempt");
        }
    }
}

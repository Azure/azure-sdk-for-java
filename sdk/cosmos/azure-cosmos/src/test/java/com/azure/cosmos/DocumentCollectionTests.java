// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.internal.DocumentCollection;
import com.google.common.collect.ImmutableList;
import org.assertj.core.api.Assertions;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DocumentCollectionTests {

    @Test(groups = { "unit" })
    public void getPartitionKey()  {
        DocumentCollection collection = new DocumentCollection();
        PartitionKeyDefinition partitionKeyDefinition = new PartitionKeyDefinition();
        partitionKeyDefinition.setPaths(ImmutableList.of("/mypk"));
        collection.setPartitionKey(partitionKeyDefinition);
        assertThat(collection.getPartitionKey()).isEqualTo(partitionKeyDefinition);
    }

    @Test(groups = { "unit" })
    public void getPartitionKey_serializeAndDeserialize()  {
        DocumentCollection collection = new DocumentCollection();
        PartitionKeyDefinition partitionKeyDefinition = new PartitionKeyDefinition();
        partitionKeyDefinition.setPaths(ImmutableList.of("/mypk"));
        partitionKeyDefinition.setVersion(PartitionKeyDefinitionVersion.V2);
        collection.setPartitionKey(partitionKeyDefinition);

        DocumentCollection parsedColl = new DocumentCollection(collection.toJson());
        assertThat(parsedColl.getPartitionKey().getKind().toString()).isEqualTo(partitionKeyDefinition.getKind().toString());
        assertThat(parsedColl.getPartitionKey().getPaths()).isEqualTo(partitionKeyDefinition.getPaths());
        assertThat(parsedColl.getPartitionKey().getVersion()).isEqualTo(partitionKeyDefinition.getVersion());
    }

    @Test(groups = { "unit"})
    public void indexingPolicy_serializeAndDeserialize() {
        SpatialSpec spatialSpec = new SpatialSpec();
        List<SpatialSpec> spatialSpecList = new ArrayList<>();
        spatialSpecList.add(spatialSpec);
        IndexingPolicy indexingPolicy = new IndexingPolicy();
        indexingPolicy.setSpatialIndexes(spatialSpecList);
        DocumentCollection documentCollection = new DocumentCollection();
        documentCollection.setIndexingPolicy(indexingPolicy);
        String json = documentCollection.toJson();

        DocumentCollection documentCollectionPostSerialization = new DocumentCollection(json);
        IndexingPolicy indexingPolicyPostSerialization = documentCollectionPostSerialization.getIndexingPolicy();
        assertThat(indexingPolicyPostSerialization).isNotNull();
        List<SpatialSpec> spatialSpecListPostSerialization = indexingPolicyPostSerialization.getSpatialIndexes();
        Assertions.assertThat(spatialSpecListPostSerialization).isNotNull();
    }
}

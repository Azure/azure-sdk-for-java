// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos;

import com.azure.data.cosmos.internal.DocumentCollection;
import com.google.common.collect.ImmutableList;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class DocumentCollectionTests {

    @Test(groups = { "unit" })
    public void getPartitionKey()  {
        DocumentCollection collection = new DocumentCollection();
        PartitionKeyDefinition partitionKeyDefinition = new PartitionKeyDefinition();
        partitionKeyDefinition.paths(ImmutableList.of("/mypk"));
        collection.setPartitionKey(partitionKeyDefinition);
        assertThat(collection.getPartitionKey()).isEqualTo(partitionKeyDefinition);
    }

    @Test(groups = { "unit" })
    public void getPartitionKey_serializeAndDeserialize()  {
        DocumentCollection collection = new DocumentCollection();
        PartitionKeyDefinition partitionKeyDefinition = new PartitionKeyDefinition();
        partitionKeyDefinition.paths(ImmutableList.of("/mypk"));
        partitionKeyDefinition.version(PartitionKeyDefinitionVersion.V2);
        collection.setPartitionKey(partitionKeyDefinition);

        DocumentCollection parsedColl = new DocumentCollection(collection.toJson());
        assertThat(parsedColl.getPartitionKey().kind().toString()).isEqualTo(partitionKeyDefinition.kind().toString());
        assertThat(parsedColl.getPartitionKey().paths()).isEqualTo(partitionKeyDefinition.paths());
        assertThat(parsedColl.getPartitionKey().version()).isEqualTo(partitionKeyDefinition.version());
    }

    @Test(groups = { "unit"})
    public void indexingPolicy_serializeAndDeserialize() {
        SpatialSpec spatialSpec = new SpatialSpec();
        List<SpatialSpec> spatialSpecList = new ArrayList<>();
        spatialSpecList.add(spatialSpec);
        IndexingPolicy indexingPolicy = new IndexingPolicy();
        indexingPolicy.spatialIndexes(spatialSpecList);
        DocumentCollection documentCollection = new DocumentCollection();
        documentCollection.setIndexingPolicy(indexingPolicy);
        String json = documentCollection.toJson();

        DocumentCollection documentCollectionPostSerialization = new DocumentCollection(json);
        IndexingPolicy indexingPolicyPostSerialization = documentCollectionPostSerialization.getIndexingPolicy();
        assertThat(indexingPolicyPostSerialization).isNotNull();
        List<SpatialSpec> spatialSpecListPostSerialization = indexingPolicyPostSerialization.spatialIndexes();
        assertThat(spatialSpecListPostSerialization).isNotNull();
    }
}

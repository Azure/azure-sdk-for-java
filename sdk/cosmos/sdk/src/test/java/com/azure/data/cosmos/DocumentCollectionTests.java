/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 * 
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

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

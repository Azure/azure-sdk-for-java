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

package com.microsoft.azure.cosmosdb;

import com.google.common.collect.ImmutableList;
import org.testng.annotations.Test;

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
}

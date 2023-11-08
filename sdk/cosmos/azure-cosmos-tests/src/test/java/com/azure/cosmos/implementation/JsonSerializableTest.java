// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation;

import com.azure.cosmos.implementation.directconnectivity.Address;
import com.azure.cosmos.implementation.query.PartitionedQueryExecutionInfoInternal;
import com.azure.cosmos.implementation.query.QueryInfo;
import com.azure.cosmos.implementation.query.QueryItem;
import com.azure.cosmos.implementation.routing.Range;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonSerializableTest {
    @Test(groups = {"unit"})
    public void instantiateWithObjectNodeAndType() throws IOException {
        String json = "{\"id\": \"id1\"}";
        List<Class<?>> klassList = Arrays.asList(Document.class, InternalObjectNode.class, PartitionKeyRange.class,
                                                 Range.class, QueryInfo.class,
                                                 PartitionedQueryExecutionInfoInternal.class, QueryItem.class,
                                                 Address.class, DatabaseAccount.class, DatabaseAccountLocation.class,
                                                 ReplicationPolicy.class, ConsistencyPolicy.class,
                                                 DocumentCollection.class, Database.class);
        ObjectNode objectNode = (ObjectNode) Utils.getSimpleObjectMapper().readTree(json);

        for (Class<?> klass: klassList) {
            JsonSerializable jsonSerializable = JsonSerializable.instantiateFromObjectNodeAndType(objectNode, klass);
            assertThat(jsonSerializable).isNotNull();
            assertThat(jsonSerializable).isInstanceOf(klass);
        }
    }
}

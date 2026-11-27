// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.models.CosmosPatchOperations;
import com.azure.cosmos.models.PartitionKey;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class CosmosItemIdValidationTest {
    @DataProvider
    public Object[][] invalidItemIds() {
        return new Object[][] {
            { "", "Item id must not be null or empty." },
            { "item/name", "Item id must not contain a slash or backslash." },
            { "item\\name", "Item id must not contain a slash or backslash." }
        };
    }

    @Test(groups = "unit", dataProvider = "invalidItemIds")
    public void readItemRejectsInvalidId(String itemId, String expectedMessage) {
        CosmosAsyncDatabase database = mock(CosmosAsyncDatabase.class);
        CosmosAsyncContainer container = new CosmosAsyncContainer("container", database);
        PartitionKey partitionKey = new PartitionKey("partition");

        assertInvalidId(container.readItem(itemId, partitionKey, Object.class), expectedMessage);
        verify(database, never()).getDocClientWrapper();
    }

    @Test(groups = "unit", dataProvider = "invalidItemIds")
    public void deleteItemRejectsInvalidId(String itemId, String expectedMessage) {
        CosmosAsyncDatabase database = mock(CosmosAsyncDatabase.class);
        CosmosAsyncContainer container = new CosmosAsyncContainer("container", database);
        PartitionKey partitionKey = new PartitionKey("partition");

        assertInvalidId(container.deleteItem(itemId, partitionKey), expectedMessage);
        verify(database, never()).getDocClientWrapper();
    }

    @Test(groups = "unit", dataProvider = "invalidItemIds")
    public void replaceItemRejectsInvalidId(String itemId, String expectedMessage) {
        CosmosAsyncDatabase database = mock(CosmosAsyncDatabase.class);
        CosmosAsyncContainer container = new CosmosAsyncContainer("container", database);
        PartitionKey partitionKey = new PartitionKey("partition");

        assertInvalidId(
            container.replaceItem(JsonNodeFactory.instance.objectNode().put("id", itemId), itemId, partitionKey),
            expectedMessage);
        verify(database, never()).getDocClientWrapper();
    }

    @Test(groups = "unit", dataProvider = "invalidItemIds")
    public void patchItemRejectsInvalidId(String itemId, String expectedMessage) {
        CosmosAsyncDatabase database = mock(CosmosAsyncDatabase.class);
        CosmosAsyncContainer container = new CosmosAsyncContainer("container", database);
        PartitionKey partitionKey = new PartitionKey("partition");

        assertInvalidId(
            container.patchItem(itemId, partitionKey, CosmosPatchOperations.create().set("/name", "value"), Object.class),
            expectedMessage);
        verify(database, never()).getDocClientWrapper();
    }

    private static void assertInvalidId(Mono<?> operation, String expectedMessage) {
        StepVerifier.create(operation)
            .expectErrorMatches(error -> error instanceof IllegalArgumentException
                && expectedMessage.equals(error.getMessage()))
            .verify();
    }
}

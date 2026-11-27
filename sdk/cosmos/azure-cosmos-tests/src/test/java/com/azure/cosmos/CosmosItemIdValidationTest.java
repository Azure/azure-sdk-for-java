// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.models.PartitionKey;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.test.StepVerifier;

import static org.mockito.Mockito.mock;

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
        CosmosAsyncContainer container = new CosmosAsyncContainer("container", mock(CosmosAsyncDatabase.class));

        StepVerifier.create(container.readItem(itemId, new PartitionKey("partition"), Object.class))
            .expectErrorMatches(error -> error instanceof IllegalArgumentException
                && expectedMessage.equals(error.getMessage()))
            .verify();
    }
}

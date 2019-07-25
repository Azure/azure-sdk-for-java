// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity;

import com.azure.data.cosmos.PartitionKey;
import com.azure.data.cosmos.PartitionKeyDefinition;
import com.azure.data.cosmos.internal.Undefined;
import com.azure.data.cosmos.internal.RMResources;
import com.azure.data.cosmos.internal.routing.PartitionKeyInternalHelper;
import com.google.common.collect.ImmutableList;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.fail;

public class PartitionKeyTest {

    @DataProvider(name = "paramProvider")
    public Object[][] paramProvider() {
        return new Object[][] {
                { Undefined.Value(), "[{}]" },
                { null, "[null]"},
                { false, "[false]"},
                { true, "[true]"},
                { 123.456, "[123.456]"},
                { 5, "[5.0]"},
                { "PartitionKeyValue",  "[\"PartitionKeyValue\"]"},
        };
    }

    /**
     * Simple test for @{@link PartitionKey}.
     */
    @Test(groups = "unit", dataProvider = "paramProvider")
    public void partitionKey(Object partitionKey, String partitionKeyAsJson) {
        assertThat(new PartitionKey(partitionKey).toString()).isEqualTo(partitionKeyAsJson);
    }

    /**
     * Test equals override for @{@link PartitionKey}
     */
    @Test(groups = "unit", dataProvider = "paramProvider")
    public void partitionKeyCompare(Object partitionKey, String partitionKeyAsJson) {
        assertThat(new PartitionKey(partitionKey)).isEqualTo(PartitionKey.fromJsonString(partitionKeyAsJson));
    }

    /**
     * too few partition key values.
     */
    @Test(groups = "unit")
    public void tooFewPartitionKeyComponents() {
        PartitionKeyDefinition pkd = new PartitionKeyDefinition();
        pkd.paths(ImmutableList.of("/pk1", "/pk2"));
        PartitionKey pk = PartitionKey.fromJsonString("[\"PartitionKeyValue\"]");

        try {
            PartitionKeyInternalHelper.getEffectivePartitionKeyString(pk.getInternalPartitionKey(), pkd);
            fail("should throw");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo(RMResources.TooFewPartitionKeyComponents);
        }
    }

    /**
     * too many partition key values.
     */
    @Test(groups = "unit")
    public void tooManyPartitionKeyComponents() {
        PartitionKeyDefinition pkd = new PartitionKeyDefinition();
        pkd.paths(ImmutableList.of("/pk1"));
        PartitionKey pk = PartitionKey.fromJsonString("[true, false]");

        try {
            PartitionKeyInternalHelper.getEffectivePartitionKeyString(pk.getInternalPartitionKey(), pkd);
            fail("should throw");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo(RMResources.TooManyPartitionKeyComponents);
        }
    }
}
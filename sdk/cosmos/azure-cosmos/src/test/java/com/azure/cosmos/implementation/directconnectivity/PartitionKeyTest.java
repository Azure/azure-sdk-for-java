// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.BridgeInternal;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.models.PartitionKeyDefinition;
import com.azure.cosmos.implementation.RMResources;
import com.azure.cosmos.implementation.Undefined;
import com.azure.cosmos.implementation.routing.PartitionKeyInternalHelper;
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.fail;

public class PartitionKeyTest {

    @DataProvider(name = "paramProvider")
    public Object[][] paramProvider() {
        return new Object[][] {
                { Undefined.value(), "[{}]" },
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
        assertThat(new PartitionKey(partitionKey)).isEqualTo(ModelBridgeInternal.partitionKeyfromJsonString(partitionKeyAsJson));
    }

    /**
     * too few partition key values.
     */
    @Test(groups = "unit")
    public void tooFewPartitionKeyComponents() {
        PartitionKeyDefinition pkd = new PartitionKeyDefinition();
        pkd.setPaths(ImmutableList.of("/pk1", "/pk2"));
        PartitionKey pk = ModelBridgeInternal.partitionKeyfromJsonString("[\"PartitionKeyValue\"]");

        try {
            PartitionKeyInternalHelper.getEffectivePartitionKeyString(ModelBridgeInternal.getPartitionKeyInternal(pk), pkd);
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
        pkd.setPaths(ImmutableList.of("/pk1"));
        PartitionKey pk = ModelBridgeInternal.partitionKeyfromJsonString(("[true, false]"));

        try {
            PartitionKeyInternalHelper.getEffectivePartitionKeyString(ModelBridgeInternal.getPartitionKeyInternal(pk), pkd);
            fail("should throw");
        } catch (IllegalArgumentException e) {
            assertThat(e.getMessage()).isEqualTo(RMResources.TooManyPartitionKeyComponents);
        }
    }
}

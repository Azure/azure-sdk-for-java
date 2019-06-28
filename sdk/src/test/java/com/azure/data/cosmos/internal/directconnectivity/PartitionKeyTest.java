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

package com.azure.data.cosmos.internal.directconnectivity;

import com.azure.data.cosmos.PartitionKey;
import com.azure.data.cosmos.PartitionKeyDefinition;
import com.azure.data.cosmos.Undefined;
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
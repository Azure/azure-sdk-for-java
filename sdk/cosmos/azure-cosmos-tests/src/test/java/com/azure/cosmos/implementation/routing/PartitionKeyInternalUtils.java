// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.routing;

import com.azure.cosmos.implementation.guava25.collect.ImmutableList;

public class PartitionKeyInternalUtils {

    public static PartitionKeyInternal createPartitionKeyInternal(String str) {
        return new PartitionKeyInternal(ImmutableList.of(
                new StringPartitionKeyComponent(str)));

    }
}

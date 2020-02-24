// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.routing;

import com.google.common.collect.ImmutableList;

public class PartitionKeyInternalUtils {

    public static PartitionKeyInternal createPartitionKeyInternal(String str) {
        return new PartitionKeyInternal(ImmutableList.of(
                new StringPartitionKeyComponent(str)));

    }
}

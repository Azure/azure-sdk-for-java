// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.internal.PartitionKeyRange;

import java.util.List;

public class GatewayTestUtils {

    public static PartitionKeyRange setParent(PartitionKeyRange pkr, List<String> parents) {
        pkr.setParents(parents);
        return pkr;
    }
}

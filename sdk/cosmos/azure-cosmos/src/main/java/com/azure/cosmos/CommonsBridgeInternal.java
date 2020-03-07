// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

public class CommonsBridgeInternal {
    public static boolean isV2(PartitionKeyDefinition pkd) {
        return pkd.getVersion() != null && PartitionKeyDefinitionVersion.V2.val == pkd.getVersion().val;
    }

    public static void setV2(PartitionKeyDefinition pkd) {
        pkd.setVersion(PartitionKeyDefinitionVersion.V2);
    }
}

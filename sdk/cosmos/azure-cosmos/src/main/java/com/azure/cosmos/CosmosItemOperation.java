// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.util.Beta;

@Beta(value = Beta.SinceVersion.V4_7_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public interface CosmosItemOperation {
    String getId();

    PartitionKey getPartitionKeyValue();

    CosmosItemOperationType getOperationType();

    <T> T getItem();
}

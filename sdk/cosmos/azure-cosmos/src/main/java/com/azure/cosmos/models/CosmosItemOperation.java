// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.util.Beta;

/**
 * Encapsulates Cosmos Item Operation
 */
@Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public interface CosmosItemOperation {
    String getId();

    PartitionKey getPartitionKeyValue();

    CosmosItemOperationType getOperationType();

    <T> T getItem();

    <T> T getContext();
}

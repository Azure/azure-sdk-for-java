// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.models;

import com.azure.cosmos.util.Beta;

/**
 * Encapsulates Cosmos Item Operation
 */
@Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
public interface CosmosItemOperation {

    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    String getId();

    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    PartitionKey getPartitionKeyValue();

    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    CosmosItemOperationType getOperationType();

    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    <T> T getItem();

    @Beta(value = Beta.SinceVersion.V4_19_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
    <T> T getContext();
}

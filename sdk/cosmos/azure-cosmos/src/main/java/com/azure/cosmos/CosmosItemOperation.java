// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.models.PartitionKey;
import com.azure.cosmos.util.Beta;

/**
 * @deprecated forRemoval = true, since = "4.19"
 * This class is not necessary anymore and will be removed. Please use {@link com.azure.cosmos.models.CosmosItemOperation}
 */
@Beta(value = Beta.SinceVersion.V4_7_0, warningText = Beta.PREVIEW_SUBJECT_TO_CHANGE_WARNING)
@Deprecated() //forRemoval = true, since = "4.19"
public interface CosmosItemOperation {

    @Deprecated() //forRemoval = true, since = "4.19"
    String getId();

    @Deprecated() //forRemoval = true, since = "4.19"
    PartitionKey getPartitionKeyValue();

    @Deprecated() //forRemoval = true, since = "4.19"
    CosmosItemOperationType getOperationType();

    @Deprecated() //forRemoval = true, since = "4.19"
    <T> T getItem();

    @Deprecated() //forRemoval = true, since = "4.19"
    <T> T getContext();
}

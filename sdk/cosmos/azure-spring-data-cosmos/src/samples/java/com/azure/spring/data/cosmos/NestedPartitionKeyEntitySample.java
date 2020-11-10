// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos;

import com.azure.spring.data.cosmos.core.mapping.Container;

@Container(containerName = "nested-partition-key", partitionKeyPath = "/nestedEntitySample/nestedPartitionKey")
public class NestedPartitionKeyEntitySample {

    private NestedEntitySample nestedEntitySample;
}

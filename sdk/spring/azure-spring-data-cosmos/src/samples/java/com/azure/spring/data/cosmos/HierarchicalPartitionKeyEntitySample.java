// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos;

import com.azure.spring.data.cosmos.core.mapping.Container;

// BEGIN: readme-sample-HierarchicalPartitionKeyEntitySample
@Container(containerName = "hierarchical-partition-key", hierarchicalPartitionKeyPaths = {"/id", "/firstName", "/lastName"})
public class HierarchicalPartitionKeyEntitySample {

    private HierarchicalEntitySample hierarchicalEntitySample;
}
// END: readme-sample-HierarchicalPartitionKeyEntitySample

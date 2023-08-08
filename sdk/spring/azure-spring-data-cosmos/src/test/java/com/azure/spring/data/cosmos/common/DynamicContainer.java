// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.data.cosmos.common;

public class DynamicContainer {
    private final String containerName;

    public DynamicContainer(String containerName) {
        this.containerName = containerName;
    }

    public String getContainerName() {
        return this.containerName;
    }
}

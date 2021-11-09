// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.resourcemanager;

import com.azure.resourcemanager.AzureResourceManager;

import static org.mockito.Mockito.mock;


class TestAzureResourceManager {
    static AzureResourceManager getAzureResourceManager() {
        return mock(AzureResourceManager.class);
    }
}

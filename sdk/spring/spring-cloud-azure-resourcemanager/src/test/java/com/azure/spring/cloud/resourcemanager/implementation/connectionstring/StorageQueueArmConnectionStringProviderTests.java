// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.resourcemanager.implementation.connectionstring;

import com.azure.spring.cloud.core.service.AzureServiceType;

public class StorageQueueArmConnectionStringProviderTests extends AbstractArmConnectionStringProviderTests<AzureServiceType.StorageQueue> {

    @Override
    ArmConnectionStringProvider<AzureServiceType.StorageQueue> getArmConnectionStringProvider() {
        return new StorageQueueArmConnectionStringProvider(resourceManager, resourceMetadata, "test");
    }
}

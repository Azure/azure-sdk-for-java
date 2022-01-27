// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.resourcemanager.connectionstring;

import com.azure.spring.core.service.AzureServiceType;

public class StorageQueueArmConnectionStringProviderTests extends AbstractArmConnectionStringProviderTests<AzureServiceType.StorageQueue> {

    @Override
    ArmConnectionStringProvider<AzureServiceType.StorageQueue> getArmConnectionStringProvider() {
        return new StorageQueueArmConnectionStringProvider(resourceManager, resourceMetadata, "test");
    }
}

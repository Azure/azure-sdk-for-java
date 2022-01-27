// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.resourcemanager.connectionstring;

import com.azure.spring.core.service.AzureServiceType;

class EventHubsArmConnectionStringProviderTests extends AbstractArmConnectionStringProviderTests<AzureServiceType.EventHubs> {


    @Override
    ArmConnectionStringProvider<AzureServiceType.EventHubs> getArmConnectionStringProvider() {
        return new EventHubsArmConnectionStringProvider(resourceManager, resourceMetadata, "test");
    }
}

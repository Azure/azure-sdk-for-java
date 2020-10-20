// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.context.core.impl;

import com.microsoft.azure.management.Azure;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.azure.spring.cloud.context.core.config.AzureProperties;

public class ResourceGroupManager extends AzureManager<ResourceGroup, String> {

    public ResourceGroupManager(Azure azure, AzureProperties azureProperties) {
        super(azure, azureProperties);
    }

    @Override
    String getResourceName(String key) {
        return key;
    }

    @Override
    String getResourceType() {
        return ResourceGroup.class.getSimpleName();
    }

    @Override
    public ResourceGroup internalGet(String key) {
        return azure.resourceGroups().getByName(key);
    }

    @Override
    public ResourceGroup internalCreate(String key) {
        return azure.resourceGroups().define(key).withRegion(azureProperties.getRegion()).create();
    }
}

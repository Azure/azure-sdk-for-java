// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appplatform.implementation;

import com.azure.resourcemanager.appplatform.fluent.models.ServiceRegistryResourceInner;
import com.azure.resourcemanager.appplatform.models.SpringService;
import com.azure.resourcemanager.appplatform.models.SpringServiceRegistry;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.ExternalChildResourcesNonCachedImpl;

public class SpringServiceRegistriesImpl extends
    ExternalChildResourcesNonCachedImpl<SpringServiceRegistryImpl, SpringServiceRegistry, ServiceRegistryResourceInner, SpringServiceImpl, SpringService> {
    public SpringServiceRegistriesImpl(SpringServiceImpl parentImpl) {
        super(parentImpl, parentImpl.taskGroup(), "SpringServiceRegistry");
    }

    public void prepareCreate() {
        prepareInlineDefine(new SpringServiceRegistryImpl(Constants.DEFAULT_TANZU_COMPONENT_NAME, getParent(),
            new ServiceRegistryResourceInner()));
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.containerinstance.implementation;

import com.azure.resourcemanager.containerinstance.fluent.models.ContainerAttachResponseInner;
import com.azure.resourcemanager.containerinstance.models.ContainerAttachResult;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;

class ContainerAttachResultImpl implements ContainerAttachResult, HasInnerModel<ContainerAttachResponseInner> {

    private final ContainerAttachResponseInner innerModel;

    ContainerAttachResultImpl(ContainerAttachResponseInner innerModel) {
        this.innerModel = innerModel;
    }

    @Override
    public String webSocketUri() {
        return innerModel().webSocketUri();
    }

    @Override
    public String password() {
        return innerModel().password();
    }

    @Override
    public ContainerAttachResponseInner innerModel() {
        return this.innerModel;
    }
}

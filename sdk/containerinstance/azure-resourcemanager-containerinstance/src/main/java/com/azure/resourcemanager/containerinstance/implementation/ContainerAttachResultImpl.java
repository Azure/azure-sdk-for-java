// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.containerinstance.implementation;

import com.azure.resourcemanager.containerinstance.fluent.models.ContainerAttachResultInner;
import com.azure.resourcemanager.containerinstance.models.ContainerAttachResult;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;

class ContainerAttachResultImpl implements ContainerAttachResult, HasInnerModel<ContainerAttachResultInner> {

    private final ContainerAttachResultInner innerModel;

    ContainerAttachResultImpl(ContainerAttachResultInner innerModel) {
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
    public ContainerAttachResultInner innerModel() {
        return this.innerModel;
    }
}

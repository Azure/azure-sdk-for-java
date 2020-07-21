// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.containerinstance.implementation;

import com.azure.resourcemanager.containerinstance.fluent.inner.ContainerExecResponseInner;
import com.azure.resourcemanager.containerinstance.models.ContainerExecResponse;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.WrapperImpl;

/** Implementation for RegistryCredentials. */
public class ContainerExecResponseImpl extends WrapperImpl<ContainerExecResponseInner>
    implements ContainerExecResponse {
    protected ContainerExecResponseImpl(ContainerExecResponseInner innerObject) {
        super(innerObject);
    }

    @Override
    public String webSocketUri() {
        return this.inner().webSocketUri();
    }

    @Override
    public String password() {
        return this.inner().password();
    }
}

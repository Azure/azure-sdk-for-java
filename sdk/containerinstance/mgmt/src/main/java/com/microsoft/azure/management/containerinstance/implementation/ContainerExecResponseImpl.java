/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.containerinstance.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.containerinstance.ContainerExecResponse;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;

/**
 * Implementation for RegistryCredentials.
 */
@LangDefinition
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

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.resourcemanager.containerinstance.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.implementation.annotation.Beta;
import com.azure.resourcemanager.containerinstance.fluent.inner.ContainerExecResponseInner;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;

/**
 * Response containing the container exec command.
 */
@Fluent
@Beta
public interface ContainerExecResponse extends HasInner<ContainerExecResponseInner> {
    /**
     * Get the webSocketUri value.
     *
     * @return the webSocketUri value
     */
    String webSocketUri();

    /**
     * Get the password value.
     *
     * @return the password value
     */
    String password();
}

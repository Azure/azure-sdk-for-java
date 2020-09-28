// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.containerinstance.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.containerinstance.fluent.models.ContainerExecResponseInner;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;

/** Response containing the container exec command. */
@Fluent
public interface ContainerExecResponse extends HasInnerModel<ContainerExecResponseInner> {
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

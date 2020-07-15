/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.containerinstance;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.containerinstance.implementation.ContainerExecResponseInner;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;

/**
 * Response containing the container exec command.
 */
@Fluent
@Beta(Beta.SinceVersion.V1_11_0)
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

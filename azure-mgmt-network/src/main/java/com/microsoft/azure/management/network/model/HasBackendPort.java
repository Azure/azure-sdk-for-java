/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.model;

/**
 * An interface representing a model's ability to reference a load balancer backend port.
 */
public interface HasBackendPort  {
    /**
     * @return the backend port number the network traffic is sent to
     */
    int backendPort();
}

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.model;

import com.microsoft.azure.management.network.TransportProtocol;

/**
 * An interface representing a model's ability to reference a transport protocol.
 */
public interface HasTransportProtocol  {
    /**
     * @return the transport protocol
     */
    TransportProtocol protocol();
}

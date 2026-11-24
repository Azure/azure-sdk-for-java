// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.containerinstance.models;

/**
 * The information for the output stream from container attach.
 */
public interface ContainerAttachResult {

    /**
     * Gets the WebSocket URI for the output stream from the attach.
     *
     * @return the WebSocket URI.
     */
    String webSocketUri();

    /**
     * Gets the password to the output stream from the attach. Send as an Authorization header
     * value when connecting to the WebSocket URI.
     *
     * @return the password.
     */
    String password();
}

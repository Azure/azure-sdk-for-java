// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.TcpServerMock;

/**
 * Use this method to indicate the response type client want to receive.
 */
public enum RequestResponseType {
    CHANNEL_FIN,
    CHANNEL_RST
}

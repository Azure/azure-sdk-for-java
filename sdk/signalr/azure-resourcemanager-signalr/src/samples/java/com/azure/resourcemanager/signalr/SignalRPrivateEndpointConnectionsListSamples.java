// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.signalr;

import com.azure.core.util.Context;

/** Samples for SignalRPrivateEndpointConnections List. */
public final class SignalRPrivateEndpointConnectionsListSamples {
    /**
     * Sample code: SignalRPrivateEndpointConnections_List.
     *
     * @param signalRManager Entry point to SignalRManager. REST API for Azure SignalR Service.
     */
    public static void signalRPrivateEndpointConnectionsList(
        com.azure.resourcemanager.signalr.SignalRManager signalRManager) {
        signalRManager.signalRPrivateEndpointConnections().list("myResourceGroup", "mySignalRService", Context.NONE);
    }
}

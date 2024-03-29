// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.resourcemanager.deviceupdate.generated;

import com.azure.core.util.BinaryData;
import com.azure.resourcemanager.deviceupdate.models.RemotePrivateEndpointConnection;

public final class RemotePrivateEndpointConnectionTests {
    @org.junit.jupiter.api.Test
    public void testDeserialize() throws Exception {
        RemotePrivateEndpointConnection model
            = BinaryData.fromString("{\"id\":\"hmlwpaztzpo\"}").toObject(RemotePrivateEndpointConnection.class);
    }

    @org.junit.jupiter.api.Test
    public void testSerialize() throws Exception {
        RemotePrivateEndpointConnection model = new RemotePrivateEndpointConnection();
        model = BinaryData.fromObject(model).toObject(RemotePrivateEndpointConnection.class);
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.servicebus;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ServiceBusUtilsTest {

    @Test
    public void testGetNamespace() {
        String connectionString = "Endpoint=sb://namespace.servicebus.windows.net/;"
            + "SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=key";
        Assertions.assertEquals("namespace", ServiceBusUtils.getNamespace(connectionString));
    }

    @Test
    public void testGetNamespaceWithInvalidConnectionString() {
        Assertions.assertNull(ServiceBusUtils.getNamespace("fake-connection-str"));
    }
}

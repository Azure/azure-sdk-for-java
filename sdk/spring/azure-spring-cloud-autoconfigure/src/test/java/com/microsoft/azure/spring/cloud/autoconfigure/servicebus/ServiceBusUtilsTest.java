// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.cloud.autoconfigure.servicebus;

import org.junit.Assert;
import org.junit.Test;

public class ServiceBusUtilsTest {

    @Test
    public void testGetNamespace() {
        String connectionString = "Endpoint=sb://namespace.servicebus.windows.net/;"
            + "SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=key";
        Assert.assertEquals("namespace", ServiceBusUtils.getNamespace(connectionString));
    }
}

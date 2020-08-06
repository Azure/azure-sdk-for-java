/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.servicebus;

import org.junit.Assert;
import org.junit.Test;

public class ServiceBusUtilsTest {

    @Test
    public void testGetNamespace(){
        String connectionString = "Endpoint=sb://namespace.servicebus.windows.net/;" +
                "SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=key";
        Assert.assertEquals("namespace", ServiceBusUtils.getNamespace(connectionString));
    }
}

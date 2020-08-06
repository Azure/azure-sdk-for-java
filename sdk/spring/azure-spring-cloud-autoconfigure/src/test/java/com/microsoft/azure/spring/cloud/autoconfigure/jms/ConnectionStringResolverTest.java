/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.cloud.autoconfigure.jms;

import org.junit.Assert;
import org.junit.Test;

public class ConnectionStringResolverTest {
    @Test
    public void testConnectionStringResolver(){
        String connectionString = "Endpoint=sb://host/;SharedAccessKeyName=sasKeyName;SharedAccessKey=sasKey";

        ServiceBusKey serviceBusKey = ConnectionStringResolver.getServiceBusKey(connectionString);
        String host = serviceBusKey.getHost();
        String sasKeyName = serviceBusKey.getSharedAccessKeyName();
        String sasKey = serviceBusKey.getSharedAccessKey();

        Assert.assertEquals("host", host);
        Assert.assertEquals("sasKeyName", sasKeyName);
        Assert.assertEquals("sasKey", sasKey);
    }
}

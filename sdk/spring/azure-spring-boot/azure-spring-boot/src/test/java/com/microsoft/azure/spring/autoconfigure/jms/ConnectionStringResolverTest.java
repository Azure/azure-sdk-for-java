/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.autoconfigure.jms;

import org.junit.Assert;
import org.junit.Test;

public class ConnectionStringResolverTest {
    @Test
    public void testConnectionStringResolver(){
        final String connectionString = "Endpoint=sb://host/;SharedAccessKeyName=sasKeyName;SharedAccessKey=sasKey";

        final ServiceBusKey serviceBusKey = ConnectionStringResolver.getServiceBusKey(connectionString);
        final String host = serviceBusKey.getHost();
        final String sasKeyName = serviceBusKey.getSharedAccessKeyName();
        final String sasKey = serviceBusKey.getSharedAccessKey();

        Assert.assertEquals("host", host);
        Assert.assertEquals("sasKeyName", sasKeyName);
        Assert.assertEquals("sasKey", sasKey);
    }
}

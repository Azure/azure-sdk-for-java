// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.autoconfigure.jms;

import org.junit.Assert;
import org.junit.Test;

public class ConnectionStringResolverTest {
    @Test
    public void testConnectionStringResolver() {
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

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.connectionstring.implementation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static com.azure.spring.core.connectionstring.ConnectionStringSegments.ACCOUNT_KEY;
import static com.azure.spring.core.connectionstring.ConnectionStringSegments.ACCOUNT_NAME;
import static com.azure.spring.core.connectionstring.ConnectionStringSegments.DEFAULT_DEDPOINTS_PROTOCOL;
import static com.azure.spring.core.connectionstring.ConnectionStringSegments.ENDPOINT;
import static com.azure.spring.core.connectionstring.ConnectionStringSegments.ENDPOINT_SUFFIX;
import static com.azure.spring.core.connectionstring.ConnectionStringSegments.ENTITY_PATH;
import static com.azure.spring.core.connectionstring.ConnectionStringSegments.ID;
import static com.azure.spring.core.connectionstring.ConnectionStringSegments.SECRET;
import static com.azure.spring.core.connectionstring.ConnectionStringSegments.SHARED_ACCESS_KEY;
import static com.azure.spring.core.connectionstring.ConnectionStringSegments.SHARED_ACCESS_KEY_NAME;
import static com.azure.spring.core.connectionstring.ConnectionStringSegments.SHARED_ACCESS_SIGNATURE;

class ConnectionStringTest {

    @Test
    void testEmptyTokenValueShouldThrowException() {
        final String str = "Endpoint=https://abc;Id=my-id;Secret=";

        Assertions.assertThrows(IllegalArgumentException.class,
                      () -> new ConnectionString(str, ConnectionStringType.APP_CONFIGURATION)).printStackTrace();
    }

    @Test
    void testAppConfiguration() {
        final String str = "Endpoint=https://abc;Id=my-id;Secret=my-secret=";

        ConnectionString connectionString = new ConnectionString(str, ConnectionStringType.APP_CONFIGURATION);

        Assertions.assertEquals("https://abc", connectionString.getSegment(ENDPOINT));
        Assertions.assertEquals("https", connectionString.getEndpointUri().getScheme());
        Assertions.assertEquals("abc", connectionString.getEndpointUri().getHost());
        Assertions.assertEquals("my-id", connectionString.getSegment(ID));
        Assertions.assertEquals("my-secret=", connectionString.getSegment(SECRET));
    }

    @Test
    void testEventHubsSbProtocol() {
        final String str = "Endpoint=sb://abc.servicebus.windows.net/;SharedAccessKeyName=name;SharedAccessKey=value==";

        ConnectionString connectionString = new ConnectionString(str, ConnectionStringType.EVENT_HUB);

        Assertions.assertEquals("sb://abc.servicebus.windows.net/", connectionString.getSegment(ENDPOINT));
        Assertions.assertEquals("sb", connectionString.getEndpointUri().getScheme());
        Assertions.assertEquals("abc.servicebus.windows.net", connectionString.getEndpointUri().getHost());
        Assertions.assertEquals("name", connectionString.getSegment(SHARED_ACCESS_KEY_NAME));
        Assertions.assertEquals("value==", connectionString.getSegment(SHARED_ACCESS_KEY));

        Assertions.assertNull(connectionString.getSegment(ENTITY_PATH));
        Assertions.assertNull(connectionString.getSegment(SHARED_ACCESS_SIGNATURE));
    }

    @Test
    void testEventHubsSbProtocolAndEntityPath() {
        final String str = "Endpoint=sb://abc.servicebus.windows.net/;SharedAccessKeyName=name;SharedAccessKey=value==;EntityPath=eventhub";

        ConnectionString connectionString = new ConnectionString(str, ConnectionStringType.EVENT_HUB);

        Assertions.assertEquals("sb://abc.servicebus.windows.net/", connectionString.getSegment(ENDPOINT));
        Assertions.assertEquals("sb", connectionString.getEndpointUri().getScheme());
        Assertions.assertEquals("abc.servicebus.windows.net", connectionString.getEndpointUri().getHost());
        Assertions.assertEquals("name", connectionString.getSegment(SHARED_ACCESS_KEY_NAME));
        Assertions.assertEquals("value==", connectionString.getSegment(SHARED_ACCESS_KEY));
        Assertions.assertEquals("eventhub", connectionString.getSegment(ENTITY_PATH));

        Assertions.assertNull(connectionString.getSegment(SHARED_ACCESS_SIGNATURE));
    }

    @Test
    void testEventHubsSbProtocolAndSasShouldThrowException() {
        final String str = "Endpoint=sb://abc.servicebus.windows.net/;SharedAccessSignature=sas";

        Assertions.assertThrows(IllegalArgumentException.class,
            () -> new ConnectionString(str, ConnectionStringType.EVENT_HUB)).printStackTrace();
    }

    @Test
    void testEventHubsSbProtocolAndSasShouldPass() {
        final String str = "Endpoint=sb://abc.servicebus.windows.net/;SharedAccessSignature=sas";

        ConnectionString connectionString = new ConnectionString(str, ConnectionStringType.SERVICE_BUS);

        Assertions.assertEquals("sb://abc.servicebus.windows.net/", connectionString.getSegment(ENDPOINT));
        Assertions.assertEquals("sb", connectionString.getEndpointUri().getScheme());
        Assertions.assertEquals("abc.servicebus.windows.net", connectionString.getEndpointUri().getHost());
        Assertions.assertEquals("sas", connectionString.getSegment(SHARED_ACCESS_SIGNATURE));

        Assertions.assertNull(connectionString.getSegment(SHARED_ACCESS_KEY_NAME));
        Assertions.assertNull(connectionString.getSegment(SHARED_ACCESS_KEY));
        Assertions.assertNull(connectionString.getSegment(ENTITY_PATH));
    }

    @Test
    void testStorage() {
        final String str = "DefaultEndpointsProtocol=https;AccountName=an;AccountKey=ak==;EndpointSuffix=core.windows.net";

        ConnectionString connectionString = new ConnectionString(str, ConnectionStringType.STORAGE);

        Assertions.assertEquals("https", connectionString.getSegment(DEFAULT_DEDPOINTS_PROTOCOL));
        Assertions.assertEquals("core.windows.net", connectionString.getSegment(ENDPOINT_SUFFIX));
        Assertions.assertEquals("an", connectionString.getSegment(ACCOUNT_NAME));
        Assertions.assertEquals("ak==", connectionString.getSegment(ACCOUNT_KEY));
        Assertions.assertNull(connectionString.getEndpointUri());
    }


}

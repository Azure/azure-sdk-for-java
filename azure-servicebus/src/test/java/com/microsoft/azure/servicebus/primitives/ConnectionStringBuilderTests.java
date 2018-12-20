package com.microsoft.azure.servicebus.primitives;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ConnectionStringBuilderTests {

    @Test
    public void ConnectionStringBuilderTest() {
        String connectionString = "Endpoint=sb://test.servicebus.windows.net/;SharedAccessSignatureToken=SharedAccessSignature sr=amqp%3A%2F%2test.servicebus.windows.net%2topic";
        ConnectionStringBuilder builder = new ConnectionStringBuilder(connectionString);

        assertEquals("SharedAccessSignature sr=amqp%3A%2F%2test.servicebus.windows.net%2topic", builder.getSharedAccessSignatureToken());
        assertEquals(connectionString, builder.toString());
    }

}
// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.connstrbuilder;

import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.IllegalConnectionStringFormatException;
import com.microsoft.azure.eventhubs.TransportType;
import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;
import java.util.function.Consumer;

public class ConnStrBuilderTest {
    private static final String CORRECT_CONNECTION_STRING = "Endpoint=sb://endpoint1;EntityPath=eventhub1;SharedAccessKeyName=somevalue;SharedAccessKey=something;OperationTimeout=PT5S;TransportType=AMQP";
    private static final Consumer<ConnectionStringBuilder> VALIDATE_CONN_STR_BUILDER = new Consumer<ConnectionStringBuilder>() {
        @Override
        public void accept(ConnectionStringBuilder connStrBuilder) {
            Assert.assertEquals("eventhub1", connStrBuilder.getEventHubName());
            Assert.assertEquals("endpoint1", connStrBuilder.getEndpoint().getHost());
            Assert.assertEquals("something", connStrBuilder.getSasKey());
            Assert.assertEquals("somevalue", connStrBuilder.getSasKeyName());
            Assert.assertSame(connStrBuilder.getTransportType(), TransportType.AMQP);
            Assert.assertEquals(connStrBuilder.getOperationTimeout(), Duration.ofSeconds(5));
        }
    };

    @Test(expected = IllegalConnectionStringFormatException.class)
    public void parseInvalidConnectionString() {
        new ConnectionStringBuilder("something");
    }

    @Test(expected = IllegalConnectionStringFormatException.class)
    public void throwOnUnrecognizedParts() {
        new ConnectionStringBuilder(CORRECT_CONNECTION_STRING + ";" + "something");
    }

    @Test(expected = IllegalConnectionStringFormatException.class)
    public void throwOnInvalidTransportType() {
        ConnectionStringBuilder connectionStringBuilder = new ConnectionStringBuilder(CORRECT_CONNECTION_STRING);
        String connectionStringWithTransportType = connectionStringBuilder.setTransportType(TransportType.AMQP_WEB_SOCKETS).toString();
        String connectionStringWithInvalidTransportType = connectionStringWithTransportType.replace(TransportType.AMQP_WEB_SOCKETS.toString(), "invalid");
        new ConnectionStringBuilder(connectionStringWithInvalidTransportType);
    }

    @Test
    public void parseValidConnectionString() {
        final ConnectionStringBuilder connStrBuilder = new ConnectionStringBuilder(CORRECT_CONNECTION_STRING);
        VALIDATE_CONN_STR_BUILDER.accept(connStrBuilder);
    }

    @Test
    public void exchangeConnectionStringAcrossConstructors() {
        final ConnectionStringBuilder connStrBuilder = new ConnectionStringBuilder(CORRECT_CONNECTION_STRING);
        final ConnectionStringBuilder secondConnStr = new ConnectionStringBuilder()
                .setEndpoint(connStrBuilder.getEndpoint())
                .setEventHubName(connStrBuilder.getEventHubName())
                .setSasKeyName(connStrBuilder.getSasKeyName())
                .setSasKey(connStrBuilder.getSasKey())
                .setTransportType(connStrBuilder.getTransportType())
                .setOperationTimeout(connStrBuilder.getOperationTimeout());

        VALIDATE_CONN_STR_BUILDER.accept(new ConnectionStringBuilder(secondConnStr.toString()));
    }

    @Test
    public void testPropertySetters() {
        final ConnectionStringBuilder connStrBuilder = new ConnectionStringBuilder(CORRECT_CONNECTION_STRING);
        final ConnectionStringBuilder testConnStrBuilder = new ConnectionStringBuilder(connStrBuilder.toString());
        VALIDATE_CONN_STR_BUILDER.accept(testConnStrBuilder);

        connStrBuilder.setOperationTimeout(Duration.ofSeconds(8));
        connStrBuilder.setTransportType(TransportType.AMQP_WEB_SOCKETS);

        ConnectionStringBuilder testConnStrBuilder1 = new ConnectionStringBuilder(connStrBuilder.toString());
        Assert.assertEquals(8, testConnStrBuilder1.getOperationTimeout().getSeconds());
        Assert.assertSame(testConnStrBuilder1.getTransportType(), TransportType.AMQP_WEB_SOCKETS);
    }
}

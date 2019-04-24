// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.lib;

import com.microsoft.azure.eventhubs.ConnectionStringBuilder;
import com.microsoft.azure.eventhubs.impl.SharedAccessSignatureTokenProvider;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.time.Duration;

public class SasTokenTestBase extends ApiTestBase {

    private static ConnectionStringBuilder originalConnectionString;

    @BeforeClass
    public static void replaceConnectionString() throws Exception {

        originalConnectionString = TestContext.getConnectionString();
        final String connectionStringWithSasToken = new ConnectionStringBuilder()
                .setEndpoint(originalConnectionString.getEndpoint())
                .setEventHubName(originalConnectionString.getEventHubName())
                .setSharedAccessSignature(
                        SharedAccessSignatureTokenProvider.generateSharedAccessSignature(originalConnectionString.getSasKeyName(),
                                originalConnectionString.getSasKey(),
                                String.format("amqp://%s/%s", originalConnectionString.getEndpoint().getHost(), originalConnectionString.getEventHubName()),
                                Duration.ofDays(1))
                )
                .toString();

        TestContext.setConnectionString(connectionStringWithSasToken);
    }

    @AfterClass
    public static void undoReplace() {
        if (originalConnectionString != null) {
            TestContext.setConnectionString(originalConnectionString.toString());
        }
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.implementation.logging.ServiceLogger;
import com.azure.eventhubs.implementation.ReactorProvider;
import com.azure.eventhubs.implementation.SharedAccessSignatureTokenProvider;
import org.junit.Assert;
import org.junit.Test;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public class EventHubClientTest {
    private static final String CONNECTION_STRING = "";
    private final ServiceLogger logger = new ServiceLogger(EventHubClient.class);

    @Test(expected = NullPointerException.class)
    public void nullConstructor() {
        new EventHubClient(null, null, null, null);
    }

    @Test(expected = NullPointerException.class)
    public void invalidConnectionStringEndpoint() {
        new EventHubClient(new ConnectionStringBuilder(), null, null, null);
    }

    @Test
    public void getPartitionInformation() throws InterruptedException, InvalidKeyException, NoSuchAlgorithmException {
        final ConnectionStringBuilder builder = new ConnectionStringBuilder(CONNECTION_STRING);
        final Scheduler scheduler = Schedulers.newElastic("AMQPConnection");
        final ReactorProvider provider = new ReactorProvider();
        final SharedAccessSignatureTokenProvider tokenProvider = new SharedAccessSignatureTokenProvider(builder.sasKeyName(), builder.sasKey());
        EventHubClient client = new EventHubClient(builder, scheduler, provider, tokenProvider);

        StepVerifier.create(client.getHubProperties())
            .assertNext(properties -> {
                Assert.assertNotNull(properties);
                Assert.assertNotEquals(0, properties.partitionIds().length);
            }).verifyComplete();

        Thread.sleep(1000);

        System.out.println("Disposing of the client.");
        client.close();

        Thread.sleep(1000);
    }
}

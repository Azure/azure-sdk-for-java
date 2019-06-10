// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.amqp.TransportType;
import com.azure.core.implementation.logging.ServiceLogger;
import com.azure.eventhubs.implementation.ReactorHandlerProvider;
import com.azure.eventhubs.implementation.ReactorProvider;
import com.azure.eventhubs.implementation.SharedAccessSignatureTokenProvider;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Ignore;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;

public class EventHubClientTest extends TestBase {
    private final ServiceLogger logger = new ServiceLogger(EventHubClient.class);

    @Test(expected = NullPointerException.class)
    public void nullConstructor() {
        new EventHubClient(null, null, null);
    }

    @Test
    public void getPartitionInformation() throws InterruptedException, InvalidKeyException, NoSuchAlgorithmException {
        Assume.assumeTrue(isTestConfigurationSet());
        final CredentialInfo credentialInfo = CredentialInfo.from(getConnectionString());
        final Scheduler scheduler = Schedulers.newElastic("AMQPConnection");
        final Duration timeout = Duration.ofSeconds(60);
        final ReactorProvider provider = new ReactorProvider();
        final ReactorHandlerProvider handlerProvider = new ReactorHandlerProvider(provider);
        final SharedAccessSignatureTokenProvider tokenProvider = new SharedAccessSignatureTokenProvider(credentialInfo.sharedAccessKeyName(), credentialInfo.sharedAccessKey());
        final ConnectionParameters connectionParameters = new ConnectionParameters(credentialInfo, timeout, tokenProvider, TransportType.AMQP, Retry.getDefaultRetry(), new ProxyConfiguration(), scheduler);
        final EventHubClient client = new EventHubClient(connectionParameters, provider, handlerProvider);

        StepVerifier.create(client.getProperties())
            .assertNext(properties -> {
                Assert.assertNotNull(properties);
                Assert.assertNotEquals(0, properties.partitionIds().length);
            }).verifyComplete();

        Thread.sleep(1000);

        System.out.println("Disposing of the client.");
        client.close();

        Thread.sleep(1000);
    }

    @Ignore
    @Test
    public void parallelEventHubClients() {
        final String partitionId = "0";
        final int noOfClients = 4;
        final String consumerGroupName = TestBase.getConsumerGroupName();

        EventHubClient[] ehClients = new EventHubClient[noOfClients];
        for (int i = 0; i < noOfClients; i++) {
            ehClients[i] = getEventHubClientBuilder().build();
        }

        ReceiverOptions receiverOptions = new ReceiverOptions()
            .consumerGroup(consumerGroupName)
            .beginReceivingAt(EventPosition.newEventsOnly());

        EventHubClient senderClient = getEventHubClientBuilder().build();

        for (EventHubClient ehClient : ehClients) {
            EventReceiver receiver = ehClient.createReceiver(partitionId, receiverOptions);
            Flux<EventData> eventReceived = receiver.receive();

            StepVerifier.create(eventReceived)
                .then(() -> TestBase.pushEventsToPartition(senderClient, partitionId, 10))
                .expectNextCount(10)
                .verifyComplete();
        }
    }
}

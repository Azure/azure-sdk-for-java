// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.amqp.TransportType;
import com.azure.core.test.TestMode;
import com.azure.eventhubs.implementation.ReactorHandlerProvider;
import com.azure.eventhubs.implementation.ReactorProvider;
import com.azure.eventhubs.implementation.SharedAccessSignatureTokenProvider;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;

public class EventHubClientTest extends TestBase {
    private ConnectionStringBuilder builder;
    private Scheduler scheduler;
    private ReactorProvider provider;
    private ReactorHandlerProvider handlerProvider;
    private SharedAccessSignatureTokenProvider tokenProvider;
    private EventHubClient client;

    @Before
    public void setup() throws InvalidKeyException, NoSuchAlgorithmException {
        builder = new ConnectionStringBuilder(getConnectionString());
        scheduler = Schedulers.newElastic("AMQPConnection");
        provider = new ReactorProvider();
        handlerProvider = new ReactorHandlerProvider(provider);
        tokenProvider = new SharedAccessSignatureTokenProvider(builder.sasKeyName(), builder.sasKey());
        client = new EventHubClient(builder, tokenProvider, provider, handlerProvider, scheduler);
    }

    @Test(expected = NullPointerException.class)
    public void nullConstructor() {
        new EventHubClient(null, null, null);
    }

    @Test
    public void getEventHubInformation() throws InterruptedException {
        Assume.assumeTrue(getTestMode() == TestMode.RECORD);

        StepVerifier.create(client.getProperties())
            .assertNext(properties -> {
                Assert.assertNotNull(properties);
                Assert.assertEquals("conniey-test", properties.path());
                Assert.assertEquals(2, properties.partitionIds().length);
            }).verifyComplete();

        Thread.sleep(1000);

        System.out.println("Disposing of the client.");
        client.close();

        Thread.sleep(1000);
    }

    @Test
    public void getPartitionIds() {
        Assume.assumeTrue(getTestMode() == TestMode.RECORD);

        StepVerifier.create(client.getPartitionIds())
            .assertNext(ids -> {
                Assert.assertEquals(2, ids.length);
            })
            .verifyComplete();
    }


    @Test
    public void getPartitionInformation() {
        Assume.assumeTrue(getTestMode() == TestMode.RECORD);

        final String partitionId = "0";
        // Act
//        final Flux<PartitionProperties> partitionProperties = client.getPartitionIds()
//            .flatMapMany(ids -> {
//                List<Mono<PartitionProperties>> results = new ArrayList<>();
//                for (String id : ids) {
//                    results.add(client.getPartitionProperties(id));
//                }
//
//                return Flux.merge(results);
//            });

        // Assert
        StepVerifier.create(client.getPartitionProperties(partitionId))
            .assertNext(properties -> {
                Assert.assertEquals("conniey-test", properties.eventHubPath());
                Assert.assertEquals(partitionId, properties.id());
            })
            .verifyComplete();
    }
}

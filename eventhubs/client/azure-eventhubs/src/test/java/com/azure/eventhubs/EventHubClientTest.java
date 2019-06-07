// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.amqp.TransportType;
import com.azure.core.test.TestMode;
import com.azure.eventhubs.implementation.ReactorHandlerProvider;
import com.azure.eventhubs.implementation.ReactorProvider;
import com.azure.eventhubs.implementation.SharedAccessSignatureTokenProvider;
import org.junit.After;
import org.junit.Assert;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventHubClientTest extends TestBase {
    private ConnectionStringBuilder builder;
    private Scheduler scheduler;
    private ReactorProvider provider;
    private ReactorHandlerProvider handlerProvider;
    private SharedAccessSignatureTokenProvider tokenProvider;
    private EventHubClient client;
    private ExpectedData data;

    @Before
    public void setup() throws InvalidKeyException, NoSuchAlgorithmException {
        final String connectionString = getTestMode() == TestMode.RECORD ? getConnectionString() : TestBase.TEST_CONNECTION_STRING;

        builder = new ConnectionStringBuilder(connectionString);
        scheduler = Schedulers.newElastic("AMQPConnection");
        provider = new ReactorProvider();
        handlerProvider = new ReactorHandlerProvider(provider);
        tokenProvider = new SharedAccessSignatureTokenProvider(builder.sasKeyName(), builder.sasKey());
        client = new EventHubClient(builder, tokenProvider, provider, handlerProvider, scheduler);

        data = new ExpectedData(getTestMode(), builder);
    }

    @After
    public void teardown() {
        client.close();
    }

    @Test(expected = NullPointerException.class)
    public void nullConstructor() {
        new EventHubClient(null, null, null);
    }

    @Test
    public void getEventHubInformation() {
        Assume.assumeTrue(getTestMode() == TestMode.RECORD);

        // Act & Assert
        StepVerifier.create(client.getProperties())
            .assertNext(properties -> {
                Assert.assertNotNull(properties);
                Assert.assertEquals(data.properties.path(), properties.path());
                Assert.assertEquals(data.properties.partitionIds().length, properties.partitionIds().length);
            }).verifyComplete();
    }

    @Test
    public void getPartitionIds() {
        Assume.assumeTrue(getTestMode() == TestMode.RECORD);

        // Act & Assert
        StepVerifier.create(client.getPartitionIds())
            .assertNext(ids -> Assert.assertEquals(data.properties.partitionIds().length, ids.length))
            .verifyComplete();
    }

    @Test
    public void getPartitionInformation() {
        Assume.assumeTrue(getTestMode() == TestMode.RECORD);

        // Act & Assert
        for (String partitionId : data.properties.partitionIds()) {
            StepVerifier.create(client.getPartitionProperties(partitionId))
                .assertNext(properties -> {
                    final PartitionProperties expected = data.getPartitionProperties(properties.id());
                    Assert.assertNotNull(expected);
                    Assert.assertEquals(expected.eventHubPath(), properties.eventHubPath());
                    Assert.assertEquals(partitionId, properties.id());
                })
                .verifyComplete();
        }
    }

    @Test
    public void getPartitionInformationMultipleCalls() {
        Assume.assumeTrue(getTestMode() == TestMode.RECORD);

        // Act
        final Flux<PartitionProperties> partitionProperties = client.getPartitionIds()
            .flatMapMany(ids -> {
                final List<Mono<PartitionProperties>> results = new ArrayList<>();
                for (String id : ids) {
                    results.add(client.getPartitionProperties(id));
                }

                return Flux.merge(results);
            });

        // Assert
        StepVerifier.create(partitionProperties)
            .assertNext(properties -> {
                final PartitionProperties expected = data.getPartitionProperties(properties.id());
                Assert.assertNotNull(expected);
                Assert.assertEquals(expected.eventHubPath(), properties.eventHubPath());
            })
            .assertNext(properties -> {
                final PartitionProperties expected = data.getPartitionProperties(properties.id());
                Assert.assertNotNull(expected);
                Assert.assertEquals(expected.eventHubPath(), properties.eventHubPath());
            })
            .verifyComplete();
    }

    @Test
    public void getPartitionIdsAndThenInformation() {
        Assume.assumeTrue(getTestMode() == TestMode.RECORD);

        // Act
        StepVerifier.create(client.getProperties())
            .assertNext(properties -> {
                Assert.assertEquals(data.properties.partitionIds().length, properties.partitionIds().length);
            })
            .verifyComplete();

        final Flux<PartitionProperties> partitionProperties = client.getPartitionIds()
            .flatMapMany(ids -> {
                List<Mono<PartitionProperties>> results = new ArrayList<>();
                for (String id : ids) {
                    results.add(client.getPartitionProperties(id));
                }

                return Flux.merge(results);
            });

        // Assert
        StepVerifier.create(partitionProperties)
            .assertNext(properties -> {
                final PartitionProperties expected = data.getPartitionProperties(properties.id());
                Assert.assertNotNull(expected);
                Assert.assertEquals(expected.eventHubPath(), properties.eventHubPath());
            })
            .assertNext(properties -> {
                final PartitionProperties expected = data.getPartitionProperties(properties.id());
                Assert.assertNotNull(expected);
                Assert.assertEquals(expected.eventHubPath(), properties.eventHubPath());
            })
            .verifyComplete();
    }

    private static class ExpectedData {
        private final EventHubProperties properties;
        private final Map<String, PartitionProperties> partitionPropertiesMap;

        ExpectedData(TestMode testMode, ConnectionStringBuilder builder) {
            final String eventHubPath;
            final String[] partitionIds;
            switch (testMode) {
                case PLAYBACK:
                    eventHubPath = "test-event-hub";
                    partitionIds = new String[]{"test-1", "test-2"};
                    break;
                case RECORD:
                    eventHubPath = builder.eventHubName();
                    partitionIds = new String[]{"0", "1"};
                    break;
                default:
                    throw new IllegalArgumentException("Test mode not recognized.");
            }

            final Instant now = Instant.now();
            this.properties = new EventHubProperties(eventHubPath, Instant.EPOCH, partitionIds);
            this.partitionPropertiesMap = new HashMap<>();

            for (int i = 0; i < partitionIds.length; i++) {
                final String key = String.valueOf(i);

                this.partitionPropertiesMap.put(key, new PartitionProperties(
                    eventHubPath, key, -1, -1,
                    "lastEnqueued", Instant.now(), true, now));
            }
        }

        public EventHubProperties getProperties() {
            return properties;
        }

        public PartitionProperties getPartitionProperties(String id) {
            return partitionPropertiesMap.get(id);
        }
    }
}

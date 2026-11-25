// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.implementation.Utils;
import org.apache.kafka.connect.source.SourceConnectorContext;
import org.mockito.Mockito;
import org.objenesis.ObjenesisStd;
import org.testng.annotations.Test;
import reactor.core.Disposable;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;

public class MetadataMonitorThreadLifecycleTest {
    @Test(groups = "unit", timeOut = 30_000)
    public void closeCancelsMonitoringSubscription() throws Exception {
        CountDownLatch subscribed = new CountDownLatch(1);
        CountDownLatch cancelled = new CountDownLatch(1);
        MetadataMonitorThread monitorThread = createMonitorThread(
            "closeCancelsMonitoringSubscription",
            createMetadataReader(subscribed, cancelled));

        monitorThread.run();
        assertThat(subscribed.await(10, TimeUnit.SECONDS)).isTrue();

        monitorThread.close();

        assertThat(cancelled.await(10, TimeUnit.SECONDS)).isTrue();
    }

    @Test(groups = "unit", timeOut = 30_000)
    public void closeBeforeRegistrationCancelsMonitoringSubscription() throws Exception {
        CountDownLatch subscribed = new CountDownLatch(1);
        CountDownLatch cancelled = new CountDownLatch(1);
        CountDownLatch registrationStarted = new CountDownLatch(1);
        CountDownLatch allowRegistration = new CountDownLatch(1);
        MetadataMonitorThread monitorThread = new DelayedRegistrationMetadataMonitorThread(
            createMetadataReader(subscribed, cancelled),
            registrationStarted,
            allowRegistration);
        Thread runThread = new Thread(monitorThread::run);

        runThread.start();
        assertThat(registrationStarted.await(10, TimeUnit.SECONDS)).isTrue();
        assertThat(subscribed.await(10, TimeUnit.SECONDS)).isTrue();

        monitorThread.close();
        allowRegistration.countDown();
        runThread.join(TimeUnit.SECONDS.toMillis(10));

        assertThat(runThread.isAlive()).isFalse();
        assertThat(cancelled.await(10, TimeUnit.SECONDS)).isTrue();
    }

    private static MetadataMonitorThread createMonitorThread(
        String connectorName,
        IMetadataReader metadataReader) {

        return new MetadataMonitorThread(
            connectorName,
            createSourceContainersConfig(),
            createMetadataConfig(),
            Mockito.mock(SourceConnectorContext.class),
            metadataReader,
            new ObjenesisStd().newInstance(CosmosAsyncClient.class));
    }

    private static IMetadataReader createMetadataReader(
        CountDownLatch subscribed,
        CountDownLatch cancelled) {

        IMetadataReader metadataReader = Mockito.mock(IMetadataReader.class);
        Mockito.when(metadataReader.getContainersMetadataOffset(anyString(), anyString()))
            .thenReturn(Mono.<Utils.ValueHolder<ContainersMetadataTopicOffset>>never()
                .doOnSubscribe(ignored -> subscribed.countDown())
                .doOnCancel(cancelled::countDown));
        return metadataReader;
    }

    private static CosmosSourceContainersConfig createSourceContainersConfig() {
        return new CosmosSourceContainersConfig(
            "database",
            false,
            Arrays.asList("container"),
            new HashMap<>());
    }

    private static CosmosMetadataConfig createMetadataConfig() {
        return new CosmosMetadataConfig(
            1,
            CosmosMetadataStorageType.KAFKA,
            "metadata");
    }

    private static final class DelayedRegistrationMetadataMonitorThread extends MetadataMonitorThread {
        private final CountDownLatch registrationStarted;
        private final CountDownLatch allowRegistration;

        private DelayedRegistrationMetadataMonitorThread(
            IMetadataReader metadataReader,
            CountDownLatch registrationStarted,
            CountDownLatch allowRegistration) {

            super(
                "closeBeforeRegistrationCancelsMonitoringSubscription",
                createSourceContainersConfig(),
                createMetadataConfig(),
                Mockito.mock(SourceConnectorContext.class),
                metadataReader,
                new ObjenesisStd().newInstance(CosmosAsyncClient.class));
            this.registrationStarted = registrationStarted;
            this.allowRegistration = allowRegistration;
        }

        @Override
        void updateMonitoringSubscription(Disposable subscription) {
            this.registrationStarted.countDown();
            try {
                if (!this.allowRegistration.await(10, TimeUnit.SECONDS)) {
                    throw new AssertionError("Timed out waiting to register the monitoring subscription");
                }
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
                throw new AssertionError("Interrupted while waiting to register the monitoring subscription", exception);
            }
            super.updateMonitoringSubscription(subscription);
        }
    }
}

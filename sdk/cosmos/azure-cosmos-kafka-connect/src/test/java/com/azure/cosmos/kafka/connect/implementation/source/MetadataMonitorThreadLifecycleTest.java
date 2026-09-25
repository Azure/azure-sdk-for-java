// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation.source;

import com.azure.cosmos.CosmosAsyncClient;
import org.apache.kafka.connect.source.SourceConnectorContext;
import org.mockito.Mockito;
import org.objenesis.ObjenesisStd;
import org.testng.annotations.Test;
import reactor.core.Disposable;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

public class MetadataMonitorThreadLifecycleTest {
    @Test(groups = "unit", timeOut = 30_000)
    public void closeDisposesMonitoringSubscription() throws Exception {
        MetadataMonitorThread monitorThread =
            new MetadataMonitorThread(
                "closeDisposesMonitoringSubscription",
                new CosmosSourceContainersConfig(
                    "database",
                    false,
                    Arrays.asList("container"),
                    new HashMap<>()),
                new CosmosMetadataConfig(
                    (int) TimeUnit.MINUTES.toMillis(5),
                    CosmosMetadataStorageType.KAFKA,
                    "metadata"),
                Mockito.mock(SourceConnectorContext.class),
                Mockito.mock(IMetadataReader.class),
                new ObjenesisStd().newInstance(CosmosAsyncClient.class));

        monitorThread.run();

        Field subscriptionField = MetadataMonitorThread.class
            .getDeclaredField("monitoringSubscription");
        subscriptionField.setAccessible(true);
        Disposable subscription = (Disposable) subscriptionField.get(monitorThread);
        assertThat(subscription.isDisposed()).isFalse();

        monitorThread.close();

        assertThat(subscription.isDisposed()).isTrue();
    }
}

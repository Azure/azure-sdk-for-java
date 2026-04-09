// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import com.azure.cosmos.models.ChangeFeedProcessorOptions;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;

public class ChangeFeedCheckpointStrategyTests {

    @Test(groups = {"unit"})
    public void optionsDefaultCheckpointStrategyIsNull() {
        ChangeFeedProcessorOptions options = new ChangeFeedProcessorOptions();

        Assert.assertNull(options.getCheckpointStrategy());
    }

    @Test(groups = {"unit"})
    public void setCheckpointStrategyNullThrows() {
        ChangeFeedProcessorOptions options = new ChangeFeedProcessorOptions();

        Assert.assertThrows(NullPointerException.class, () -> options.setCheckpointStrategy(null));
    }

    @Test(groups = {"unit"})
    public void timeIntervalCheckpointStrategyRequiresPositiveDuration() {
        Assert.assertThrows(NullPointerException.class, () -> new TimeIntervalCheckpointStrategy(null));
        Assert.assertThrows(IllegalArgumentException.class, () -> new TimeIntervalCheckpointStrategy(Duration.ZERO));
        Assert.assertThrows(IllegalArgumentException.class, () -> new TimeIntervalCheckpointStrategy(Duration.ofSeconds(-1)));
    }

    @Test(groups = {"unit"})
    public void builderRejectsTimeIntervalCheckpointDelayGreaterThanOrEqualToLeaseExpirationInterval() {
        ChangeFeedProcessorOptions options = new ChangeFeedProcessorOptions()
            .setLeaseExpirationInterval(Duration.ofSeconds(30))
            .setCheckpointStrategy(new TimeIntervalCheckpointStrategy(Duration.ofSeconds(30)));

        ChangeFeedProcessorBuilder builder = new ChangeFeedProcessorBuilder()
            .hostName("host-1")
            .feedContainer(Mockito.mock(CosmosAsyncContainer.class))
            .leaseContainer(Mockito.mock(CosmosAsyncContainer.class))
            .handleChanges(changes -> {
            })
            .options(options);

        Assert.assertThrows(IllegalArgumentException.class, builder::buildChangeFeedProcessor);
    }

    @Test(groups = {"unit"})
    public void builderAcceptsTimeIntervalCheckpointDelayLessThanLeaseExpirationInterval() {
        ChangeFeedProcessorOptions options = new ChangeFeedProcessorOptions()
            .setLeaseExpirationInterval(Duration.ofSeconds(30))
            .setCheckpointStrategy(new TimeIntervalCheckpointStrategy(Duration.ofSeconds(10)));

        ChangeFeedProcessorBuilder builder = new ChangeFeedProcessorBuilder()
            .hostName("host-1")
            .feedContainer(Mockito.mock(CosmosAsyncContainer.class))
            .leaseContainer(Mockito.mock(CosmosAsyncContainer.class))
            .handleChanges(changes -> {
                if (changes == null) {
                    throw new IllegalStateException("changes should not be null");
                }
            })
            .options(options);

        try {
            Method method = ChangeFeedProcessorBuilder.class.getDeclaredMethod("validateChangeFeedProcessorOptions");
            method.setAccessible(true);
            method.invoke(builder);
        } catch (InvocationTargetException e) {
            Assert.fail("Expected no exception but got: " + e.getCause(), e.getCause());
        } catch (Exception e) {
            Assert.fail("Expected no exception but got: " + e.getMessage(), e);
        }
    }
}




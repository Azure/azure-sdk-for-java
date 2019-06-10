// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.eventhubs;

import com.azure.core.amqp.Retry;
import com.azure.core.amqp.exception.AmqpException;
import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;

public class RetryTest {
    @Test
    public void defaultRetryPolicy() {
        Retry retry = Retry.getDefaultRetry();
        Exception exception = new AmqpException(true, "error message");
        Duration sixtySec = Duration.ofSeconds(60);

        retry.incrementRetryCount();
        Duration firstRetryInterval = retry.getNextRetryInterval(exception, sixtySec);
        Assert.assertNotNull(firstRetryInterval);

        retry.incrementRetryCount();
        Duration secondRetryInterval = retry.getNextRetryInterval(exception, sixtySec);
        Assert.assertNotNull(secondRetryInterval);
        Assert.assertTrue(secondRetryInterval.getSeconds() > firstRetryInterval.getSeconds()
            || (secondRetryInterval.getSeconds() == firstRetryInterval.getSeconds()
                && secondRetryInterval.getNano() > firstRetryInterval.getNano()));
    }

    @Test
    public void atomicIntegerProperty() {
        Retry retry = Retry.getDefaultRetry();
        Assert.assertEquals(0, retry.getRetryCount());

        retry.incrementRetryCount();
        Assert.assertEquals(1, retry.getRetryCount());

        retry.incrementRetryCount();
        Assert.assertEquals(2, retry.getRetryCount());

        retry.resetRetryInterval();
        Assert.assertEquals(0, retry.getRetryCount());

        retry.incrementRetryCount();
        Assert.assertEquals(1, retry.getRetryCount());
    }

    @Test
    public void isRetriable() {
        Exception exception = new AmqpException(true, "error message");
        Assert.assertTrue(Retry.isRetriableException(exception));
    }

    @Test
    public void notRetriable() {
        Exception invalidException = new RuntimeException("invalid exception");
        Assert.assertFalse(Retry.isRetriableException(invalidException));
    }

    @Test
    public void noRetryPolicy() {
        Retry noRetry = Retry.getNoRetry();
        Exception exception = new AmqpException(true, "error message");
        Duration sixtySec = Duration.ofSeconds(60);
        Duration nullDuration = noRetry.getNextRetryInterval(exception, sixtySec);
        int ct = noRetry.incrementRetryCount();
        Assert.assertEquals(1, ct);
        Assert.assertNull(nullDuration);
    }

    @Test
    public void excessMaxRetry() {
        Retry retry = Retry.getDefaultRetry();
        Exception exception = new AmqpException(true, "error message");
        Duration sixtySec = Duration.ofSeconds(60);

        for (int i = 0; i < Retry.DEFAULT_MAX_RETRY_COUNT; i++) {
            retry.incrementRetryCount();
        }
        Assert.assertEquals(retry.getRetryCount(), Retry.DEFAULT_MAX_RETRY_COUNT);
        Duration firstRetryInterval = retry.getNextRetryInterval(exception, sixtySec);
        Assert.assertNull(firstRetryInterval);
    }
}

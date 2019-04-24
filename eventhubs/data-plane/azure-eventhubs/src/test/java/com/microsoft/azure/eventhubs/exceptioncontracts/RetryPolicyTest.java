// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.exceptioncontracts;

import com.microsoft.azure.eventhubs.AuthorizationFailedException;
import com.microsoft.azure.eventhubs.RetryPolicy;
import com.microsoft.azure.eventhubs.ServerBusyException;
import com.microsoft.azure.eventhubs.lib.TestBase;
import org.junit.Assert;
import org.junit.Test;

import java.time.Duration;
import java.util.logging.Level;

public class RetryPolicyTest extends TestBase {
    @Test
    public void testRetryPolicy() {
        String clientId = "someClientEntity";
        RetryPolicy retry = RetryPolicy.getDefault();

        retry.incrementRetryCount(clientId);
        Duration firstRetryInterval = retry.getNextRetryInterval(clientId, new ServerBusyException(), Duration.ofSeconds(60));
        TestBase.TEST_LOGGER.log(Level.FINE, "firstRetryInterval: " + firstRetryInterval.toString());
        Assert.assertNotNull(firstRetryInterval);

        retry.incrementRetryCount(clientId);
        Duration secondRetryInterval = retry.getNextRetryInterval(clientId, new ServerBusyException(), Duration.ofSeconds(60));
        TestBase.TEST_LOGGER.log(Level.FINE, "secondRetryInterval: " + secondRetryInterval.toString());

        Assert.assertNotNull(secondRetryInterval);
        Assert.assertTrue(secondRetryInterval.getSeconds() > firstRetryInterval.getSeconds()
            || (secondRetryInterval.getSeconds() == firstRetryInterval.getSeconds() && secondRetryInterval.getNano() > firstRetryInterval.getNano()));

        retry.incrementRetryCount(clientId);
        Duration thirdRetryInterval = retry.getNextRetryInterval(clientId, new ServerBusyException(), Duration.ofSeconds(60));
        TestBase.TEST_LOGGER.log(Level.FINE, "thirdRetryInterval: " + thirdRetryInterval.toString());

        Assert.assertNotNull(thirdRetryInterval);
        Assert.assertTrue(thirdRetryInterval.getSeconds() > secondRetryInterval.getSeconds()
            || (thirdRetryInterval.getSeconds() == secondRetryInterval.getSeconds() && thirdRetryInterval.getNano() > secondRetryInterval.getNano()));

        retry.incrementRetryCount(clientId);
        Duration fourthRetryInterval = retry.getNextRetryInterval(clientId, new ServerBusyException(), Duration.ofSeconds(60));
        TestBase.TEST_LOGGER.log(Level.FINE, "fourthRetryInterval: " + fourthRetryInterval.toString());

        Assert.assertNotNull(fourthRetryInterval);
        Assert.assertTrue(fourthRetryInterval.getSeconds() > thirdRetryInterval.getSeconds()
            || (fourthRetryInterval.getSeconds() == thirdRetryInterval.getSeconds() && fourthRetryInterval.getNano() > thirdRetryInterval.getNano()));

        retry.incrementRetryCount(clientId);
        Duration fifthRetryInterval = retry.getNextRetryInterval(clientId, new ServerBusyException(), Duration.ofSeconds(60));
        TestBase.TEST_LOGGER.log(Level.FINE, "fifthRetryInterval: " + fifthRetryInterval.toString());

        Assert.assertNotNull(fifthRetryInterval);
        Assert.assertTrue(fifthRetryInterval.getSeconds() > fourthRetryInterval.getSeconds()
            || (fifthRetryInterval.getSeconds() == fourthRetryInterval.getSeconds() && fifthRetryInterval.getNano() > fourthRetryInterval.getNano()));

        retry.incrementRetryCount(clientId);
        Duration sixthRetryInterval = retry.getNextRetryInterval(clientId, new ServerBusyException(), Duration.ofSeconds(60));
        TestBase.TEST_LOGGER.log(Level.FINE, "sixthRetryInterval: " + sixthRetryInterval.toString());

        Assert.assertNotNull(sixthRetryInterval);
        Assert.assertTrue(sixthRetryInterval.getSeconds() > fifthRetryInterval.getSeconds()
            || (sixthRetryInterval.getSeconds() == fifthRetryInterval.getSeconds() && sixthRetryInterval.getNano() > fifthRetryInterval.getNano()));

        retry.incrementRetryCount(clientId);
        Duration seventhRetryInterval = retry.getNextRetryInterval(clientId, new ServerBusyException(), Duration.ofSeconds(60));
        TestBase.TEST_LOGGER.log(Level.FINE, "seventhRetryInterval: " + seventhRetryInterval.toString());

        Assert.assertNotNull(seventhRetryInterval);
        Assert.assertTrue(seventhRetryInterval.getSeconds() > sixthRetryInterval.getSeconds()
            || (seventhRetryInterval.getSeconds() == sixthRetryInterval.getSeconds() && seventhRetryInterval.getNano() > sixthRetryInterval.getNano()));

        retry.incrementRetryCount(clientId);
        Duration nextRetryInterval = retry.getNextRetryInterval(clientId, new AuthorizationFailedException("authorizationerror"), Duration.ofSeconds(60));
        Assert.assertNull(nextRetryInterval);

        retry.resetRetryCount(clientId);
        retry.incrementRetryCount(clientId);
        Duration firstRetryIntervalAfterReset = retry.getNextRetryInterval(clientId, new ServerBusyException(), Duration.ofSeconds(60));
        Assert.assertTrue(firstRetryInterval.equals(firstRetryIntervalAfterReset));
    }
}

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.samples;

import com.microsoft.azure.management.servicebus.samples.ServiceBusPublishSubscribeAdvanceFeatures;
import com.microsoft.azure.management.servicebus.samples.ServiceBusPublishSubscribeBasic;
import com.microsoft.azure.management.servicebus.samples.ServiceBusQueueAdvanceFeatures;
import com.microsoft.azure.management.servicebus.samples.ServiceBusQueueBasic;
import com.microsoft.azure.management.servicebus.samples.ServiceBusWithClaimBasedAuthorization;
import org.junit.Assert;
import org.junit.Test;

public class ServiceBusSampleTests extends SamplesTestBase {
    @Test
    public void testServiceBusQueueBasic() {
        Assert.assertTrue(ServiceBusQueueBasic.runSample(azure));
    }

    @Test
    public void testServiceBusPublishSubscribeBasic() {
        Assert.assertTrue(ServiceBusPublishSubscribeBasic.runSample(azure));
    }

    @Test
    public void testServiceBusWithClaimBasedAuthorization() {
        Assert.assertTrue(ServiceBusWithClaimBasedAuthorization.runSample(azure));
    }

    @Test
    public void testServiceBusQueueAdvanceFeatures() {
        Assert.assertTrue(ServiceBusQueueAdvanceFeatures.runSample(azure));
    }

    @Test
    public void testServiceBusPublishSubscribeAdvanceFeatures() {
        Assert.assertTrue(ServiceBusPublishSubscribeAdvanceFeatures.runSample(azure));
    }
}

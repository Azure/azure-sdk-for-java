// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.samples;

import com.azure.core.test.annotation.DoNotRecord;
import com.azure.resourcemanager.servicebus.samples.ServiceBusPublishSubscribeAdvanceFeatures;
import com.azure.resourcemanager.servicebus.samples.ServiceBusPublishSubscribeBasic;
import com.azure.resourcemanager.servicebus.samples.ServiceBusQueueAdvanceFeatures;
import com.azure.resourcemanager.servicebus.samples.ServiceBusQueueBasic;
import com.azure.resourcemanager.servicebus.samples.ServiceBusWithClaimBasedAuthorization;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ServiceBusSampleLiveOnlyTests extends SamplesTestBase {
    @Test
    @DoNotRecord
    public void testServiceBusQueueBasic() {
        if (skipInPlayback()) {
            return;
        }
        Assertions.assertTrue(ServiceBusQueueBasic.runSample(azureResourceManager));
    }

    @Test
    @DoNotRecord
    public void testServiceBusPublishSubscribeBasic() {
        if (skipInPlayback()) {
            return;
        }
        Assertions.assertTrue(ServiceBusPublishSubscribeBasic.runSample(azureResourceManager));
    }

    @Test
    @DoNotRecord
    public void testServiceBusWithClaimBasedAuthorization() {
        if (skipInPlayback()) {
            return;
        }
        Assertions.assertTrue(ServiceBusWithClaimBasedAuthorization.runSample(azureResourceManager));
    }

    @Test
    @DoNotRecord
    public void testServiceBusQueueAdvanceFeatures() {
        if (skipInPlayback()) {
            return;
        }
        Assertions.assertTrue(ServiceBusQueueAdvanceFeatures.runSample(azureResourceManager));
    }

    @Test
    @DoNotRecord
    public void testServiceBusPublishSubscribeAdvanceFeatures() {
        if (skipInPlayback()) {
            return;
        }
        Assertions.assertTrue(ServiceBusPublishSubscribeAdvanceFeatures.runSample(azureResourceManager));
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CredentialsTests {
    @Test
    public void testKeyUpdate() {
        final MetricsAdvisorKeyCredential credential
            = new MetricsAdvisorKeyCredential("sub-id-1", "key-1");

        Assertions.assertEquals("sub-id-1", credential.getKeys().getSubscriptionKey());
        Assertions.assertEquals("key-1", credential.getKeys().getApiKey());

        credential.updateKey(null, null);
        Assertions.assertNull(credential.getKeys().getSubscriptionKey());
        Assertions.assertNull(credential.getKeys().getApiKey());

        credential.updateKey("sub-id-2", "key-2");
        Assertions.assertEquals("sub-id-2", credential.getKeys().getSubscriptionKey());
        Assertions.assertEquals("key-2", credential.getKeys().getApiKey());
    }
}

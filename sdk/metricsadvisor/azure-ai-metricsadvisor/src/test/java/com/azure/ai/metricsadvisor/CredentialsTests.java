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

        Assertions.assertTrue(credential.getSubscriptionKey().equals("sub-id-1"));
        Assertions.assertTrue(credential.getApiKey().equals("key-1"));

        credential.updateSubscriptionKey(null);
        Assertions.assertNull(credential.getSubscriptionKey());

        credential.updateApiKey(null);
        Assertions.assertNull(credential.getApiKey());

        credential.updateSubscriptionKey("sub-id-2");
        Assertions.assertTrue(credential.getSubscriptionKey().equals("sub-id-2"));

        credential.updateApiKey("key-2");
        Assertions.assertTrue(credential.getApiKey().equals("key-2"));
    }
}

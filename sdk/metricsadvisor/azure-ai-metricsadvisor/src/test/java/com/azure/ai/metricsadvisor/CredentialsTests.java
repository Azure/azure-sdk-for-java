// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class CredentialsTests {
    @Test
    public void testApiKeyUpdate() {
        final MetricsAdvisorKeyCredential credential
            = new MetricsAdvisorKeyCredential("sub-id", "key-1");

        Assertions.assertTrue(credential.getApiKey().equals("key-1"));

        credential.updateApiKey(null);
        Assertions.assertNull(credential.getApiKey());

        credential.updateApiKey("key-2");
        Assertions.assertTrue(credential.getApiKey().equals("key-2"));
    }
}

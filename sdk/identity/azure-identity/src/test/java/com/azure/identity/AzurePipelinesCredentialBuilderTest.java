// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.test.utils.TestConfigurationSource;
import com.azure.core.util.Configuration;
import com.azure.identity.util.TestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class AzurePipelinesCredentialBuilderTest {

    @Test
    public void testRequiredParameters() {
        // setup
        String clientId = "clientId";
        String tenantId = "tenantId";
        String serviceConnectionId = "serviceConnectionId";
        String systemAccessToken = "FakeToken";
        Configuration configuration = TestUtils.createTestConfiguration(new TestConfigurationSource()
            .put("SYSTEM_OIDCREQUESTURI", null));

        // test

        assertThrows(IllegalArgumentException.class, () -> {
            AzurePipelinesCredential credential = new AzurePipelinesCredentialBuilder()
                .build();
        });

        // This assert validates the one required parameter from the environment, SYSTEM_OIDCREQUESTURI, exists.
        assertThrows(IllegalArgumentException.class, () -> {
            AzurePipelinesCredential credential = new AzurePipelinesCredentialBuilder()
                .clientId(clientId)
                .tenantId(tenantId)
                .serviceConnectionId(serviceConnectionId)
                .systemAccessToken(systemAccessToken)
                .configuration(configuration)
                .build();
        });

    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.v2;

import com.azure.identity.v2.util.TestUtils;
import io.clientcore.core.utils.configuration.Configuration;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class AzurePipelinesCredentialBuilderTest {

    @Test
    public void testRequiredParameters() {
        // setup
        String clientId = "clientId";
        String tenantId = "tenantId";
        String serviceConnectionId = "serviceConnectionId";
        String systemAccessToken = "FakeToken";
        Configuration configuration = TestUtils.createTestConfiguration(source -> {
            Map<String, String> propertiesMap = new HashMap<>();
            propertiesMap.put("SYSTEM_OIDCREQUESTURI", null);
            return propertiesMap;
        });

        // test
        assertThrows(IllegalArgumentException.class, () -> {
            AzurePipelinesCredential credential = new AzurePipelinesCredentialBuilder().build();
        });

        // This assert validates the one required parameter from the environment, SYSTEM_OIDCREQUESTURI, exists.
        assertThrows(IllegalArgumentException.class, () -> {
            AzurePipelinesCredential credential = new AzurePipelinesCredentialBuilder().clientId(clientId)
                .tenantId(tenantId)
                .serviceConnectionId(serviceConnectionId)
                .systemAccessToken(systemAccessToken)
                .configuration(configuration)
                .build();
        });
    }
}

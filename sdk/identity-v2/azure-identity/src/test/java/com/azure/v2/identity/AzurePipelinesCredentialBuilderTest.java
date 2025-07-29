// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.identity;

import io.clientcore.core.utils.configuration.Configuration;
import io.clientcore.core.utils.configuration.ConfigurationSource;
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
        Configuration configuration = Configuration.from(new ConfigurationSource() {
            @Override
            public String getProperty(String name) {
                return null;
            }

            @Override
            public boolean isMutable() {
                return false;
            }
        });

        // test
        assertThrows(IllegalArgumentException.class, () -> new AzurePipelinesCredentialBuilder().build());

        // This assert validates the one required parameter from the environment, SYSTEM_OIDCREQUESTURI, exists.
        assertThrows(IllegalArgumentException.class,
            () -> new AzurePipelinesCredentialBuilder().clientId(clientId)
                .tenantId(tenantId)
                .serviceConnectionId(serviceConnectionId)
                .systemAccessToken(systemAccessToken)
                .configuration(configuration)
                .build());
    }
}

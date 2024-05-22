// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.test.utils.TestConfigurationSource;
import com.azure.core.util.Configuration;
import com.azure.core.util.ConfigurationBuilder;
import com.azure.core.util.ConfigurationSource;
import com.azure.identity.util.TestUtils;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class AzurePiplinesCredentialBuilderTest {

    @Test
    public void testRequiredParameters() {
        // setup
        String clientId = "clientId";
        String tenantId = "tenantId";
        String serviceConnectionId = "serviceConnectionId";
        String systemAccessToken = "FakeToken";
        Configuration configuration = TestUtils.createTestConfiguration(new TestConfigurationSource()
            .put("SYSTEM_TEAMFOUNDATIONCOLLECTIONURI", "teamfoundationcollectionuri")
            .put("SYSTEM_TEAMPROJECTID", "teamprojectid")
            .put("SYSTEM_PLANID", "planid")
            .put("SYSTEM_JOBID", "jobid"));

        // test

        assertThrows(IllegalArgumentException.class, () -> {
            AzurePipelinesCredential credential = new AzurePipelinesCredentialBuilder()
                .build();
        });

        assertThrows(IllegalArgumentException.class, () -> {
                AzurePipelinesCredential credential = new AzurePipelinesCredentialBuilder()
                    .clientId(clientId)
                    .tenantId(tenantId)
                    .serviceConnectionId(serviceConnectionId)
                    .systemAccessToken(systemAccessToken)
                    .build();
            });

    }
}

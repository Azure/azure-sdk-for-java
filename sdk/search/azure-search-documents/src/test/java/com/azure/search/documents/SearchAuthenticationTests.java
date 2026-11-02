// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.search.documents;

import com.azure.core.test.TestMode;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Configuration;
import com.azure.identity.AzureCliCredential;
import com.azure.identity.AzurePipelinesCredential;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class SearchAuthenticationTests {
    @ParameterizedTest
    @EnumSource(value = TestMode.class, names = { "LIVE", "RECORD" })
    public void pipelineConfigurationUsesFederatedCredential(TestMode mode) {
        Configuration configuration = new Configuration().clone()
            .put("AZURESUBSCRIPTION_SERVICE_CONNECTION_ID", "connection")
            .put("AZURESUBSCRIPTION_CLIENT_ID", "client")
            .put("AZURESUBSCRIPTION_TENANT_ID", "tenant")
            .put("SYSTEM_ACCESSTOKEN", "test-token")
            .put("SYSTEM_OIDCREQUESTURI", "https://example.invalid/oidc");
        assertInstanceOf(AzurePipelinesCredential.class, TestHelpers.createTestTokenCredential(mode, configuration));
        assertInstanceOf(MockTokenCredential.class,
            TestHelpers.createTestTokenCredential(TestMode.PLAYBACK, configuration));
    }

    @Test
    public void playbackDoesNotConstructAzureCredentials() {
        assertInstanceOf(MockTokenCredential.class,
            TestHelpers.createTestTokenCredential(TestMode.PLAYBACK, Configuration.NONE));
    }

    @ParameterizedTest
    @EnumSource(value = TestMode.class, names = { "LIVE", "RECORD" })
    public void liveAndRecordUseAzureCliLogin(TestMode mode) {
        assertInstanceOf(AzureCliCredential.class, TestHelpers.createTestTokenCredential(mode, Configuration.NONE));
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "AZURE_CLIENT_ID",
            "AZURE_CLIENT_SECRET",
            "AZURE_FEDERATED_TOKEN_FILE",
            "AZURE_CLIENT_CERTIFICATE_PATH",
            "AZURE_CLIENT_CERTIFICATE_PASSWORD" })
    public void unrelatedIdentityAndSecretSettingsDoNotReplaceAzureCliLogin(String name) {
        Configuration configuration = new Configuration().clone()
            .put(name, "unused-test-value")
            .put("AZURESUBSCRIPTION_SERVICE_CONNECTION_ID", "");
        for (TestMode mode : new TestMode[] { TestMode.RECORD, TestMode.LIVE }) {
            assertInstanceOf(AzureCliCredential.class, TestHelpers.createTestTokenCredential(mode, configuration));
        }
        assertInstanceOf(MockTokenCredential.class,
            TestHelpers.createTestTokenCredential(TestMode.PLAYBACK, configuration));
    }
}

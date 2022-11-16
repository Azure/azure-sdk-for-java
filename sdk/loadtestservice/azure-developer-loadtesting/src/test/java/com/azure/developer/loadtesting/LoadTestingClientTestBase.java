// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.developer.loadtesting;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import java.time.OffsetDateTime;
import reactor.core.publisher.Mono;

class LoadTestingClientTestBase extends TestBase {
    protected LoadTestingClientBuilder builder;

    private final String defaultEndpoint = "REDACTED.eus.cnt-prod.loadtesting.azure.com";

    protected final String existingTestId = Configuration.getGlobalConfiguration().get("EXISTING_TEST_ID", "11111111-1234-1234-1234-123456789012");
    protected final String newTestId = Configuration.getGlobalConfiguration().get("NEW_TEST_ID", "22222222-1234-1234-1234-123456789012");
    protected final String existingTestRunId = Configuration.getGlobalConfiguration().get("EXISING_TEST_RUN_ID", "33333333-1234-1234-1234-123456789012");
    protected final String newTestRunId = Configuration.getGlobalConfiguration().get("NEW_TEST_RUN_ID", "44444444-1234-1234-1234-123456789012");
    protected final String defaultUploadFileName = Configuration.getGlobalConfiguration().get("UPLOAD_FILE_NAME", "sample-JMX-file.jmx");
    protected final String defaultAppComponentResourceId = Configuration.getGlobalConfiguration().get("APP_COMPONENT_RESOURCE_ID", "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/samplerg/providers/microsoft.insights/components/appcomponentresource");
    protected final String defaultServerMetricId = Configuration.getGlobalConfiguration().get("SERVER_METRIC_ID", "/subscriptions/00000000-0000-0000-0000-000000000000/resourceGroups/samplerg/providers/microsoft.insights/components/appcomponentresource/providers/microsoft.insights/metricdefinitions/requests/duration");

    private TokenCredential getTokenCredential() {
        DefaultAzureCredentialBuilder credentialBuilder = new DefaultAzureCredentialBuilder();
        String authorityHost = Configuration.getGlobalConfiguration().get("AUTHORITY_HOST");
        if (authorityHost != null) {
            credentialBuilder.authorityHost(authorityHost);
        }
        return credentialBuilder.build();
    }

    @Override
    protected void beforeTest() {
        LoadTestingClientBuilder loadTestingClientBuilder =
                new LoadTestingClientBuilder()
                        .endpoint(Configuration.getGlobalConfiguration().get("ENDPOINT", defaultEndpoint))
                        .httpClient(HttpClient.createDefault())
                        .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));
        if (getTestMode() == TestMode.PLAYBACK) {
            loadTestingClientBuilder
                    .httpClient(interceptorManager.getPlaybackClient())
                    .credential(request -> Mono.just(new AccessToken("this_is_a_token", OffsetDateTime.MAX)));
        } else if (getTestMode() == TestMode.RECORD) {
            loadTestingClientBuilder
                    .addPolicy(interceptorManager.getRecordPolicy())
                    .credential(getTokenCredential());
        } else if (getTestMode() == TestMode.LIVE) {
            loadTestingClientBuilder.credential(getTokenCredential());
        }
        builder = loadTestingClientBuilder;
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.developer.loadtesting;

import com.azure.core.credential.AccessToken;
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
    protected LoadTestingClient client;

    private final String defaultEndpoint = "REDACTED.eus.cnt-prod.loadtesting.azure.com";

    protected final String defaultTestId = "11111111-1234-1234-1234-123456789012";
    protected final String defaultFileId = "22222222-1234-1234-1234-123456789012";
    protected final String defaultTestRunId = "33333333-1234-1234-1234-123456789012";

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
                    .credential(new DefaultAzureCredentialBuilder().build());
        } else if (getTestMode() == TestMode.LIVE) {
            loadTestingClientBuilder.credential(new DefaultAzureCredentialBuilder().build());
        }
        client = loadTestingClientBuilder.buildClient();
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentsafety;

// The Java test files under 'generated' package are generated for your reference.
// If you wish to modify these files, please copy them out of the 'generated' package, and modify there.
// See https://aka.ms/azsdk/dpg/java/tests for guide on adding a test.

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.KeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

class ContentSafetyClientTestBase extends TestProxyTestBase {
    protected ContentSafetyClient contentSafetyClient;
    protected ContentSafetyClient contentSafetyClientAAD;
    protected ContentSafetyAsyncClient contentSafetyAsyncClient;
    protected BlocklistClient blocklistClient;
    protected BlocklistAsyncClient blocklistAsyncClient;

    @Override
    protected void beforeTest() {
        String endpoint = Configuration.getGlobalConfiguration()
            .get("CONTENT_SAFETY_ENDPOINT", "https://fake_cs_resource.cognitiveservices.azure.com");
        String key
            = Configuration.getGlobalConfiguration().get("CONTENT_SAFETY_KEY", "00000000000000000000000000000000");
        ContentSafetyClientBuilder contentSafetyClientBuilder
            = new ContentSafetyClientBuilder().credential(new KeyCredential(key))
                .endpoint(endpoint)
                .httpClient(getHttpClientOrUsePlayback(getHttpClients().findFirst().orElse(null)))
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));
        if (getTestMode() == TestMode.PLAYBACK) {
            contentSafetyClientBuilder.httpClient(interceptorManager.getPlaybackClient());
        } else if (getTestMode() == TestMode.RECORD) {
            contentSafetyClientBuilder.addPolicy(interceptorManager.getRecordPolicy());
        }
        contentSafetyClient = contentSafetyClientBuilder.buildClient();

        ContentSafetyClientBuilder contentSafetyClientAADBuilder
            = new ContentSafetyClientBuilder().credential(new DefaultAzureCredentialBuilder().build())
                .endpoint(endpoint)
                .httpClient(getHttpClientOrUsePlayback(getHttpClients().findFirst().orElse(null)))
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));
        if (getTestMode() == TestMode.PLAYBACK) {
            contentSafetyClientAADBuilder.httpClient(interceptorManager.getPlaybackClient())
                .credential(request -> Mono.just(new AccessToken("this_is_a_token", OffsetDateTime.MAX)));
        } else if (getTestMode() == TestMode.RECORD) {
            contentSafetyClientAADBuilder.addPolicy(interceptorManager.getRecordPolicy());
        }
        contentSafetyClientAAD = contentSafetyClientAADBuilder.buildClient();

        ContentSafetyClientBuilder contentSafetyAsyncClientBuilder
            = new ContentSafetyClientBuilder().credential(new KeyCredential(key))
                .endpoint(endpoint)
                .httpClient(getHttpClientOrUsePlayback(getHttpClients().findFirst().orElse(null)))
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));
        if (getTestMode() == TestMode.PLAYBACK) {
            contentSafetyAsyncClientBuilder.httpClient(interceptorManager.getPlaybackClient());
        } else if (getTestMode() == TestMode.RECORD) {
            contentSafetyAsyncClientBuilder.addPolicy(interceptorManager.getRecordPolicy());
        }
        contentSafetyAsyncClient = contentSafetyAsyncClientBuilder.buildAsyncClient();

        BlocklistClientBuilder blocklistClientBuilder = new BlocklistClientBuilder().credential(new KeyCredential(key))
            .endpoint(endpoint)
            .httpClient(getHttpClientOrUsePlayback(getHttpClients().findFirst().orElse(null)))
            .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));
        if (getTestMode() == TestMode.PLAYBACK) {
            blocklistClientBuilder.httpClient(interceptorManager.getPlaybackClient());
        } else if (getTestMode() == TestMode.RECORD) {
            blocklistClientBuilder.addPolicy(interceptorManager.getRecordPolicy());
        }
        blocklistClient = blocklistClientBuilder.buildClient();

        BlocklistClientBuilder blocklistAsyncClientBuilder
            = new BlocklistClientBuilder().credential(new KeyCredential(key))
                .endpoint(endpoint)
                .httpClient(getHttpClientOrUsePlayback(getHttpClients().findFirst().orElse(null)))
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BASIC));
        if (getTestMode() == TestMode.PLAYBACK) {
            blocklistAsyncClientBuilder.httpClient(interceptorManager.getPlaybackClient());
        } else if (getTestMode() == TestMode.RECORD) {
            blocklistAsyncClientBuilder.addPolicy(interceptorManager.getRecordPolicy());
        }
        blocklistAsyncClient = blocklistClientBuilder.buildAsyncClient();
    }
}

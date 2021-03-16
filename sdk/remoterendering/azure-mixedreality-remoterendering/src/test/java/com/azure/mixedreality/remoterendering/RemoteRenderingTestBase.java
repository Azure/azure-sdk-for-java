// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.mixedreality.remoterendering;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.core.util.polling.PollerFlux;
import com.azure.core.util.polling.SyncPoller;
import com.azure.mixedreality.authentication.MixedRealityStsAsyncClient;
import com.azure.mixedreality.authentication.MixedRealityStsClientBuilder;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class RemoteRenderingTestBase extends TestBase {
    static final String RESPONSE_CODE_400 = "400";
    static final String RESPONSE_CODE_403 = "403";

    private final String accountId = Configuration.getGlobalConfiguration().get("REMOTERENDERING_ARR_ACCOUNT_ID");
    private final String accountDomain = Configuration.getGlobalConfiguration().get("REMOTERENDERING_ARR_ACCOUNT_DOMAIN");
    private final String accountKey = Configuration.getGlobalConfiguration().get("REMOTERENDERING_ARR_ACCOUNT_KEY");
    private final String storageAccountName = Configuration.getGlobalConfiguration().get("REMOTERENDERING_ARR_STORAGE_ACCOUNT_NAME");
    private final String storageAccountKey = Configuration.getGlobalConfiguration().get("REMOTERENDERING_ARR_STORAGE_ACCOUNT_KEY");
    private final String blobContainerName = Configuration.getGlobalConfiguration().get("REMOTERENDERING_ARR_BLOB_CONTAINER_NAME");
    private final String blobContainerSasToken = Configuration.getGlobalConfiguration().get("REMOTERENDERING_ARR_SAS_TOKEN");
    private final String serviceEndpoint = Configuration.getGlobalConfiguration().get("REMOTERENDERING_ARR_SERVICE_ENDPOINT");

    // NOT REAL ACCOUNT DETAILS
    private final String playbackAccountId = "d879da79-415d-45f0-b641-1cfec1386ddf";
    private final String playbackAccountDomain = "mixedreality.azure.com";
    private final String playbackAccountKey = "Sanitized";
    private final String playbackStorageAccountName = "sdkTest";
    private final String playbackStorageAccountKey = "Sanitized";
    private final String playbackBlobContainerName = "test";
    private final String playbackBlobContainerSasToken = "Sanitized";
    private final String playbackServiceEndpoint = "http://localhost:8080";

    HttpPipeline getHttpPipeline(HttpClient httpClient) {
        final List<HttpPipelinePolicy> policies = new ArrayList<>();

        String scope = getServiceEndpoint().replaceFirst("/$", "") + "/.default";

        if (!interceptorManager.isPlaybackMode()) {
            MixedRealityStsAsyncClient stsClient = new MixedRealityStsClientBuilder()
                .accountId(getAccountId())
                .accountDomain(getAccountDomain())
                .credential(getAccountKey())
                .buildAsyncClient();
            policies.add(new BearerTokenAuthenticationPolicy(r -> stsClient.getToken(), scope));
        }

        if (getTestMode() == TestMode.RECORD) {
            policies.add(interceptorManager.getRecordPolicy());
        }

        return new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
            .build();
    }

    String getAccountDomain() {
        return interceptorManager.isPlaybackMode()
            ? this.playbackAccountDomain
            : this.accountDomain;
    }

    String getAccountId() {

        return interceptorManager.isPlaybackMode()
            ? this.playbackAccountId
            : this.accountId;
    }

    AzureKeyCredential getAccountKey() {
        String accountKeyValue = interceptorManager.isPlaybackMode()
            ? this.playbackAccountKey
            : this.accountKey;

        return new AzureKeyCredential(accountKeyValue);
    }

    String getStorageUrl() {
        String storageAccount = interceptorManager.isPlaybackMode()
            ? this.playbackStorageAccountName
            : this.storageAccountName;

        String blobContainer = interceptorManager.isPlaybackMode()
            ? this.playbackBlobContainerName
            : this.blobContainerName;

        return "https://" + storageAccount + ".blob.core.windows.net/" + blobContainer;
    }

    String getBlobContainerSasToken() {

        return interceptorManager.isPlaybackMode()
            ? this.playbackBlobContainerSasToken
            : this.blobContainerSasToken;
    }

    String getServiceEndpoint() {

        return interceptorManager.isPlaybackMode()
            ? this.playbackServiceEndpoint
            : this.serviceEndpoint;
    }

    String getRandomId(String playback) {
        if (!interceptorManager.isPlaybackMode()) {
            return UUID.randomUUID().toString();
        }
        else {
            return playback;
        }
    }

    <T, U> SyncPoller<T, U> setSyncPollerPollInterval(SyncPoller<T, U> syncPoller) {
        return interceptorManager.isPlaybackMode() ? syncPoller.setPollInterval(Duration.ofMillis(1)) : syncPoller;
    }

    <T, U> PollerFlux<T, U> setPollerFluxPollInterval(PollerFlux<T, U> pollerFlux) {
        return interceptorManager.isPlaybackMode() ? pollerFlux.setPollInterval(Duration.ofMillis(1)) : pollerFlux;
    }
}

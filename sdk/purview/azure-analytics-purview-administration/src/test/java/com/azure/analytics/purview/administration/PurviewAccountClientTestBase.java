// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.purview.administration;

import com.azure.core.credential.AccessToken;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Objects;

public class PurviewAccountClientTestBase extends TestProxyTestBase {
    protected String getEndpoint() {
        String endpoint = interceptorManager.isPlaybackMode()
            ? "https://localhost:8080"
            : Configuration.getGlobalConfiguration().get("ENDPOINT");
        Objects.requireNonNull(endpoint);
        return endpoint;
    }

    AccountsClientBuilder purviewAccountClientBuilderSetUp() {
        AccountsClientBuilder builder = new AccountsClientBuilder();
        if (interceptorManager.isPlaybackMode()) {
            builder
                .httpClient(interceptorManager.getPlaybackClient())
                .credential(new MockTokenCredential());
        } else {
            builder
                .httpClient(HttpClient.createDefault())
                .credential(new DefaultAzureCredentialBuilder().build());
        }

        if (interceptorManager.isRecordMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        builder.endpoint(getEndpoint());
        return Objects.requireNonNull(builder);
    }

    CollectionsClientBuilder purviewCollectionClientBuilderSetUp() {
        CollectionsClientBuilder builder = new CollectionsClientBuilder();

        if (interceptorManager.isPlaybackMode()) {
            builder
                .httpClient(interceptorManager.getPlaybackClient())
                .credential(new MockTokenCredential());
        } else {
            builder
                .httpClient(HttpClient.createDefault())
                .credential(new DefaultAzureCredentialBuilder().build());
        }

        if (interceptorManager.isRecordMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        builder.endpoint(getEndpoint());

        return Objects.requireNonNull(builder);
    }

    MetadataPolicyClientBuilder purviewMetadataClientBuilderSetUp() {
        MetadataPolicyClientBuilder builder = new MetadataPolicyClientBuilder();
        if (interceptorManager.isPlaybackMode()) {
            builder
                .httpClient(interceptorManager.getPlaybackClient())
                .credential(new MockTokenCredential());
        } else {
            builder
                .httpClient(HttpClient.createDefault())
                .credential(new DefaultAzureCredentialBuilder().build());
        }

        if (interceptorManager.isRecordMode()) {
            builder.addPolicy(interceptorManager.getRecordPolicy());
        }

        builder.endpoint(getEndpoint());
        return Objects.requireNonNull(builder);
    }
}

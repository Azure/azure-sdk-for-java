// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.purview.catalog;

import com.azure.core.http.HttpClient;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Configuration;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.util.Objects;

public class PurviewCatalogClientTestBase extends TestProxyTestBase {
    protected String getEndpoint() {
        String endpoint = interceptorManager.isPlaybackMode()
            ? "https://localhost:8080"
            : Configuration.getGlobalConfiguration().get("ATLAS_ENDPOINT");
        Objects.requireNonNull(endpoint);
        return endpoint;
    }

    GlossaryClientBuilder builderSetUp() {
        GlossaryClientBuilder builder = new GlossaryClientBuilder();

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

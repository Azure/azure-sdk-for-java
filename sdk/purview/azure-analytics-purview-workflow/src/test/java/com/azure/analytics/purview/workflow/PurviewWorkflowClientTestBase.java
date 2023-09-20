// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.purview.workflow;

import com.azure.core.http.HttpClient;
import com.azure.core.test.TestProxyTestBase;
import com.azure.core.test.models.TestProxySanitizer;
import com.azure.core.test.models.TestProxySanitizerType;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Configuration;
import com.azure.identity.UsernamePasswordCredentialBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

class PurviewWorkflowClientTestBase extends TestProxyTestBase {
    protected PurviewWorkflowClient purviewWorkflowClient;

    protected String getEndpoint() {
        String endpoint = interceptorManager.isPlaybackMode()
            ? "https://localhost:8080"
            : Configuration.getGlobalConfiguration().get("ENDPOINT", "endpoint");
        Objects.requireNonNull(endpoint);
        return endpoint;
    }

    @Override
    protected void beforeTest() {
        PurviewWorkflowClientBuilder purviewWorkflowClientbuilder = new PurviewWorkflowClientBuilder();
        if (interceptorManager.isPlaybackMode()) {
            purviewWorkflowClientbuilder
                .httpClient(interceptorManager.getPlaybackClient())
                .credential(new MockTokenCredential());
        } else {
            purviewWorkflowClientbuilder
                .httpClient(HttpClient.createDefault())
                .credential(new UsernamePasswordCredentialBuilder()
                    .clientId(Configuration.getGlobalConfiguration().get("CLIENTID", "clientId"))
                    .tenantId(Configuration.getGlobalConfiguration().get("TENANTID", "tenantId"))
                    .username(Configuration.getGlobalConfiguration().get("USERNAME", "username"))
                    .password(Configuration.getGlobalConfiguration().get("PASSWORD", "password"))
                    .authorityHost(Configuration.getGlobalConfiguration().get("HOST", "host"))
                    .build()
                );
        }

        if (interceptorManager.isRecordMode()) {
            purviewWorkflowClientbuilder
                .addPolicy(interceptorManager.getRecordPolicy());
        }

        List<TestProxySanitizer> customSanitizer = new ArrayList<>();
        if (!interceptorManager.isLiveMode()) {
            // sanitize response body keys
            customSanitizer.add(new TestProxySanitizer("$..requestor", null, "REDACTED", TestProxySanitizerType.BODY_KEY));
            customSanitizer.add(new TestProxySanitizer("$..createdBy", null, "REDACTED", TestProxySanitizerType.BODY_KEY));
            customSanitizer.add(new TestProxySanitizer("$..updatedBy", null, "REDACTED", TestProxySanitizerType.BODY_KEY));
            interceptorManager.addSanitizers(customSanitizer);
        }

        purviewWorkflowClient = purviewWorkflowClientbuilder.endpoint(getEndpoint()).buildClient();
    }
}

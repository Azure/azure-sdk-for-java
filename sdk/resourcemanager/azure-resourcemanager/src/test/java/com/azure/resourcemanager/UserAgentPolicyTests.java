// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.management.profile.AzureProfile;
import com.azure.core.util.CoreUtils;
import com.azure.resourcemanager.resources.fluentcore.utils.HttpPipelineProvider;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.test.ResourceManagerTestProxyTestBase;
import com.azure.resourcemanager.test.utils.TestDelayProvider;
import com.azure.resourcemanager.test.utils.TestIdentifierProvider;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class UserAgentPolicyTests extends ResourceManagerTestProxyTestBase {
    private AzureResourceManager azureResourceManager;
    VerificationPolicy verificationPolicy = new VerificationPolicy();

    @Test
    public void assertSdkVersionCorrect() {
        azureResourceManager.virtualMachines().list().stream().count();
        Assertions.assertTrue(verificationPolicy.callCount.get() > 0);
    }

    @Override
    protected HttpPipeline buildHttpPipeline(TokenCredential credential, AzureProfile profile,
        HttpLogOptions httpLogOptions, List<HttpPipelinePolicy> policies, HttpClient httpClient) {
        policies.add(verificationPolicy);
        return HttpPipelineProvider.buildHttpPipeline(credential, profile, null, httpLogOptions, null,
            new RetryPolicy("Retry-After", ChronoUnit.SECONDS), policies, httpClient);
    }

    @Override
    protected void initializeClients(HttpPipeline httpPipeline, AzureProfile profile) {
        ResourceManagerUtils.InternalRuntimeContext.setDelayProvider(new TestDelayProvider(!isPlaybackMode()));
        ResourceManagerUtils.InternalRuntimeContext internalContext = new ResourceManagerUtils.InternalRuntimeContext();
        internalContext.setIdentifierFunction(name -> new TestIdentifierProvider(testResourceNamer));
        this.azureResourceManager = buildManager(AzureResourceManager.class, httpPipeline, profile);
        setInternalContext(internalContext, azureResourceManager);
    }

    @Override
    protected void cleanUpResources() {
    }

    private static class VerificationPolicy implements HttpPipelinePolicy {
        private String sdkVersion;
        private AtomicInteger callCount = new AtomicInteger();

        @Override
        public Mono<HttpResponse> process(HttpPipelineCallContext httpPipelineCallContext,
            HttpPipelineNextPolicy httpPipelineNextPolicy) {
            synchronized (this) {
                if (sdkVersion == null) {
                    sdkVersion = CoreUtils.getProperties("azure-resourcemanager-compute.properties").get("version");
                }
            }
            Assertions.assertNotNull(sdkVersion);
            Assertions.assertTrue(httpPipelineCallContext.getHttpRequest()
                .getHeaders()
                .get("User-Agent")
                .getValue()
                .contains(sdkVersion));
            callCount.incrementAndGet();
            return httpPipelineNextPolicy.process();
        }
    }
}

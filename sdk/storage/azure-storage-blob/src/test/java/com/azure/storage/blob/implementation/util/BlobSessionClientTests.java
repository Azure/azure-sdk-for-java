// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.BlobTestBase;
import com.azure.storage.common.test.shared.StorageCommonTestUtils;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BlobSessionClientTests extends BlobTestBase {

    @Test
    public void createSessionSyncUsesProvidedHttpPipeline() {
        AtomicInteger policyInvocationCount = new AtomicInteger();
        BlobSessionClient sessionClient = new BlobSessionClient(createOAuthPipeline(policyInvocationCount),
            ENVIRONMENT.getPrimaryAccount().getBlobEndpoint(), BlobServiceVersion.getLatest(),
            cc.getBlobContainerName());

        StorageSessionCredential credential = sessionClient.createSessionSync();

        assertNotNull(credential);
        assertNotNull(credential.getSessionToken());
        assertNotNull(credential.getSessionKey());
        assertNotNull(credential.getExpiration());
        assertEquals(1, policyInvocationCount.get());
    }

    @Test
    public void createSessionAsyncUsesProvidedHttpPipeline() {
        AtomicInteger policyInvocationCount = new AtomicInteger();
        BlobSessionClient sessionClient = new BlobSessionClient(createOAuthPipeline(policyInvocationCount),
            ENVIRONMENT.getPrimaryAccount().getBlobEndpoint(), BlobServiceVersion.getLatest(),
            ccAsync.getBlobContainerName());

        StepVerifier.create(sessionClient.createSessionAsync()).assertNext(credential -> {
            assertNotNull(credential);
            assertNotNull(credential.getSessionToken());
            assertNotNull(credential.getSessionKey());
            assertNotNull(credential.getExpiration());
        }).verifyComplete();

        assertEquals(1, policyInvocationCount.get());
    }

    private HttpPipeline createOAuthPipeline(AtomicInteger policyInvocationCount) {
        HttpPipelinePolicy policy = (context, next) -> {
            policyInvocationCount.incrementAndGet();
            return next.process();
        };

        BlobServiceClientBuilder builder
            = new BlobServiceClientBuilder().endpoint(ENVIRONMENT.getPrimaryAccount().getBlobEndpoint())
                .credential(StorageCommonTestUtils.getTokenCredential(interceptorManager))
                .addPolicy(policy);

        instrument(builder);
        return builder.buildClient().getHttpPipeline();
    }
}

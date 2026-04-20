// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.storage.blob.BlobContainerAsyncClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.BlobTestBase;
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.common.test.shared.StorageCommonTestUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class BlobSessionClientTests extends BlobTestBase {

    @Test
    public void createSessionReturnsTokenAndKey() {
        BlobContainerClient oauthCc = getOAuthServiceClient().getBlobContainerClient(cc.getBlobContainerName());
        BlobSessionClient sessionClient
            = new BlobSessionClient(oauthCc.getHttpPipeline(), ENVIRONMENT.getPrimaryAccount().getBlobEndpoint(),
                BlobServiceVersion.getLatest(), cc.getBlobContainerName());

        StorageSessionCredential credential = sessionClient.createSessionSync();

        assertNotNull(credential);
        assertNotNull(credential.getSessionToken());
        assertNotNull(credential.getSessionKey());
        assertNotNull(credential.getExpiration());
    }

    @Test
    public void createSessionAsyncReturnsTokenAndKey() {
        BlobContainerAsyncClient oauthCc
            = getOAuthServiceAsyncClient().getBlobContainerAsyncClient(ccAsync.getBlobContainerName());
        BlobSessionClient sessionClient
            = new BlobSessionClient(oauthCc.getHttpPipeline(), ENVIRONMENT.getPrimaryAccount().getBlobEndpoint(),
                BlobServiceVersion.getLatest(), ccAsync.getBlobContainerName());

        StepVerifier.create(sessionClient.createSessionAsync()).assertNext(credential -> {
            assertNotNull(credential);
            assertNotNull(credential.getSessionToken());
            assertNotNull(credential.getSessionKey());
            assertNotNull(credential.getExpiration());
        }).verifyComplete();
    }

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
            //            assertEquals(AuthenticationType.HMAC, session.getAuthenticationType());
        }).verifyComplete();

        assertEquals(1, policyInvocationCount.get());
    }

    @Disabled("Service does not yet support User Delegation SAS for Create Session — returns InvalidSessionAuthenticationType")
    @Test
    public void createSessionWithUserDelegationSas() {
        BlobContainerClient oauthCc = getOAuthServiceClient().getBlobContainerClient(cc.getBlobContainerName());

        String sas = generateUserDelegationContainerSas(oauthCc);

        BlobContainerClientBuilder builder = new BlobContainerClientBuilder().endpoint(oauthCc.getBlobContainerUrl());

        BlobContainerClient sasCc = instrument(builder.sasToken(sas)).buildClient();

        BlobSessionClient sessionClient
            = new BlobSessionClient(sasCc.getHttpPipeline(), ENVIRONMENT.getPrimaryAccount().getBlobEndpoint(),
                BlobServiceVersion.getLatest(), sasCc.getBlobContainerName());

        StorageSessionCredential credential = sessionClient.createSessionSync();

        assertNotNull(credential);
        assertNotNull(credential.getSessionToken());
        assertNotNull(credential.getSessionKey());
        assertNotNull(credential.getExpiration());
        assertEquals(false, credential.isExpired());
    }

    @Disabled("Service does not yet support User Delegation SAS for Create Session — returns InvalidSessionAuthenticationType")
    @Test
    public void createSessionAsyncWithUserDelegationSas() {
        BlobContainerClient oauthCc = getOAuthServiceClient().getBlobContainerClient(ccAsync.getBlobContainerName());

        String sas = generateUserDelegationContainerSas(oauthCc);

        BlobContainerClient sasCc
            = instrument(new BlobContainerClientBuilder().endpoint(oauthCc.getBlobContainerUrl()).sasToken(sas))
                .buildClient();

        BlobSessionClient sessionClient
            = new BlobSessionClient(sasCc.getHttpPipeline(), ENVIRONMENT.getPrimaryAccount().getBlobEndpoint(),
                BlobServiceVersion.getLatest(), ccAsync.getBlobContainerName());

        StepVerifier.create(sessionClient.createSessionAsync()).assertNext(credential -> {
            assertNotNull(credential);
            assertNotNull(credential.getSessionToken());
            assertNotNull(credential.getSessionKey());
            assertNotNull(credential.getExpiration());
            assertEquals(false, credential.isExpired());
        }).verifyComplete();
    }

    private String generateUserDelegationContainerSas(BlobContainerClient containerClient) {
        BlobContainerSasPermission permissions = new BlobContainerSasPermission().setReadPermission(true)
            .setWritePermission(true)
            .setCreatePermission(true)
            .setListPermission(true);
        BlobServiceSasSignatureValues sasValues
            = new BlobServiceSasSignatureValues(testResourceNamer.now().plusDays(1), permissions);

        return containerClient.generateUserDelegationSas(sasValues, getOAuthServiceClient()
            .getUserDelegationKey(testResourceNamer.now().minusDays(1), testResourceNamer.now().plusDays(1)));
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

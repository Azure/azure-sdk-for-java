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
import com.azure.storage.blob.models.SessionCredential;
import com.azure.storage.blob.models.SessionRequestContext;
import com.azure.storage.blob.sas.BlobContainerSasPermission;
import com.azure.storage.blob.sas.BlobServiceSasSignatureValues;
import com.azure.storage.common.test.shared.StorageCommonTestUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TokenCredentialSessionProviderTests extends BlobTestBase {

    @Test
    public void createSessionReturnsTokenAndKey() {
        AtomicReference<String> requestPath = new AtomicReference<>();
        TokenCredentialSessionProvider sessionProvider = new TokenCredentialSessionProvider(
            createOAuthPipeline(new AtomicInteger(), requestPath), ENVIRONMENT.getPrimaryAccount().getBlobEndpoint(),
            BlobServiceVersion.getLatest(), ENVIRONMENT.getPrimaryAccount().getName());

        SessionCredential credential
            = sessionProvider.getSession(new SessionRequestContext().setContainerName(cc.getBlobContainerName()));

        assertNotNull(credential);
        assertNotNull(credential.getSessionToken());
        assertNotNull(credential.getSessionKey());
        assertNotNull(credential.getExpiresAt());
        assertEquals("/" + cc.getBlobContainerName(), requestPath.get());
    }

    @Test
    public void createSessionAsyncReturnsTokenAndKey() {
        AtomicReference<String> requestPath = new AtomicReference<>();
        TokenCredentialSessionProvider sessionProvider = new TokenCredentialSessionProvider(
            createOAuthPipeline(new AtomicInteger(), requestPath), ENVIRONMENT.getPrimaryAccount().getBlobEndpoint(),
            BlobServiceVersion.getLatest(), ENVIRONMENT.getPrimaryAccount().getName());

        StepVerifier
            .create(sessionProvider
                .getSessionAsync(new SessionRequestContext().setContainerName(ccAsync.getBlobContainerName())))
            .assertNext(credential -> {
                assertNotNull(credential);
                assertNotNull(credential.getSessionToken());
                assertNotNull(credential.getSessionKey());
                assertNotNull(credential.getExpiresAt());
            })
            .verifyComplete();
        assertEquals("/" + ccAsync.getBlobContainerName(), requestPath.get());
    }

    @Test
    public void createSessionSyncUsesProvidedHttpPipeline() {
        AtomicInteger policyInvocationCount = new AtomicInteger();
        TokenCredentialSessionProvider sessionProvider = new TokenCredentialSessionProvider(
            createOAuthPipeline(policyInvocationCount), ENVIRONMENT.getPrimaryAccount().getBlobEndpoint(),
            BlobServiceVersion.getLatest(), ENVIRONMENT.getPrimaryAccount().getName());

        SessionCredential credential
            = sessionProvider.getSession(new SessionRequestContext().setContainerName(cc.getBlobContainerName()));

        assertNotNull(credential);
        assertNotNull(credential.getSessionToken());
        assertNotNull(credential.getSessionKey());
        assertNotNull(credential.getExpiresAt());
        assertEquals(1, policyInvocationCount.get());
    }

    @Test
    public void createSessionAsyncUsesProvidedHttpPipeline() {
        AtomicInteger policyInvocationCount = new AtomicInteger();
        TokenCredentialSessionProvider sessionProvider = new TokenCredentialSessionProvider(
            createOAuthPipeline(policyInvocationCount), ENVIRONMENT.getPrimaryAccount().getBlobEndpoint(),
            BlobServiceVersion.getLatest(), ENVIRONMENT.getPrimaryAccount().getName());

        StepVerifier
            .create(sessionProvider
                .getSessionAsync(new SessionRequestContext().setContainerName(ccAsync.getBlobContainerName())))
            .assertNext(credential -> {
                assertNotNull(credential);
                assertNotNull(credential.getSessionToken());
                assertNotNull(credential.getSessionKey());
                assertNotNull(credential.getExpiresAt());
                //            assertEquals(AuthenticationType.HMAC, session.getAuthenticationType());
            })
            .verifyComplete();

        assertEquals(1, policyInvocationCount.get());
    }

    @Disabled("Service does not yet support User Delegation SAS for Create Session — returns InvalidSessionAuthenticationType")
    @Test
    public void createSessionWithUserDelegationSas() {
        BlobContainerClient oauthCc = getOAuthServiceClient().getBlobContainerClient(cc.getBlobContainerName());

        String sas = generateUserDelegationContainerSas(oauthCc);

        BlobContainerClientBuilder builder = new BlobContainerClientBuilder().endpoint(oauthCc.getBlobContainerUrl());

        BlobContainerClient sasCc = instrument(builder.sasToken(sas)).buildClient();

        TokenCredentialSessionProvider sessionProvider = new TokenCredentialSessionProvider(sasCc.getHttpPipeline(),
            ENVIRONMENT.getPrimaryAccount().getBlobEndpoint(), BlobServiceVersion.getLatest(),
            ENVIRONMENT.getPrimaryAccount().getName());

        SessionCredential credential
            = sessionProvider.getSession(new SessionRequestContext().setContainerName(sasCc.getBlobContainerName()));

        assertNotNull(credential);
        assertNotNull(credential.getSessionToken());
        assertNotNull(credential.getSessionKey());
        assertNotNull(credential.getExpiresAt());
        assertFalse(credential.isExpired());
    }

    @Disabled("Service does not yet support User Delegation SAS for Create Session — returns InvalidSessionAuthenticationType")
    @Test
    public void createSessionAsyncWithUserDelegationSas() {
        BlobContainerClient oauthCc = getOAuthServiceClient().getBlobContainerClient(ccAsync.getBlobContainerName());

        String sas = generateUserDelegationContainerSas(oauthCc);

        BlobContainerClient sasCc
            = instrument(new BlobContainerClientBuilder().endpoint(oauthCc.getBlobContainerUrl()).sasToken(sas))
                .buildClient();

        TokenCredentialSessionProvider sessionProvider = new TokenCredentialSessionProvider(sasCc.getHttpPipeline(),
            ENVIRONMENT.getPrimaryAccount().getBlobEndpoint(), BlobServiceVersion.getLatest(),
            ENVIRONMENT.getPrimaryAccount().getName());

        StepVerifier
            .create(sessionProvider
                .getSessionAsync(new SessionRequestContext().setContainerName(ccAsync.getBlobContainerName())))
            .assertNext(credential -> {
                assertNotNull(credential);
                assertNotNull(credential.getSessionToken());
                assertNotNull(credential.getSessionKey());
                assertNotNull(credential.getExpiresAt());
                assertFalse(credential.isExpired());
            })
            .verifyComplete();
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
        return createOAuthPipeline(policyInvocationCount, null);
    }

    private HttpPipeline createOAuthPipeline(AtomicInteger policyInvocationCount, AtomicReference<String> requestPath) {
        HttpPipelinePolicy policy = (context, next) -> {
            policyInvocationCount.incrementAndGet();
            if (requestPath != null) {
                requestPath.set(context.getHttpRequest().getUrl().getPath());
            }
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

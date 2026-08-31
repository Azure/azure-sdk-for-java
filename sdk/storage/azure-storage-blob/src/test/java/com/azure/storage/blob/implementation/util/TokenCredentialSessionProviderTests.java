// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpPipelinePolicy;
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
import com.azure.storage.common.test.shared.extensions.LiveOnly;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.Clock;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

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

    @LiveOnly
    @Test
    public void actualExpirationCausesSessionReplacement() {
        AtomicInteger policyInvocationCount = new AtomicInteger();
        MutableClock clock = new MutableClock(Instant.now());
        TokenCredentialSessionProvider sessionProvider = new TokenCredentialSessionProvider(
            createOAuthPipeline(policyInvocationCount), ENVIRONMENT.getPrimaryAccount().getBlobEndpoint(),
            BlobServiceVersion.getLatest(), ENVIRONMENT.getPrimaryAccount().getName(), clock);
        SessionRequestContext context = new SessionRequestContext().setContainerName(cc.getBlobContainerName());

        SessionCredential first = sessionProvider.getSession(context);
        assertTrue(first.getExpiresAt().isAfter(OffsetDateTime.now(clock)));

        clock.setInstant(first.getExpiresAt().plusSeconds(1).toInstant());
        SessionCredential replacement = sessionProvider.getSession(context);

        assertNotSame(first, replacement);
        assertEquals(2, policyInvocationCount.get());
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

    private static final class MutableClock extends Clock {
        private final ZoneId zone;
        private Instant instant;

        private MutableClock(Instant instant) {
            this(instant, ZoneOffset.UTC);
        }

        private MutableClock(Instant instant, ZoneId zone) {
            this.instant = instant;
            this.zone = zone;
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(ZoneId newZone) {
            return new MutableClock(instant, newZone);
        }

        @Override
        public Instant instant() {
            return instant;
        }

        private void setInstant(Instant instant) {
            this.instant = instant;
        }
    }
}

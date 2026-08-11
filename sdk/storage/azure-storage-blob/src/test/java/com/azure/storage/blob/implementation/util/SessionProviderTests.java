// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.models.SessionCredential;
import com.azure.storage.blob.models.SessionProvider;
import com.azure.storage.blob.models.SessionRequestContext;
import com.azure.storage.common.test.shared.session.CreateSessionMockHttpClient;
import com.azure.storage.common.test.shared.session.SessionTestHelper;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Small, focused tests for the public {@link SessionProvider} contract implemented by
 * {@link TokenCredentialSessionProvider}.
 * <p>
 * These verify that {@link TokenCredentialSessionProvider#getSessionAsync(SessionRequestContext)} and
 * {@link TokenCredentialSessionProvider#getSession(SessionRequestContext)} route the CreateSession REST call to the
 * container named on the {@link SessionRequestContext}, proving out the "per-request container" seam that
 * backs the BYO {@link SessionProvider} extension point, and that a context missing a container
 * name is rejected rather than silently falling back to some default. This complements (and does not
 * duplicate) {@code BlobSessionClientTests}, which exercises these same paths against the live service, and
 * {@code TokenCredentialSessionProviderCacheTest}, which fakes the transport wholesale to test per-container cache
 * timing behavior. Here {@link TokenCredentialSessionProvider} is real and only the transport is faked, so the
 * container name
 * actually placed on the wire is what's being verified.
 */
public class SessionProviderTests {

    @Test
    public void getSessionAsyncUsesContainerFromContext() {
        CreateSessionMockHttpClient httpClient = new CreateSessionMockHttpClient();
        TokenCredentialSessionProvider sessionProvider = createSessionProvider(httpClient);

        SessionRequestContext context
            = new SessionRequestContext().setContainerName(SessionTestHelper.TEST_CONTAINER_NAME)
                .setAccountName(SessionTestHelper.TEST_ACCOUNT_NAME);

        StepVerifier.create(sessionProvider.getSessionAsync(context)).assertNext(credential -> {
            assertNotNull(credential);
            assertEquals("session-token-for-testcontainer", credential.getSessionToken());
            assertNotNull(credential.getSessionKey());
        }).verifyComplete();

        assertEquals(SessionTestHelper.TEST_CONTAINER_NAME, httpClient.getRequestedContainer());
    }

    @Test
    public void getSessionSyncUsesContainerFromContext() {
        CreateSessionMockHttpClient httpClient = new CreateSessionMockHttpClient();
        TokenCredentialSessionProvider sessionProvider = createSessionProvider(httpClient);

        SessionRequestContext context
            = new SessionRequestContext().setContainerName(SessionTestHelper.TEST_CONTAINER_NAME)
                .setAccountName(SessionTestHelper.TEST_ACCOUNT_NAME);

        SessionCredential credential = sessionProvider.getSession(context);

        assertNotNull(credential);
        assertEquals("session-token-for-testcontainer", credential.getSessionToken());
        assertNotNull(credential.getSessionKey());
        assertEquals(SessionTestHelper.TEST_CONTAINER_NAME, httpClient.getRequestedContainer());
    }

    @Test
    public void missingContextContainerThrowsSync() {
        TokenCredentialSessionProvider sessionProvider = createSessionProvider(new CreateSessionMockHttpClient());

        // There is no constructor-supplied fallback container: a context with no container name must be
        // rejected rather than silently degrading to some default.
        SessionRequestContext context = new SessionRequestContext();

        assertThrows(IllegalArgumentException.class, () -> sessionProvider.getSession(context));
    }

    @Test
    public void missingContextContainerThrowsAsync() {
        TokenCredentialSessionProvider sessionProvider = createSessionProvider(new CreateSessionMockHttpClient());

        SessionRequestContext context = new SessionRequestContext();

        StepVerifier.create(sessionProvider.getSessionAsync(context)).verifyError(IllegalArgumentException.class);
    }

    private static TokenCredentialSessionProvider createSessionProvider(CreateSessionMockHttpClient httpClient) {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(httpClient).build();
        return new TokenCredentialSessionProvider(pipeline,
            "https://" + SessionTestHelper.TEST_ACCOUNT_NAME + ".blob.core.windows.net", BlobServiceVersion.getLatest(),
            SessionTestHelper.TEST_ACCOUNT_NAME);
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.models.SessionCredential;
import com.azure.storage.blob.models.SessionProvider;
import com.azure.storage.blob.models.SessionRequestContext;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

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

    private static final String ACCOUNT_NAME = "myaccount";
    private static final String CONTEXT_CONTAINER = "context-container";

    @Test
    public void getSessionAsyncUsesContainerFromContext() {
        AtomicReference<String> requestedContainer = new AtomicReference<>();
        TokenCredentialSessionProvider sessionProvider = createSessionProvider(requestedContainer);

        SessionRequestContext context
            = new SessionRequestContext().setContainerName(CONTEXT_CONTAINER).setAccountName(ACCOUNT_NAME);

        StepVerifier.create(sessionProvider.getSessionAsync(context)).assertNext(credential -> {
            assertNotNull(credential);
            assertNotNull(credential.getSessionToken());
            assertNotNull(credential.getSessionKey());
        }).verifyComplete();

        assertEquals(CONTEXT_CONTAINER, requestedContainer.get());
    }

    @Test
    public void getSessionSyncUsesContainerFromContext() {
        AtomicReference<String> requestedContainer = new AtomicReference<>();
        TokenCredentialSessionProvider sessionProvider = createSessionProvider(requestedContainer);

        SessionRequestContext context
            = new SessionRequestContext().setContainerName(CONTEXT_CONTAINER).setAccountName(ACCOUNT_NAME);

        SessionCredential credential = sessionProvider.getSession(context);

        assertNotNull(credential);
        assertNotNull(credential.getSessionToken());
        assertNotNull(credential.getSessionKey());
        assertEquals(CONTEXT_CONTAINER, requestedContainer.get());
    }

    @Test
    public void missingContextContainerThrowsSync() {
        AtomicReference<String> requestedContainer = new AtomicReference<>();
        TokenCredentialSessionProvider sessionProvider = createSessionProvider(requestedContainer);

        // There is no constructor-supplied fallback container: a context with no container name must be
        // rejected rather than silently degrading to some default.
        SessionRequestContext context = new SessionRequestContext();

        assertThrows(IllegalArgumentException.class, () -> sessionProvider.getSession(context));
    }

    @Test
    public void missingContextContainerThrowsAsync() {
        AtomicReference<String> requestedContainer = new AtomicReference<>();
        TokenCredentialSessionProvider sessionProvider = createSessionProvider(requestedContainer);

        SessionRequestContext context = new SessionRequestContext();

        StepVerifier.create(sessionProvider.getSessionAsync(context)).verifyError(IllegalArgumentException.class);
    }

    private static TokenCredentialSessionProvider createSessionProvider(AtomicReference<String> requestedContainer) {
        HttpPipeline pipeline
            = new HttpPipelineBuilder().httpClient(new CreateSessionMockClient(requestedContainer)).build();
        return new TokenCredentialSessionProvider(pipeline, "https://" + ACCOUNT_NAME + ".blob.core.windows.net",
            BlobServiceVersion.getLatest(), ACCOUNT_NAME);
    }

    /**
     * A fake transport that parses the container name out of the CreateSession request's path (the
     * container is the first path segment; {@code restype=container&comp=session} is on the query string)
     * and echoes it back into the session token, so tests can assert on the container that was actually
     * requested without needing a real service.
     */
    private static final class CreateSessionMockClient implements HttpClient {

        private final AtomicReference<String> requestedContainer;

        CreateSessionMockClient(AtomicReference<String> requestedContainer) {
            this.requestedContainer = requestedContainer;
        }

        @Override
        public Mono<HttpResponse> send(HttpRequest request) {
            String path = request.getUrl().getPath();
            // Path looks like "/<container>"; strip the leading slash.
            String container = path.startsWith("/") ? path.substring(1) : path;
            requestedContainer.set(container);

            String body = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + "<CreateSessionResult>"
                + "<Id>test-session-id</Id>" + "<Expiration>Wed, 09 Sep 2099 00:00:00 GMT</Expiration>"
                + "<AuthenticationType>HMAC</AuthenticationType>" + "<Credentials>" + "<SessionToken>session-token-for-"
                + container + "</SessionToken>"
                + "<SessionKey>dGVzdFNlc3Npb25LZXkxMjM0NTY3ODkwMTIzNDU2Nzg5MA==</SessionKey>" + "</Credentials>"
                + "</CreateSessionResult>";

            HttpResponse response = new MockHttpResponse(request, 201, body.getBytes(StandardCharsets.UTF_8))
                .addHeader("Content-Type", "application/xml");
            return Mono.just(response);
        }
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.test.http.NoOpHttpClient;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.BlobTestBase;
import com.azure.storage.blob.models.SessionCredential;
import com.azure.storage.blob.models.SessionProvider;
import com.azure.storage.blob.models.SessionRequestContext;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Small, focused tests for the public {@link SessionProvider} contract implemented by
 * {@link ContainerSessionProvider}.
 * <p>
 * These verify default synchronous retrieval and rejection of a context missing a container name.
 * The successful sync and async routing paths are covered by {@code TokenCredentialSessionProviderTests} against the
 * live service, while {@code TokenCredentialSessionProviderCacheTest} fakes the transport to test cache timing.
 */
public class SessionProviderTests {

    @Test
    public void missingContextContainerThrowsSync() {
        ContainerSessionProvider sessionProvider = createSessionProvider();

        // There is no constructor-supplied fallback container: a context with no container name must be
        // rejected rather than silently degrading to some default.
        SessionRequestContext context = new SessionRequestContext();

        assertThrows(IllegalArgumentException.class, () -> sessionProvider.getSession(context));
    }

    @Test
    public void missingContextContainerThrowsAsync() {
        ContainerSessionProvider sessionProvider = createSessionProvider();

        SessionRequestContext context = new SessionRequestContext();

        StepVerifier.create(sessionProvider.getSessionAsync(context)).verifyError(IllegalArgumentException.class);
    }

    @Test
    public void getSessionDelegatesToAsync() {
        SessionRequestContext context = new SessionRequestContext().setContainerName("container");
        SessionCredential credential
            = new SessionCredential("token", "key", OffsetDateTime.now().plusMinutes(5), "account");
        AtomicInteger subscriptions = new AtomicInteger();
        SessionProvider sessionProvider = createSessionProvider(requestContext -> {
            assertSame(context, requestContext);
            return Mono.defer(() -> {
                subscriptions.incrementAndGet();
                return Mono.just(credential);
            });
        });

        assertSame(credential, sessionProvider.getSession(context));
        assertEquals(1, subscriptions.get());
    }

    @Test
    public void getSessionPropagatesAcquisitionError() {
        IllegalStateException failure = new IllegalStateException("Session acquisition failed.");
        SessionProvider sessionProvider = createSessionProvider(context -> Mono.error(failure));

        assertSame(failure,
            assertThrows(IllegalStateException.class, () -> sessionProvider.getSession(new SessionRequestContext())));
    }

    private static SessionProvider
        createSessionProvider(Function<SessionRequestContext, Mono<SessionCredential>> acquisition) {
        return new SessionProvider() {
            @Override
            public boolean isRequestEligible(com.azure.core.http.HttpRequest request) {
                return request != null && request.getHttpMethod() == com.azure.core.http.HttpMethod.GET;
            }

            @Override
            public Mono<SessionCredential> getSessionAsync(SessionRequestContext context) {
                return acquisition.apply(context);
            }

            @Override
            public boolean invalidateSession(SessionRequestContext context, SessionCredential rejectedCredential) {
                return false;
            }

            @Override
            public void refreshSession(SessionRequestContext context) {
            }
        };
    }

    private static ContainerSessionProvider createSessionProvider() {
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(new NoOpHttpClient()).build();
        return new ContainerSessionProvider(pipeline,
            "https://" + BlobTestBase.TEST_SESSION_ACCOUNT_NAME + ".blob.core.windows.net",
            BlobServiceVersion.getLatest(), BlobTestBase.TEST_SESSION_ACCOUNT_NAME);
    }
}

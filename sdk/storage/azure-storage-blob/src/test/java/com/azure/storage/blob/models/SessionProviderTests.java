// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.models;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.http.NoOpHttpClient;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.Context;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.implementation.util.BuilderHelper;
import com.azure.storage.blob.BlobServiceVersion;
import com.azure.storage.blob.BlobTestBase;
import com.azure.storage.blob.implementation.util.SessionCredential;
import com.azure.storage.blob.implementation.util.SessionRequestContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.OffsetDateTime;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Small, focused tests for the internal {@link SessionProvider} operations implemented by
 * {@link ContainerSessionProvider}.
 * <p>
 * These verify default synchronous retrieval and rejection of a context missing a container name.
 * Successful sync and async acquisition paths are covered by {@link ContainerSessionProviderTests},
 * while {@link ContainerSessionProviderCacheTest} fakes the transport to test cache timing.
 */
public class SessionProviderTests {

    @Test
    public void publicProviderHierarchyIsClosedAndDoesNotExposeOperations() throws Exception {
        assertTrue(Modifier.isAbstract(SessionProvider.class.getModifiers()));
        assertTrue(Modifier.isFinal(ContainerSessionProvider.class.getModifiers()));
        assertEquals(0, SessionProvider.class.getConstructors().length);
        assertEquals(0, SessionProvider.class.getDeclaredConstructor().getModifiers());
        assertTrue(Modifier.isFinal(
            com.azure.storage.blob.implementation.accesshelpers.SessionProviderConstructorProxy.class.getModifiers()));
        assertEquals(Modifier.PRIVATE,
            com.azure.storage.blob.implementation.accesshelpers.SessionProviderConstructorProxy.class
                .getDeclaredConstructor()
                .getModifiers());
        for (Class<?> type : new Class<?>[] { SessionProvider.class, ContainerSessionProvider.class }) {
            for (java.lang.reflect.Method method : type.getMethods()) {
                assertEquals(Object.class, method.getDeclaringClass(), method.toString());
            }
        }
        assertThrows(ClassNotFoundException.class,
            () -> Class.forName("com.azure.storage.blob.models.SessionCredential"));
        assertThrows(ClassNotFoundException.class,
            () -> Class.forName("com.azure.storage.blob.models.SessionRequestContext"));
    }

    @Test
    public void accessorInitializesAndCreatesProviderWithSuppliedConfiguration() {
        com.azure.storage.blob.implementation.accesshelpers.SessionProviderConstructorProxy.SessionProviderConstructorAccessor accessor
            = com.azure.storage.blob.implementation.accesshelpers.SessionProviderConstructorProxy.getAccessor();
        assertNotNull(accessor);
        Clock clock = Clock.fixed(Instant.parse("2026-06-19T00:00:00Z"), ZoneOffset.UTC);
        BlobServiceVersion version = BlobServiceVersion.values()[0];
        HttpPipeline pipeline = new HttpPipelineBuilder().httpClient(request -> {
            assertEquals("https://custom.example.com/container?restype=container&comp=session",
                request.getUrl().toString());
            assertEquals(version.getVersion(),
                request.getHeaders().getValue(HttpHeaderName.fromString("x-ms-version")));
            String body = "<CreateSessionResult><Id>id</Id><AuthenticationType>HMAC</AuthenticationType>"
                + "<Credentials><SessionToken>shared</SessionToken><SessionKey>a2V5</SessionKey>"
                + "</Credentials></CreateSessionResult>";
            return Mono.just(new MockHttpResponse(request, 201, body.getBytes(StandardCharsets.UTF_8))
                .addHeader("Content-Type", "application/xml"));
        }).build();
        ContainerSessionProvider provider
            = accessor.create(pipeline, "https://custom.example.com", version, "signing-account", clock);

        SessionCredential session
            = accessor.getSession(provider, new SessionRequestContext().setContainerName("container"));
        assertEquals("shared", session.getSessionToken());
        assertEquals("signing-account", session.getAccountName());
        assertEquals(OffsetDateTime.now(clock).plusMinutes(5), session.getExpiresAt());
        assertNotSame(provider,
            accessor.create(pipeline, "https://custom.example.com", version, "signing-account", clock));
        assertSame(accessor,
            com.azure.storage.blob.implementation.accesshelpers.SessionProviderConstructorProxy.getAccessor());
    }

    @Test
    public void sharedAccessorUsesSuppliedProviderSync() {
        com.azure.storage.blob.implementation.accesshelpers.SessionProviderConstructorProxy.SessionProviderConstructorAccessor accessor
            = com.azure.storage.blob.implementation.accesshelpers.SessionProviderConstructorProxy.getAccessor();
        TestSessionProvider first = mock(TestSessionProvider.class);
        TestSessionProvider second = mock(TestSessionProvider.class);
        HttpRequest request = new HttpRequest(HttpMethod.GET, "https://account.blob.core.windows.net/container/blob");
        SessionRequestContext context = new SessionRequestContext().setContainerName("container");
        SessionCredential credential
            = new SessionCredential("token", "a2V5", OffsetDateTime.now().plusMinutes(5), "account");
        when(first.isRequestEligible(request)).thenReturn(true);
        when(first.getSession(context)).thenReturn(credential);
        when(first.invalidateSession(context, credential)).thenReturn(true);

        assertTrue(accessor.isRequestEligible(first, request));
        assertFalse(accessor.isRequestEligible(second, request));
        assertSame(credential, accessor.getSession(first, context));
        assertNull(accessor.getSession(second, context));
        assertTrue(accessor.invalidateSession(first, context, credential));
        assertFalse(accessor.invalidateSession(second, context, credential));
        accessor.refreshSession(first, context);
        accessor.refreshSession(second, context);

        verify(first).isRequestEligible(request);
        verify(second).isRequestEligible(request);
        verify(first).getSession(context);
        verify(second).getSession(context);
        verify(first).invalidateSession(context, credential);
        verify(second).invalidateSession(context, credential);
        verify(first).refreshSession(context);
        verify(second).refreshSession(context);
        assertSame(accessor,
            com.azure.storage.blob.implementation.accesshelpers.SessionProviderConstructorProxy.getAccessor());
    }

    @Test
    public void sharedAccessorUsesSuppliedProviderAsync() {
        com.azure.storage.blob.implementation.accesshelpers.SessionProviderConstructorProxy.SessionProviderConstructorAccessor accessor
            = com.azure.storage.blob.implementation.accesshelpers.SessionProviderConstructorProxy.getAccessor();
        TestSessionProvider first = mock(TestSessionProvider.class);
        TestSessionProvider second = mock(TestSessionProvider.class);
        SessionRequestContext context = new SessionRequestContext().setContainerName("container");
        SessionCredential credential
            = new SessionCredential("token", "a2V5", OffsetDateTime.now().plusMinutes(5), "account");
        when(first.getSessionAsync(context)).thenReturn(Mono.just(credential));
        when(second.getSessionAsync(context)).thenReturn(Mono.empty());

        StepVerifier.create(accessor.getSessionAsync(first, context)).expectNext(credential).verifyComplete();
        StepVerifier.create(accessor.getSessionAsync(second, context)).verifyComplete();
        verify(first).getSessionAsync(context);
        verify(second).getSessionAsync(context);
        assertSame(accessor,
            com.azure.storage.blob.implementation.accesshelpers.SessionProviderConstructorProxy.getAccessor());
    }

    @Test
    public void publicConstructionValidatesEndpointAndCredential() {
        MockTokenCredential credential = new MockTokenCredential();
        assertNotNull(
            new ContainerSessionProvider("https://account.blob.core.windows.net/c/b?sig=removed", credential));
        assertThrows(NullPointerException.class, () -> new ContainerSessionProvider(null, credential));
        assertThrows(NullPointerException.class,
            () -> new ContainerSessionProvider("https://account.blob.core.windows.net", null));
        assertThrows(IllegalArgumentException.class,
            () -> new ContainerSessionProvider("http://account.blob.core.windows.net", credential));
    }

    @ParameterizedTest
    @CsvSource({
        "https://account.blob.core.windows.net/c/b?sig=secret&snapshot=old, https://account.blob.core.windows.net",
        "https://custom.example.com/c/b?versionid=old, https://custom.example.com",
        "https://127.0.0.1:10000/account/c/b?comp=list, https://127.0.0.1:10000/account",
        "https://localhost:10000/account/c/b?sig=secret, https://localhost:10000/account" })
    public void publicConstructionNormalizesEndpointAndScopesEligibility(String source, String endpoint) {
        assertEquals(endpoint, BuilderHelper.getSessionEndpoint(source));
        ContainerSessionProvider provider = new ContainerSessionProvider(source, new MockTokenCredential());
        assertTrue(provider.isRequestEligible(new HttpRequest(HttpMethod.GET, endpoint + "/container/blob")));
        assertFalse(provider.isRequestEligible(
            new HttpRequest(HttpMethod.GET, "https://another.blob.core.windows.net/container/blob")));
        assertFalse(provider.isRequestEligible(new HttpRequest(HttpMethod.GET, endpoint + "/container")));
        assertFalse(
            provider.isRequestEligible(new HttpRequest(HttpMethod.GET, endpoint + "/container/blob?comp=list")));
        assertFalse(provider.isRequestEligible(new HttpRequest(HttpMethod.PUT, endpoint + "/container/blob")));
        assertFalse(provider.isRequestEligible(new HttpRequest(HttpMethod.POST, endpoint + "/container?comp=session")));
    }

    @Test
    public void createSessionPostUsesBearerEvenWhenSessionsAreEnabled() throws Exception {
        BlobServiceClient client = new BlobServiceClientBuilder().endpoint("https://account.blob.core.windows.net")
            .credential(new MockTokenCredential())
            .httpClient(request -> {
                assertTrue(request.getHeaders().getValue(HttpHeaderName.AUTHORIZATION).startsWith("Bearer "));
                return Mono.just(new MockHttpResponse(request, 201));
            })
            .sessionOptions(new SessionOptions().setSessionMode(SessionOptions.SessionMode.ENABLED))
            .buildClient();
        HttpPipeline pipeline = BuilderHelper.createSessionPipeline(client);
        HttpRequest request = new HttpRequest(HttpMethod.POST,
            "https://account.blob.core.windows.net/container?restype=container&comp=session");

        try (HttpResponse response = pipeline.sendSync(request, Context.NONE)) {
            assertNotNull(response);
        }
    }

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
            BlobServiceVersion.getLatest(), BlobTestBase.TEST_SESSION_ACCOUNT_NAME, Clock.systemUTC());
    }
}

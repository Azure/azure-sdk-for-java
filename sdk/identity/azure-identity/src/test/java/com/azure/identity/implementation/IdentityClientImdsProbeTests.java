// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.credential.TokenRequestContext;
import com.azure.core.exception.ClientAuthenticationException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.policy.FixedDelay;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.utils.TestConfigurationSource;
import com.azure.identity.CredentialUnavailableException;
import com.azure.identity.util.ImdsProbeTestServer;
import com.azure.identity.util.TestUtils;
import com.microsoft.aad.msal4j.ManagedIdentityApplication;
import com.microsoft.aad.msal4j.ManagedIdentitySourceType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.MockedStatic;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.DisposableServer;
import reactor.netty.tcp.TcpServer;
import reactor.test.StepVerifier;

import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class IdentityClientImdsProbeTests {
    private static final TokenRequestContext REQUEST
        = new TokenRequestContext().addScopes("https://management.azure.com/.default");
    private static final Duration TEST_TIMEOUT = Duration.ofSeconds(5);

    @Test
    public void testNonHttpEndpointIsUnavailable() {
        AtomicInteger tokenRequests = new AtomicInteger();
        try (MockedStatic<ManagedIdentityApplication> application
            = mockStatic(ManagedIdentityApplication.class, CALLS_REAL_METHODS)) {
            application.when(ManagedIdentityApplication::getManagedIdentitySource)
                .thenReturn(ManagedIdentitySourceType.DEFAULT_TO_IMDS);

            StepVerifier
                .create(createClient("file:///imds", tokenRequests).authenticateWithManagedIdentityMsalClient(REQUEST))
                .expectErrorMatches(error -> error instanceof CredentialUnavailableException
                    && error.getCause() instanceof ClassCastException)
                .verify(TEST_TIMEOUT);
            assertEquals(0, tokenRequests.get());
        }
    }

    @ParameterizedTest
    @MethodSource("probeShutdownSignals")
    public void testProbeShutdownIsNotCredentialUnavailable(RuntimeException signal) throws IOException {
        AtomicInteger tokenRequests = new AtomicInteger();
        try (
            MockedStatic<ManagedIdentityApplication> application
                = mockStatic(ManagedIdentityApplication.class, CALLS_REAL_METHODS);
            MockedStatic<IdentityClientBase> identityClientBase
                = mockStatic(IdentityClientBase.class, CALLS_REAL_METHODS)) {
            application.when(ManagedIdentityApplication::getManagedIdentitySource)
                .thenReturn(ManagedIdentitySourceType.DEFAULT_TO_IMDS);
            URL url = mock(URL.class);
            HttpURLConnection connection = mock(HttpURLConnection.class);
            identityClientBase.when(() -> IdentityClientBase.getUrl(anyString())).thenReturn(url);
            when(url.openConnection()).thenReturn(connection);
            when(connection.getResponseCode()).thenThrow(signal);

            StepVerifier
                .create(
                    createClient("http://localhost", tokenRequests).authenticateWithManagedIdentityMsalClient(REQUEST))
                .expectErrorMatches(error -> error == signal)
                .verify(TEST_TIMEOUT);
            verify(connection).disconnect();
            assertEquals(0, tokenRequests.get());
        }
    }

    private static Stream<RuntimeException> probeShutdownSignals() {
        return Stream.of(new RuntimeException(new InterruptedException("Thread interrupted")),
            new IllegalStateException("Shutdown in progress"));
    }

    @Test
    public void testAcceptedConnectionWithoutResponseIsUnavailable() {
        AtomicInteger tokenRequests = new AtomicInteger();
        try (ImdsProbeTestServer server = new ImdsProbeTestServer((request, response) -> Mono.never());
            MockedStatic<ManagedIdentityApplication> application
                = mockStatic(ManagedIdentityApplication.class, CALLS_REAL_METHODS)) {
            application.when(ManagedIdentityApplication::getManagedIdentitySource)
                .thenReturn(ManagedIdentitySourceType.DEFAULT_TO_IMDS);
            IdentityClient client = createClient(server.getEndpoint(), tokenRequests);

            StepVerifier.create(client.authenticateWithManagedIdentityMsalClient(REQUEST))
                .expectErrorMatches(error -> error instanceof CredentialUnavailableException
                    && error.getCause() instanceof SocketTimeoutException)
                .verify(TEST_TIMEOUT);

            assertEquals(0, tokenRequests.get());
            assertEquals(1, server.getRequestCount());
            assertEquals("/metadata/identity/oauth2/token?api-version=2018-02-01", server.getRequestUri());
            assertNull(server.getMetadataHeader());
        }
    }

    @Test
    public void testConnectionClosedBeforeResponseIsUnavailable() {
        AtomicInteger tokenRequests = new AtomicInteger();
        try (ImdsProbeTestServer server = new ImdsProbeTestServer((request, response) -> {
            response.withConnection(Connection::dispose);
            return Mono.never();
        });
            MockedStatic<ManagedIdentityApplication> application
                = mockStatic(ManagedIdentityApplication.class, CALLS_REAL_METHODS)) {
            application.when(ManagedIdentityApplication::getManagedIdentitySource)
                .thenReturn(ManagedIdentitySourceType.DEFAULT_TO_IMDS);

            StepVerifier
                .create(createClient(server.getEndpoint(), tokenRequests)
                    .authenticateWithManagedIdentityMsalClient(REQUEST))
                .expectErrorMatches(
                    error -> error instanceof CredentialUnavailableException && error.getCause() instanceof IOException)
                .verify(TEST_TIMEOUT);
            assertEquals(0, tokenRequests.get());
        }
    }

    @Test
    public void testClosedEndpointIsUnavailable() {
        AtomicInteger tokenRequests = new AtomicInteger();
        String endpoint;
        try (ImdsProbeTestServer server = new ImdsProbeTestServer((request, response) -> Mono.never())) {
            endpoint = server.getEndpoint();
        }
        try (MockedStatic<ManagedIdentityApplication> application
            = mockStatic(ManagedIdentityApplication.class, CALLS_REAL_METHODS)) {
            application.when(ManagedIdentityApplication::getManagedIdentitySource)
                .thenReturn(ManagedIdentitySourceType.DEFAULT_TO_IMDS);

            StepVerifier
                .create(createClient(endpoint, tokenRequests).authenticateWithManagedIdentityMsalClient(REQUEST))
                .expectErrorMatches(error -> error instanceof CredentialUnavailableException
                    && (error.getCause() instanceof ConnectException
                        || error.getCause() instanceof SocketTimeoutException))
                .verify(TEST_TIMEOUT);
            assertEquals(0, tokenRequests.get());
        }
    }

    @Test
    public void testInvalidHttpResponseIsUnavailable() {
        AtomicInteger tokenRequests = new AtomicInteger();
        DisposableServer server = TcpServer.create()
            .host("127.0.0.1")
            .port(0)
            .handle((inbound, outbound) -> outbound.sendString(Mono.just("not an HTTP response\r\n\r\n")))
            .bindNow(TEST_TIMEOUT);
        try (MockedStatic<ManagedIdentityApplication> application
            = mockStatic(ManagedIdentityApplication.class, CALLS_REAL_METHODS)) {
            application.when(ManagedIdentityApplication::getManagedIdentitySource)
                .thenReturn(ManagedIdentitySourceType.DEFAULT_TO_IMDS);

            StepVerifier
                .create(createClient("http://127.0.0.1:" + server.port(), tokenRequests)
                    .authenticateWithManagedIdentityMsalClient(REQUEST))
                .expectErrorMatches(error -> error instanceof CredentialUnavailableException
                    && error.getCause() instanceof IOException
                    && error.getCause().getMessage().contains("valid HTTP response"))
                .verify(TEST_TIMEOUT);
            assertEquals(0, tokenRequests.get());
        } finally {
            server.disposeNow(TEST_TIMEOUT);
        }
    }

    @ParameterizedTest
    @ValueSource(ints = { 200, 400, 500 })
    public void testResponseHeadersAllowTokenAcquisitionWithoutReadingBody(int status) {
        AtomicInteger tokenRequests = new AtomicInteger();
        try (
            ImdsProbeTestServer server = new ImdsProbeTestServer((request,
                response) -> response.status(status).header("Content-Length", "100").sendHeaders().then(Mono.never()));
            MockedStatic<ManagedIdentityApplication> application
                = mockStatic(ManagedIdentityApplication.class, CALLS_REAL_METHODS)) {
            application.when(ManagedIdentityApplication::getManagedIdentitySource)
                .thenReturn(ManagedIdentitySourceType.DEFAULT_TO_IMDS);
            IdentityClient client = createClient(server.getEndpoint(), tokenRequests, 200, true, null);

            StepVerifier.create(client.authenticateWithManagedIdentityMsalClient(REQUEST))
                .expectNextMatches(token -> "managed-identity-token".equals(token.getToken()))
                .expectComplete()
                .verify(TEST_TIMEOUT);
            assertEquals(1, server.getRequestCount());
            assertEquals(1, tokenRequests.get());
            assertNull(server.getMetadataHeader());
        }
    }

    @Test
    public void testProbeDoesNotFollowRedirects() {
        AtomicInteger tokenRequests = new AtomicInteger();
        try (ImdsProbeTestServer redirectTarget = new ImdsProbeTestServer((request, response) -> Mono.never());
            ImdsProbeTestServer server = new ImdsProbeTestServer(
                (request, response) -> response.status(302).header("Location", redirectTarget.getEndpoint()).send());
            MockedStatic<ManagedIdentityApplication> application
                = mockStatic(ManagedIdentityApplication.class, CALLS_REAL_METHODS)) {
            application.when(ManagedIdentityApplication::getManagedIdentitySource)
                .thenReturn(ManagedIdentitySourceType.DEFAULT_TO_IMDS);

            StepVerifier
                .create(createClient(server.getEndpoint(), tokenRequests, 200, true, null)
                    .authenticateWithManagedIdentityMsalClient(REQUEST))
                .expectNextCount(1)
                .expectComplete()
                .verify(TEST_TIMEOUT);
            assertEquals(1, server.getRequestCount());
            assertEquals(0, redirectTarget.getRequestCount());
            assertEquals(1, tokenRequests.get());
        }
    }

    @Test
    public void testAuthenticationFailureAfterProbeRemainsAuthenticationFailure() {
        AtomicInteger tokenRequests = new AtomicInteger();
        try (ImdsProbeTestServer server = new ImdsProbeTestServer((request, response) -> response.status(400).send());
            MockedStatic<ManagedIdentityApplication> application
                = mockStatic(ManagedIdentityApplication.class, CALLS_REAL_METHODS)) {
            application.when(ManagedIdentityApplication::getManagedIdentitySource)
                .thenReturn(ManagedIdentitySourceType.DEFAULT_TO_IMDS);

            StepVerifier
                .create(createClient(server.getEndpoint(), tokenRequests)
                    .authenticateWithManagedIdentityMsalClient(REQUEST))
                .expectErrorMatches(error -> error instanceof ClientAuthenticationException
                    && !(error instanceof CredentialUnavailableException))
                .verify(TEST_TIMEOUT);
            assertEquals(1, server.getRequestCount());
            assertEquals(1, tokenRequests.get());
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void testStandaloneAndExplicitManagedIdentitySkipProbe(boolean chained) {
        AtomicInteger tokenRequests = new AtomicInteger();
        try (ImdsProbeTestServer server = new ImdsProbeTestServer((request, response) -> Mono.never());
            MockedStatic<ManagedIdentityApplication> application
                = mockStatic(ManagedIdentityApplication.class, CALLS_REAL_METHODS)) {
            application.when(ManagedIdentityApplication::getManagedIdentitySource)
                .thenReturn(ManagedIdentitySourceType.DEFAULT_TO_IMDS);
            IdentityClient client = createClient(server.getEndpoint(), tokenRequests, 200, chained,
                chained ? "ManagedIdentityCredential" : null);

            StepVerifier.create(client.authenticateWithManagedIdentityMsalClient(REQUEST))
                .expectNextCount(1)
                .expectComplete()
                .verify(TEST_TIMEOUT);
            assertEquals(0, server.getRequestCount());
            assertEquals(1, tokenRequests.get());
        }
    }

    private static IdentityClient createClient(String endpoint, AtomicInteger tokenRequests) {
        return createClient(endpoint, tokenRequests, 500, true, null);
    }

    private static IdentityClient createClient(String endpoint, AtomicInteger tokenRequests, int tokenStatus,
        boolean chained, String selectedCredential) {
        String tokenJson = "{\"access_token\":\"managed-identity-token\",\"expires_on\":\""
            + OffsetDateTime.now().plusHours(1).toEpochSecond() + "\",\"token_type\":\"Bearer\"}";
        HttpClient transport = request -> {
            tokenRequests.incrementAndGet();
            String body = tokenStatus == 200
                ? tokenJson
                : "{\"error\":\"server_error\",\"error_description\":\"Token failed\"}";
            return Mono.just(new MockHttpResponse(request, tokenStatus, body.getBytes(StandardCharsets.UTF_8)));
        };
        TestConfigurationSource configuration
            = new TestConfigurationSource().put("AZURE_POD_IDENTITY_AUTHORITY_HOST", endpoint);
        if (selectedCredential != null) {
            configuration.put("AZURE_TOKEN_CREDENTIALS", selectedCredential);
        }
        IdentityClientOptions options = new IdentityClientOptions().setChained(chained)
            .setConfiguration(TestUtils.createTestConfiguration(configuration))
            .setHttpClient(transport)
            .setRetryPolicy(new RetryPolicy(new FixedDelay(0, Duration.ZERO)));
        // MSAL shares its managed-identity token cache across applications.
        return new IdentityClientBuilder().clientId(UUID.randomUUID().toString())
            .identityClientOptions(options)
            .build();
    }
}

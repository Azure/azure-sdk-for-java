// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.ClientCertificateBinding;
import com.azure.core.credential.ProofOfPossessionOptions;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpProtocolVersion;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.util.Context;
import com.azure.core.util.Contexts;
import com.azure.core.validation.http.LocalTestServer;
import com.azure.core.validation.http.MtlsTestCertificates;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import org.eclipse.jetty.util.Callback;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLSession;
import java.security.cert.X509Certificate;
import java.time.Duration;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Tests sending requests bound to a TLS client certificate, for mutual TLS (mTLS) proof-of-possession, against local
 * servers.
 */
public class NettyClientCertificateBindingTests {
    private static final ClientCertificateBinding BINDING_A
        = MtlsTestCertificates.loadBinding(MtlsTestCertificates.CLIENT_A);
    private static final ClientCertificateBinding BINDING_B
        = MtlsTestCertificates.loadBinding(MtlsTestCertificates.CLIENT_B);
    private static final String NO_CERTIFICATE = "none";
    private static final String TLS_1_2 = "TLSv1.2";
    private static final String TLS_1_3 = "TLSv1.3";

    // The servers close the connection after responding to requests whose path starts with this prefix.
    private static final String CLOSE_CONNECTION_PATH = "/close/";

    // The requests the servers received, by path.
    private static final Map<String, Queue<ReceivedRequest>> RECEIVED = new ConcurrentHashMap<>();

    // Requests a client certificate, and accepts TLS 1.2 and TLS 1.3.
    private static LocalTestServer requestingServer;

    // Servers that only accept one TLS protocol, by protocol.
    private static final Map<String, LocalTestServer> REQUESTING_SERVERS = new ConcurrentHashMap<>();
    private static final Map<String, LocalTestServer> NON_REQUESTING_SERVERS = new ConcurrentHashMap<>();
    private static final Map<String, LocalTestServer> HTTP2_SERVERS = new ConcurrentHashMap<>();

    @BeforeAll
    public static void startServers() {
        LocalTestServer.RequestHandler handler = (request, response, body) -> {
            X509Certificate certificate = LocalTestServer.getClientCertificate(request);
            SSLSession session = LocalTestServer.getTlsSession(request);
            RECEIVED.computeIfAbsent(request.getRequestURI(), path -> new ConcurrentLinkedQueue<>())
                .add(new ReceivedRequest(certificate == null ? NO_CERTIFICATE : subject(certificate),
                    request.getHeader("Authorization"), request.getProtocol(),
                    session == null ? null : session.getProtocol(), session == null ? 0 : session.getCreationTime(),
                    request.getRemotePort()));

            if (request.getRequestURI().startsWith(CLOSE_CONNECTION_PATH)) {
                response.setHeader("Connection", "close");
            }
            response.setStatus(200);
            response.setContentLength(0);
            response.getHttpOutput().flush();
            response.getHttpOutput().complete(Callback.NOOP);
        };

        requestingServer = start(LocalTestServer.createMutualTlsServer(handler, true));
        for (String tlsProtocol : Arrays.asList(TLS_1_2, TLS_1_3)) {
            REQUESTING_SERVERS.put(tlsProtocol,
                start(LocalTestServer.createMutualTlsServer(handler, true, HttpProtocolVersion.HTTP_1_1, tlsProtocol)));
            NON_REQUESTING_SERVERS.put(tlsProtocol, start(
                LocalTestServer.createMutualTlsServer(handler, false, HttpProtocolVersion.HTTP_1_1, tlsProtocol)));
            HTTP2_SERVERS.put(tlsProtocol,
                start(LocalTestServer.createMutualTlsServer(handler, true, HttpProtocolVersion.HTTP_2, tlsProtocol)));
        }
    }

    @AfterAll
    public static void stopServers() {
        List<LocalTestServer> servers = new ArrayList<>();
        servers.add(requestingServer);
        servers.addAll(REQUESTING_SERVERS.values());
        servers.addAll(NON_REQUESTING_SERVERS.values());
        servers.addAll(HTTP2_SERVERS.values());
        for (LocalTestServer server : servers) {
            if (server != null) {
                server.stop();
            }
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { TLS_1_2, TLS_1_3 })
    public void boundTokenIsSentWithItsCertificate(String tlsProtocol) {
        AccessToken token = new AccessToken("token", OffsetDateTime.now().plusHours(1), null, "mtls_pop", BINDING_A);
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new BearerTokenAuthenticationPolicy(request -> Mono.just(token), "scope"))
            .httpClient(createClient())
            .build();
        String path = createPath();

        try (HttpResponse response = pipeline.sendSync(
            new HttpRequest(HttpMethod.GET, REQUESTING_SERVERS.get(tlsProtocol).getHttpsUri() + path), Context.NONE)) {
            assertEquals(200, response.getStatusCode());
        }

        ReceivedRequest received = RECEIVED.get(path).peek();
        assertEquals(subject(BINDING_A), received.certificate);
        assertEquals("mtls_pop token", received.authorization);
        assertEquals(tlsProtocol, received.tlsProtocol);
    }

    @ParameterizedTest
    @ValueSource(strings = { TLS_1_2, TLS_1_3 })
    public void boundRequestIsSentOverHttp2(String tlsProtocol) {
        // With HTTP/2, the request is sent on a stream whose parent connection presented the certificate.
        HttpClient client = new NettyAsyncHttpClientBuilder().maximumHttpVersion(HttpProtocolVersion.HTTP_2)
            .clientCertificateBindingTrustManagerFactory(InsecureTrustManagerFactory.INSTANCE)
            .build();
        String path = createPath();

        StepVerifier.create(send(client, HTTP2_SERVERS.get(tlsProtocol), path, BINDING_A))
            .expectNext(200)
            .expectComplete()
            .verify(Duration.ofSeconds(30));

        ReceivedRequest received = RECEIVED.get(path).peek();
        assertEquals(subject(BINDING_A), received.certificate);
        assertEquals("HTTP/2.0", received.httpProtocol);
        assertEquals(tlsProtocol, received.tlsProtocol);
    }

    @ParameterizedTest
    @ValueSource(strings = { TLS_1_2, TLS_1_3 })
    public void boundRequestIsSentOverResumedTlsSession(String tlsProtocol) {
        // The server closes each connection, so each request opens a new one, which resumes the TLS session. A resumed
        // session doesn't present the client certificate again, but keeps the one presented when it was created.
        HttpClient client = createClient();
        String path = CLOSE_CONNECTION_PATH + UUID.randomUUID();

        for (int i = 0; i < 3; i++) {
            StepVerifier.create(send(client, REQUESTING_SERVERS.get(tlsProtocol), path, BINDING_A))
                .expectNext(200)
                .expectComplete()
                .verify(Duration.ofSeconds(30));
        }

        List<ReceivedRequest> received = new ArrayList<>(RECEIVED.get(path));
        assertEquals(Collections.nCopies(3, subject(BINDING_A)), getPresentedCertificates(path));
        assertEquals(Collections.nCopies(3, tlsProtocol),
            received.stream().map(request -> request.tlsProtocol).collect(Collectors.toList()));
        // Three connections, which all used the session the first one created.
        assertEquals(3, received.stream().mapToInt(request -> request.remotePort).distinct().count());
        assertEquals(1, received.stream().mapToLong(request -> request.sessionCreationTime).distinct().count());
    }

    @Test
    public void boundTokenIsNotSentByClientThatCantBind() {
        // A wrapper that doesn't override supportsClientCertificateBinding() can't bind, even around a Netty client.
        HttpClient nettyClient = createClient();
        HttpClient wrapper = nettyClient::send;
        AccessToken token = new AccessToken("token", OffsetDateTime.now().plusHours(1), null, "mtls_pop", BINDING_A);
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new BearerTokenAuthenticationPolicy(request -> Mono.just(token), "scope"))
            .httpClient(wrapper)
            .build();
        String path = createPath();

        StepVerifier.create(pipeline.send(new HttpRequest(HttpMethod.GET, requestingServer.getHttpsUri() + path)))
            .expectErrorMatches(error -> error instanceof IllegalStateException
                && error.getMessage().contains("can't present the certificate"))
            .verify(Duration.ofSeconds(30));
        assertNull(RECEIVED.get(path));
    }

    @Test
    public void boundTokenIsReusedForConcurrentRequestsToDifferentUrls() {
        AtomicInteger credentialCalls = new AtomicInteger();
        AccessToken token = new AccessToken("token", OffsetDateTime.now().plusHours(1), null, "mtls_pop", BINDING_A);
        // The delay keeps the token request in flight while the other requests ask for a token for their own URL.
        HttpPipeline pipeline = createProofOfPossessionPipeline(createClient(), request -> Mono.defer(() -> {
            credentialCalls.incrementAndGet();
            return Mono.just(token).delayElement(Duration.ofMillis(200));
        }));
        List<String> paths = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            paths.add(createPath());
        }

        StepVerifier.create(Flux.fromIterable(paths).flatMap(path -> send(pipeline, path)))
            .expectNextCount(10)
            .expectComplete()
            .verify(Duration.ofSeconds(60));

        assertEquals(1, credentialCalls.get());
        for (String path : paths) {
            ReceivedRequest received = RECEIVED.get(path).peek();
            assertEquals(subject(BINDING_A), received.certificate);
            assertEquals("mtls_pop token", received.authorization);
        }
    }

    @Test
    public void signedRequestTokenIsReplacedForAnotherUrl() throws SSLException {
        AtomicInteger credentialCalls = new AtomicInteger();
        HttpPipeline pipeline = createProofOfPossessionPipeline(createClientTrustingServers(),
            request -> Mono.fromSupplier(() -> new AccessToken("token-" + credentialCalls.incrementAndGet(),
                OffsetDateTime.now().plusHours(1), null, "PoP")));
        String path = createPath();
        String otherPath = createPath();

        for (String requestPath : Arrays.asList(path, path, otherPath)) {
            StepVerifier.create(send(pipeline, requestPath))
                .expectNext(200)
                .expectComplete()
                .verify(Duration.ofSeconds(30));
        }

        // The token isn't bound to a certificate, so the requests go over connections that don't present one.
        assertEquals(Arrays.asList("PoP token-1", "PoP token-1"), getAuthorizations(path));
        assertEquals(Collections.singletonList("PoP token-2"), getAuthorizations(otherPath));
        assertEquals(Arrays.asList(NO_CERTIFICATE, NO_CERTIFICATE), getPresentedCertificates(path));
    }

    @ParameterizedTest
    @ValueSource(strings = { TLS_1_2, TLS_1_3 })
    public void boundRequestIsNotSentWhenServerDoesNotRequestCertificate(String tlsProtocol) {
        String path = createPath();

        StepVerifier.create(send(createClient(), NON_REQUESTING_SERVERS.get(tlsProtocol), path, BINDING_A))
            .expectErrorMatches(error -> error instanceof IllegalStateException
                && error.getMessage().contains("didn't present a client certificate"))
            .verify(Duration.ofSeconds(30));
        assertNull(RECEIVED.get(path));
    }

    @ParameterizedTest
    @ValueSource(strings = { TLS_1_2, TLS_1_3 })
    public void boundRequestIsNotSentWhenConnectionPresentsDifferentCertificate(String tlsProtocol) {
        // The key manager presents certificate B, but the token is bound to certificate A.
        ClientCertificateBinding mismatched = new ClientCertificateBinding(BINDING_A.getCertificate(),
            MtlsTestCertificates.loadKeyManager(MtlsTestCertificates.CLIENT_B));
        String path = createPath();

        StepVerifier.create(send(createClient(), REQUESTING_SERVERS.get(tlsProtocol), path, mismatched))
            .expectErrorMatches(error -> error instanceof IllegalStateException
                && error.getMessage().contains("presented a different client certificate"))
            .verify(Duration.ofSeconds(30));
        assertNull(RECEIVED.get(path));
    }

    @Test
    public void rotatedCertificatesArePresented() {
        NettyAsyncHttpClient client = (NettyAsyncHttpClient) createClient();
        String path = createPath();

        for (ClientCertificateBinding binding : Arrays.asList(BINDING_A, BINDING_B, BINDING_A)) {
            StepVerifier.create(send(client, requestingServer, path, binding))
                .expectNext(200)
                .expectComplete()
                .verify(Duration.ofSeconds(30));
        }

        assertEquals(Arrays.asList(subject(BINDING_A), subject(BINDING_B), subject(BINDING_A)),
            getPresentedCertificates(path));
        assertEquals(Arrays.asList(BINDING_B.getKeyId(), BINDING_A.getKeyId()), client.boundClients.getKeyIds());
    }

    @Test
    public void expiredCertificateIsRetired() {
        AtomicReference<Instant> now = new AtomicReference<>(Instant.now());
        NettyAsyncHttpClient client = (NettyAsyncHttpClient) new NettyAsyncHttpClientBuilder()
            .clientCertificateBindingTrustManagerFactory(InsecureTrustManagerFactory.INSTANCE)
            .clientCertificateBindingClock(now::get)
            .build();
        String path = createPath();

        StepVerifier.create(send(client, requestingServer, path, BINDING_A))
            .expectNext(200)
            .expectComplete()
            .verify(Duration.ofSeconds(30));
        now.set(BINDING_A.getCertificate().getNotAfter().toInstant().plusSeconds(1));
        StepVerifier.create(send(client, requestingServer, path, BINDING_B))
            .expectNext(200)
            .expectComplete()
            .verify(Duration.ofSeconds(30));

        assertEquals(Collections.singletonList(BINDING_B.getKeyId()), client.boundClients.getKeyIds());
        assertEquals(Arrays.asList(subject(BINDING_A), subject(BINDING_B)), getPresentedCertificates(path));
    }

    @Test
    public void concurrentRequestsPresentTheirOwnCertificates() {
        HttpClient client = createClient();
        String pathA = createPath();
        String pathB = createPath();

        StepVerifier
            .create(Flux.range(0, 20)
                .flatMap(i -> i % 2 == 0
                    ? send(client, requestingServer, pathA, BINDING_A)
                    : send(client, requestingServer, pathB, BINDING_B)))
            .expectNextCount(20)
            .expectComplete()
            .verify(Duration.ofSeconds(60));

        assertEquals(Collections.nCopies(10, subject(BINDING_A)), getPresentedCertificates(pathA));
        assertEquals(Collections.nCopies(10, subject(BINDING_B)), getPresentedCertificates(pathB));
    }

    @Test
    public void reactorClientPassedToBuilderSendsBoundAndUnboundRequests() throws SSLException {
        HttpClient client = createClientTrustingServers();
        String path = createPath();

        for (ClientCertificateBinding binding : Arrays.asList(null, BINDING_A, null)) {
            StepVerifier.create(send(client, requestingServer, path, binding))
                .expectNext(200)
                .expectComplete()
                .verify(Duration.ofSeconds(30));
        }

        assertEquals(Arrays.asList(NO_CERTIFICATE, subject(BINDING_A), NO_CERTIFICATE), getPresentedCertificates(path));
    }

    private static HttpClient createClient() {
        return new NettyAsyncHttpClientBuilder()
            .clientCertificateBindingTrustManagerFactory(InsecureTrustManagerFactory.INSTANCE)
            .build();
    }

    private static LocalTestServer start(LocalTestServer server) {
        server.start();
        return server;
    }

    // Creates a client from a Reactor Netty client that trusts the local servers, so that requests that aren't bound to
    // a certificate can reach them too.
    private static HttpClient createClientTrustingServers() throws SSLException {
        SslContext sslContext
            = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        return new NettyAsyncHttpClientBuilder(
            reactor.netty.http.client.HttpClient.create().secure(spec -> spec.sslContext(sslContext)))
                .clientCertificateBindingTrustManagerFactory(InsecureTrustManagerFactory.INSTANCE)
                .build();
    }

    // Creates a pipeline whose authentication policy asks for a proof-of-possession token for each request's URL and
    // method.
    private static HttpPipeline createProofOfPossessionPipeline(HttpClient client, TokenCredential credential) {
        BearerTokenAuthenticationPolicy policy = new BearerTokenAuthenticationPolicy(credential, "scope") {
            @Override
            public Mono<Void> authorizeRequest(HttpPipelineCallContext context) {
                HttpRequest request = context.getHttpRequest();
                return setAuthorizationHeader(context,
                    new TokenRequestContext().addScopes("scope")
                        .setProofOfPossessionOptions(new ProofOfPossessionOptions().setRequestUrl(request.getUrl())
                            .setRequestMethod(request.getHttpMethod())));
            }
        };

        return new HttpPipelineBuilder().policies(policy).httpClient(client).build();
    }

    // Sends a GET request through the pipeline to the server that requests a client certificate, and emits the
    // response status code.
    private static Mono<Integer> send(HttpPipeline pipeline, String path) {
        return pipeline.send(new HttpRequest(HttpMethod.GET, requestingServer.getHttpsUri() + path))
            .flatMap(response -> response.getBodyAsByteArray()
                .then(Mono.fromSupplier(response::getStatusCode))
                .doFinally(ignored -> response.close()));
    }

    // Sends a GET request bound to the certificate, or unbound if it's null, and emits the response status code.
    private static Mono<Integer> send(HttpClient client, LocalTestServer server, String path,
        ClientCertificateBinding binding) {
        Context context
            = binding == null ? Context.NONE : Contexts.empty().setClientCertificateBinding(binding).getContext();

        return client.send(new HttpRequest(HttpMethod.GET, server.getHttpsUri() + path), context)
            .flatMap(response -> response.getBodyAsByteArray()
                .then(Mono.fromSupplier(response::getStatusCode))
                .doFinally(ignored -> response.close()));
    }

    private static List<String> getPresentedCertificates(String path) {
        List<String> certificates = new ArrayList<>();
        for (ReceivedRequest request : RECEIVED.getOrDefault(path, new ConcurrentLinkedQueue<>())) {
            certificates.add(request.certificate);
        }

        return certificates;
    }

    private static List<String> getAuthorizations(String path) {
        List<String> authorizations = new ArrayList<>();
        for (ReceivedRequest request : RECEIVED.getOrDefault(path, new ConcurrentLinkedQueue<>())) {
            authorizations.add(request.authorization);
        }

        return authorizations;
    }

    private static String createPath() {
        return "/" + UUID.randomUUID();
    }

    private static String subject(ClientCertificateBinding binding) {
        return subject(binding.getCertificate());
    }

    private static String subject(X509Certificate certificate) {
        return certificate.getSubjectX500Principal().getName();
    }

    private static final class ReceivedRequest {
        private final String certificate;
        private final String authorization;
        private final String httpProtocol;
        private final String tlsProtocol;
        private final long sessionCreationTime;
        private final int remotePort;

        ReceivedRequest(String certificate, String authorization, String httpProtocol, String tlsProtocol,
            long sessionCreationTime, int remotePort) {
            this.certificate = certificate;
            this.authorization = authorization;
            this.httpProtocol = httpProtocol;
            this.tlsProtocol = tlsProtocol;
            this.sessionCreationTime = sessionCreationTime;
            this.remotePort = remotePort;
        }
    }
}

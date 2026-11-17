// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk.httpclient;

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
import org.eclipse.jetty.util.Callback;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.TrustManagerFactorySpi;
import javax.net.ssl.X509ExtendedTrustManager;
import java.net.Socket;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests sending requests bound to a TLS client certificate, for mutual TLS (mTLS) proof-of-possession, against local
 * servers.
 */
@DisabledForJreRange(max = JRE.JAVA_11)
public class JdkClientCertificateBindingTests {
    private static final ClientCertificateBinding BINDING_A
        = MtlsTestCertificates.loadBinding(MtlsTestCertificates.CLIENT_A);
    private static final ClientCertificateBinding BINDING_B
        = MtlsTestCertificates.loadBinding(MtlsTestCertificates.CLIENT_B);
    private static final String NO_CERTIFICATE = "none";

    // The servers redirect a request whose path starts with this prefix to the same path without it.
    private static final String REDIRECT_PREFIX = "/redirect";

    // The requests the servers received, by path.
    private static final Map<String, Queue<ReceivedRequest>> RECEIVED = new ConcurrentHashMap<>();

    private static LocalTestServer requestingServer;
    private static LocalTestServer nonRequestingServer;
    private static LocalTestServer http2Server;

    @BeforeAll
    public static void startServers() {
        LocalTestServer.RequestHandler handler = (request, response, body) -> {
            X509Certificate certificate = LocalTestServer.getClientCertificate(request);
            String path = request.getRequestURI();
            RECEIVED.computeIfAbsent(path, ignored -> new ConcurrentLinkedQueue<>())
                .add(new ReceivedRequest(certificate == null ? NO_CERTIFICATE : subject(certificate),
                    request.getHeader("Authorization"), request.getProtocol()));

            if (path.startsWith(REDIRECT_PREFIX)) {
                response.setStatus(302);
                response.setHeader("Location", path.substring(REDIRECT_PREFIX.length()));
            } else {
                response.setStatus(200);
            }

            response.setContentLength(0);
            response.getHttpOutput().flush();
            response.getHttpOutput().complete(Callback.NOOP);
        };

        requestingServer = LocalTestServer.createMutualTlsServer(handler, true);
        requestingServer.start();
        nonRequestingServer = LocalTestServer.createMutualTlsServer(handler, false);
        nonRequestingServer.start();
        http2Server = LocalTestServer.createMutualTlsServer(handler, true, HttpProtocolVersion.HTTP_2);
        http2Server.start();
    }

    @AfterAll
    public static void stopServers() {
        for (LocalTestServer server : Arrays.asList(requestingServer, nonRequestingServer, http2Server)) {
            if (server != null) {
                server.stop();
            }
        }
    }

    @Test
    public void boundTokenIsSentWithItsCertificate() {
        AccessToken token = new AccessToken("token", OffsetDateTime.now().plusHours(1), null, "mtls_pop", BINDING_A);
        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(new BearerTokenAuthenticationPolicy(request -> Mono.just(token), "scope"))
            .httpClient(createClient())
            .build();
        String path = createPath();

        try (HttpResponse response
            = pipeline.sendSync(new HttpRequest(HttpMethod.GET, requestingServer.getHttpsUri() + path), Context.NONE)) {
            assertEquals(200, response.getStatusCode());
        }

        ReceivedRequest received = RECEIVED.get(path).peek();
        assertEquals(subject(BINDING_A), received.certificate);
        assertEquals("mtls_pop token", received.authorization);
    }

    @Test
    public void boundRequestIsSentOverHttp2() {
        // The JDK client sends the HTTP/2 connection preface before the request, so it's checked first.
        HttpClient client = createBuilder().maximumHttpVersion(HttpProtocolVersion.HTTP_2).build();
        String path = createPath();

        StepVerifier.create(send(client, http2Server, path, BINDING_A))
            .expectNext(200)
            .expectComplete()
            .verify(Duration.ofSeconds(30));

        ReceivedRequest received = RECEIVED.get(path).peek();
        assertEquals(subject(BINDING_A), received.certificate);
        assertEquals("HTTP/2.0", received.protocol);
    }

    @Test
    public void boundTokenIsNotSentByClientThatCantBind() {
        // A wrapper that doesn't override supportsClientCertificateBinding() can't bind, even around a JDK client.
        HttpClient jdkClient = createClient();
        HttpClient wrapper = jdkClient::send;
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
    public void signedRequestTokenIsReplacedForAnotherUrl() {
        AtomicInteger credentialCalls = new AtomicInteger();
        HttpPipeline pipeline = createProofOfPossessionPipeline(createClient(),
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

    @Test
    public void boundRequestIsNotSentWhenServerDoesNotRequestCertificate() {
        String path = createPath();

        StepVerifier.create(send(createClient(), nonRequestingServer, path, BINDING_A))
            .expectErrorMatches(error -> error instanceof IllegalStateException
                && error.getMessage().contains("didn't present a client certificate"))
            .verify(Duration.ofSeconds(30));
        assertNull(RECEIVED.get(path));
    }

    @Test
    public void boundRequestIsNotSentWhenConnectionPresentsDifferentCertificate() {
        // The key manager presents certificate B, but the token is bound to certificate A.
        ClientCertificateBinding mismatched = new ClientCertificateBinding(BINDING_A.getCertificate(),
            MtlsTestCertificates.loadKeyManager(MtlsTestCertificates.CLIENT_B));
        String path = createPath();
        HttpRequest request = new HttpRequest(HttpMethod.GET, requestingServer.getHttpsUri() + path);

        IllegalStateException error = assertThrows(IllegalStateException.class,
            () -> createClient().sendSync(request, createContext(mismatched)));
        assertTrue(error.getMessage().contains("presented a different client certificate"));
        assertNull(RECEIVED.get(path));
    }

    @Test
    public void boundRequestIsNotSentWithoutTls() {
        String path = createPath();
        HttpRequest request = new HttpRequest(HttpMethod.GET, requestingServer.getHttpUri() + path);

        StepVerifier.create(createClient().send(request, createContext(BINDING_A)))
            .expectErrorMatches(error -> error instanceof IllegalStateException
                && error.getMessage().contains("didn't present a client certificate"))
            .verify(Duration.ofSeconds(30));
        assertNull(RECEIVED.get(path));
    }

    @Test
    public void boundRequestDoesNotFollowRedirects() {
        HttpClient client = new JdkHttpClientBuilder(
            createTrustingJdkBuilder().followRedirects(java.net.http.HttpClient.Redirect.ALWAYS))
                .clientCertificateBindingTrustManagerFactory(new TrustAllServersFactory())
                .build();
        String target = createPath();

        StepVerifier.create(send(client, requestingServer, REDIRECT_PREFIX + target, BINDING_A))
            .expectNext(302)
            .expectComplete()
            .verify(Duration.ofSeconds(30));

        assertNull(RECEIVED.get(target));
    }

    @Test
    public void rotatedCertificatesArePresented() {
        JdkHttpClient client = (JdkHttpClient) createClient();
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
        JdkHttpClient client = (JdkHttpClient) createBuilder().clientCertificateBindingClock(now::get).build();
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
    public void boundAndUnboundRequestsAreSentByTheirOwnClients() {
        HttpClient client = createClient();
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
        return createBuilder().build();
    }

    // Creates a builder whose clients trust the local servers, for requests bound to a certificate or not.
    private static JdkHttpClientBuilder createBuilder() {
        return new JdkHttpClientBuilder(createTrustingJdkBuilder())
            .clientCertificateBindingTrustManagerFactory(new TrustAllServersFactory());
    }

    private static java.net.http.HttpClient.Builder createTrustingJdkBuilder() {
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[] { new TrustAllServersTrustManager() }, null);
            return java.net.http.HttpClient.newBuilder()
                .version(java.net.http.HttpClient.Version.HTTP_1_1)
                .sslContext(sslContext);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
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
        return client.send(new HttpRequest(HttpMethod.GET, server.getHttpsUri() + path), createContext(binding))
            .flatMap(response -> response.getBodyAsByteArray()
                .then(Mono.fromSupplier(response::getStatusCode))
                .doFinally(ignored -> response.close()));
    }

    private static Context createContext(ClientCertificateBinding binding) {
        return binding == null ? Context.NONE : Contexts.empty().setClientCertificateBinding(binding).getContext();
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
        private final String protocol;

        ReceivedRequest(String certificate, String authorization, String protocol) {
            this.certificate = certificate;
            this.authorization = authorization;
            this.protocol = protocol;
        }
    }

    // Trusts the local servers' self-signed certificate, which isn't issued for localhost.
    private static final class TrustAllServersFactory extends TrustManagerFactory {
        TrustAllServersFactory() {
            super(new TrustManagerFactorySpi() {
                @Override
                protected void engineInit(KeyStore keyStore) {
                }

                @Override
                protected void engineInit(ManagerFactoryParameters parameters) {
                }

                @Override
                protected TrustManager[] engineGetTrustManagers() {
                    return new TrustManager[] { new TrustAllServersTrustManager() };
                }
            }, null, "TrustAllServers");
        }
    }

    private static final class TrustAllServersTrustManager extends X509ExtendedTrustManager {
        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType, Socket socket) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType, Socket socket) {
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType, SSLEngine engine) {
        }

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) {
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.policy.FixedDelayOptions;
import com.azure.core.http.policy.RetryOptions;
import com.azure.core.test.http.MockHttpResponse;
import com.azure.core.test.utils.TestConfigurationSource;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.identity.implementation.customtokenproxy.CustomTokenProxyHttpClient;
import com.azure.identity.implementation.customtokenproxy.ProxyConfig;
import com.azure.identity.util.TestUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.SslProvider;
import io.netty.util.CharsetUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import javax.net.ssl.ExtendedSSLSession;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLSession;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.time.OffsetDateTime;
import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * E2E test for WorkloadIdentityCredential with AKS Custom Token Proxy using Netty mock server.
 * The certificate is loaded from pre-generated certificate from resources, which was generated as follows-
 * keytool -genkeypair -alias test-cert -keyalg RSA -keysize 2048 -validity 3650 -keystore src/test/resources/test-aks-cert.p12 -storetype PKCS12
 * -storepass password -keypass password -dname "CN=AKS-Proxy-Test" -ext "SAN=DNS:test-aks-proxy.ests.aks"
 *
 */
public class WorkloadIdentityCredentialIdentityBindingTest {

    private static final String AKS_SNI_NAME = "test-aks-proxy.ests.aks";

    private static final String TEST_CLIENT_ID = "test-client-id";
    private static final String TEST_TENANT_ID = "test-tenant-id";
    private static final String MOCK_ACCESS_TOKEN = "mock_access_token_from_aks_proxy";
    private static final String MISMATCHED_CERT_SAN = "wrong-hostname.example.com";

    @TempDir
    Path tempDir;

    private NioEventLoopGroup bossGroup;
    private NioEventLoopGroup workerGroup;
    private Channel serverChannel;
    private String serverBaseUrl;
    private Path tokenFilePath;
    private Path caCertFilePath;
    private String caCertPemData;
    private AtomicInteger tokenRequestCount;
    private AtomicInteger metadataRequestCount;
    private X509Certificate certificate;
    private volatile String lastRequestUri;
    private volatile String lastRequestBody;
    private volatile String lastRequestMethod;
    private volatile String lastCustomHeader;
    private volatile String lastServerName;
    private volatile HttpResponseStatus responseStatus = HttpResponseStatus.OK;
    private volatile String responseBodyOverride;
    private volatile String redirectLocation;
    private volatile boolean throttleFirstRequest;
    private volatile boolean closeFirstTokenRequest;
    private volatile SslContext retrySslContext;
    private final AtomicInteger connectionCount = new AtomicInteger();
    private final List<String> tokenRequestUris = new CopyOnWriteArrayList<>();

    @BeforeEach
    public void setUp() throws Exception {
        tokenRequestCount = new AtomicInteger(0);
        metadataRequestCount = new AtomicInteger(0);

        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream keyStoreStream = getClass().getResourceAsStream("/test-aks-cert.p12")) {
            if (keyStoreStream == null) {
                throw new IllegalStateException("Test certificate not found in resources: /test-aks-cert.p12");
            }
            keyStore.load(keyStoreStream, "password".toCharArray());
        }

        PrivateKey privateKey = (PrivateKey) keyStore.getKey("test-cert", "password".toCharArray());
        certificate = (X509Certificate) keyStore.getCertificate("test-cert");

        caCertPemData = toPemFormat(certificate);

        caCertFilePath = tempDir.resolve("ca-cert.pem");
        Files.write(caCertFilePath, caCertPemData.getBytes(StandardCharsets.UTF_8));

        tokenFilePath = tempDir.resolve("token.jwt");
        String mockJwt = createMockFederatedToken();
        Files.write(tokenFilePath, mockJwt.getBytes(StandardCharsets.UTF_8));

        startNettyHttpsServer(privateKey, certificate);
    }

    @AfterEach
    public void cleanup() {
        if (serverChannel != null) {
            serverChannel.close().syncUninterruptibly();
            serverChannel = null;
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully(0, 5, TimeUnit.SECONDS).syncUninterruptibly();
            bossGroup = null;
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully(0, 5, TimeUnit.SECONDS).syncUninterruptibly();
            workerGroup = null;
        }
    }

    @Test
    public void testAksProxyWithCaFile() throws CertificateParsingException {
        Configuration configuration = TestUtils
            .createTestConfiguration(new TestConfigurationSource().put("AZURE_KUBERNETES_TOKEN_PROXY", serverBaseUrl)
                .put("AZURE_KUBERNETES_CA_FILE", caCertFilePath.toString())
                .put("AZURE_KUBERNETES_SNI_NAME", AKS_SNI_NAME)
                .put(Configuration.PROPERTY_AZURE_CLIENT_ID, TEST_CLIENT_ID)
                .put(Configuration.PROPERTY_AZURE_TENANT_ID, TEST_TENANT_ID)
                .put("AZURE_FEDERATED_TOKEN_FILE", tokenFilePath.toString()));

        List<String> dnsNames = fetchCertificateSAN(certificate);

        assertTrue(dnsNames.contains("test-aks-proxy.ests.aks"),
            "Certificate should contain the SNI name 'test-aks-proxy.ests.aks'");

        WorkloadIdentityCredential credential = new WorkloadIdentityCredentialBuilder().tenantId(TEST_TENANT_ID)
            .clientId(TEST_CLIENT_ID)
            .tokenFilePath(tokenFilePath.toString())
            .configuration(configuration)
            .authorityHost(serverBaseUrl)
            .enableAzureProxy()
            .disableInstanceDiscovery()
            .build();

        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com/.default");

        AccessToken token = credential.getTokenSync(request);

        assertNotNull(token, "Access token should not be null");
        assertEquals(MOCK_ACCESS_TOKEN, token.getToken(), "Token value should match mock response");
        assertTrue(token.getExpiresAt().isAfter(OffsetDateTime.now()), "Token should not be expired");
        assertEquals(1, tokenRequestCount.get(), "Server should have received exactly one token request");
    }

    @Test
    public void testAksProxyWithCaFileAsync() {
        Configuration configuration = TestUtils
            .createTestConfiguration(new TestConfigurationSource().put("AZURE_KUBERNETES_TOKEN_PROXY", serverBaseUrl)
                .put("AZURE_KUBERNETES_CA_FILE", caCertFilePath.toString())
                .put("AZURE_KUBERNETES_SNI_NAME", AKS_SNI_NAME)
                .put(Configuration.PROPERTY_AZURE_CLIENT_ID, TEST_CLIENT_ID)
                .put(Configuration.PROPERTY_AZURE_TENANT_ID, TEST_TENANT_ID)
                .put("AZURE_FEDERATED_TOKEN_FILE", tokenFilePath.toString()));

        WorkloadIdentityCredential credential = new WorkloadIdentityCredentialBuilder().tenantId(TEST_TENANT_ID)
            .clientId(TEST_CLIENT_ID)
            .tokenFilePath(tokenFilePath.toString())
            .configuration(configuration)
            .authorityHost(serverBaseUrl)
            .enableAzureProxy()
            .disableInstanceDiscovery()
            .build();

        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com/.default");

        StepVerifier.create(credential.getToken(request))
            .expectNextMatches(token -> MOCK_ACCESS_TOKEN.equals(token.getToken())
                && token.getExpiresAt().isAfter(OffsetDateTime.now()))
            .verifyComplete();

        assertEquals(1, tokenRequestCount.get(), "Server should have received exactly one token request");
    }

    @Test
    public void testAksProxyWithCaData() {
        Configuration configuration = TestUtils
            .createTestConfiguration(new TestConfigurationSource().put("AZURE_KUBERNETES_TOKEN_PROXY", serverBaseUrl)
                .put("AZURE_KUBERNETES_CA_DATA", caCertPemData)
                .put("AZURE_KUBERNETES_SNI_NAME", AKS_SNI_NAME)
                .put(Configuration.PROPERTY_AZURE_CLIENT_ID, TEST_CLIENT_ID)
                .put(Configuration.PROPERTY_AZURE_TENANT_ID, TEST_TENANT_ID)
                .put("AZURE_FEDERATED_TOKEN_FILE", tokenFilePath.toString()));

        WorkloadIdentityCredential credential = new WorkloadIdentityCredentialBuilder().tenantId(TEST_TENANT_ID)
            .clientId(TEST_CLIENT_ID)
            .tokenFilePath(tokenFilePath.toString())
            .configuration(configuration)
            .authorityHost(serverBaseUrl)
            .enableAzureProxy()
            .disableInstanceDiscovery()
            .build();

        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com/.default");

        AccessToken token = credential.getTokenSync(request);

        assertNotNull(token, "Access token should not be null");
        assertEquals(MOCK_ACCESS_TOKEN, token.getToken(), "Token value should match mock response");
        assertEquals(1, tokenRequestCount.get(), "Server should have received exactly one token request");
    }

    @Test
    public void testAksProxyRoutesPublicAuthorityRequests() throws Exception {
        WorkloadIdentityCredential credential
            = new WorkloadIdentityCredentialBuilder().configuration(proxyConfiguration()).enableAzureProxy().build();

        AccessToken token = credential.getTokenSync(tokenRequest());

        assertEquals(MOCK_ACCESS_TOKEN, token.getToken());
        assertEquals("/" + TEST_TENANT_ID + "/oauth2/v2.0/token", lastRequestUri);
        assertEquals(AKS_SNI_NAME, lastServerName);
        Map<String, String> form = parseForm(lastRequestBody);
        assertEquals("client_credentials", form.get("grant_type"));
        assertEquals(TEST_CLIENT_ID, form.get("client_id"));
        assertEquals(new String(Files.readAllBytes(tokenFilePath), StandardCharsets.UTF_8),
            form.get("client_assertion"));
        assertTrue(Arrays.asList(form.get("scope").split(" ")).contains("https://management.azure.com/.default"));
        assertEquals(1, tokenRequestCount.get(), tokenRequestUris::toString);
    }

    @Test
    public void testAksProxyCachesAsyncTokens() {
        WorkloadIdentityCredential credential
            = new WorkloadIdentityCredentialBuilder().configuration(proxyConfiguration()).enableAzureProxy().build();
        AccessToken token = credential.getToken(tokenRequest()).block(Duration.ofSeconds(30));
        assertNotNull(token);
        int tokenRequests = tokenRequestCount.get();
        int metadataRequests = metadataRequestCount.get();
        assertTrue(tokenRequests > 0);

        AccessToken cached = credential.getToken(tokenRequest()).block(Duration.ofSeconds(30));

        assertNotNull(cached);
        assertEquals(token.getToken(), cached.getToken());
        assertEquals(tokenRequests, tokenRequestCount.get());
        assertEquals(metadataRequests, metadataRequestCount.get());
    }

    @Test
    public void testAksProxySupportsConcurrentTokenRequests() {
        WorkloadIdentityCredential credential
            = new WorkloadIdentityCredentialBuilder().configuration(proxyConfiguration()).enableAzureProxy().build();

        StepVerifier.create(Flux.range(0, 4).flatMap(ignored -> credential.getToken(tokenRequest())).collectList())
            .assertNext(tokens -> {
                assertEquals(4, tokens.size());
                tokens.forEach(token -> {
                    assertEquals(MOCK_ACCESS_TOKEN, token.getToken());
                    assertTrue(token.getExpiresAt().isAfter(OffsetDateTime.now()));
                });
            })
            .expectComplete()
            .verify(Duration.ofSeconds(30));
        assertTrue(tokenRequestCount.get() > 0);
    }

    @Test
    public void testAksProxyReadsRotatedServiceAccountToken() throws Exception {
        WorkloadIdentityCredential credential
            = new WorkloadIdentityCredentialBuilder().configuration(proxyConfiguration()).enableAzureProxy().build();
        credential.getTokenSync(tokenRequest());
        String rotatedAssertion = createMockFederatedToken() + "-rotated";
        Files.write(tokenFilePath, rotatedAssertion.getBytes(StandardCharsets.UTF_8));

        credential.getTokenSync(new TokenRequestContext().addScopes("https://vault.azure.net/.default"));

        assertEquals(rotatedAssertion, parseForm(lastRequestBody).get("client_assertion"));
        assertEquals(2, tokenRequestCount.get(), tokenRequestUris::toString);
    }

    @Test
    public void testAksProxyDisabledUsesDirectAuthentication() {
        AtomicInteger directRequests = new AtomicInteger();
        HttpClient directClient = request -> {
            assertEquals("login.microsoftonline.com", request.getUrl().getHost());
            directRequests.incrementAndGet();
            return Mono.just(new MockHttpResponse(request, 400, new HttpHeaders(),
                "{\"error\":\"invalid_client\",\"error_description\":\"AADSTS700211: No matching federated identity.\"}"
                    .getBytes(StandardCharsets.UTF_8)));
        };
        WorkloadIdentityCredential credential
            = new WorkloadIdentityCredentialBuilder().configuration(proxyConfiguration())
                .httpClient(directClient)
                .build();

        RuntimeException exception
            = assertThrows(RuntimeException.class, () -> credential.getTokenSync(tokenRequest()));

        assertTrue(exception.getMessage().contains("AADSTS700211"));
        assertTrue(directRequests.get() > 0);
        assertEquals(0, tokenRequestCount.get());
        assertEquals(0, metadataRequestCount.get());
    }

    @Test
    public void testAksProxyOptInWithoutProxyConfigurationUsesDirectAuthentication() {
        AtomicInteger directRequests = new AtomicInteger();
        HttpClient directClient = request -> {
            assertEquals("login.microsoftonline.com", request.getUrl().getHost());
            directRequests.incrementAndGet();
            return Mono.just(new MockHttpResponse(request, 200, new HttpHeaders(),
                "{\"access_token\":\"direct-token\",\"expires_in\":3600,\"token_type\":\"Bearer\"}"
                    .getBytes(StandardCharsets.UTF_8)));
        };
        WorkloadIdentityCredential credential = new WorkloadIdentityCredentialBuilder().tenantId(TEST_TENANT_ID)
            .clientId(TEST_CLIENT_ID)
            .tokenFilePath(tokenFilePath.toString())
            .configuration(TestUtils.createTestConfiguration(new TestConfigurationSource()))
            .httpClient(directClient)
            .enableAzureProxy()
            .build();

        assertEquals("direct-token", credential.getTokenSync(tokenRequest()).getToken());
        assertTrue(directRequests.get() > 0);
        assertEquals(0, tokenRequestCount.get());
        assertEquals(0, metadataRequestCount.get());
    }

    @ParameterizedTest
    @CsvSource(
        value = {
            "''|/tenant/oauth2/v2.0/token",
            "/|/tenant/oauth2/v2.0/token",
            "/proxy|/proxy/tenant/oauth2/v2.0/token",
            "/proxy/|/proxy/tenant/oauth2/v2.0/token",
            "/api%2Fv1/token|/api%2Fv1/token/tenant/oauth2/v2.0/token" },
        delimiter = '|')
    public void testAksProxyPreservesRequestAndEncodedPathAndQuery(String proxyPath, String expectedPath)
        throws Exception {
        String query = "?api-version=2.0&scope=a%2Fb&value=two%20words";
        CustomTokenProxyHttpClient client
            = new CustomTokenProxyHttpClient(new ProxyConfig(new URL(serverBaseUrl + proxyPath), AKS_SNI_NAME, null,
                caCertPemData.getBytes(StandardCharsets.UTF_8)));
        String originalUrl = AzureAuthorityHosts.AZURE_PUBLIC_CLOUD + "tenant/oauth2/v2.0/token" + query;
        HttpRequest request = new HttpRequest(HttpMethod.POST, originalUrl)
            .setHeader(HttpHeaderName.fromString("x-proxy-test"), "preserved")
            .setBody("request-body");

        for (int i = 0; i < 2; i++) {
            try (HttpResponse response = client.sendSync(request, Context.NONE)) {
                assertEquals(200, response.getStatusCode());
                assertEquals(serverBaseUrl + expectedPath + query, response.getRequest().getUrl().toString());
                assertFalse(response.getBodyAsBinaryData().toString().isEmpty());
            }
            assertEquals(expectedPath + query, lastRequestUri);
            assertEquals("request-body", lastRequestBody);
            assertEquals("POST", lastRequestMethod);
            assertEquals("preserved", lastCustomHeader);
            assertEquals(originalUrl, request.getUrl().toString(), "Retries must not prepend the proxy path twice");
        }
        assertEquals(2, tokenRequestCount.get());
    }

    @Test
    public void testAksProxyRoutesMetadataGetRequests() throws Exception {
        CustomTokenProxyHttpClient client = new CustomTokenProxyHttpClient(
            new ProxyConfig(new URL(serverBaseUrl), AKS_SNI_NAME, caCertFilePath.toString(), null));
        String path = "/" + TEST_TENANT_ID + "/v2.0/.well-known/openid-configuration";
        HttpRequest request
            = new HttpRequest(HttpMethod.GET, AzureAuthorityHosts.AZURE_PUBLIC_CLOUD + path.substring(1));

        try (HttpResponse response = client.sendSync(request, Context.NONE)) {
            assertEquals(200, response.getStatusCode());
            assertEquals("application/json", response.getHeaderValue("Content-Type"));
            assertTrue(response.getBodyAsBinaryData().toString().contains("token_endpoint"));
        }

        assertEquals("GET", lastRequestMethod);
        assertEquals(path, lastRequestUri);
        assertEquals(1, metadataRequestCount.get());
        assertEquals(0, tokenRequestCount.get());
    }

    @Test
    public void testAksProxyRoutesInstanceDiscoveryRequests() throws Exception {
        CustomTokenProxyHttpClient client = new CustomTokenProxyHttpClient(
            new ProxyConfig(new URL(serverBaseUrl), AKS_SNI_NAME, caCertFilePath.toString(), null));
        String path = "/common/discovery/instance?api-version=1.1";
        HttpRequest request
            = new HttpRequest(HttpMethod.GET, AzureAuthorityHosts.AZURE_PUBLIC_CLOUD + path.substring(1));

        try (HttpResponse response = client.sendSync(request, Context.NONE)) {
            assertEquals(200, response.getStatusCode());
            String body = response.getBodyAsBinaryData().toString();
            assertTrue(body.contains("\"tenant_discovery_endpoint\""));
            assertTrue(body.contains("\"preferred_network\":\"login.microsoftonline.com\""));
        }

        assertEquals("GET", lastRequestMethod);
        assertEquals(path, lastRequestUri);
        assertEquals(1, metadataRequestCount.get());
        assertEquals(0, tokenRequestCount.get());
    }

    @ParameterizedTest
    @ValueSource(ints = { 400, 401, 403, 500 })
    public void testAksProxyPreservesErrorResponses(int status) throws Exception {
        responseStatus = HttpResponseStatus.valueOf(status);
        responseBodyOverride = "{\"error\":\"invalid_grant\",\"error_description\":\"Proxy denied the request\"}";
        CustomTokenProxyHttpClient client = new CustomTokenProxyHttpClient(
            new ProxyConfig(new URL(serverBaseUrl), AKS_SNI_NAME, caCertFilePath.toString(), null));
        HttpRequest request = new HttpRequest(HttpMethod.POST,
            AzureAuthorityHosts.AZURE_PUBLIC_CLOUD + TEST_TENANT_ID + "/oauth2/v2.0/token").setBody("request");

        try (HttpResponse response = client.sendSync(request, Context.NONE)) {
            assertEquals(status, response.getStatusCode());
            assertEquals(responseBodyOverride, response.getBodyAsBinaryData().toString());
        }
        assertEquals(1, tokenRequestCount.get());
    }

    @Test
    public void testAksProxyDoesNotFollowRedirects() throws Exception {
        responseStatus = HttpResponseStatus.FOUND;
        redirectLocation = serverBaseUrl + "/redirected";
        CustomTokenProxyHttpClient client = new CustomTokenProxyHttpClient(
            new ProxyConfig(new URL(serverBaseUrl), AKS_SNI_NAME, caCertFilePath.toString(), null));
        HttpRequest request = new HttpRequest(HttpMethod.POST,
            AzureAuthorityHosts.AZURE_PUBLIC_CLOUD + TEST_TENANT_ID + "/oauth2/v2.0/token").setBody("request");

        try (HttpResponse response = client.sendSync(request, Context.NONE)) {
            assertEquals(302, response.getStatusCode());
            assertEquals(redirectLocation, response.getHeaderValue("Location"));
            assertFalse(response.getBodyAsBinaryData().toString().isEmpty());
        }
        assertEquals(1, tokenRequestCount.get(), "Redirects must not forward the service account assertion");
    }

    @Test
    public void testAksProxyPreservesPipelineRetries() {
        throttleFirstRequest = true;
        WorkloadIdentityCredential credential
            = new WorkloadIdentityCredentialBuilder().configuration(proxyConfiguration(serverBaseUrl + "/proxy"))
                .retryOptions(new RetryOptions(new FixedDelayOptions(1, Duration.ofMillis(1))))
                .enableAzureProxy()
                .build();

        assertEquals(MOCK_ACCESS_TOKEN, credential.getTokenSync(tokenRequest()).getToken());
        String path = "/proxy/" + TEST_TENANT_ID + "/oauth2/v2.0/token";
        assertEquals(Arrays.asList(path, path), tokenRequestUris);
    }

    @Test
    public void testAksProxyRetainsCaDuringMissingFileRotationGap() throws Exception {
        CustomTokenProxyHttpClient client = new CustomTokenProxyHttpClient(
            new ProxyConfig(new URL(serverBaseUrl), AKS_SNI_NAME, caCertFilePath.toString(), null));
        sendProxyRequest(client);

        Files.delete(caCertFilePath);

        sendProxyRequest(client);
        assertEquals(2, tokenRequestCount.get(), "A rotation gap must retain the previously loaded CA");
    }

    @Test
    public void testAksProxyRetainsCaDuringEmptyFileRotationGap() throws Exception {
        CustomTokenProxyHttpClient client = new CustomTokenProxyHttpClient(
            new ProxyConfig(new URL(serverBaseUrl), AKS_SNI_NAME, caCertFilePath.toString(), null));
        sendProxyRequest(client);
        Files.write(caCertFilePath, new byte[0]);

        sendProxyRequest(client);

        assertEquals(2, tokenRequestCount.get());
    }

    @Test
    public void testAksProxyReloadsCaAndDoesNotTrustReplacedCa() throws Exception {
        CustomTokenProxyHttpClient client = new CustomTokenProxyHttpClient(
            new ProxyConfig(new URL(serverBaseUrl), AKS_SNI_NAME, caCertFilePath.toString(), null));
        sendProxyRequest(client);
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream stream = getClass().getResourceAsStream("/test-aks-cert-mismatch.p12")) {
            assertNotNull(stream);
            keyStore.load(stream, "password".toCharArray());
        }
        X509Certificate replacement = (X509Certificate) keyStore.getCertificate("test-cert");
        Files.write(caCertFilePath, toPemFormat(replacement).getBytes(StandardCharsets.UTF_8));

        assertThrows(RuntimeException.class, () -> sendProxyRequest(client));
        assertEquals(1, tokenRequestCount.get(), "The previous CA must no longer be trusted");
        Files.write(caCertFilePath, caCertPemData.getBytes(StandardCharsets.UTF_8));
        sendProxyRequest(client);
        assertEquals(2, tokenRequestCount.get());
    }

    @Test
    public void testAksProxyInvalidCaRotationFailsInsteadOfFallingBack() throws Exception {
        CustomTokenProxyHttpClient client = new CustomTokenProxyHttpClient(
            new ProxyConfig(new URL(serverBaseUrl), AKS_SNI_NAME, caCertFilePath.toString(), null));
        sendProxyRequest(client);
        Files.write(caCertFilePath, "not-a-certificate".getBytes(StandardCharsets.UTF_8));

        assertThrows(IllegalStateException.class, () -> sendProxyRequest(client));
        assertEquals(1, tokenRequestCount.get());
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void testAksProxyMissingOrEmptyInitialCaDoesNotUseDefaultTrust(boolean missing) throws Exception {
        if (missing) {
            Files.delete(caCertFilePath);
        } else {
            Files.write(caCertFilePath, new byte[0]);
        }
        CustomTokenProxyHttpClient client = new CustomTokenProxyHttpClient(
            new ProxyConfig(new URL(serverBaseUrl), AKS_SNI_NAME, caCertFilePath.toString(), null));

        assertThrows(IllegalStateException.class, () -> sendProxyRequest(client));
        assertEquals(0, tokenRequestCount.get());
    }

    @Test
    public void testAksProxyRejectsPartiallyMalformedCaBundle() throws Exception {
        String invalidBundle
            = caCertPemData + "-----BEGIN CERTIFICATE-----\nINVALID_BASE64_DATA\n-----END CERTIFICATE-----\n";
        CustomTokenProxyHttpClient client = new CustomTokenProxyHttpClient(new ProxyConfig(new URL(serverBaseUrl),
            AKS_SNI_NAME, null, invalidBundle.getBytes(StandardCharsets.UTF_8)));

        assertThrows(RuntimeException.class, () -> sendProxyRequest(client));
        assertEquals(0, tokenRequestCount.get());
    }

    @Test
    public void testAksProxyRejectsWrongSniEvenWhenCertificateMatchesProxyUrl() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream stream = getClass().getResourceAsStream("/test-aks-cert-no-sni.p12")) {
            assertNotNull(stream);
            keyStore.load(stream, "password".toCharArray());
        }
        X509Certificate localhostCertificate = (X509Certificate) keyStore.getCertificate("test-cert");
        startNettyHttpsServer((PrivateKey) keyStore.getKey("test-cert", "password".toCharArray()),
            localhostCertificate);
        CustomTokenProxyHttpClient client = new CustomTokenProxyHttpClient(new ProxyConfig(new URL(serverBaseUrl),
            MISMATCHED_CERT_SAN, null, toPemFormat(localhostCertificate).getBytes(StandardCharsets.UTF_8)));

        assertThrows(RuntimeException.class, () -> sendProxyRequest(client));
        assertEquals(0, tokenRequestCount.get(), "The assertion must not be sent to a host with the wrong TLS name");
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void testAksProxyValidatesSniOnAutomaticPostRetry(boolean matchingServerName) throws Exception {
        String resource = matchingServerName ? "/test-aks-cert.p12" : "/test-aks-cert-no-sni.p12";
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream stream = getClass().getResourceAsStream(resource)) {
            assertNotNull(stream);
            keyStore.load(stream, "password".toCharArray());
        }
        X509Certificate retryCertificate = (X509Certificate) keyStore.getCertificate("test-cert");
        retrySslContext = SslContextBuilder
            .forServer((PrivateKey) keyStore.getKey("test-cert", "password".toCharArray()), retryCertificate)
            .sslProvider(SslProvider.JDK)
            .build();
        closeFirstTokenRequest = true;
        String trustedCertificates = caCertPemData + toPemFormat(retryCertificate);
        CustomTokenProxyHttpClient client = new CustomTokenProxyHttpClient(new ProxyConfig(new URL(serverBaseUrl),
            AKS_SNI_NAME, null, trustedCertificates.getBytes(StandardCharsets.UTF_8)));

        if (matchingServerName) {
            sendProxyRequest(client);
            assertEquals(2, tokenRequestCount.get(), "A retry with a matching TLS name must still succeed");
        } else {
            assertThrows(RuntimeException.class, () -> sendProxyRequest(client));
            assertEquals(1, tokenRequestCount.get(),
                "The retried assertion must not reach a trusted server with the wrong TLS name");
        }
        assertTrue(connectionCount.get() >= 2, "The JDK must establish a new TLS connection for the retry");
    }

    @ParameterizedTest
    @CsvSource({
        "proxy.example.com, true",
        "PROXY.EXAMPLE.COM, true",
        "example.com, false",
        "nested.proxy.example.com, false",
        "proxy.example.net, false" })
    public void testAksProxyValidatesWildcardSniBeforeSendingRequest(String serverName, boolean matches)
        throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream stream = getClass().getResourceAsStream("/test-aks-cert-wildcard.p12")) {
            assertNotNull(stream);
            keyStore.load(stream, "password".toCharArray());
        }
        X509Certificate wildcardCertificate = (X509Certificate) keyStore.getCertificate("test-cert");
        List<String> dnsNames = fetchCertificateSAN(wildcardCertificate);
        assertTrue(dnsNames.contains("localhost"));
        assertTrue(dnsNames.contains("*.example.com"));
        startNettyHttpsServer((PrivateKey) keyStore.getKey("test-cert", "password".toCharArray()), wildcardCertificate);

        // The localhost SAN allows URL-host verification to succeed independently of the configured SNI name.
        CustomTokenProxyHttpClient client = new CustomTokenProxyHttpClient(new ProxyConfig(new URL(serverBaseUrl),
            serverName, null, toPemFormat(wildcardCertificate).getBytes(StandardCharsets.UTF_8)));

        if (matches) {
            sendProxyRequest(client);
            assertEquals(serverName, lastServerName);
            assertEquals(1, tokenRequestCount.get());
        } else {
            assertThrows(RuntimeException.class, () -> sendProxyRequest(client));
            assertEquals(0, tokenRequestCount.get(), "The assertion must not be sent for an unrelated TLS name");
        }
    }

    @Test
    public void testAksProxyWithInvalidTokenFile() {
        Path nonExistentTokenFile = tempDir.resolve("non-existent-token.jwt");

        Configuration configuration = TestUtils
            .createTestConfiguration(new TestConfigurationSource().put("AZURE_KUBERNETES_TOKEN_PROXY", serverBaseUrl)
                .put("AZURE_KUBERNETES_CA_FILE", caCertFilePath.toString())
                .put("AZURE_KUBERNETES_SNI_NAME", AKS_SNI_NAME)
                .put(Configuration.PROPERTY_AZURE_CLIENT_ID, TEST_CLIENT_ID)
                .put(Configuration.PROPERTY_AZURE_TENANT_ID, TEST_TENANT_ID)
                .put("AZURE_FEDERATED_TOKEN_FILE", nonExistentTokenFile.toString()));

        WorkloadIdentityCredential credential = new WorkloadIdentityCredentialBuilder().tenantId(TEST_TENANT_ID)
            .clientId(TEST_CLIENT_ID)
            .tokenFilePath(nonExistentTokenFile.toString())
            .configuration(configuration)
            .authorityHost(serverBaseUrl)
            .enableAzureProxy()
            .disableInstanceDiscovery()
            .build();

        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com/.default");

        Exception exception = assertThrows(Exception.class, () -> credential.getTokenSync(request));

        assertNotNull(exception, "Should throw exception when token file doesn't exist");
        assertEquals(0, tokenRequestCount.get(), "No token request should reach the server with invalid token file");
    }

    @Test
    public void testAksProxyWithInvalidCaCertificate() throws Exception {
        String invalidCertData = "-----BEGIN CERTIFICATE-----\nINVALID_BASE64_DATA\n-----END CERTIFICATE-----";
        Path invalidCaCertFile = tempDir.resolve("invalid-ca.pem");
        Files.write(invalidCaCertFile, invalidCertData.getBytes(StandardCharsets.UTF_8));

        Configuration configuration = TestUtils
            .createTestConfiguration(new TestConfigurationSource().put("AZURE_KUBERNETES_TOKEN_PROXY", serverBaseUrl)
                .put("AZURE_KUBERNETES_CA_FILE", invalidCaCertFile.toString())
                .put("AZURE_KUBERNETES_SNI_NAME", AKS_SNI_NAME)
                .put(Configuration.PROPERTY_AZURE_CLIENT_ID, TEST_CLIENT_ID)
                .put(Configuration.PROPERTY_AZURE_TENANT_ID, TEST_TENANT_ID)
                .put("AZURE_FEDERATED_TOKEN_FILE", tokenFilePath.toString()));

        Exception exception = assertThrows(Exception.class, () -> {
            WorkloadIdentityCredential credential = new WorkloadIdentityCredentialBuilder().tenantId(TEST_TENANT_ID)
                .clientId(TEST_CLIENT_ID)
                .tokenFilePath(tokenFilePath.toString())
                .configuration(configuration)
                .authorityHost(serverBaseUrl)
                .enableAzureProxy()
                .disableInstanceDiscovery()
                .build();

            TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com/.default");
            credential.getTokenSync(request);
        });

        assertNotNull(exception, "Should throw exception when CA certificate is invalid");
        assertEquals(0, tokenRequestCount.get(), "No token request should succeed with invalid CA certificate");
    }

    @Test
    public void testAksProxyWithHttpScheme() {
        String httpProxyUrl = serverBaseUrl.replace("https://", "http://");

        Configuration configuration = TestUtils
            .createTestConfiguration(new TestConfigurationSource().put("AZURE_KUBERNETES_TOKEN_PROXY", httpProxyUrl)
                .put("AZURE_KUBERNETES_CA_FILE", caCertFilePath.toString())
                .put("AZURE_KUBERNETES_SNI_NAME", AKS_SNI_NAME)
                .put(Configuration.PROPERTY_AZURE_CLIENT_ID, TEST_CLIENT_ID)
                .put(Configuration.PROPERTY_AZURE_TENANT_ID, TEST_TENANT_ID)
                .put("AZURE_FEDERATED_TOKEN_FILE", tokenFilePath.toString()));

        Exception exception = assertThrows(Exception.class, () -> {
            WorkloadIdentityCredential credential = new WorkloadIdentityCredentialBuilder().tenantId(TEST_TENANT_ID)
                .clientId(TEST_CLIENT_ID)
                .tokenFilePath(tokenFilePath.toString())
                .configuration(configuration)
                .authorityHost(httpProxyUrl)
                .enableAzureProxy()
                .disableInstanceDiscovery()
                .build();

            TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com/.default");
            credential.getTokenSync(request);
        });

        assertNotNull(exception, "Should throw exception when proxy URL uses HTTP instead of HTTPS");
        assertEquals(0, tokenRequestCount.get(), "No token request should be made with HTTP proxy URL");
    }

    @Test
    public void testAksProxyWithMalformedUrl() {
        String malformedUrl = "not-a-valid-url-at-all";

        Configuration configuration = TestUtils
            .createTestConfiguration(new TestConfigurationSource().put("AZURE_KUBERNETES_TOKEN_PROXY", malformedUrl)
                .put("AZURE_KUBERNETES_CA_FILE", caCertFilePath.toString())
                .put("AZURE_KUBERNETES_SNI_NAME", AKS_SNI_NAME)
                .put(Configuration.PROPERTY_AZURE_CLIENT_ID, TEST_CLIENT_ID)
                .put(Configuration.PROPERTY_AZURE_TENANT_ID, TEST_TENANT_ID)
                .put("AZURE_FEDERATED_TOKEN_FILE", tokenFilePath.toString()));

        Exception exception = assertThrows(Exception.class, () -> {
            WorkloadIdentityCredential credential = new WorkloadIdentityCredentialBuilder().tenantId(TEST_TENANT_ID)
                .clientId(TEST_CLIENT_ID)
                .tokenFilePath(tokenFilePath.toString())
                .configuration(configuration)
                .authorityHost(malformedUrl)
                .enableAzureProxy()
                .disableInstanceDiscovery()
                .build();

            TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com/.default");
            credential.getTokenSync(request);
        });

        assertNotNull(exception, "Should throw exception when proxy URL is malformed");
        assertEquals(0, tokenRequestCount.get(), "No token request should be made with malformed proxy URL");
    }

    @Test
    public void testAksProxyUnreachable() {
        String unreachableProxyUrl = "https://localhost:19999";

        Configuration configuration = TestUtils.createTestConfiguration(
            new TestConfigurationSource().put("AZURE_KUBERNETES_TOKEN_PROXY", unreachableProxyUrl)
                .put("AZURE_KUBERNETES_CA_FILE", caCertFilePath.toString())
                .put("AZURE_KUBERNETES_SNI_NAME", AKS_SNI_NAME)
                .put(Configuration.PROPERTY_AZURE_CLIENT_ID, TEST_CLIENT_ID)
                .put(Configuration.PROPERTY_AZURE_TENANT_ID, TEST_TENANT_ID)
                .put("AZURE_FEDERATED_TOKEN_FILE", tokenFilePath.toString()));

        WorkloadIdentityCredential credential = new WorkloadIdentityCredentialBuilder().tenantId(TEST_TENANT_ID)
            .clientId(TEST_CLIENT_ID)
            .tokenFilePath(tokenFilePath.toString())
            .configuration(configuration)
            .authorityHost(unreachableProxyUrl)
            .enableAzureProxy()
            .disableInstanceDiscovery()
            .build();

        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com/.default");

        Exception exception = assertThrows(Exception.class, () -> credential.getTokenSync(request));

        assertNotNull(exception, "Should throw exception when proxy server is unreachable");
        assertEquals(0, tokenRequestCount.get(), "No token request should succeed when proxy is unreachable");
    }

    @Test
    public void testAksProxyWithEmptyTokenFile() throws Exception {
        Path emptyTokenFile = tempDir.resolve("empty-token.jwt");
        Files.write(emptyTokenFile, new byte[0]);

        Configuration configuration = TestUtils
            .createTestConfiguration(new TestConfigurationSource().put("AZURE_KUBERNETES_TOKEN_PROXY", serverBaseUrl)
                .put("AZURE_KUBERNETES_CA_FILE", caCertFilePath.toString())
                .put("AZURE_KUBERNETES_SNI_NAME", AKS_SNI_NAME)
                .put(Configuration.PROPERTY_AZURE_CLIENT_ID, TEST_CLIENT_ID)
                .put(Configuration.PROPERTY_AZURE_TENANT_ID, TEST_TENANT_ID)
                .put("AZURE_FEDERATED_TOKEN_FILE", emptyTokenFile.toString()));

        WorkloadIdentityCredential credential = new WorkloadIdentityCredentialBuilder().tenantId(TEST_TENANT_ID)
            .clientId(TEST_CLIENT_ID)
            .tokenFilePath(emptyTokenFile.toString())
            .configuration(configuration)
            .authorityHost(serverBaseUrl)
            .enableAzureProxy()
            .disableInstanceDiscovery()
            .build();

        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com/.default");

        Exception exception = assertThrows(Exception.class, () -> credential.getTokenSync(request));

        assertNotNull(exception, "Should throw exception when token file is empty");
        assertEquals(0, tokenRequestCount.get(), "No token request should be made with empty token file");
    }

    @Test
    public void testAksProxyWithUrlEncodedCharactersInPath() throws Exception {
        String encodedPath = "/api%2Fv1/token";
        String proxyUrlWithEncoding = serverBaseUrl + encodedPath;

        Configuration configuration = TestUtils.createTestConfiguration(
            new TestConfigurationSource().put("AZURE_KUBERNETES_TOKEN_PROXY", proxyUrlWithEncoding)
                .put("AZURE_KUBERNETES_CA_FILE", caCertFilePath.toString())
                .put("AZURE_KUBERNETES_SNI_NAME", AKS_SNI_NAME)
                .put(Configuration.PROPERTY_AZURE_CLIENT_ID, TEST_CLIENT_ID)
                .put(Configuration.PROPERTY_AZURE_TENANT_ID, TEST_TENANT_ID)
                .put("AZURE_FEDERATED_TOKEN_FILE", tokenFilePath.toString()));

        WorkloadIdentityCredential credential = new WorkloadIdentityCredentialBuilder().tenantId(TEST_TENANT_ID)
            .clientId(TEST_CLIENT_ID)
            .tokenFilePath(tokenFilePath.toString())
            .configuration(configuration)
            .authorityHost(proxyUrlWithEncoding)
            .enableAzureProxy()
            .disableInstanceDiscovery()
            .build();

        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com/.default");

        AccessToken token = credential.getTokenSync(request);

        assertNotNull(token, "Token should not be null");
        assertEquals(MOCK_ACCESS_TOKEN, token.getToken(), "Token value should match mock token");
        assertTrue(token.getExpiresAt().isAfter(OffsetDateTime.now()), "Token should not be expired");
        assertEquals(1, tokenRequestCount.get(), "Token request should be made once");
    }

    @Test
    public void testAksProxyWithCaFileButNoSni() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream keyStoreStream = getClass().getResourceAsStream("/test-aks-cert-no-sni.p12")) {
            assertNotNull(keyStoreStream, "Mismatched certificate not found: /test-aks-cert-no-sni.p12");
            keyStore.load(keyStoreStream, "password".toCharArray());
        }

        PrivateKey privateKey = (PrivateKey) keyStore.getKey("test-cert", "password".toCharArray());
        X509Certificate certificate = (X509Certificate) keyStore.getCertificate("test-cert");

        String noSniCertPemData = toPemFormat(certificate);
        Path noSniCaCertFile = tempDir.resolve("ca-cert-no-sni.pem");
        Files.write(noSniCaCertFile, noSniCertPemData.getBytes(StandardCharsets.UTF_8));

        startNettyHttpsServer(privateKey, certificate);

        Configuration configuration = TestUtils
            .createTestConfiguration(new TestConfigurationSource().put("AZURE_KUBERNETES_TOKEN_PROXY", serverBaseUrl)
                .put("AZURE_KUBERNETES_CA_FILE", noSniCaCertFile.toString())
                .put(Configuration.PROPERTY_AZURE_CLIENT_ID, TEST_CLIENT_ID)
                .put(Configuration.PROPERTY_AZURE_TENANT_ID, TEST_TENANT_ID)
                .put("AZURE_FEDERATED_TOKEN_FILE", tokenFilePath.toString()));

        WorkloadIdentityCredential credential = new WorkloadIdentityCredentialBuilder().tenantId(TEST_TENANT_ID)
            .clientId(TEST_CLIENT_ID)
            .tokenFilePath(tokenFilePath.toString())
            .configuration(configuration)
            .authorityHost(serverBaseUrl)
            .enableAzureProxy()
            .disableInstanceDiscovery()
            .build();

        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com/.default");

        AccessToken token = credential.getTokenSync(request);

        assertNotNull(token, "Access token should not be null");
        assertEquals(MOCK_ACCESS_TOKEN, token.getToken(), "Token value should match mock response");
        assertEquals(1, tokenRequestCount.get(), "Server should have received exactly one token request");
    }

    @Test
    public void testAksProxyWithMismatchedSniAndCertificate() throws Exception {
        KeyStore keyStore = KeyStore.getInstance("PKCS12");
        try (InputStream keyStoreStream = getClass().getResourceAsStream("/test-aks-cert-mismatch.p12")) {
            assertNotNull(keyStoreStream, "Mismatched certificate not found: /test-aks-cert-mismatch.p12");
            keyStore.load(keyStoreStream, "password".toCharArray());
        }

        PrivateKey privateKey = (PrivateKey) keyStore.getKey("test-cert", "password".toCharArray());
        X509Certificate certificate = (X509Certificate) keyStore.getCertificate("test-cert");

        List<String> dnsNames = fetchCertificateSAN(certificate);

        assertTrue(dnsNames.contains(MISMATCHED_CERT_SAN),
            "Certificate should contain DNS name 'wrong-hostname.example.com'");
        assertFalse(dnsNames.contains(AKS_SNI_NAME),
            "Certificate should NOT contain the SNI name 'test-aks-proxy.ests.aks'");

        String mismatchCertPemData = toPemFormat(certificate);
        Path mismatchCaCertFile = tempDir.resolve("ca-cert-mismatch.pem");
        Files.write(mismatchCaCertFile, mismatchCertPemData.getBytes(StandardCharsets.UTF_8));

        startNettyHttpsServer(privateKey, certificate);

        Configuration configuration = TestUtils
            .createTestConfiguration(new TestConfigurationSource().put("AZURE_KUBERNETES_TOKEN_PROXY", serverBaseUrl)
                .put("AZURE_KUBERNETES_CA_FILE", mismatchCaCertFile.toString())
                .put("AZURE_KUBERNETES_SNI_NAME", AKS_SNI_NAME)
                .put(Configuration.PROPERTY_AZURE_CLIENT_ID, TEST_CLIENT_ID)
                .put(Configuration.PROPERTY_AZURE_TENANT_ID, TEST_TENANT_ID)
                .put("AZURE_FEDERATED_TOKEN_FILE", tokenFilePath.toString()));

        WorkloadIdentityCredential credential = new WorkloadIdentityCredentialBuilder().tenantId(TEST_TENANT_ID)
            .clientId(TEST_CLIENT_ID)
            .tokenFilePath(tokenFilePath.toString())
            .configuration(configuration)
            .authorityHost(serverBaseUrl)
            .enableAzureProxy()
            .disableInstanceDiscovery()
            .build();

        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com/.default");

        Exception exception = assertThrows(Exception.class, () -> credential.getTokenSync(request));

        assertNotNull(exception, "Should throw exception when SNI doesn't match certificate SAN");

        String exceptionMessage = exception.toString().toLowerCase();
        assertTrue(exceptionMessage.contains("failed to create connection"),
            "Exception should be connection failure related, got: " + exception.getMessage());

        assertEquals(0, tokenRequestCount.get(), "No token request should succeed when SNI doesn't match certificate");
    }

    private List<String> fetchCertificateSAN(X509Certificate certificate) throws CertificateParsingException {
        Collection<List<?>> sanCollection = certificate.getSubjectAlternativeNames();

        List<String> dnsNames = new ArrayList<>();
        for (List<?> san : sanCollection) {
            if (san.size() >= 2 && san.get(0).equals(2)) {
                dnsNames.add((String) san.get(1));
            }
        }
        return dnsNames;
    }

    private void startNettyHttpsServer(PrivateKey privateKey, X509Certificate certificate) throws Exception {
        cleanup();
        SslContext sslContext
            = SslContextBuilder.forServer(privateKey, certificate).sslProvider(SslProvider.JDK).build();

        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup(2);

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    ChannelPipeline pipeline = ch.pipeline();

                    SslContext connectionContext = connectionCount.incrementAndGet() > 1 && retrySslContext != null
                        ? retrySslContext
                        : sslContext;
                    SslHandler sslHandler = connectionContext.newHandler(ch.alloc());

                    pipeline.addLast(sslHandler);
                    pipeline.addLast(new HttpServerCodec());
                    pipeline.addLast(new HttpObjectAggregator(65536));
                    pipeline.addLast(new HttpRequestHandler());
                }
            });

        serverChannel = bootstrap.bind("localhost", 0).sync().channel();
        InetSocketAddress socketAddress = (InetSocketAddress) serverChannel.localAddress();
        int port = socketAddress.getPort();
        serverBaseUrl = "https://localhost:" + port;
    }

    private class HttpRequestHandler extends SimpleChannelInboundHandler<FullHttpRequest> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
            String uri = request.uri();
            lastRequestUri = uri;
            lastRequestBody = request.content().toString(StandardCharsets.UTF_8);
            lastRequestMethod = request.method().name();
            lastCustomHeader = request.headers().get("x-proxy-test");
            SSLSession session = ctx.pipeline().get(SslHandler.class).engine().getSession();
            if (session instanceof ExtendedSSLSession) {
                List<SNIServerName> serverNames = ((ExtendedSSLSession) session).getRequestedServerNames();
                if (!serverNames.isEmpty()) {
                    lastServerName = new String(serverNames.get(0).getEncoded(), StandardCharsets.US_ASCII);
                }
            }
            String responseBody;

            // MSAL can request instance metadata before the token endpoint when its shared cache is cold.
            if (uri.contains("/common/discovery/instance")) {
                metadataRequestCount.incrementAndGet();
                responseBody
                    = String.format("{\"tenant_discovery_endpoint\":\"https://login.microsoftonline.com/%s/v2.0/"
                        + ".well-known/openid-configuration\",\"api-version\":\"1.1\",\"metadata\":["
                        + "{\"preferred_network\":\"login.microsoftonline.com\","
                        + "\"preferred_cache\":\"login.windows.net\",\"aliases\":[\"login.microsoftonline.com\","
                        + "\"login.windows.net\",\"login.microsoft.com\",\"sts.windows.net\"]}]}", TEST_TENANT_ID);
            } else if (uri.contains("/.well-known/openid-configuration")) {
                metadataRequestCount.incrementAndGet();
                String base = serverBaseUrl + "/" + TEST_TENANT_ID;
                responseBody = String
                    .format("{%n" + "  \"token_endpoint\": \"%s/oauth2/v2.0/token\",%n" + "  \"issuer\": \"%s/v2.0\",%n"
                        + "  \"authorization_endpoint\": \"%s/oauth2/v2.0/authorize\"%n" + "}", base, base, base);
            } else {
                tokenRequestCount.incrementAndGet();
                tokenRequestUris.add(uri);
                if (closeFirstTokenRequest && tokenRequestCount.get() == 1) {
                    ctx.close();
                    return;
                }
                responseBody = String.format(
                    "{\"token_type\":\"Bearer\",\"expires_in\":3600,\"ext_expires_in\":3600,\"access_token\":\"%s\"}",
                    MOCK_ACCESS_TOKEN);
            }

            if (responseBodyOverride != null) {
                responseBody = responseBodyOverride;
            }
            HttpResponseStatus status = throttleFirstRequest && tokenRequestCount.get() == 1
                ? HttpResponseStatus.TOO_MANY_REQUESTS
                : responseStatus;
            ByteBuf content = Unpooled.copiedBuffer(responseBody, CharsetUtil.UTF_8);
            FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, content);
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());
            if (redirectLocation != null) {
                response.headers().set(HttpHeaderNames.LOCATION, redirectLocation);
            }
            if (status.equals(HttpResponseStatus.TOO_MANY_REQUESTS)) {
                response.headers().set(HttpHeaderNames.RETRY_AFTER, "0");
            }

            ctx.writeAndFlush(response);
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }

    private String toPemFormat(X509Certificate certificate) throws Exception {
        Base64.Encoder encoder = Base64.getMimeEncoder(64, "\n".getBytes(StandardCharsets.UTF_8));
        byte[] encoded = encoder.encode(certificate.getEncoded());
        return "-----BEGIN CERTIFICATE-----\n" + new String(encoded, StandardCharsets.UTF_8)
            + "\n-----END CERTIFICATE-----\n";
    }

    private void sendProxyRequest(CustomTokenProxyHttpClient client) {
        HttpRequest request = new HttpRequest(HttpMethod.POST,
            AzureAuthorityHosts.AZURE_PUBLIC_CLOUD + TEST_TENANT_ID + "/oauth2/v2.0/token").setBody("request");
        try (HttpResponse response = client.sendSync(request, Context.NONE)) {
            assertEquals(200, response.getStatusCode());
            assertFalse(response.getBodyAsBinaryData().toString().isEmpty());
        }
    }

    private Configuration proxyConfiguration() {
        return proxyConfiguration(serverBaseUrl);
    }

    private Configuration proxyConfiguration(String proxyUrl) {
        return TestUtils
            .createTestConfiguration(new TestConfigurationSource().put("AZURE_KUBERNETES_TOKEN_PROXY", proxyUrl)
                .put("AZURE_KUBERNETES_CA_FILE", caCertFilePath.toString())
                .put("AZURE_KUBERNETES_SNI_NAME", AKS_SNI_NAME)
                .put(Configuration.PROPERTY_AZURE_CLIENT_ID, TEST_CLIENT_ID)
                .put(Configuration.PROPERTY_AZURE_TENANT_ID, TEST_TENANT_ID)
                .put("AZURE_FEDERATED_TOKEN_FILE", tokenFilePath.toString()));
    }

    private TokenRequestContext tokenRequest() {
        return new TokenRequestContext().addScopes("https://management.azure.com/.default");
    }

    private Map<String, String> parseForm(String body) throws Exception {
        Map<String, String> fields = new HashMap<>();
        for (String field : body.split("&")) {
            String[] pair = field.split("=", 2);
            fields.put(URLDecoder.decode(pair[0], "UTF-8"), URLDecoder.decode(pair[1], "UTF-8"));
        }
        return fields;
    }

    private String createMockFederatedToken() {
        String header = Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString("{\"alg\":\"RS256\",\"typ\":\"JWT\"}".getBytes(StandardCharsets.UTF_8));

        long exp = System.currentTimeMillis() / 1000 + 3600;
        String payload = Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(String.format(
                "{\"aud\":\"api://AzureADTokenExchange\",\"exp\":%d,\"iss\":\"kubernetes.io/serviceaccount\",\"sub\":\"system:serviceaccount:default:workload-identity-sa\"}",
                exp).getBytes(StandardCharsets.UTF_8));

        String signature
            = Base64.getUrlEncoder().withoutPadding().encodeToString("mock-signature".getBytes(StandardCharsets.UTF_8));

        return header + "." + payload + "." + signature;
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.core.test.utils.TestConfigurationSource;
import com.azure.core.util.Configuration;
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
import reactor.test.StepVerifier;

import javax.net.ssl.SSLEngine;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertificateParsingException;
import java.security.cert.X509Certificate;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

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
        }
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
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
            .enableAzureTokenProxy()
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
            .enableAzureTokenProxy()
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
            .enableAzureTokenProxy()
            .disableInstanceDiscovery()
            .build();

        TokenRequestContext request = new TokenRequestContext().addScopes("https://management.azure.com/.default");

        AccessToken token = credential.getTokenSync(request);

        assertNotNull(token, "Access token should not be null");
        assertEquals(MOCK_ACCESS_TOKEN, token.getToken(), "Token value should match mock response");
        assertEquals(1, tokenRequestCount.get(), "Server should have received exactly one token request");
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
            .enableAzureTokenProxy()
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
                .enableAzureTokenProxy()
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
                .enableAzureTokenProxy()
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
                .enableAzureTokenProxy()
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
            .enableAzureTokenProxy()
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
            .enableAzureTokenProxy()
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
            .enableAzureTokenProxy()
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
            .enableAzureTokenProxy()
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
            .enableAzureTokenProxy()
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
        SslContext sslContext
            = SslContextBuilder.forServer(privateKey, certificate).sslProvider(SslProvider.OPENSSL).build();

        bossGroup = new NioEventLoopGroup(1);
        workerGroup = new NioEventLoopGroup();

        ServerBootstrap bootstrap = new ServerBootstrap();
        bootstrap.group(bossGroup, workerGroup)
            .channel(NioServerSocketChannel.class)
            .childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) {
                    ChannelPipeline pipeline = ch.pipeline();

                    SslHandler sslHandler = sslContext.newHandler(ch.alloc());

                    sslHandler.handshakeFuture().addListener(future -> {
                        if (future.isSuccess()) {
                            SSLEngine engine = sslHandler.engine();
                        }
                    });

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
            String responseBody;

            if (uri.contains("/.well-known/openid-configuration")) {
                metadataRequestCount.incrementAndGet();
                String base = serverBaseUrl + "/" + TEST_TENANT_ID;
                responseBody = String
                    .format("{%n" + "  \"token_endpoint\": \"%s/oauth2/v2.0/token\",%n" + "  \"issuer\": \"%s/v2.0\",%n"
                        + "  \"authorization_endpoint\": \"%s/oauth2/v2.0/authorize\"%n" + "}", base, base, base);
            } else {
                tokenRequestCount.incrementAndGet();
                responseBody = String.format(
                    "{\"token_type\":\"Bearer\",\"expires_in\":3600,\"ext_expires_in\":3600,\"access_token\":\"%s\"}",
                    MOCK_ACCESS_TOKEN);
            }

            ByteBuf content = Unpooled.copiedBuffer(responseBody, CharsetUtil.UTF_8);
            FullHttpResponse response
                = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK, content);
            response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json");
            response.headers().set(HttpHeaderNames.CONTENT_LENGTH, content.readableBytes());

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

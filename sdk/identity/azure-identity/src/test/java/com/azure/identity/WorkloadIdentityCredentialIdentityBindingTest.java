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
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.GeneralName;
import org.bouncycastle.asn1.x509.GeneralNames;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.net.ssl.SSLEngine;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.Security;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * E2E test for WorkloadIdentityCredential with AKS Custom Token Proxy using Netty mock server.
 *
 */
public class WorkloadIdentityCredentialIdentityBindingTest {

    private static final String AKS_SNI_NAME = "test-aks-proxy.ests.aks";

    private static final String TEST_CLIENT_ID = "test-client-id";
    private static final String TEST_TENANT_ID = "test-tenant-id";
    private static final String MOCK_ACCESS_TOKEN = "mock_access_token_from_aks_proxy";

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

    @BeforeEach
    public void setUp() throws Exception {
        tokenRequestCount = new AtomicInteger(0);
        metadataRequestCount = new AtomicInteger(0);

        // Generate key pair and certificate
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(2048);
        KeyPair keyPair = keyPairGenerator.generateKeyPair();

        // Generate self-signed certificate with both localhost and AKS SNI name
        X509Certificate certificate = generateCertificateWithAksSni(keyPair, AKS_SNI_NAME);

        // Convert certificate to PEM format
        caCertPemData = toPemFormat(certificate);

        // Write CA certificate to file
        caCertFilePath = tempDir.resolve("ca-cert.pem");
        Files.write(caCertFilePath, caCertPemData.getBytes(StandardCharsets.UTF_8));

        // Create mock federated token file
        tokenFilePath = tempDir.resolve("token.jwt");
        String mockJwt = createMockFederatedToken();
        Files.write(tokenFilePath, mockJwt.getBytes(StandardCharsets.UTF_8));

        // Start Netty HTTPS server
        startNettyHttpsServer(keyPair.getPrivate(), certificate);
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
    public void testAksProxyWithCaFile() {
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

        AccessToken token = credential.getTokenSync(request);

        assertNotNull(token, "Access token should not be null");
        assertEquals(MOCK_ACCESS_TOKEN, token.getToken(), "Token value should match mock response");
        assertTrue(token.getExpiresAt().isAfter(OffsetDateTime.now()), "Token should not be expired");
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
    public void testAksProxyWithCaFileButNoSni() {
        Configuration configuration = TestUtils
            .createTestConfiguration(new TestConfigurationSource().put("AZURE_KUBERNETES_TOKEN_PROXY", serverBaseUrl)
                .put("AZURE_KUBERNETES_CA_FILE", caCertFilePath.toString())
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

    private X509Certificate generateCertificateWithAksSni(KeyPair keyPair, String aksSniName) throws Exception {
        if (Security.getProvider("BC") == null) {
            Security.addProvider(new BouncyCastleProvider());
        }

        long now = System.currentTimeMillis();
        Date notBefore = new Date(now - 1000L * 60 * 60);
        Date notAfter = new Date(now + 1000L * 60 * 60 * 24 * 365);
        BigInteger serial = BigInteger.valueOf(now);

        X500Name subject = new X500Name("CN=AKS-Proxy-Test");
        SubjectPublicKeyInfo publicKeyInfo = SubjectPublicKeyInfo.getInstance(keyPair.getPublic().getEncoded());

        X509v3CertificateBuilder certBuilder
            = new X509v3CertificateBuilder(subject, serial, notBefore, notAfter, subject, publicKeyInfo);

        GeneralName[] sans = new GeneralName[] {
            new GeneralName(GeneralName.dNSName, "localhost"),
            new GeneralName(GeneralName.dNSName, new org.bouncycastle.asn1.DERIA5String(aksSniName)) };
        GeneralNames subjectAltNames = new GeneralNames(sans);
        certBuilder.addExtension(Extension.subjectAlternativeName, false, subjectAltNames);

        ContentSigner signer
            = new JcaContentSignerBuilder("SHA256withRSA").setProvider("BC").build(keyPair.getPrivate());

        X509CertificateHolder certHolder = certBuilder.build(signer);

        CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
        try (InputStream in = new ByteArrayInputStream(certHolder.getEncoded())) {
            return (X509Certificate) certFactory.generateCertificate(in);
        }
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
                            System.out.println("OpenSSL handshake successful.");
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

        System.out.println("Netty HTTPS server (OpenSSL) started at: " + serverBaseUrl);
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

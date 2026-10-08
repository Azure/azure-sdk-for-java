// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.http;

import com.azure.cosmos.Http2ConnectionConfig;
import com.azure.cosmos.implementation.Configs;
import com.azure.cosmos.implementation.directconnectivity.WebExceptionUtility;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.DefaultHttp2HeadersFrame;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2FrameCodecBuilder;
import io.netty.handler.codec.http2.Http2FrameCodec;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2HeadersFrame;
import io.netty.handler.codec.http2.Http2MultiplexHandler;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.ApplicationProtocolNegotiationHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.util.AsciiString;
import io.netty.util.ReferenceCountUtil;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;
import reactor.netty.http.Http2SslContextSpec;
import reactor.netty.http.HttpProtocol;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

import javax.net.ssl.KeyManagerFactory;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyStore;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class CosmosHttp2HeaderCompatibilityTest {
    private static final String HEADER = "x-ms-serviceversion";
    private static final Duration TIMEOUT = Duration.ofSeconds(15);
    private Path certificateDirectory;
    private Path certificateStore;
    private KeyManagerFactory keys;

    @BeforeClass(groups = "unit")
    public void createLoopbackCertificate() throws Exception {
        certificateDirectory = Files.createTempDirectory("cosmos-h2-header-test-");
        certificateStore = certificateDirectory.resolve("localhost.p12");
        String keytool = Paths.get(System.getProperty("java.home"), "bin",
            System.getProperty("os.name").startsWith("Windows") ? "keytool.exe" : "keytool").toString();
        Process process = new ProcessBuilder(keytool, "-genkeypair", "-alias", "localhost", "-keyalg", "RSA",
            "-keysize", "2048", "-storetype", "PKCS12", "-keystore", certificateStore.toString(),
            "-storepass", "loopback-test-only", "-dname", "CN=localhost", "-validity", "1", "-noprompt")
            .inheritIO().start();
        if (!process.waitFor(30, TimeUnit.SECONDS)) {
            process.destroyForcibly();
            throw new IllegalStateException("Loopback certificate generation timed out");
        }
        assertThat(process.exitValue()).isZero();
        KeyStore store = KeyStore.getInstance("PKCS12");
        char[] password = "loopback-test-only".toCharArray();
        try (InputStream input = Files.newInputStream(certificateStore)) {
            store.load(input, password);
        }
        keys = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        keys.init(store, password);
    }

    @Test(groups = "unit")
    public void decoderRetainsHeaderListLimit() {
        ByteBuf block = headerBlock(":status", "200", HEADER, " version ");
        try {
            assertThatThrownBy(() -> new CosmosHttp2HeadersDecoder(32).decodeHeaders(1, block))
                .isInstanceOf(Http2Exception.HeaderListSizeException.class);
        } finally {
            block.release();
        }
    }

    @Test(groups = "unit")
    public void customDecoderRetainsNameValidation() {
        ByteBuf block = headerBlock(":status", "200", "X-MS-ServiceVersion", "version");
        try {
            assertThatThrownBy(() -> new CosmosHttp2HeadersDecoder(8192).decodeHeaders(1, block))
                .isInstanceOf(Http2Exception.StreamException.class);
        } finally {
            block.release();
        }
    }

    @DataProvider(name = "indexedValues")
    public Object[][] indexedValues() {
        return new Object[][] { { "version" }, { " version " } };
    }

    @Test(groups = "unit", dataProvider = "indexedValues")
    public void decoderPreservesIndexedValuesWithoutCopyingArrays(String value) throws Exception {
        CosmosHttp2HeadersDecoder decoder = new CosmosHttp2HeadersDecoder(8192);
        ByteBuf first = headerBlock(HEADER, value);
        ByteBuf second = Unpooled.buffer(1).writeByte(0xbe);
        first.setByte(0, 0x40);
        try {
            Http2Headers a = decoder.decodeHeaders(1, first);
            Http2Headers b = decoder.decodeHeaders(3, second);
            assertThat(a.get(HEADER).toString()).isEqualTo("version");
            assertThat(b.get(HEADER).toString()).isEqualTo("version");
            assertThat(((AsciiString) b.get(HEADER)).array())
                .isSameAs(((AsciiString) a.get(HEADER)).array());
            if (value.equals("version")) {
                assertThat(b.get(HEADER)).isSameAs(a.get(HEADER));
            }
        } finally {
            first.release();
            second.release();
        }
    }

    @AfterClass(groups = "unit", alwaysRun = true)
    public void deleteLoopbackCertificate() throws Exception {
        if (certificateStore != null) {
            Files.deleteIfExists(certificateStore);
        }
        if (certificateDirectory != null) {
            Files.deleteIfExists(certificateDirectory);
        }
    }

    @DataProvider(name = "padding")
    public Object[][] padding() {
        return new Object[][] {
            { "version", "version" }, { " version", "version" }, { "version ", "version" },
            { "\t version \t", "version" }, { " \t ", "" }, { "v er\tsion", "v er\tsion" }
        };
    }

    @Test(groups = "unit", dataProvider = "padding")
    public void decoderNormalizesOnlyServiceVersionPadding(String value, String expected) throws Exception {
        ByteBuf block = headerBlock(":status", "200", HEADER, value);
        try {
            Http2Headers headers = new CosmosHttp2HeadersDecoder(8192).decodeHeaders(1, block);
            assertThat(headers.get(HEADER).toString()).isEqualTo(expected);
        } finally {
            block.release();
        }
    }

    @DataProvider(name = "prohibitedValues")
    public Object[][] prohibitedValues() {
        return new Object[][] {
            { HEADER, " v\r " }, { HEADER, " v\n " }, { HEADER, " v\0 " },
            { HEADER, "\rv" }, { HEADER, "v\n" }, { HEADER, "\0" },
            { "x-other", " v " }, { "x-other", "v\r" }, { "x-other", "v\n" }, { "x-other", "v\0" }
        };
    }

    @Test(groups = "unit", dataProvider = "prohibitedValues")
    public void decoderRetainsStrictValueValidation(String name, String value) {
        ByteBuf block = headerBlock(":status", "200", name, value);
        try {
            assertThatThrownBy(() -> new CosmosHttp2HeadersDecoder(8192).decodeHeaders(1, block))
                .isInstanceOf(Http2Exception.StreamException.class).hasMessageContaining(name);
        } finally {
            block.release();
        }
    }

    @Test(groups = "unit")
    public void customDecoderRetainsOriginalPseudoHeaderWireOrderValidation() {
        ByteBuf block = headerBlock(HEADER, " version ", ":status", "200");
        try {
            assertThatThrownBy(() -> new CosmosHttp2HeadersDecoder(8192).decodeHeaders(1, block))
                .isInstanceOf(Http2Exception.StreamException.class).hasMessageContaining("after regular header");
        } finally {
            block.release();
        }
    }

    @Test(groups = "unit")
    public void releasedBootstrapValidationRemainsEnabledForBothProtocols() {
        HttpClient client = HttpClient.createFixed(clientConfig(true));
        try {
            reactor.netty.http.client.HttpClient transport = com.azure.cosmos.implementation.directconnectivity
                .ReflectionUtils.get(reactor.netty.http.client.HttpClient.class, client, "httpClient");
            assertThat(transport.configuration().decoder().validateHeaders()).isTrue();
        } finally {
            client.shutdown();
        }
    }

    @Test(groups = "unit")
    public void realTlsHttp2CosmosClientAcceptsPaddingAndReusesParent() throws Exception {
        try (LoopbackServer server = new LoopbackServer(" version ", true)) {
            HttpClient client = HttpClient.createFixed(clientConfig(true));
            try {
                reactor.netty.http.client.HttpClient baseline = reactor.netty.http.client.HttpClient.newConnection()
                    .protocol(HttpProtocol.H2)
                    .secure(spec -> spec.sslContext(Http2SslContextSpec.forClient()
                        .configure(builder -> builder.sslProvider(SslProvider.JDK)
                            .trustManager(InsecureTrustManagerFactory.INSTANCE))));
                assertThatThrownBy(() -> baseline.get().uri(server.uri()).responseSingle(
                    (response, body) -> body.thenReturn(response.responseHeaders().get(HEADER))).block(TIMEOUT))
                    .hasMessageContaining("x-ms-serviceversion");
                HttpRequest first = new HttpRequest(HttpMethod.GET, server.uri(), server.port());
                HttpRequest second = new HttpRequest(HttpMethod.GET, server.uri(), server.port());
                assertThat(readHeader(client, first)).isEqualTo("version");
                assertThat(readHeader(client, second)).isEqualTo("version");
                assertThat(first.reactorNettyRequestRecord().isHttp2()).isTrue();
                assertThat(second.reactorNettyRequestRecord().isHttp2()).isTrue();
                assertThat(second.reactorNettyRequestRecord().getParentChannelId())
                    .isEqualTo(first.reactorNettyRequestRecord().getParentChannelId());
                assertThat(second.reactorNettyRequestRecord().getChannelId())
                    .isNotEqualTo(first.reactorNettyRequestRecord().getChannelId());
            } finally {
                client.shutdown();
            }
        }
    }

    @Test(groups = "unit")
    public void realTlsCosmosClientRetainsHttp11Fallback() throws Exception {
        try (LoopbackServer server = new LoopbackServer("version", false)) {
            HttpClient client = HttpClient.createFixed(clientConfig(true));
            try {
                HttpRequest request = new HttpRequest(HttpMethod.GET, server.uri(), server.port());
                assertThat(readHeader(client, request)).isEqualTo("version");
                assertThat(request.reactorNettyRequestRecord().isHttp2()).isFalse();
            } finally {
                client.shutdown();
            }
        }
    }

    @Test(groups = "unit")
    public void realTlsCosmosClientRetainsHttp11WhenHttp2Disabled() throws Exception {
        try (LoopbackServer server = new LoopbackServer("version", false)) {
            HttpClient client = HttpClient.createFixed(clientConfig(false));
            try {
                HttpRequest request = new HttpRequest(HttpMethod.GET, server.uri(), server.port());
                assertThat(readHeader(client, request)).isEqualTo("version");
                assertThat(request.reactorNettyRequestRecord().isHttp2()).isFalse();
            } finally {
                client.shutdown();
            }
        }
    }

    @Test(groups = "unit")
    public void realTlsHttp11FallbackUsesNativeValueValidation() throws Exception {
        try (LoopbackServer server = new LoopbackServer("a\0b", false)) {
            HttpClient client = HttpClient.createFixed(clientConfig(true));
            try {
                HttpRequest request = new HttpRequest(HttpMethod.GET, server.uri(), server.port());
                assertThatThrownBy(() -> readHeader(client, request))
                    .isInstanceOf(IllegalArgumentException.class);
            } finally {
                client.shutdown();
            }
        }
    }

    @DataProvider(name = "wireControls")
    public Object[][] wireControls() {
        return new Object[][] { { " v\r\n " }, { " v\0 " } };
    }

    @Test(groups = "unit", dataProvider = "wireControls")
    public void realTlsCosmosClientRejectsProhibitedServiceVersionOctets(String value) throws Exception {
        try (LoopbackServer server = new LoopbackServer(value, true)) {
            HttpClient client = HttpClient.createFixed(clientConfig(true));
            try {
                HttpRequest request = new HttpRequest(HttpMethod.GET, server.uri(), server.port());
                assertThatThrownBy(() -> readHeader(client, request)).hasMessageContaining(HEADER);
            } finally {
                client.shutdown();
            }
        }
    }

    @DataProvider(name = "malformedWireHeaders")
    public Object[][] malformedWireHeaders() {
        return new Object[][] {
            { new String[] { HEADER, "version", ":status", "200" }, "after regular header" },
            { new String[] { ":status", "200", ":status", "201" }, "Duplicate" },
            { new String[] { ":status", "200", "X-UPPER", "value" }, "invalid header name" },
            { new String[] { ":status", "200", "connection", "close" }, "connection-specific" },
            { new String[] { ":status", "200", ":method", "GET" }, "Mix of request and response" }
        };
    }

    @Test(groups = "unit", dataProvider = "malformedWireHeaders")
    public void productionPipelineRejectsMalformedWireHeaders(String[] fields, String expectedError) throws Exception {
        try (LoopbackServer server = new LoopbackServer("unused", true, fields)) {
            HttpClient client = HttpClient.createFixed(clientConfig(true));
            try {
                HttpRequest request = new HttpRequest(HttpMethod.GET, server.uri(), server.port());
                assertThatThrownBy(() -> readHeader(client, request)).hasMessageContaining(expectedError);
            } finally {
                client.shutdown();
            }
        }
    }

    @Test(groups = "unit")
    public void productionPipelineMultiplexesConcurrentRequests() throws Exception {
        try (LoopbackServer server = new LoopbackServer(" version ", true)) {
            HttpClient client = HttpClient.createFixed(clientConfig(true));
            try {
                List<HttpRequest> requests = new ArrayList<>();
                for (int i = 0; i < 20; i++) {
                    requests.add(new HttpRequest(HttpMethod.GET, server.uri(), server.port()));
                }
                List<String> values = Flux.fromIterable(requests)
                    .flatMap(request -> readHeaderAsync(client, request), 12).collectList().block(TIMEOUT);
                assertThat(values).hasSize(20).allMatch("version"::equals);
                String parent = requests.get(0).reactorNettyRequestRecord().getParentChannelId();
                assertThat(requests).allSatisfy(request -> {
                    assertThat(request.reactorNettyRequestRecord().isHttp2()).isTrue();
                    assertThat(request.reactorNettyRequestRecord().getParentChannelId()).isEqualTo(parent);
                });
                assertThat(requests.stream().map(request -> request.reactorNettyRequestRecord().getChannelId()).distinct()
                    .count()).isEqualTo(20);
            } finally {
                client.shutdown();
            }
        }
    }

    @Test(groups = "unit")
    public void productionPipelineRetainsParentAfterInvalidResponseStream() throws Exception {
        try (LoopbackServer server = new LoopbackServer(" version ", true)) {
            HttpClient client = HttpClient.createFixed(clientConfig(true));
            try {
                HttpRequest first = new HttpRequest(HttpMethod.GET, server.uri(), server.port());
                assertThat(readHeader(client, first)).isEqualTo("version");
                HttpRequest invalid = new HttpRequest(HttpMethod.GET, server.uri() + "invalid", server.port());
                assertThatThrownBy(() -> readHeader(client, invalid)).hasMessageContaining(HEADER);
                HttpRequest next = new HttpRequest(HttpMethod.GET, server.uri(), server.port());
                assertThat(readHeader(client, next)).isEqualTo("version");
                assertThat(next.reactorNettyRequestRecord().getParentChannelId())
                    .isEqualTo(first.reactorNettyRequestRecord().getParentChannelId());
            } finally {
                client.shutdown();
            }
        }
    }

    @Test(groups = "unit")
    public void productionPipelineSupportsUnpooledClient() throws Exception {
        try (LoopbackServer server = new LoopbackServer(" version ", true)) {
            HttpClient client = HttpClient.create(clientConfig(true));
            try {
                HttpRequest request = new HttpRequest(HttpMethod.GET, server.uri(), server.port());
                assertThat(readHeader(client, request)).isEqualTo("version");
                assertThat(request.reactorNettyRequestRecord().isHttp2()).isTrue();
            } finally {
                client.shutdown();
            }
        }
    }

    @Test(groups = "unit")
    public void productionPipelineReportsCloseBeforeSettingsWithoutWaitingForRequestTimeout() throws Exception {
        try (LoopbackServer server = new LoopbackServer("unused", true, null, true)) {
            HttpClient client = HttpClient.createFixed(clientConfig(true));
            try {
                HttpRequest request = new HttpRequest(HttpMethod.GET, server.uri(), server.port());
                assertThatThrownBy(() -> readHeader(client, request)).satisfies(error -> {
                    Throwable cause = reactor.core.Exceptions.unwrap(error);
                    assertThat(cause).isInstanceOf(Http2PrematureCloseException.class)
                        .hasMessageContaining("before receiving initial HTTP/2 SETTINGS");
                    assertThat(WebExceptionUtility.isNetworkFailure((Exception) cause)).isTrue();
                });
            } finally {
                client.shutdown();
            }
        }
    }

    @DataProvider(name = "connectionPooling")
    public Object[][] connectionPooling() {
        return new Object[][] { { true }, { false } };
    }

    @Test(groups = "unit", dataProvider = "connectionPooling")
    public void http2EnabledClientRetainsPlaintextHttp11Fallback(boolean pooled) throws Exception {
        DisposableServer server = HttpServer.create().host("127.0.0.1").port(0)
            .handle((request, response) -> response.header(HEADER, "version").sendString(Mono.just("plaintext-body")))
            .bindNow(TIMEOUT);
        HttpClient client = null;
        try {
            client = pooled ? HttpClient.createFixed(clientConfig(true)) : HttpClient.create(clientConfig(true));
            HttpRequest request = new HttpRequest(HttpMethod.GET, "http://127.0.0.1:" + server.port() + "/", server.port());
            String body = client.send(request).flatMap(response -> {
                assertThat(response.statusCode()).isEqualTo(200);
                assertThat(response.headerValue(HEADER)).isEqualTo("version");
                assertThat(response.internConnection().channel().pipeline().get(CosmosHttp2ChannelInitializer.HANDLER_NAME))
                    .isNull();
                assertThat(response.internConnection().channel().pipeline().get(Http2SettingsHandler.HANDLER_NAME)).isNull();
                return response.bodyAsString();
            }).block(TIMEOUT);
            assertThat(body).isEqualTo("plaintext-body");
            assertThat(request.reactorNettyRequestRecord().isHttp2()).isFalse();
        } finally {
            if (client != null) {
                client.shutdown();
            }
            server.disposeNow(TIMEOUT);
        }
    }

    private static HttpClientConfig clientConfig(boolean http2) {
        return new HttpClientConfig(new Configs()).withServerCertValidationDisabled(true)
            .withNetworkRequestTimeout(TIMEOUT)
            .withHttp2ConnectionConfig(new Http2ConnectionConfig().setEnabled(http2)
                .setMinConnectionPoolSize(1).setMaxConnectionPoolSize(1));
    }

    private static String readHeader(HttpClient client, HttpRequest request) {
        return readHeaderAsync(client, request).block(TIMEOUT);
    }

    private static Mono<String> readHeaderAsync(HttpClient client, HttpRequest request) {
        return client.send(request).flatMap(response -> {
            assertThat(response.statusCode()).isEqualTo(200);
            if (request.reactorNettyRequestRecord().isHttp2()) {
                Channel channel = response.internConnection().channel();
                Channel parent = channel.parent() != null ? channel.parent() : channel;
                assertThat(parent.pipeline().get(Http2SettingsHandler.HANDLER_NAME))
                    .isSameAs(Http2SettingsHandler.INSTANCE);
                assertThat(parent.pipeline().get(CosmosHttp2ChannelInitializer.HANDLER_NAME)).isNull();
                assertThat(parent.pipeline().names().indexOf(Http2SettingsHandler.HANDLER_NAME))
                    .isLessThan(parent.pipeline().names().indexOf("reactor.left.h2MultiplexHandler"));
            } else {
                assertThat(response.internConnection().channel().pipeline().get(
                    Http2SettingsHandler.HANDLER_NAME)).isNull();
            }
            String value = response.headerValue(HEADER);
            return response.bodyAsString().defaultIfEmpty("").thenReturn(value);
        });
    }

    private static ByteBuf headerBlock(String... fields) {
        ByteBuf block = Unpooled.buffer();
        for (int i = 0; i < fields.length; i += 2) {
            block.writeByte(0).writeByte(fields[i].length());
            block.writeCharSequence(fields[i], StandardCharsets.US_ASCII);
            block.writeByte(fields[i + 1].length());
            block.writeCharSequence(fields[i + 1], StandardCharsets.US_ASCII);
        }
        return block;
    }

    private final class LoopbackServer implements AutoCloseable {
        private final NioEventLoopGroup acceptor = new NioEventLoopGroup(1);
        private final NioEventLoopGroup workers = new NioEventLoopGroup(1);
        private final Channel channel;

        private LoopbackServer(String value, boolean http2) throws Exception {
            this(value, http2, null);
        }

        private LoopbackServer(String value, boolean http2, String[] wireHeaders) throws Exception {
            this(value, http2, wireHeaders, false);
        }

        private LoopbackServer(String value, boolean http2, String[] wireHeaders, boolean closeBeforeSettings)
            throws Exception {
            String protocol = http2 ? ApplicationProtocolNames.HTTP_2 : ApplicationProtocolNames.HTTP_1_1;
            SslContext ssl = SslContextBuilder.forServer(keys).sslProvider(SslProvider.JDK)
                .applicationProtocolConfig(new ApplicationProtocolConfig(ApplicationProtocolConfig.Protocol.ALPN,
                    ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                    ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT, protocol)).build();
            boolean bound = false;
            try {
                channel = bind(ssl, value, http2, protocol, wireHeaders, closeBeforeSettings);
                bound = true;
            } finally {
                if (!bound) {
                    shutdownEventLoops();
                }
            }
        }

        private Channel bind(SslContext ssl, String value, boolean http2, String protocol, String[] wireHeaders,
                             boolean closeBeforeSettings)
            throws InterruptedException {
            return new ServerBootstrap().group(acceptor, workers).channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel child) {
                        child.pipeline().addLast(ssl.newHandler(child.alloc()));
                        child.pipeline().addLast(new ApplicationProtocolNegotiationHandler(protocol) {
                            @Override
                            protected void configurePipeline(ChannelHandlerContext context, String selected) {
                                assertThat(selected).isEqualTo(protocol);
                                if (closeBeforeSettings) {
                                    context.close();
                                    return;
                                }
                                if (http2) {
                                    context.pipeline().addLast(Http2FrameCodecBuilder.forServer().build(),
                                        new Http2MultiplexHandler(new ChannelInitializer<Channel>() {
                                            @Override
                                            protected void initChannel(Channel stream) {
                                                stream.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                                    @Override
                                                    public void channelRead(ChannelHandlerContext ctx, Object message) {
                                                        try {
                                                            if (message instanceof Http2HeadersFrame) {
                                                                if (wireHeaders == null) {
                                                                    String responseValue = "/invalid".equals(
                                                                        ((Http2HeadersFrame) message).headers().path().toString())
                                                                        ? " v\0 " : value;
                                                                    Http2Headers headers = new DefaultHttp2Headers(false, false, 16)
                                                                        .status("200").add(HEADER, responseValue);
                                                                    ctx.writeAndFlush(new DefaultHttp2HeadersFrame(headers, true));
                                                                } else {
                                                                    writeRawHeaders(ctx, (Http2HeadersFrame) message, wireHeaders);
                                                                }
                                                            }
                                                        } finally {
                                                            ReferenceCountUtil.release(message);
                                                        }
                                                    }
                                                });
                                            }
                                        }));
                                } else {
                                    context.pipeline().addLast(new HttpServerCodec(), new HttpObjectAggregator(8192),
                                        new SimpleChannelInboundHandler<FullHttpRequest>() {
                                            @Override
                                            protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest request) {
                                                ctx.pipeline().context(HttpServerCodec.class).writeAndFlush(
                                                    Unpooled.copiedBuffer("HTTP/1.1 200 OK\r\n"
                                                    + HEADER + ": " + value + "\r\nContent-Length: 0\r\n\r\n",
                                                    StandardCharsets.US_ASCII));
                                            }
                                        });
                                }
                            }
                        });
                    }
                }).bind("127.0.0.1", 0).sync().channel();
        }

        private int port() {
            return ((InetSocketAddress) channel.localAddress()).getPort();
        }

        private void writeRawHeaders(ChannelHandlerContext ctx, Http2HeadersFrame request, String[] fields) {
            ByteBuf block = headerBlock(fields);
            try {
                ByteBuf frame = ctx.alloc().buffer(9 + block.readableBytes());
                frame.writeMedium(block.readableBytes()).writeByte(1).writeByte(5).writeInt(request.stream().id());
                frame.writeBytes(block);
                // Bypass the server encoder so pseudo-header order and duplicates stay intact on the wire.
                ctx.channel().parent().pipeline().context(Http2FrameCodec.class).writeAndFlush(frame);
            } finally {
                block.release();
            }
        }

        private String uri() {
            return "https://127.0.0.1:" + port() + "/";
        }

        @Override
        public void close() throws Exception {
            try {
                channel.close().sync();
            } finally {
                shutdownEventLoops();
            }
        }

        private void shutdownEventLoops() {
            try {
                acceptor.shutdownGracefully(0, 1, TimeUnit.SECONDS).syncUninterruptibly();
            } finally {
                workers.shutdownGracefully(0, 1, TimeUnit.SECONDS).syncUninterruptibly();
            }
        }
    }
}

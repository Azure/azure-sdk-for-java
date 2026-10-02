// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.http;

import com.azure.cosmos.Http2ConnectionConfig;
import com.azure.cosmos.implementation.Configs;
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
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.DefaultHttp2HeadersDecoder;
import io.netty.handler.codec.http2.DefaultHttp2HeadersFrame;
import io.netty.handler.codec.http2.Http2Exception;
import io.netty.handler.codec.http2.Http2FrameCodecBuilder;
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
import reactor.netty.DisposableServer;
import reactor.netty.http.Http2SslContextSpec;
import reactor.netty.http.HttpProtocol;
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
            assertThatThrownBy(() -> new DefaultHttp2HeadersDecoder(false, 32).decodeHeaders(1, block))
                .isInstanceOf(Http2Exception.HeaderListSizeException.class);
        } finally {
            block.release();
        }
    }

    @Test(groups = "unit")
    public void handlerRetainsNameValidation() {
        ByteBuf block = headerBlock(":status", "200", "X-MS-ServiceVersion", "version");
        try {
            assertThatThrownBy(() -> Http2ResponseHeaderValidationHandler.validateHeaders(
                new DefaultHttp2HeadersDecoder(false, 8192).decodeHeaders(1, block)))
                .isInstanceOf(IllegalArgumentException.class);
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
        DefaultHttp2HeadersDecoder decoder = new DefaultHttp2HeadersDecoder(false, 8192);
        ByteBuf first = headerBlock(HEADER, value);
        ByteBuf second = Unpooled.buffer(1).writeByte(0xbe);
        first.setByte(0, 0x40);
        try {
            Http2Headers a = decoder.decodeHeaders(1, first);
            Http2Headers b = decoder.decodeHeaders(3, second);
            Http2ResponseHeaderValidationHandler.validateHeaders(a);
            Http2ResponseHeaderValidationHandler.validateHeaders(b);
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

    @DataProvider(name = "cleartextProtocols")
    public Object[][] cleartextProtocols() {
        return new Object[][] {
            { new HttpProtocol[] { HttpProtocol.H2C } },
            { new HttpProtocol[] { HttpProtocol.HTTP11, HttpProtocol.H2C } }
        };
    }

    @Test(groups = "unit", dataProvider = "cleartextProtocols")
    public void cleartextBootstrapDisablesValidationWithConfiguredLimit(HttpProtocol[] protocols) {
        HttpServer template = HttpServer.create().host("127.0.0.1").port(0)
            .protocol(HttpProtocol.HTTP11, HttpProtocol.H2C)
            .handle((request, response) -> response.header(HEADER, "version").sendString(Mono.just("ok")));
        DisposableServer server = template.bindNow(TIMEOUT);
        try {
            DisposableServer second = template.bindNow(TIMEOUT);
            try {
                reactor.netty.http.client.HttpClient client = reactor.netty.http.client.HttpClient.newConnection()
                    .protocol(protocols).http2Settings(settings -> settings.maxHeaderListSize(16384))
                    .httpResponseDecoder(spec -> spec.validateHeaders(false))
                    .observe((connection, state) -> {
                        Channel ch = connection.channel();
                        Channel parent = ch.parent() != null ? ch.parent() : ch;
                        io.netty.handler.codec.http2.Http2FrameCodec codec = parent.pipeline().get(
                            io.netty.handler.codec.http2.Http2FrameCodec.class);
                        if (codec != null && parent.pipeline().get(
                            Http2ResponseHeaderValidationHandler.HANDLER_NAME) == null) {
                            parent.pipeline().addAfter(parent.pipeline().context(codec).name(),
                                Http2ResponseHeaderValidationHandler.HANDLER_NAME,
                                Http2ResponseHeaderValidationHandler.INSTANCE);
                        }
                    })
                    .doOnResponse((response, connection) -> {
                        assertThat(connection.channel().parent()).isNotNull();
                        assertThat(connection.channel().parent().pipeline().get(
                            io.netty.handler.codec.http2.Http2FrameCodec.class)
                            .decoder().localSettings().maxHeaderListSize()).isEqualTo(16384L);
                    });
                for (DisposableServer target : new DisposableServer[] { server, second }) {
                    assertThat(client.get().uri("http://127.0.0.1:" + target.port() + "/")
                        .responseSingle((response, body) -> body.asString()).block(TIMEOUT)).isEqualTo("ok");
                }
            } finally {
                second.disposeNow(TIMEOUT);
            }
        } finally {
            server.disposeNow(TIMEOUT);
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
    public void handlerNormalizesOnlyServiceVersionPadding(String value, String expected) throws Exception {
        ByteBuf block = headerBlock(":status", "200", HEADER, value);
        try {
            Http2Headers headers = new DefaultHttp2HeadersDecoder(false, 8192).decodeHeaders(1, block);
            Http2ResponseHeaderValidationHandler.validateHeaders(headers);
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
    public void handlerRetainsStrictValueValidation(String name, String value) {
        ByteBuf block = headerBlock(":status", "200", name, value);
        try {
            assertThatThrownBy(() -> Http2ResponseHeaderValidationHandler.validateHeaders(
                new DefaultHttp2HeadersDecoder(false, 8192).decodeHeaders(1, block)))
                .isInstanceOf(IllegalArgumentException.class).hasMessageContaining(name);
        } finally {
            block.release();
        }
    }

    @Test(groups = "unit")
    public void decodedHeadersDoNotRetainOriginalPseudoHeaderWireOrder() throws Exception {
        ByteBuf block = headerBlock(HEADER, " version ", ":status", "200");
        try {
            Http2Headers headers = new DefaultHttp2HeadersDecoder(false, 8192).decodeHeaders(1, block);
            assertThat(headers.iterator().next().getKey().toString()).isEqualTo(":status");
        } finally {
            block.release();
        }
    }

    @Test(groups = "unit")
    public void releasedBootstrapValidationFlagIsFalseForHttp2() {
        HttpClient client = HttpClient.createFixed(clientConfig(true));
        try {
            reactor.netty.http.client.HttpClient transport = com.azure.cosmos.implementation.directconnectivity
                .ReflectionUtils.get(reactor.netty.http.client.HttpClient.class, client, "httpClient");
            assertThat(transport.configuration().decoder().validateHeaders()).isFalse();
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

    private static HttpClientConfig clientConfig(boolean http2) {
        return new HttpClientConfig(new Configs()).withServerCertValidationDisabled(true)
            .withNetworkRequestTimeout(TIMEOUT)
            .withHttp2ConnectionConfig(new Http2ConnectionConfig().setEnabled(http2)
                .setMinConnectionPoolSize(1).setMaxConnectionPoolSize(1));
    }

    private static String readHeader(HttpClient client, HttpRequest request) {
        return client.send(request).flatMap(response -> {
            assertThat(response.statusCode()).isEqualTo(200);
            if (request.reactorNettyRequestRecord().isHttp2()) {
                Channel channel = response.internConnection().channel();
                Channel parent = channel.parent() != null ? channel.parent() : channel;
                assertThat(parent.pipeline().get(Http2ResponseHeaderValidationHandler.HANDLER_NAME))
                    .isSameAs(Http2ResponseHeaderValidationHandler.INSTANCE);
                assertThat(parent.pipeline().names().indexOf(Http2ResponseHeaderValidationHandler.HANDLER_NAME))
                    .isLessThan(parent.pipeline().names().indexOf("reactor.left.h2MultiplexHandler"));
            } else {
                assertThat(response.internConnection().channel().pipeline().get(
                    Http2ResponseHeaderValidationHandler.HANDLER_NAME)).isNull();
            }
            String value = response.headerValue(HEADER);
            return response.bodyAsString().defaultIfEmpty("").thenReturn(value);
        }).block(TIMEOUT);
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
            String protocol = http2 ? ApplicationProtocolNames.HTTP_2 : ApplicationProtocolNames.HTTP_1_1;
            SslContext ssl = SslContextBuilder.forServer(keys).sslProvider(SslProvider.JDK)
                .applicationProtocolConfig(new ApplicationProtocolConfig(ApplicationProtocolConfig.Protocol.ALPN,
                    ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE,
                    ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT, protocol)).build();
            boolean bound = false;
            try {
                channel = bind(ssl, value, http2, protocol);
                bound = true;
            } finally {
                if (!bound) {
                    shutdownEventLoops();
                }
            }
        }

        private Channel bind(SslContext ssl, String value, boolean http2, String protocol) throws InterruptedException {
            return new ServerBootstrap().group(acceptor, workers).channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel child) {
                        child.pipeline().addLast(ssl.newHandler(child.alloc()));
                        child.pipeline().addLast(new ApplicationProtocolNegotiationHandler(protocol) {
                            @Override
                            protected void configurePipeline(ChannelHandlerContext context, String selected) {
                                assertThat(selected).isEqualTo(protocol);
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
                                                                Http2Headers headers = new DefaultHttp2Headers(false, false, 16)
                                                                    .status("200").add(HEADER, value);
                                                                ctx.writeAndFlush(new DefaultHttp2HeadersFrame(headers, true));
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

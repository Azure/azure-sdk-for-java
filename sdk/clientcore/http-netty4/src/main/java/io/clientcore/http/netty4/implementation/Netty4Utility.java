// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.netty4.implementation;

import io.clientcore.core.http.client.HttpProtocolVersion;
import io.clientcore.core.http.models.HttpHeader;
import io.clientcore.core.http.models.HttpHeaderName;
import io.clientcore.core.http.models.HttpHeaders;
import io.clientcore.core.http.models.HttpRequest;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.instrumentation.logging.LogLevel;
import io.clientcore.core.instrumentation.logging.LoggingEvent;
import io.clientcore.core.models.CoreException;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.models.binarydata.FileBinaryData;
import io.clientcore.core.models.binarydata.InputStreamBinaryData;
import io.clientcore.core.utils.CoreUtils;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpChunkedInput;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpContent;
import io.netty.handler.codec.http.HttpDecoderConfig;
import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeadersFactory;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http2.DefaultHttp2Connection;
import io.netty.handler.codec.http2.DelegatingDecompressorFrameListener;
import io.netty.handler.codec.http2.Http2Connection;
import io.netty.handler.codec.http2.Http2FrameListener;
import io.netty.handler.codec.http2.Http2SecurityUtil;
import io.netty.handler.codec.http2.Http2Settings;
import io.netty.handler.codec.http2.HttpToHttp2ConnectionHandlerBuilder;
import io.netty.handler.codec.http2.InboundHttp2ToHttpAdapterBuilder;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolNames;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslProvider;
import io.netty.handler.ssl.SupportedCipherSuiteFilter;
import io.netty.handler.stream.ChunkedInput;
import io.netty.handler.stream.ChunkedNioFile;
import io.netty.handler.stream.ChunkedStream;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.Version;

import javax.net.ssl.SSLException;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static io.netty.handler.codec.http.DefaultHttpHeadersFactory.trailersFactory;

/**
 * Helper class containing utility methods.
 */
public final class Netty4Utility {
    private static final ClientLogger LOGGER = new ClientLogger(Netty4Utility.class);

    private static final HttpHeadersFactory HTTP_HEADERS_FACTORY = new WrappedHttpHeadersFactory();

    static final String PROPERTIES_FILE_NAME = "http-netty.properties";
    static final String NETTY_VERSION_PROPERTY = "netty-version";

    // List of Netty artifacts that should match the 'netty.version' property in the pom.xml file.
    // Non-native dependencies are required while native dependencies are optional. Without the native dependencies
    // the SDK will fall back to using the JDK implementations.
    private static final List<String> REQUIRED_NETTY_VERSION_ARTIFACTS
        = Arrays.asList("netty-buffer", "netty-codec", "netty-codec-http", "netty-codec-http2", "netty-common",
            "netty-handler", "netty-handler-proxy", "netty-resolver", "netty-resolver-dns", "netty-transport");
    private static final List<String> OPTIONAL_NETTY_VERSION_ARTIFACTS = Arrays
        .asList("netty-transport-native-unix-common", "netty-transport-native-epoll", "netty-transport-native-kqueue");

    private static final int TWO_FIFTY_SIX_KB = 256 * 1024;

    /**
     * Converts Netty HttpHeaders to ClientCore HttpHeaders.
     * <p>
     * Most Netty requests should store headers in {@link WrappedHttp11Headers}, but if that doesn't happen this method
     * can be used to convert the Netty headers to ClientCore headers.
     *
     * @param nettyHeaders Netty HttpHeaders.
     * @return Converted ClientCore HttpHeaders.
     */
    public static HttpHeaders convertHeaders(io.netty.handler.codec.http.HttpHeaders nettyHeaders) {
        int initialSize = (int) (nettyHeaders.size() / 0.75f);
        HttpHeaders coreHeaders = new HttpHeaders(initialSize);

        // Use Netty's iteratorAsString() to get the headers as a iterator of key-value pairs.
        // This is preferred over using Netty's names() and getAll(String) methods as those result in multiple scans
        // over the Netty headers.
        // This does result in more calls to HttpHeaderName.fromString(String) though, but in most cases this is
        // preferable as it is rare to have two headers with the same name, which is the only case where we'd see more
        // calls than necessary.
        nettyHeaders.iteratorAsString()
            .forEachRemaining(entry -> coreHeaders.add(HttpHeaderName.fromString(entry.getKey()), entry.getValue()));
        return coreHeaders;
    }

    /**
     * Awaits for the passed {@link CountDownLatch} to reach zero.
     *
     * @param latch The latch to wait for.
     */
    public static void awaitLatch(CountDownLatch latch) {
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw LOGGER.throwableAtError().log("Interrupted while waiting for latch", e, CoreException::from);
        }
    }

    /**
     * Reads the contents of the {@link ByteBuf} into an {@link OutputStream}.
     * <p>
     * Content will only be written to the {@link OutputStream} if the {@link ByteBuf} is non-null and is
     * {@link ByteBuf#isReadable()}. The entire {@link ByteBuf} will be consumed.
     * </p>
     * <p><strong>Warning:</strong> This is a helper method and does NOT release the {@link ByteBuf}
     * after it is consumed, and it must be manually released to avoid memory leaks (either the {@link ByteBuf}
     * or the container holding the {@link ByteBuf}).
     *
     * @param byteBuf The Netty {@link ByteBuf} to read from.
     * @param stream The {@link OutputStream} to write to.
     * @throws NullPointerException If {@code stream} is null.
     * @throws IOException If an I/O error occurs.
     */
    static void readByteBufIntoOutputStream(ByteBuf byteBuf, OutputStream stream) throws IOException {
        Objects.requireNonNull(stream, "Cannot read a ByteBuf into a null 'stream'.");
        if (byteBuf == null || !byteBuf.isReadable()) {
            return;
        }

        byteBuf.readBytes(stream, byteBuf.readableBytes());
    }

    /**
     * Creates an {@link HttpClientCodec} that uses a custom {@link HttpDecoderConfig} that injects
     * {@link WrappedHttp11Headers} functionality.
     *
     * @return A new {@link HttpClientCodec} instance.
     */
    public static HttpClientCodec createCodec() {
        return new HttpClientCodec(new HttpDecoderConfig().setHeadersFactory(HTTP_HEADERS_FACTORY)
            // For now, set the max header size to 256 KB. Follow up to see if this should be configurable.
            .setMaxHeaderSize(256 * 1024), HttpClientCodec.DEFAULT_PARSE_HTTP_AFTER_CONNECT_REQUEST,
            HttpClientCodec.DEFAULT_FAIL_ON_MISSING_RESPONSE);
    }

    /**
     * Custom implementation of {@link HttpHeadersFactory} that creates {@link WrappedHttp11Headers}.
     * <p>
     * Using {@link WrappedHttp11Headers} is a performance optimization to remove converting Netty's HttpHeaders to
     * ClientCore's HttpHeaders and vice versa.
     */
    private static final class WrappedHttpHeadersFactory implements HttpHeadersFactory {
        @Override
        public io.netty.handler.codec.http.HttpHeaders newHeaders() {
            return new WrappedHttp11Headers(new io.clientcore.core.http.models.HttpHeaders());
        }

        @Override
        public io.netty.handler.codec.http.HttpHeaders newEmptyHeaders() {
            return new WrappedHttp11Headers(new io.clientcore.core.http.models.HttpHeaders());
        }
    }

    /**
     * Convenience method for getting the header value of a given name from the Netty
     * {@link io.netty.handler.codec.http.HttpHeaders}.
     * <p>
     * This method inspects the Netty {@link io.netty.handler.codec.http.HttpHeaders} for being an instance of
     * {@link WrappedHttp11Headers}. If it is not an instanceof it will use the {@code nettyHeaderName} to retrieve all
     * values. If it is an instanceof it will use the {@code clientCoreHeaderName}.
     * <p>
     * This method is an attempt to optimize retrieval as Netty and ClientCore use different structures for managing
     * headers, where in many cases lookup is faster for ClientCore headers.
     *
     * @param headers The Netty {@link io.netty.handler.codec.http.HttpHeaders} to retrieve all header values from.
     * @param nettyHeaderName The header name to use when retrieving from a non-{@link WrappedHttp11Headers}.
     * @param clientCoreHeaderName The header name to use when retrieving from a {@link WrappedHttp11Headers}.
     * @return The value for the header name, or null if the header didn't exist in the headers.
     */
    public static String get(io.netty.handler.codec.http.HttpHeaders headers, CharSequence nettyHeaderName,
        HttpHeaderName clientCoreHeaderName) {
        List<String> all = getAll(headers, nettyHeaderName, clientCoreHeaderName);
        return all.isEmpty() ? null : all.get(0);
    }

    /**
     * Convenience method for getting all header values of a given name from the Netty
     * {@link io.netty.handler.codec.http.HttpHeaders}.
     * <p>
     * This method inspects the Netty {@link io.netty.handler.codec.http.HttpHeaders} for being an instance of
     * {@link WrappedHttp11Headers}. If it is not an instanceof it will use the {@code nettyHeaderName} to retrieve all
     * values. If it is an instanceof it will use the {@code clientCoreHeaderName}.
     * <p>
     * This method is an attempt to optimize retrieval as Netty and ClientCore use different structures for managing
     * headers, where in many cases lookup is faster for ClientCore headers.
     *
     * @param headers The Netty {@link io.netty.handler.codec.http.HttpHeaders} to retrieve all header values from.
     * @param nettyHeaderName The header name to use when retrieving from a non-{@link WrappedHttp11Headers}.
     * @param clientCoreHeaderName The header name to use when retrieving from a {@link WrappedHttp11Headers}.
     * @return The list of values for the header name, or an empty list if the header didn't exist in the headers.
     */
    public static List<String> getAll(io.netty.handler.codec.http.HttpHeaders headers, CharSequence nettyHeaderName,
        HttpHeaderName clientCoreHeaderName) {
        if (headers instanceof WrappedHttp11Headers) {
            HttpHeader header = ((WrappedHttp11Headers) headers).getCoreHeaders().get(clientCoreHeaderName);
            return (header == null) ? Collections.emptyList() : header.getValues();
        } else {
            return headers.getAll(nettyHeaderName);
        }
    }

    /**
     * Utility method that either sets ({@link AtomicReference#set(Object)}) the error reference or updates the existing
     * error reference value with a suppressed exception ({@link Throwable#addSuppressed(Throwable)}).
     *
     * @param errorReference The {@link AtomicReference} to set or update.
     * @param error The error to set or suppress.
     */
    public static void setOrSuppressError(AtomicReference<Throwable> errorReference, Throwable error) {
        errorReference.accumulateAndGet(error, (existing, newError) -> {
            if (existing == null) {
                // No error has been set, set the new error.
                return newError;
            } else if (existing == newError) {
                // Case where two different locations threw the same error. Throwable isn't allowed to suppress itself,
                // so return the existing error.
                return existing;
            } else {
                // An existing error already exists, suppress the new error so we don't lose information.
                existing.addSuppressed(newError);
                return existing;
            }
        });
    }

    /**
     * Sends an HTTP/1.1 request using the provided {@link Channel}.
     *
     * @param request The HTTP request to send.
     * @param channel The Channel to send the request.
     * @param errorReference An AtomicReference tracking exceptions seen during the request lifecycle.
     * @return A ChannelFuture that will complete once the request has been sent.
     */
    public static ChannelFuture sendHttp11Request(HttpRequest request, Channel channel,
        AtomicReference<Throwable> errorReference) {
        HttpMethod nettyMethod = HttpMethod.valueOf(request.getHttpMethod().toString());
        String uri = request.getUri().toString();
        WrappedHttp11Headers wrappedHttp11Headers = new WrappedHttp11Headers(request.getHeaders());

        // TODO (alzimmer): This will mutate the underlying ClientCore HttpHeaders. Will need to think about this design
        //  more once it's closer to completion.
        wrappedHttp11Headers.getCoreHeaders().set(HttpHeaderName.HOST, request.getUri().getHost());

        BinaryData requestBody = request.getBody();
        if (requestBody instanceof FileBinaryData) {
            FileBinaryData fileBinaryData = (FileBinaryData) requestBody;
            try {
                return sendChunkedHttp11(channel,
                    new ChunkedNioFile(FileChannel.open(fileBinaryData.getFile(), StandardOpenOption.READ),
                        fileBinaryData.getPosition(), fileBinaryData.getLength(), 8192),
                    new DefaultHttpRequest(HttpVersion.HTTP_1_1, nettyMethod, uri, wrappedHttp11Headers),
                    errorReference);
            } catch (IOException ex) {
                return channel.newFailedFuture(ex);
            }
        } else if (requestBody instanceof InputStreamBinaryData) {
            return sendChunkedHttp11(channel, new ChunkedStream(requestBody.toStream()),
                new DefaultHttpRequest(HttpVersion.HTTP_1_1, nettyMethod, uri, wrappedHttp11Headers), errorReference);
        } else {
            ByteBuf body = Unpooled.EMPTY_BUFFER;
            if (requestBody != null && requestBody != BinaryData.empty()) {
                // Longer term, see if there is a way to have BinaryData act as the ByteBuf body to further eliminate
                // copying of byte[]s.
                body = Unpooled.wrappedBuffer(requestBody.toBytes());
            }
            if (body.readableBytes() > 0) {
                // TODO (alzimmer): Should we be setting Content-Length here again? Shouldn't this be handled externally
                //  by the creator of the HttpRequest?
                wrappedHttp11Headers.getCoreHeaders()
                    .set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(body.readableBytes()));
            }

            Throwable error = errorReference.get();
            if (error != null) {
                return channel.newFailedFuture(error);
            }

            return channel.writeAndFlush(new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, nettyMethod, uri, body,
                wrappedHttp11Headers, trailersFactory().newHeaders()));
        }
    }

    private static ChannelFuture sendChunkedHttp11(Channel channel, ChunkedInput<ByteBuf> chunkedInput,
        io.netty.handler.codec.http.HttpRequest initialLineAndHeaders, AtomicReference<Throwable> errorReference) {
        if (channel.pipeline().get(Netty4HandlerNames.CHUNKED_WRITER) == null) {
            // Add the ChunkedWriteHandler which will handle sending the chunkedInput.
            channel.pipeline()
                .addAfter(Netty4HandlerNames.HTTP_CODEC, Netty4HandlerNames.CHUNKED_WRITER, new ChunkedWriteHandler());
        }

        Throwable error = errorReference.get();
        if (error != null) {
            return channel.newFailedFuture(error);
        }

        channel.write(initialLineAndHeaders);
        return channel.writeAndFlush(new HttpChunkedInput(chunkedInput));
    }

    /**
     * Checks the runtime version of the Netty libraries.
     * <p>
     * If the runtime versions match the dependencies in the pom.xml file, this method will do nothing.
     * <p>
     * If the runtime versions do not match the dependencies in the pom.xml file, this method will log a warning. The
     * warning will contain the versions found in runtime and the expected versions to be used by the SDK.
     */
    public static void validateNettyVersions() {
        if (!LOGGER.canLogAtLevel(LogLevel.INFORMATIONAL)) {
            return;
        }

        try {
            validateNettyVersionsInternal();
        } catch (Exception ex) {
            LOGGER.atInfo()
                .setThrowable(ex)
                .log("Unable to load Netty version information. If Netty version validation is required, "
                    + "please review this exception. Otherwise, this log message can be ignored.");
        }
    }

    static void validateNettyVersionsInternal() {
        Map<String, String> pomVersions = CoreUtils.getProperties(PROPERTIES_FILE_NAME);
        NettyVersionLogInformation versionLogInformation
            = createNettyVersionLogInformation(pomVersions.get(NETTY_VERSION_PROPERTY));
        if (versionLogInformation.shouldLog()) {
            versionLogInformation.log();
        }
    }

    static NettyVersionLogInformation createNettyVersionLogInformation(String nettyVersion) {
        Map<String, String> classpathNettyVersions = new LinkedHashMap<>();

        Map<String, Version> nettyVersions = Version.identify(Netty4Utility.class.getClassLoader());

        for (String artifact : REQUIRED_NETTY_VERSION_ARTIFACTS) {
            Version version = nettyVersions.get(artifact);

            // Version shouldn't be null as http-netty4 has it as a dependency, but it could have been
            // excluded. Include it as a warning.
            if (version == null) {
                classpathNettyVersions.put("io.netty:" + artifact, "unknown (not found and is required)");
            } else {
                classpathNettyVersions.put("io.netty:" + version.artifactId(), version.artifactVersion());
            }
        }

        for (String artifact : OPTIONAL_NETTY_VERSION_ARTIFACTS) {
            Version version = nettyVersions.get(artifact);

            // Version shouldn't be null as http-netty4 has it as a dependency, but it could have been
            // excluded. Don't include it as a warning for native dependencies as it is optional.
            if (version != null) {
                classpathNettyVersions.put("io.netty:" + version.artifactId(), version.artifactVersion());
            }
        }

        return new NettyVersionLogInformation(nettyVersion, classpathNettyVersions);
    }

    static final class NettyVersionLogInformation {
        private final String nettyVersion;
        final Map<String, String> classpathNettyVersions;

        NettyVersionLogInformation(String nettyVersion, Map<String, String> classpathNettyVersions) {
            this.nettyVersion = nettyVersion;
            this.classpathNettyVersions = classpathNettyVersions;
        }

        boolean shouldLog() {
            return classpathNettyVersions.values().stream().anyMatch(version -> !Objects.equals(version, nettyVersion));
        }

        private void log() {
            LoggingEvent loggingEvent = LOGGER.atInfo().addKeyValue("netty-version", nettyVersion);

            for (Map.Entry<String, String> entry : classpathNettyVersions.entrySet()) {
                loggingEvent.addKeyValue("classpath-netty-version-" + entry.getKey(), entry.getValue());
            }

            loggingEvent.log("The following Netty versions were found on the classpath and have a mismatch with "
                + "the versions used by http-netty. If your application runs without issue this message can be "
                + "ignored, otherwise please align the Netty versions used in your application. For more information, "
                + "see https://aka.ms/azsdk/java/dependency/troubleshoot.");
        }
    }

    /**
     * Helper method that hot paths some well-known AsciiString HttpHeaderNames that are known to be used by Netty
     * internally.
     *
     * @param asciiString The CharSequence to check for a known HttpHeaderName.
     * @return The corresponding HttpHeaderName if it matches a known one, otherwise a new HttpHeaderName created from
     * the given CharSequence.
     */
    @SuppressWarnings("deprecation")
    public static HttpHeaderName fromPossibleAsciiString(CharSequence asciiString) {
        if (HttpHeaderNames.ACCEPT_ENCODING == asciiString) {
            return HttpHeaderName.ACCEPT_ENCODING;
        } else if (HttpHeaderNames.CONNECTION == asciiString) {
            return HttpHeaderName.CONNECTION;
        } else if (HttpHeaderNames.CONTENT_ENCODING == asciiString) {
            return HttpHeaderName.CONTENT_ENCODING;
        } else if (HttpHeaderNames.CONTENT_LENGTH == asciiString) {
            return HttpHeaderName.CONTENT_LENGTH;
        } else if (HttpHeaderNames.CONTENT_TYPE == asciiString) {
            return HttpHeaderName.CONTENT_TYPE;
        } else if (HttpHeaderNames.COOKIE == asciiString) {
            return HttpHeaderName.COOKIE;
        } else if (HttpHeaderNames.EXPECT == asciiString) {
            return HttpHeaderName.EXPECT;
        } else if (HttpHeaderNames.HOST == asciiString) {
            return HttpHeaderName.HOST;
        } else if (HttpHeaderNames.KEEP_ALIVE == asciiString) {
            return HttpHeaderName.KEEP_ALIVE;
        } else if (HttpHeaderNames.PROXY_AUTHORIZATION == asciiString) {
            return HttpHeaderName.PROXY_AUTHORIZATION;
        } else if (HttpHeaderNames.TE == asciiString) {
            return HttpHeaderName.TE;
        } else if (HttpHeaderNames.TRAILER == asciiString) {
            return HttpHeaderName.TRAILER;
        } else if (HttpHeaderNames.TRANSFER_ENCODING == asciiString) {
            return HttpHeaderName.TRANSFER_ENCODING;
        } else {
            return HttpHeaderName.fromString(asciiString.toString());
        }
    }

    /**
     * Configures the pipeline for either HTTP/1.1 or HTTP/2 based on the negotiated protocol.
     * <p>
     * This method adds the appropriate {@link Netty4HandlerNames#HTTP_CODEC} and
     * {@link Netty4HandlerNames#HTTP_RESPONSE} handlers to the pipeline, positioned correctly
     * relatively to the {@link Netty4HandlerNames#PROGRESS_AND_TIMEOUT} or {@link Netty4HandlerNames#SSL} handlers.
     *
     * @param pipeline The channel pipeline to configure.
     * @param request The HTTP request.
     * @param protocol The negotiated HTTP protocol version.
     * @param responseReference The atomic reference to hold the response state.
     * @param errorReference The atomic reference to hold any errors.
     * @param latch The countdown latch to signal completion.
     */
    public static void configureHttpsPipeline(ChannelPipeline pipeline, HttpRequest request,
        HttpProtocolVersion protocol, AtomicReference<ResponseStateInfo> responseReference,
        AtomicReference<Throwable> errorReference, CountDownLatch latch) {
        final ChannelHandler httpCodec;
        if (HttpProtocolVersion.HTTP_2 == protocol) {
            httpCodec = createHttp2Codec();
        } else { // HTTP/1.1
            httpCodec = createCodec();
        }

        Netty4ResponseHandler responseHandler
            = new Netty4ResponseHandler(request, responseReference, errorReference, latch);

        if (pipeline.get(Netty4HandlerNames.PROGRESS_AND_TIMEOUT) != null) {
            pipeline.addAfter(Netty4HandlerNames.PROGRESS_AND_TIMEOUT, Netty4HandlerNames.HTTP_RESPONSE,
                responseHandler);
            pipeline.addBefore(Netty4HandlerNames.PROGRESS_AND_TIMEOUT, Netty4HandlerNames.HTTP_CODEC, httpCodec);
        } else {
            pipeline.addAfter(Netty4HandlerNames.SSL, Netty4HandlerNames.HTTP_CODEC, httpCodec);
            pipeline.addAfter(Netty4HandlerNames.HTTP_CODEC, Netty4HandlerNames.HTTP_RESPONSE, responseHandler);
        }
    }

    public static ChannelHandler createHttp2Codec() {
        // TODO (alzimmer): InboundHttp2ToHttpAdapter buffers the entire response into a FullHttpResponse. Need to
        //  create a streaming version of this to support huge response payloads.
        Http2Connection http2Connection = new DefaultHttp2Connection(false);
        Http2Settings settings = new Http2Settings().headerTableSize(4096)
            .maxHeaderListSize(TWO_FIFTY_SIX_KB)
            .pushEnabled(false)
            .initialWindowSize(TWO_FIFTY_SIX_KB);
        Http2FrameListener frameListener = new DelegatingDecompressorFrameListener(http2Connection,
            new InboundHttp2ToHttpAdapterBuilder(http2Connection).maxContentLength(Integer.MAX_VALUE)
                .propagateSettings(true)
                .validateHttpHeaders(true)
                .build(),
            0);

        return new HttpToHttp2ConnectionHandlerBuilder().initialSettings(settings)
            .frameListener(frameListener)
            .connection(http2Connection)
            .validateHeaders(true)
            .build();
    }

    public static void sendHttp2Request(HttpRequest request, Channel channel, AtomicReference<Throwable> errorReference,
        CountDownLatch latch) {
        io.netty.handler.codec.http.HttpRequest nettyRequest = toNettyHttpRequest(request);

        final ChannelFuture writeFuture;

        if (nettyRequest instanceof FullHttpRequest) {
            writeFuture = channel.writeAndFlush(nettyRequest);
        } else {
            channel.write(nettyRequest);

            BinaryData requestBody = request.getBody();
            ChunkedInput<HttpContent> chunkedInput = new HttpChunkedInput(new ChunkedStream(requestBody.toStream()));

            writeFuture = channel.writeAndFlush(chunkedInput);
        }

        writeFuture.addListener(future -> {
            if (future.isSuccess()) {
                channel.read();
            } else {
                setOrSuppressError(errorReference, future.cause());
                latch.countDown();
            }
        });
    }

    private static io.netty.handler.codec.http.HttpRequest toNettyHttpRequest(HttpRequest request) {
        HttpMethod nettyMethod = HttpMethod.valueOf(request.getHttpMethod().toString());
        String uri = request.getUri().toString();
        WrappedHttp11Headers nettyHeaders = new WrappedHttp11Headers(request.getHeaders());
        nettyHeaders.getCoreHeaders().set(HttpHeaderName.HOST, request.getUri().getHost());

        BinaryData body = request.getBody();
        if (body == null || body.getLength() == 0 || body.isReplayable()) {
            ByteBuf bodyBytes = (body == null || body.getLength() == 0)
                ? Unpooled.EMPTY_BUFFER
                : Unpooled.wrappedBuffer(body.toBytes());

            nettyHeaders.getCoreHeaders().set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(bodyBytes.readableBytes()));
            return new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, nettyMethod, uri, bodyBytes, nettyHeaders,
                trailersFactory().newHeaders());
        } else {
            return new DefaultHttpRequest(HttpVersion.HTTP_1_1, nettyMethod, uri, nettyHeaders);
        }
    }

    public static SslContext buildSslContext(HttpProtocolVersion maximumHttpVersion,
        Consumer<SslContextBuilder> sslContextModifier) throws SSLException {
        SslContextBuilder sslContextBuilder = SslContextBuilder.forClient().endpointIdentificationAlgorithm("HTTPS");
        if (maximumHttpVersion == HttpProtocolVersion.HTTP_2) {
            // If HTTP/2 is the maximum version, we need to ensure that ALPN is enabled.
            SslProvider sslProvider = SslContext.defaultClientProvider();
            ApplicationProtocolConfig.SelectorFailureBehavior selectorBehavior;
            ApplicationProtocolConfig.SelectedListenerFailureBehavior selectedBehavior;
            if (sslProvider == SslProvider.JDK) {
                selectorBehavior = ApplicationProtocolConfig.SelectorFailureBehavior.FATAL_ALERT;
                selectedBehavior = ApplicationProtocolConfig.SelectedListenerFailureBehavior.FATAL_ALERT;
            } else {
                // Netty OpenSslContext doesn't support FATAL_ALERT, use NO_ADVERTISE and ACCEPT
                // instead.
                selectorBehavior = ApplicationProtocolConfig.SelectorFailureBehavior.NO_ADVERTISE;
                selectedBehavior = ApplicationProtocolConfig.SelectedListenerFailureBehavior.ACCEPT;
            }

            sslContextBuilder.ciphers(Http2SecurityUtil.CIPHERS, SupportedCipherSuiteFilter.INSTANCE)
                .applicationProtocolConfig(
                    new ApplicationProtocolConfig(ApplicationProtocolConfig.Protocol.ALPN, selectorBehavior,
                        selectedBehavior, ApplicationProtocolNames.HTTP_2, ApplicationProtocolNames.HTTP_1_1));
        }
        if (sslContextModifier != null) {
            // Allow the caller to modify the SslContextBuilder before it is built.
            sslContextModifier.accept(sslContextBuilder);
        }

        return sslContextBuilder.build();
    }

    private Netty4Utility() {
    }
}

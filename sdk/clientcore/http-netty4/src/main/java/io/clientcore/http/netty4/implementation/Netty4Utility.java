// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.netty4.implementation;

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
import io.netty.channel.ChannelPipeline;
import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.DefaultHttpRequest;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpDecoderConfig;
import io.netty.handler.codec.http.HttpHeadersFactory;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpVersion;
import io.netty.handler.codec.http2.DefaultHttp2DataFrame;
import io.netty.handler.codec.http2.DefaultHttp2Headers;
import io.netty.handler.codec.http2.DefaultHttp2HeadersFrame;
import io.netty.handler.codec.http2.Http2DataChunkedInput;
import io.netty.handler.codec.http2.Http2Headers;
import io.netty.handler.codec.http2.Http2HeadersFrame;
import io.netty.handler.stream.ChunkedInput;
import io.netty.handler.stream.ChunkedNioFile;
import io.netty.handler.stream.ChunkedStream;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.util.Version;

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

    /**
     * Name given to the {@link Netty4ProgressAndTimeoutHandler} used in the {@link ChannelPipeline} created by
     * {@code NettyHttpClient}.
     */
    public static final String PROGRESS_AND_TIMEOUT_HANDLER_NAME = "Netty4-Progress-And-Timeout-Handler";

    /**
     * Converts Netty HttpHeaders to ClientCore HttpHeaders.
     * <p>
     * Most Netty requests should store headers in {@link WrappedHttpHeaders}, but if that doesn't happen this method
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
    static void awaitLatch(CountDownLatch latch) {
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
        if (byteBuf.refCnt() > 0) {
            // Release the ByteBuf as we've consumed it.
            byteBuf.release();
        }
    }

    /**
     * Creates an {@link HttpClientCodec} that uses a custom {@link HttpDecoderConfig} that injects
     * {@link WrappedHttpHeaders} functionality.
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
     * Custom implementation of {@link HttpHeadersFactory} that creates {@link WrappedHttpHeaders}.
     * <p>
     * Using {@link WrappedHttpHeaders} is a performance optimization to remove converting Netty's HttpHeaders to
     * ClientCore's HttpHeaders and vice versa.
     */
    private static final class WrappedHttpHeadersFactory implements HttpHeadersFactory {
        @Override
        public io.netty.handler.codec.http.HttpHeaders newHeaders() {
            return new WrappedHttpHeaders(new io.clientcore.core.http.models.HttpHeaders());
        }

        @Override
        public io.netty.handler.codec.http.HttpHeaders newEmptyHeaders() {
            return new WrappedHttpHeaders(new io.clientcore.core.http.models.HttpHeaders());
        }
    }

    /**
     * Convenience method for getting the header value of a given name from the Netty
     * {@link io.netty.handler.codec.http.HttpHeaders}.
     * <p>
     * This method inspects the Netty {@link io.netty.handler.codec.http.HttpHeaders} for being an instance of
     * {@link WrappedHttpHeaders}. If it is not an instanceof it will use the {@code nettyHeaderName} to retrieve all
     * values. If it is an instanceof it will use the {@code clientCoreHeaderName}.
     * <p>
     * This method is an attempt to optimize retrieval as Netty and ClientCore use different structures for managing
     * headers, where in many cases lookup is faster for ClientCore headers.
     *
     * @param headers The Netty {@link io.netty.handler.codec.http.HttpHeaders} to retrieve all header values from.
     * @param nettyHeaderName The header name to use when retrieving from a non-{@link WrappedHttpHeaders}.
     * @param clientCoreHeaderName The header name to use when retrieving from a {@link WrappedHttpHeaders}.
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
     * {@link WrappedHttpHeaders}. If it is not an instanceof it will use the {@code nettyHeaderName} to retrieve all
     * values. If it is an instanceof it will use the {@code clientCoreHeaderName}.
     * <p>
     * This method is an attempt to optimize retrieval as Netty and ClientCore use different structures for managing
     * headers, where in many cases lookup is faster for ClientCore headers.
     *
     * @param headers The Netty {@link io.netty.handler.codec.http.HttpHeaders} to retrieve all header values from.
     * @param nettyHeaderName The header name to use when retrieving from a non-{@link WrappedHttpHeaders}.
     * @param clientCoreHeaderName The header name to use when retrieving from a {@link WrappedHttpHeaders}.
     * @return The list of values for the header name, or an empty list if the header didn't exist in the headers.
     */
    public static List<String> getAll(io.netty.handler.codec.http.HttpHeaders headers, CharSequence nettyHeaderName,
        HttpHeaderName clientCoreHeaderName) {
        if (headers instanceof WrappedHttpHeaders) {
            HttpHeader header = ((WrappedHttpHeaders) headers).getCoreHeaders().get(clientCoreHeaderName);
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
     * @param progressAndTimeoutHandlerAdded Whether the ChannelPipeline associated with the Channel had the progress
     * and timeout handler added.
     * @param errorReference An AtomicReference tracking exceptions seen during the request lifecycle.
     * @return A ChannelFuture that will complete once the request has been sent.
     */
    public static ChannelFuture sendHttp11Request(HttpRequest request, Channel channel,
        boolean progressAndTimeoutHandlerAdded, AtomicReference<Throwable> errorReference) {
        HttpMethod nettyMethod = HttpMethod.valueOf(request.getHttpMethod().toString());
        String uri = request.getUri().toString();
        WrappedHttpHeaders wrappedHttpHeaders = new WrappedHttpHeaders(request.getHeaders());

        // TODO (alzimmer): This will mutate the underlying ClientCore HttpHeaders. Will need to think about this design
        //  more once it's closer to completion.
        wrappedHttpHeaders.getCoreHeaders().set(HttpHeaderName.HOST, request.getUri().getHost());

        BinaryData requestBody = request.getBody();
        if (requestBody instanceof FileBinaryData) {
            FileBinaryData fileBinaryData = (FileBinaryData) requestBody;
            try {
                return sendChunkedHttp11(channel,
                    new ChunkedNioFile(FileChannel.open(fileBinaryData.getFile(), StandardOpenOption.READ),
                        fileBinaryData.getPosition(), fileBinaryData.getLength(), 8192),
                    new DefaultHttpRequest(HttpVersion.HTTP_1_1, nettyMethod, uri, wrappedHttpHeaders),
                    progressAndTimeoutHandlerAdded, errorReference);
            } catch (IOException ex) {
                return channel.newFailedFuture(ex);
            }
        } else if (requestBody instanceof InputStreamBinaryData) {
            return sendChunkedHttp11(channel, new ChunkedStream(requestBody.toStream()),
                new DefaultHttpRequest(HttpVersion.HTTP_1_1, nettyMethod, uri, wrappedHttpHeaders),
                progressAndTimeoutHandlerAdded, errorReference);
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
                wrappedHttpHeaders.getCoreHeaders()
                    .set(HttpHeaderName.CONTENT_LENGTH, String.valueOf(body.readableBytes()));
            }

            Throwable error = errorReference.get();
            if (error != null) {
                return channel.newFailedFuture(error);
            }

            return channel.writeAndFlush(new DefaultFullHttpRequest(HttpVersion.HTTP_1_1, nettyMethod, uri, body,
                wrappedHttpHeaders, trailersFactory().newHeaders()));
        }
    }

    private static ChannelFuture sendChunkedHttp11(Channel channel, ChunkedInput<ByteBuf> chunkedInput,
        io.netty.handler.codec.http.HttpRequest initialLineAndHeaders, boolean progressAndTimeoutHandlerAdded,
        AtomicReference<Throwable> errorReference) {
        if (channel.pipeline().get(Netty4HandlerNames.CHUNKED_WRITER) == null) {
            // Add the ChunkedWriteHandler which will handle sending the chunkedInput.
            ChunkedWriteHandler chunkedWriteHandler = new ChunkedWriteHandler();
            if (progressAndTimeoutHandlerAdded) {
                channel.pipeline()
                    .addBefore(Netty4HandlerNames.PROGRESS_AND_TIMEOUT, Netty4HandlerNames.CHUNKED_WRITER,
                        chunkedWriteHandler);
            } else {
                channel.pipeline().addLast(Netty4HandlerNames.CHUNKED_WRITER, chunkedWriteHandler);
            }
        }

        Throwable error = errorReference.get();
        if (error != null) {
            return channel.newFailedFuture(error);
        }

        channel.write(initialLineAndHeaders);
        return channel.writeAndFlush(chunkedInput);
    }

    /**
     * Sends an HTTP/2 request using the provided {@link Channel}.
     *
     * @param request The HTTP request to send.
     * @param channel The Channel to send the request.
     * @param progressAndTimeoutHandlerAdded Whether the ChannelPipeline associated with the Channel had the progress
     * and timeout handler added.
     * @param errorReference An AtomicReference tracking exceptions seen during the request lifecycle.
     * @return A ChannelFuture that will complete once the request has been sent.
     */
    public static ChannelFuture sendHttp2Request(HttpRequest request, Channel channel,
        boolean progressAndTimeoutHandlerAdded, AtomicReference<Throwable> errorReference) {
        // HTTP/2 requests are more complicated than HTTP/1.1 as they are a stream of frames with specific purposes.
        // Additionally, since we're using multiplexing, we need to associate a stream ID with each frame.

        // Send the headers frame(s).
        // Unlike in HTTP/1.1, there isn't a status line on requests. Rather pseudo headers are used.
        // TODO (alzimmer): Create an Http2Headers implementations similar to WrappedHttpHeaders.
        Http2Headers headers = new DefaultHttp2Headers();
        headers.method(request.getHttpMethod().toString());
        headers.scheme(request.getUri().getScheme());
        headers.authority(request.getUri().getAuthority());
        if (request.getUri().getPath() != null) {
            headers.path(request.getUri().getPath());
        }

        // If the request doesn't have a body or is a HEAD request, only a headers frame should be sent before the
        // client indicates closure of its half of the stream.
        BinaryData requestBody = request.getBody();
        Long bodyLength = requestBody == null ? null : requestBody.getLength();
        boolean headersOnly = (bodyLength == null || bodyLength == 0)
            || request.getHttpMethod() == io.clientcore.core.http.models.HttpMethod.HEAD;

        request.getHeaders()
            .stream()
            .forEach(httpHeader -> headers.add(httpHeader.getName().getCaseInsensitiveName(), httpHeader.getValues()));
        Http2HeadersFrame headersFrame = new DefaultHttp2HeadersFrame(headers, headersOnly);

        if (headersOnly) {
            return channel.write(headersFrame);
        }

        channel.write(headersFrame);

        // Now it's time to write the data frames.
        if (requestBody instanceof FileBinaryData) {
            FileBinaryData fileBinaryData = (FileBinaryData) requestBody;
            try {
                return sendChunkedHttp2(channel,
                    new ChunkedNioFile(FileChannel.open(fileBinaryData.getFile(), StandardOpenOption.READ),
                        fileBinaryData.getPosition(), fileBinaryData.getLength(), 8192),
                    progressAndTimeoutHandlerAdded, errorReference);
            } catch (IOException ex) {
                return channel.newFailedFuture(ex);
            }
        } else if (requestBody instanceof InputStreamBinaryData) {
            return sendChunkedHttp2(channel, new ChunkedStream(requestBody.toStream()), progressAndTimeoutHandlerAdded,
                errorReference);
        } else {
            ByteBuf body = Unpooled.wrappedBuffer(requestBody.toBytes());

            Throwable error = errorReference.get();
            if (error != null) {
                return channel.newFailedFuture(error);
            }

            return channel.writeAndFlush(new DefaultHttp2DataFrame(body, true));
        }
    }

    private static ChannelFuture sendChunkedHttp2(Channel channel, ChunkedInput<ByteBuf> chunkedInput,
        boolean progressAndTimeoutHandlerAdded, AtomicReference<Throwable> errorReference) {
        if (channel.pipeline().get(Netty4HandlerNames.CHUNKED_WRITER) == null) {
            // Add the ChunkedWriteHandler which will handle sending the chunkedInput.
            ChunkedWriteHandler chunkedWriteHandler = new ChunkedWriteHandler();
            if (progressAndTimeoutHandlerAdded) {
                channel.pipeline()
                    .addBefore(Netty4HandlerNames.PROGRESS_AND_TIMEOUT, Netty4HandlerNames.CHUNKED_WRITER,
                        chunkedWriteHandler);
            } else {
                channel.pipeline().addLast(Netty4HandlerNames.CHUNKED_WRITER, chunkedWriteHandler);
            }
        }

        Throwable error = errorReference.get();
        if (error != null) {
            return channel.newFailedFuture(error);
        }

        return channel.writeAndFlush(new Http2DataChunkedInput(chunkedInput, null));
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

    private Netty4Utility() {
    }
}

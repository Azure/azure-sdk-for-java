// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk.httpclient.implementation;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.WritableByteChannel;
import java.time.Duration;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import static com.azure.core.http.jdk.httpclient.implementation.JdkHttpUtils.fromJdkHttpHeaders;

/**
 * Synchronous response implementation for JDK HttpClient.
 */
public final class JdkHttpResponseSync extends JdkHttpResponseBase {
    private static final ClientLogger LOGGER = new ClientLogger(JdkHttpResponseSync.class);
    private BinaryData binaryData = null;
    public static final int STREAM_READ_SIZE = 8192;

    private final InputStream bodyStream;
    private byte[] bodyBytes;

    private volatile int disposed = 0;
    private static final AtomicIntegerFieldUpdater<JdkHttpResponseSync> DISPOSED_UPDATER
        = AtomicIntegerFieldUpdater.newUpdater(JdkHttpResponseSync.class, "disposed");

    /**
     * Creates an instance of {@link JdkHttpResponseSync}.
     *
     * @param request the request which resulted in this response.
     * @param statusCode the status code of the response.
     * @param headers the headers of the response.
     * @param bytes the response body bytes.
     */
    public JdkHttpResponseSync(HttpRequest request, int statusCode, HttpHeaders headers, byte[] bytes) {
        super(request, statusCode, headers);
        this.bodyStream = null;
        this.bodyBytes = bytes;
    }

    /**
     * Creates an instance of {@link JdkHttpResponseSync}.
     *
     * @param request the request which resulted in this response.
     * @param streamResponse the JDK HttpClient response.
     * @param readTimeout The duration before a read operation times out.
     */
    public JdkHttpResponseSync(HttpRequest request, java.net.http.HttpResponse<InputStream> streamResponse,
        Duration readTimeout) {
        super(request, streamResponse.statusCode(), fromJdkHttpHeaders(streamResponse.headers()));
        this.bodyStream = new InputStreamWithReadTimeout(streamResponse.body(), readTimeout);
        this.bodyBytes = null;
    }

    @Override
    public Flux<ByteBuffer> getBody() {
        if (bodyBytes != null) {
            return Mono.fromSupplier(() -> ByteBuffer.wrap(bodyBytes)).flux();
        } else {
            return Flux.using(() -> this, ignored -> FluxUtil.toFluxByteBuffer(bodyStream), JdkHttpResponseSync::close);
        }
    }

    @Override
    public Mono<byte[]> getBodyAsByteArray() {
        if (bodyBytes != null) {
            return Mono.just(bodyBytes);
        } else {
            return super.getBodyAsByteArray();
        }
    }

    @Override
    public BinaryData getBodyAsBinaryData() {
        if (bodyBytes != null) {
            return BinaryData.fromBytes(bodyBytes);
        } else {
            // we shouldn't create multiple binary data instances for a single stream
            return getBinaryData();
        }
    }

    @Override
    public void writeBodyTo(WritableByteChannel channel) throws IOException {
        if (bodyBytes != null) {
            channel.write(ByteBuffer.wrap(bodyBytes));
        } else {
            int nRead;
            byte[] data = new byte[STREAM_READ_SIZE];
            while ((nRead = bodyStream.read(data, 0, data.length)) != -1) {
                channel.write(ByteBuffer.wrap(data, 0, nRead));
            }
            close();
        }
    }

    @Override
    public void close() {
        if (bodyStream != null && DISPOSED_UPDATER.compareAndSet(this, 0, 1)) {
            try {
                bodyStream.close();
            } catch (IOException e) {
                throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
            }
        }
    }

    @Override
    public HttpResponse buffer() {
        if (bodyBytes == null) {
            bodyBytes = getBytes();
            close();
        }

        return this; // This response is already buffered.
    }

    private byte[] getBytes() {
        try {
            ByteArrayOutputStream dataOutputBuffer = new ByteArrayOutputStream();
            int nRead;
            byte[] data = new byte[STREAM_READ_SIZE];
            while ((nRead = bodyStream.read(data, 0, data.length)) != -1) {
                dataOutputBuffer.write(data, 0, nRead);
            }
            return dataOutputBuffer.toByteArray();
        } catch (IOException ex) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(ex));
        }
    }

    private BinaryData getBinaryData() {
        if (binaryData == null) {
            binaryData = BinaryData.fromStream(bodyStream);
        }
        return binaryData;
    }
}

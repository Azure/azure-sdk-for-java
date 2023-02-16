// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk.httpclient;

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

import static com.azure.core.http.jdk.httpclient.JdkHttpClient.fromJdkHttpHeaders;

final class JdkHttpResponseSync extends JdkHttpResponseBase {
    private static final ClientLogger LOGGER = new ClientLogger(JdkHttpResponseSync.class);
    private BinaryData binaryData = null;
    public static final int STREAM_READ_SIZE = 8192;

    private final InputStream bodyStream;
    private byte[] bodyBytes;

    private volatile boolean disposed = false;
    JdkHttpResponseSync(final HttpRequest request, int statusCode, HttpHeaders headers, byte[] bytes) {
        super(request, statusCode, headers);
        this.bodyStream = null;
        this.bodyBytes = bytes;
    }

    JdkHttpResponseSync(final HttpRequest request, java.net.http.HttpResponse<InputStream> streamResponse) {
        super(request, streamResponse.statusCode(), fromJdkHttpHeaders(streamResponse.headers()));
        this.bodyStream = streamResponse.body();
        this.bodyBytes = null;
    }

    @Override
    public Flux<ByteBuffer> getBody() {
        if (bodyBytes != null) {
            return Mono.fromSupplier(() -> ByteBuffer.wrap(bodyBytes)).flux();
        } else {
            return FluxUtil.toFluxByteBuffer(bodyStream).doFinally(ignored -> close());
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
            try {
                int nRead;
                byte[] data = new byte[STREAM_READ_SIZE];
                while ((nRead = bodyStream.read(data, 0, data.length)) != -1) {
                    channel.write(ByteBuffer.wrap(data, 0, nRead));
                }
            } catch (IOException ex) {
                throw LOGGER.logExceptionAsError(new UncheckedIOException(ex));
            }

            close();
        }
    }

    @Override
    public void close() {
        if (!disposed && bodyStream != null) {
            disposed = true;
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

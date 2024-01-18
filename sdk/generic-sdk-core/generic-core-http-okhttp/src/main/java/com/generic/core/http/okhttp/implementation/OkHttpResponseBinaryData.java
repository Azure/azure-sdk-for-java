// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.generic.core.http.okhttp.implementation;

import com.generic.core.models.BinaryData;
import com.generic.core.models.ByteArrayBinaryData;
import com.generic.core.models.TypeReference;
import com.generic.core.util.serializer.ObjectSerializer;
import okhttp3.ResponseBody;
import okio.Okio;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.WritableByteChannel;
import java.util.Objects;

/**
 * {@link BinaryData} based on {@link okhttp3.Response}.
 */
public class OkHttpResponseBinaryData extends BinaryData {
    private final ResponseBody responseBody;
    private final Long contentLength;

    /**
     * Creates an instance of {@link OkHttpResponseBinaryData}.
     *
     * @param responseBody The response body that is used as the content for this instance.
     * @throws NullPointerException if {@code response} is null.
     */
    public OkHttpResponseBinaryData(ResponseBody responseBody) {
        this.responseBody = Objects.requireNonNull(responseBody, "'responseBody' cannot be null.");
        long contentLength = responseBody.contentLength();
        this.contentLength = (contentLength == -1) ? null : contentLength;
    }

    @Override
    public byte[] toBytes() {
        try {
            return responseBody.bytes();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public String toString() {
        try {
            return responseBody.string();
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }
    }

    @Override
    public <T> T toObject(TypeReference<T> typeReference, ObjectSerializer serializer) {
        return serializer.deserialize(toStream(), typeReference);
    }

    @Override
    public InputStream toStream() {
        return responseBody.byteStream();
    }

    @Override
    public ByteBuffer toByteBuffer() {
        return ByteBuffer.wrap(toBytes());
    }

    @Override
    public Long getLength() {
        return contentLength;
    }

    @Override
    public void writeTo(OutputStream outputStream) throws IOException {
        responseBody.source().readAll(Okio.sink(outputStream));
    }

    @Override
    public void writeTo(WritableByteChannel channel) throws IOException {
        writeTo(Channels.newOutputStream(channel));
    }

    @Override
    public boolean isReplayable() {
        return false;
    }

    @Override
    public BinaryData toReplayableBinaryData() {
        return new ByteArrayBinaryData(toBytes());
    }

    @Override
    public void close() throws IOException {
        responseBody.source().close();
    }
}

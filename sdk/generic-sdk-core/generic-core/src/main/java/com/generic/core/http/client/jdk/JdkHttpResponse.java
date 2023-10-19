// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.generic.core.http.client.jdk;

import com.generic.core.http.models.HttpRequest;
import com.generic.core.models.BinaryData;
import com.generic.core.models.Headers;
import com.generic.core.util.logging.ClientLogger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

final class JdkHttpResponse extends JdkHttpResponseBase {
    private static final ClientLogger LOGGER = new ClientLogger(JdkHttpResponse.class);
    private BinaryData binaryData = null;
    public static final int STREAM_READ_SIZE = 8192;

    private final InputStream bodyStream;
    private byte[] bodyBytes;

    private volatile boolean disposed = false;
    JdkHttpResponse(final HttpRequest request, int statusCode, Headers headers, byte[] bytes) {
        super(request, statusCode, headers);
        this.bodyStream = null;
        this.bodyBytes = bytes;
    }

    JdkHttpResponse(final HttpRequest request, java.net.http.HttpResponse<InputStream> streamResponse) {
        super(request, streamResponse.statusCode(), null);
        this.bodyStream = streamResponse.body();
        this.bodyBytes = null;
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

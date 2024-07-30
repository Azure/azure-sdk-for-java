// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.http.okhttp3.implementation;

import io.clientcore.core.util.ClientLogger;
import io.clientcore.core.util.binarydata.InputStreamBinaryData;
import okhttp3.MediaType;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * An {@link okhttp3.RequestBody} subtype that sends {@link InputStreamBinaryData} in an unbuffered manner.
 */
public class OkHttpInputStreamRequestBody extends OkHttpStreamableRequestBody<InputStreamBinaryData> {

    private static final ClientLogger LOGGER = new ClientLogger(OkHttpInputStreamRequestBody.class);

    private final AtomicBoolean bodySent = new AtomicBoolean(false);

    public OkHttpInputStreamRequestBody(InputStreamBinaryData content, long effectiveContentLength,
        MediaType mediaType) {
        super(content, effectiveContentLength, mediaType);
    }

    @Override
    public void writeTo(BufferedSink bufferedSink) throws IOException {
        if (bodySent.compareAndSet(false, true)) {
            Source source = Okio.source(content.toStream());

            bufferedSink.writeAll(source);
        } else {
            // Prevent OkHttp from potentially re-sending non-repeatable body outside of retry policies.
            throw LOGGER.logThrowableAsError(new IOException("Re-attempt to send InputStream body is not supported."));
        }
    }
}

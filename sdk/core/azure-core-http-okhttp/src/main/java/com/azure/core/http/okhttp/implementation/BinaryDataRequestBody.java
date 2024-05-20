// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.core.http.okhttp.implementation;

import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.BufferedSink;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * Implementation of {@link RequestBody} where the content is a {@link BinaryData}.
 */
public class BinaryDataRequestBody extends RequestBody {
    private static final ClientLogger LOGGER = new ClientLogger(BinaryDataRequestBody.class);

    private final MediaType contentType;
    private final BinaryData body;
    private final long effectiveContentLength;

    private volatile int bodySent = 0;
    private static final AtomicIntegerFieldUpdater<BinaryDataRequestBody> BODY_SENT_UPDATER
        = AtomicIntegerFieldUpdater.newUpdater(BinaryDataRequestBody.class, "bodySent");

    /**
     * Creates a new instance of the BinaryDataRequestBody class.
     *
     * @param body The {@link BinaryData} to use as the body.
     * @param contentType The content type of the body.
     * @param effectiveContentLength The length of the body.
     */
    public BinaryDataRequestBody(BinaryData body, MediaType contentType, long effectiveContentLength) {
        this.body = body;
        this.contentType = contentType;
        this.effectiveContentLength = effectiveContentLength;
    }

    @Override
    public long contentLength() throws IOException {
        return effectiveContentLength;
    }

    @Override
    public boolean isOneShot() {
        return !body.isReplayable();
    }

    @Override
    public MediaType contentType() {
        return contentType;
    }

    @Override
    public void writeTo(BufferedSink bufferedSink) throws IOException {
        if (!body.isReplayable() && !BODY_SENT_UPDATER.compareAndSet(this, 0, 1)) {
            // Prevent OkHttp from potentially re-sending non-repeatable body outside of retry policies.
            throw LOGGER.logThrowableAsError(new IOException("Re-attempt to send body is not supported."));
        } else {
            body.writeTo(bufferedSink);
        }
    }
}

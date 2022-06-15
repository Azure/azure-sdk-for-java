// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp.implementation;

import com.azure.core.util.ProgressReporter;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okio.Buffer;
import okio.BufferedSink;
import okio.ForwardingSink;
import okio.Okio;
import okio.Sink;

import java.io.IOException;
import java.util.Objects;

/**
 * An {@link okhttp3.RequestBody} subtype that adds progress
 * reporting functionality on top of other {@link okhttp3.RequestBody}.
 */
public class OkHttpProgressReportingRequestBody extends RequestBody {
    private final RequestBody delegate;
    private final ProgressReporter progressReporter;

    public OkHttpProgressReportingRequestBody(RequestBody delegate, ProgressReporter progressReporter) {
        this.delegate = Objects.requireNonNull(delegate, "'delegate' must not be null");
        this.progressReporter = Objects.requireNonNull(progressReporter, "'progressReporter' must not be null");
    }

    @Override
    public MediaType contentType() {
        return delegate.contentType();
    }

    @Override
    public long contentLength() throws IOException {
        return delegate.contentLength();
    }

    @Override
    public boolean isOneShot() {
        return delegate.isOneShot();
    }

    @Override
    public boolean isDuplex() {
        return delegate.isDuplex();
    }

    @Override
    public void writeTo(BufferedSink sink) throws IOException {
        BufferedSink bufferedSink;

        CountingSink countingSink = new CountingSink(sink, progressReporter);
        bufferedSink = Okio.buffer(countingSink);

        delegate.writeTo(bufferedSink);

        bufferedSink.flush();
    }

    private static final class CountingSink extends ForwardingSink {
        private final ProgressReporter progressReporter;

        CountingSink(Sink delegate, ProgressReporter progressReporter) {
            super(delegate);
            this.progressReporter = progressReporter;
        }

        @Override
        public void write(Buffer source, long byteCount) throws IOException {
            super.write(source, byteCount);
            progressReporter.reportProgress(byteCount);
        }
    }
}

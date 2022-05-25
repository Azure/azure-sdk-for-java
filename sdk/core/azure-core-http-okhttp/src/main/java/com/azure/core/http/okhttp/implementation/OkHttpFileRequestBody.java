// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.okhttp.implementation;

import com.azure.core.implementation.util.FileContent;
import okhttp3.MediaType;
import okio.BufferedSink;

import java.io.IOException;
import java.nio.channels.FileChannel;
import java.nio.file.StandardOpenOption;

/**
 * An {@link okhttp3.RequestBody} subtype that sends {@link FileContent} in an unbuffered manner.
 */
public class OkHttpFileRequestBody extends OkHttpStreamableRequestBody<FileContent> {

    public OkHttpFileRequestBody(FileContent content, long effectiveContentLength, MediaType mediaType) {
        super(content, effectiveContentLength, mediaType);
    }

    @Override
    public void writeTo(BufferedSink bufferedSink) throws IOException {
        long count = effectiveContentLength;
        if (count < 0) {
            // OkHttp marks chunked encoding as -1.
            // The content length is not specified so sending all remaining content.
            count = Long.MAX_VALUE;
        }
        // RequestBody.create(File) does not support position and length.
        // BufferedSink implements WritableByteChannel so we can leverage FileChannel as source.
        // FileChannel supports positional reads.
        try (FileChannel channel = FileChannel.open(content.getFile(), StandardOpenOption.READ)) {
            channel.transferTo(0, count, bufferedSink);
        }
    }
}

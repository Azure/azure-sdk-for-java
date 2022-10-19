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
        // RequestBody.create(File) does not support position and length.
        // BufferedSink implements WritableByteChannel so we can leverage FileChannel as source.
        // FileChannel supports positional reads.
        try (FileChannel channel = FileChannel.open(content.getFile(), StandardOpenOption.READ)) {
            // FileContent.getLength always returns non-null.
            long pendingTransfer = content.getLength();
            long position = content.getPosition();
            do {
                long transferred = channel.transferTo(position, pendingTransfer, bufferedSink);
                if (transferred < 0) {
                    break;
                }
                position += transferred;
                pendingTransfer -= transferred;
            } while (pendingTransfer > 0);
        }
    }
}

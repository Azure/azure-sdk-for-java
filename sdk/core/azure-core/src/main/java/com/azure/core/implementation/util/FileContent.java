// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.util.RequestContent;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.Exceptions;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;

/**
 * A {@link RequestContent} implementation which is backed by a file.
 */
public final class FileContent implements RequestContent {
    private final ClientLogger logger = new ClientLogger(FileContent.class);

    private final Path file;
    private final long offset;
    private final long length;

    /**
     * Creates a new instance of {@link FileContent}.
     *
     * @param file The {@link Path} content.
     * @param offset The offset in the {@link Path} to begin reading data.
     * @param length The length of the content.
     */
    public FileContent(Path file, long offset, long length) {
        this.file = file;
        this.offset = offset;
        this.length = length;
    }

    @Override
    public Flux<ByteBuffer> asFluxByteBuffer() {
        return Flux.using(() -> FileChannel.open(file), channel -> Flux.generate(() -> 0, (count, sink) -> {
            if (count == length) {
                sink.complete();
                return count;
            }

            int readCount = (int) Math.min(8096, length - count);
            try {
                sink.next(channel.map(FileChannel.MapMode.READ_ONLY, offset + count, readCount));
            } catch (IOException ex) {
                sink.error(ex);
            }

            return count + readCount;
        }), channel -> {
            try {
                channel.close();
            } catch (IOException ex) {
                throw logger.logExceptionAsError(Exceptions.propagate(ex));
            }
        });
    }

    @Override
    public Long getLength() {
        return length;
    }
}

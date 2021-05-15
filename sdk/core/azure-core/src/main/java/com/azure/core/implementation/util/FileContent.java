// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.util;

import com.azure.core.util.FluxUtil;
import com.azure.core.util.RequestContent;
import com.azure.core.util.RequestOutbound;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Flux;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
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
    public void writeTo(RequestOutbound requestOutbound) {
        try {
            FileChannel.open(file).transferTo(offset, length, requestOutbound.getRequestChannel());
        } catch (IOException e) {
            throw logger.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    @Override
    public Flux<ByteBuffer> asFluxByteBuffer() {
        return Flux.using(() -> AsynchronousFileChannel.open(file), FluxUtil::readFile, channel -> {
            try {
                channel.close();
            } catch (IOException ex) {
                throw logger.logExceptionAsError(new UncheckedIOException(ex));
            }
        });
    }

    @Override
    public Long getLength() {
        return length;
    }
}

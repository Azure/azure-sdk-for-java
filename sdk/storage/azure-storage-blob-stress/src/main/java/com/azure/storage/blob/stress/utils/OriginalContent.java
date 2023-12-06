// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.stress.utils;

import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.tracing.Tracer;
import com.azure.core.util.tracing.TracerProvider;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.stress.CrcInputStream;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.CRC32;

import static com.azure.core.util.FluxUtil.monoError;

public class OriginalContent {
    private final static ClientLogger LOGGER = new ClientLogger(OriginalContent.class);
    private final static Tracer TRACER = TracerProvider.getDefaultProvider().createTracer("unused", null, null, null);
    private static final String BLOB_CONTENT_HEAD_STRING = "Lorem ipsum dolor sit amet, consectetur adipiscing elit, "
        + "sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. "
        + "Pellentesque elit ullamcorper dignissim cras tincidunt lobortis feugiat vivamus. Massa sapien faucibus et molestie ac feugiat sed lectus. "
        + "Sed pulvinar proin gravida hendrerit.";

    private static final BinaryData BLOB_CONTENT_HEAD = BinaryData.fromString(BLOB_CONTENT_HEAD_STRING);
    private long dataChecksum = -1;
    private long blobSize = 0;

    public OriginalContent() {
    }

    public Mono<Void> setupBlob(BlobAsyncClient blobClient, long blobSize) {
        if (dataChecksum != -1) {
            throw LOGGER.logExceptionAsError(new IllegalStateException("setupBlob can't be called again"));
        }

        this.blobSize = blobSize;
        return Mono.using(() -> new CrcInputStream(BLOB_CONTENT_HEAD, blobSize),
            data -> blobClient
                .upload(BinaryData.fromStream(data))
                .doFinally(i -> dataChecksum = data.getCrc()),
                CrcInputStream::close)
            .then();
    }

    public Mono<Boolean> checkMatch(Path downloadPath, Context span) {
        if (dataChecksum == -1) {
            return monoError(LOGGER, new IllegalStateException("setupBlob must complete first"));
        }

        return calculateCrc(BinaryData.fromFile(downloadPath).toFluxByteBuffer()).map(crc -> {
                if (crc != dataChecksum) {
                    try(AutoCloseable scope = TRACER.makeSpanCurrent(span)) {
                        logMismatch(crc, getFileSize(downloadPath), readHead(downloadPath));
                    } catch (Exception e) {
                        throw LOGGER.logExceptionAsError(new RuntimeException(e));
                    }
                    return false;
                }
                return true;
            });
   }

   public Mono<Boolean> checkMatch(Flux<ByteBuffer> data, Context span) {
       if (dataChecksum == -1) {
           return monoError(LOGGER, new IllegalStateException("setupBlob must complete first"));
       }
       return calculateCrc(data)
           .map(crc -> {
               if (crc != dataChecksum) {
                   try(AutoCloseable scope = TRACER.makeSpanCurrent(span)) {
                       logMismatch(crc, blobSize, BLOB_CONTENT_HEAD_STRING.getBytes(StandardCharsets.UTF_8));
                   } catch (Exception e) {
                       throw LOGGER.logExceptionAsError(new RuntimeException(e));
                   }
                   return false;
               }
               return true;
           });
   }

    public Mono<Long> calculateCrc(Flux<ByteBuffer> data) {
        return data
            .reduce(new CRC32(),
                (crc, bb) -> {
                    crc.update(bb);
                    return crc;
                })
            .map(CRC32::getValue);
    }

    private void logMismatch(long actualCrc, long actualLength, byte[] actualContentHead) {
        // future: if mismatch, compare against original file
        LOGGER.atError()
            .addKeyValue("expectedCrc", dataChecksum)
            .addKeyValue("actualCrc", actualCrc)
            .addKeyValue("expectedLength", blobSize)
            .addKeyValue("actualLength", actualLength)
            .addKeyValue("actualContentHead", new String(actualContentHead, StandardCharsets.UTF_8))
            .log("mismatched crc");
    }

    private static long getFileSize(Path path) {
        try {
            return Files.size(path);
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    private byte[] readHead(Path path) {
        int len = (int)Math.min(blobSize, 1024L);
        try (InputStream file = Files.newInputStream(path)) {
            byte[] buf = new byte[len];
            int pos = 0;
            int read;
            while (pos < len && (read = file.read(buf, pos, len)) != -1) {
                pos += read;
            }
            return buf;
        }
        catch (IOException e) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    public Flux<ByteBuffer> convertInputStreamToFluxByteBuffer(InputStream inputStream) {
        byte[] buffer = new byte[4096];
        int b;
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            while ((b = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, b);
            }
        } catch (IOException ex) {
            throw new UncheckedIOException(ex);
        }

        return Flux.just(ByteBuffer.wrap(outputStream.toByteArray()));
    }

    public Flux<ByteBuffer> copySeekableByteChannelToFluxByteBuffer(SeekableByteChannel src) throws IOException {
        int read;
        byte[] temp = new byte[4096];
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ByteBuffer bb = ByteBuffer.wrap(temp);
        while ((read = src.read(bb)) != -1) {
            outputStream.write(temp, 0, read);
            bb.clear();
        }
        return Flux.just(ByteBuffer.wrap(outputStream.toByteArray()));
    }
}

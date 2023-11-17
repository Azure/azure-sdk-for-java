// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.stress.utils;

import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.BlobAsyncClient;
import com.azure.storage.stress.RandomInputStream;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.zip.CRC32;

import static com.azure.core.util.FluxUtil.monoError;

public class OriginalContent {
    private final static ClientLogger LOGGER = new ClientLogger(OriginalContent.class);
    private ByteBuffer contentHead = null;
    private long dataChecksum = -1;
    private long blobSize = 0;

    private boolean locked = false;
    public OriginalContent() {
    }

    public Mono<Void> setupBlob(BlobAsyncClient blobClient, long blobSize, int blobPrintableSize) {
        if (locked) {
            throw LOGGER.logExceptionAsError(new IllegalStateException("setupBlob can't be called again"));
        }

        this.blobSize = blobSize;
        locked = true;
        RandomInputStream data = new RandomInputStream(blobSize, blobPrintableSize);
        return blobClient.upload(BinaryData.fromStream(data))
            .map(i -> {
                dataChecksum = data.getCrc().getValue();
                contentHead = data.getContentHead();
                try {
                    data.close();
                } catch (IOException e) {
                    return monoError(LOGGER, new UncheckedIOException(e));
                }
                return i;
            })
            .then();
    }

    public boolean checkMatch(CRC32 otherCrc, Long otherLength, ByteBuffer otherContentHead) {
        if (!locked) {
            throw LOGGER.logExceptionAsError(new IllegalStateException("setupBlob must be called first"));
        }
        long crc = otherCrc.getValue();
        if (crc != dataChecksum) {
            logMismatch(crc, otherLength, otherContentHead);
            return false;
        }
        return true;
    }

    private void logMismatch(long actualCrc, long actualLength, ByteBuffer actualContentHead) {
        // future: if mismatch, compare against original file
        LOGGER.atError()
            .addKeyValue("expectedCrc", dataChecksum)
            .addKeyValue("actualCrc", actualCrc)
            .addKeyValue("expectedLength", blobSize)
            .addKeyValue("actualLength", actualLength)
            .addKeyValue("originalContentHead", () -> Base64.getEncoder().encodeToString(contentHead.array()))
            .addKeyValue("actualContentHead", () -> Base64.getEncoder().encodeToString(actualContentHead.array()))
            .log("mismatched crc");
    }
}

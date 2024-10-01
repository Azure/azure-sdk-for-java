// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.share;

import com.azure.storage.common.StorageOutputStream;
import com.azure.storage.common.implementation.Constants;
import com.azure.storage.file.share.models.ShareFileUploadRangeOptions;
import com.azure.storage.file.share.models.ShareStorageException;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Arrays;

/**
 * Provides an output stream to write a given storage file resource.
 */
public class StorageFileOutputStream extends StorageOutputStream {
    private long offsetPos;

    private final ShareFileClient client;

    StorageFileOutputStream(final ShareFileClient client, long offsetPos) {
        super(4 * Constants.MB);
        this.client = client;
        this.offsetPos = offsetPos;
    }

    private void uploadData(byte[] inputData, int writeLength, long offset) throws IOException {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(inputData, 0, writeLength);
            ShareFileUploadRangeOptions options = new ShareFileUploadRangeOptions(inputStream, writeLength).setOffset(offset);
            client.uploadRangeWithResponse(options, null, null);
        } catch (ShareStorageException e) {
            this.lastError = new IOException(e);
            throw this.lastError; // Ensure the exception is propagated
        }
    }

    @Override
    protected Mono<Void> dispatchWrite(byte[] data, int writeLength, long offset) {
        if (writeLength == 0) {
            return Mono.empty();
        }
        return Mono.fromRunnable(() -> {
            try {
                // Calculate the correct file offset before uploading data
                long fileOffset = this.offsetPos;
                this.offsetPos += writeLength;  // Update the global offset after writing

                this.uploadData(Arrays.copyOfRange(data, (int) offset, (int) (offset + writeLength)), writeLength, fileOffset);  // Perform the upload
            } catch (IOException e) {
                this.lastError = e;  // Capture the IOException
                throw new RuntimeException(e);  // Wrap it in an unchecked exception to allow propagation
            }
        }).onErrorMap(RuntimeException.class, ex -> {
            // Handle the IOException and other exceptions as needed
            if (ex.getCause() instanceof IOException) {
                throw new UncheckedIOException((IOException) ex.getCause()); // Propagate the IOException if that was the cause
            }
            throw ex;  // Propagate any other exception
        }).then();
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.perf;

import com.azure.core.implementation.ReflectionUtilsApi;
import com.azure.perf.test.core.NullOutputStream;
import com.azure.storage.blob.models.BlobDownloadAsyncResponse;
import com.azure.storage.blob.perf.core.AbstractDownloadTest;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.invoke.MethodHandle;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousByteChannel;
import java.nio.channels.CompletionHandler;
import java.util.Arrays;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;

public class DownloadBlobTest extends AbstractDownloadTest<BlobPerfStressOptions> {

    private static final MethodHandle NEW_DOWNLOAD;

    static {
        NEW_DOWNLOAD = Arrays.stream(BlobDownloadAsyncResponse.class.getDeclaredMethods())
            .filter(method -> method.getName().equals("writeValueToAsync"))
            .findFirst()
            .map(method -> {
                try {
                    return ReflectionUtilsApi.INSTANCE.getLookupToUse(BlobDownloadAsyncResponse.class).unreflect(method);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).orElse(null);
    }

    private static final int BUFFER_SIZE = 16 * 1024 * 1024;
    private static final OutputStream DEV_NULL = new NullOutputStream();
    private final AsynchronousByteChannel DEV_NULL_CHANNEL = new BlackHoleAsynchronousByteChannel();

    private final byte[] buffer = new byte[BUFFER_SIZE];

    public DownloadBlobTest(BlobPerfStressOptions options) {
        super(options);
    }


    // Perform the API call to be tested here
    @Override
    public void run() {
        blobClient.download(DEV_NULL);
    }


    @Override
    public Mono<Void> runAsync() {
        if (NEW_DOWNLOAD != null) {
            return blobAsyncClient.downloadStreamWithResponse(null, null, null, false)
                .flatMap(response -> {
                    try {
                        return (Mono<Void>) NEW_DOWNLOAD.invokeWithArguments(response, DEV_NULL_CHANNEL, null);
                    } catch (Throwable e) {
                        return Mono.error(e);
                    }
                });
        } else {
            return blobAsyncClient.downloadStream()
                .map(b -> {
                    int readCount = 0;
                    int remaining = b.remaining();
                    while (readCount < remaining) {
                        int expectedReadCount = Math.min(remaining - readCount, BUFFER_SIZE);
                        b.get(buffer, 0, expectedReadCount);
                        readCount += expectedReadCount;
                    }

                    return 1;
                }).then();
        }
    }

    private final class BlackHoleAsynchronousByteChannel implements AsynchronousByteChannel {

        @Override
        public <A> void read(ByteBuffer dst, A attachment, CompletionHandler<Integer, ? super A> handler) {
            handler.failed(new UnsupportedOperationException(), attachment);
        }

        @Override
        public Future<Integer> read(ByteBuffer dst) {
            CompletableFuture<Integer> future = new CompletableFuture<>();
            future.completeExceptionally(new UnsupportedOperationException());
            return future;
        }

        @Override
        public <A> void write(ByteBuffer src, A attachment, CompletionHandler<Integer, ? super A> handler) {
            int readCount = 0;
            int remaining = src.remaining();
            while (readCount < remaining) {
                int expectedReadCount = Math.min(remaining - readCount, BUFFER_SIZE);
                src.get(buffer, 0, expectedReadCount);
                readCount += expectedReadCount;
            }
            handler.completed(readCount, attachment);
        }

        @Override
        public Future<Integer> write(ByteBuffer src) {
            CompletableFuture<Integer> future = new CompletableFuture<>();
            future.completeExceptionally(new UnsupportedOperationException());
            return future;
        }

        @Override
        public boolean isOpen() {
            return true;
        }

        @Override
        public void close() throws IOException {

        }
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.quickquery.implementation.util;

import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.blob.models.BlobStorageException;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Subscribes to a Flux coming in from the network and allows data to be read.
 */
public class NetworkInputStream extends InputStream {

    private ClientLogger logger;

    private Flux<ByteBuffer> data;


    private Subscription subscription; // Subscription to request more data from as needed

    private ByteArrayInputStream buffer;

    private boolean subscribed;
    private boolean fluxComplete;
    private boolean waiting;

    /**
     * Creates a new NetworkInputStream from the Flux.
     * @param data The flux to read the data from.
     */
    public NetworkInputStream(Flux<ByteBuffer> data, ClientLogger logger) {
        this.subscribed = false;
        this.fluxComplete = false;
        this.waiting = false;
        this.data = data;
        this.logger = logger;
    }

    @Override
    public int read() throws IOException {
        return read(new byte[1], 0, 1);
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        // Not subscribed? subscribe
        if (!subscribed) {
            waiting = true;
            subscribeToData();
        }

        // Subscribed

        // Right after the first subscription, data may or may not be available.
        // The user stream will never have been initialized if data is not available on first subscription, return 0.
        if (this.buffer == null) {
            return 0;
        } else {
            // Middle of stream.
            // End of last buffer read, no more data available.
            if (this.buffer.available() == 0) {
                // If the flux completed, there is no more data available to be read from the stream. Return -1.
                if (this.fluxComplete) {
                    return -1;
                }
                // If we are not waiting for another request to come in, request more data.
                if (!waiting) {
                    waiting = true;
                    subscription.request(1);
                }
                return 0; // Wait for more data to come in.
            } else if (this.buffer.available() > 0) {
                // Data available in buffer, read the buffer.
                return this.buffer.read(b, off, len);
            }
        }
        return 0;
    }

    @Override
    public void close() throws IOException {
        super.close();
    }

    private void subscribeToData() {
        this.data
            .subscribe(
            // ByteBuffer consumer
            byteBuffer -> {
                this.buffer = new ByteArrayInputStream(FluxUtil.byteBufferToArray(byteBuffer));
                this.waiting = false;
            },
            // Error consumer
            throwable -> {
                this.fluxComplete = true;
                this.waiting = false;
                if (throwable instanceof BlobStorageException) {
                    throw logger.logExceptionAsError((BlobStorageException) throwable);
                } else if (throwable instanceof IllegalArgumentException) {
                    throw logger.logExceptionAsError((IllegalArgumentException) throwable);
                }
            },
            // Complete consumer
            () -> {
                this.fluxComplete = true;
                this.waiting = false;
            },
            // Subscription consumer
            subscription -> {
                this.subscription = subscription;
                this.subscribed = true;
                this.subscription.request(1);
            }
        );
    }
}

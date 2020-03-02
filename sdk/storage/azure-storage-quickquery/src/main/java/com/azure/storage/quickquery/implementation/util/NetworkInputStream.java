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

/**
 * Subscribes to a Flux coming in from the network and allows data to be read.
 */
public class NetworkInputStream extends InputStream {

    private ClientLogger logger;

    private Flux<ByteBuffer> data;

    private Subscription subscription; // Subscription to request more data from as needed

    private ByteArrayInputStream userStream;

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
            subscribeToData();
        }

        // Data may or may not be available
        if (this.userStream == null) {
            return 0;
        } else {
            // End of last chunk - request more data
            if (this.userStream.available() == 0) {
                if (this.fluxComplete) {
                    return -1;
                }
                if (!waiting) {
                    waiting = true;
                    subscription.request(1);
                    return 0; // Wait for data to come in.
                }
            } else if (this.userStream.available() > 0) { // Data is in buffer - read and return
                return this.userStream.read(b, off, len);
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
                this.userStream = new ByteArrayInputStream(FluxUtil.byteBufferToArray(byteBuffer));
                this.waiting = false;
            },
            // Error consumer
            throwable -> {
                this.fluxComplete = true;
                if (throwable instanceof BlobStorageException) {
                    throw logger.logExceptionAsError((BlobStorageException) throwable);
                } else if (throwable instanceof IllegalArgumentException) {
                    throw logger.logExceptionAsError((IllegalArgumentException) throwable);
                }
            },
            // Complete consumer
            () -> this.fluxComplete = true,
            // Subscription consumer
            subscription -> {
                this.subscription = subscription;
                this.subscribed = true;
                this.subscription.request(1);
            }
        );
    }
}

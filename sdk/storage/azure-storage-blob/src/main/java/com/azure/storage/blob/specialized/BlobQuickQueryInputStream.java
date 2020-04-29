package com.azure.storage.blob.specialized;

import com.azure.storage.common.implementation.FluxInputStream;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;

public class BlobQuickQueryInputStream extends FluxInputStream {
    /**
     * Creates a new FluxInputStream
     *
     * @param data The data to subscribe to and read from.
     */
    public BlobQuickQueryInputStream(Flux<ByteBuffer> data) {
        super(data);
    }
}

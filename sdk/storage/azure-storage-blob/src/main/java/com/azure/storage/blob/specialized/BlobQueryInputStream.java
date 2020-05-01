// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.specialized;

import com.azure.storage.common.implementation.FluxInputStream;
import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;

/**
 * An <code>InputStream</code> interface that represents the stream to use for reading the query response.
 */
public class BlobQueryInputStream extends FluxInputStream {
    /**
     * Creates a new BlobQueryInputStream
     *
     * @param data The data to subscribe to and read from.
     */
    public BlobQueryInputStream(Flux<ByteBuffer> data) {
        super(data);
    }
}

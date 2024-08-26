// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.internal.avro.implementation;

/**
 * An interface that represents an AvroSyncReader.
 */
public interface AvroSyncReader {
    /**
     * Read a stream of {@link AvroObject}.
     *
     * @return An Iterable of {@link AvroObject}.
     */
    Iterable<AvroObject> read();
}

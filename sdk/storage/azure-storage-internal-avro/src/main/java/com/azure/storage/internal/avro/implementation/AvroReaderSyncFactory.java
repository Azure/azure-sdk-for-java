// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.internal.avro.implementation;

import java.nio.ByteBuffer;

/**
 * Factory class for {@link AvroSyncReader}.
 */
public class AvroReaderSyncFactory {

    /**
     * Gets a new instance of {@link AvroReader}.
     *
     * @param data The ByteBuffer that contains the Avro data.
     * @return An AvroReader.
     */
    public AvroSyncReader getAvroReader(ByteBuffer data) {
        return new AvroSyncReader() {
            private final AvroSyncParser parser = new AvroSyncParser();
            @Override
            public Iterable<AvroObject> read() {
                return parser.parse(data);
            }
        };
    }
}

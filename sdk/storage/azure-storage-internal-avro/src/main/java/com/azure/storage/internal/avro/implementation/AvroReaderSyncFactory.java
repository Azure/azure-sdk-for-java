// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.internal.avro.implementation;

import java.nio.ByteBuffer;

/**
 * Factory class for {@link AvroSyncReader}.
 */
public class AvroReaderSyncFactory {

    /**
     * Gets a new instance of {@link AvroSyncReader} with support for offset and thresholdIndex.
     *
     * @param data The ByteBuffer that contains the Avro data.
     * @param offset The position in the ByteBuffer from where to start parsing.
     * @param thresholdIndex The minimum index of the objects to be returned.
     * @return An AvroSyncReader.
     */
    public AvroSyncReader getAvroReader(ByteBuffer data, int offset, long thresholdIndex) {
        return new AvroSyncReader() {
            private final AvroSyncParser parser = new AvroSyncParser(true); // assuming true means handling partial reads

            @Override
            public Iterable<AvroObject> read() {
                // Set the position of the buffer to the offset
                data.position(offset);

                // Parse the header
                parser.parse(data);

                // Prepare parser to read the body at an offset
                parser.prepareParserToReadBody(offset, thresholdIndex);

                // Return iterable of parsed objects
                return parser.parse(data);
            }
        };
    }

    /**
     * Gets a new instance of {@link AvroSyncReader}.
     *
     * @param data The ByteBuffer that contains the Avro data.
     * @return An AvroReader.
     */
    public AvroSyncReader getAvroReader(ByteBuffer data) {
        return new AvroSyncReader() {
            private final AvroSyncParser parser = new AvroSyncParser(false);
            @Override
            public Iterable<AvroObject> read() {
                return parser.parse(data);
            }
        };
    }


}

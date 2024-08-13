// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.internal.avro.implementation;

import reactor.core.publisher.Flux;

import java.nio.ByteBuffer;

/**
 * Factory class for {@link AvroSyncReader}.
 */
public class AvroReaderSyncFactory {

    /**
     * Gets a new instance of {@link AvroSyncReader} with support for offset and thresholdIndex.
     *
     * @param avroHeader The ByteBuffer containing the Avro header.
     * @param avroBody The ByteBuffer that starts at the offset and represents the start of a block.
     * @param offset The position in the ByteBuffer from where to start parsing.
     * @param thresholdIndex The minimum index of the objects to be returned.
     * @return An AvroSyncReader.
     */
    public AvroSyncReader getAvroReader(ByteBuffer avroHeader, ByteBuffer avroBody, long offset, long thresholdIndex) {
        return new AvroSyncReader() {
            private final AvroSyncParser parser = new AvroSyncParser(true); // assuming true means handling partial reads

            @Override
            public Iterable<AvroObject> read() {
                // Parse the header
                avroHeader.position(0); // Ensure the header buffer is at the start
                parser.parse(avroHeader);

                // Prepare parser to read the body at an offset, only if necessary
                parser.prepareParserToReadBody(offset, thresholdIndex);

                // Set the position of the body buffer to the offset
                avroBody.position((int) offset);

                // Return iterable of parsed objects from the body
                return parser.parse(avroBody);
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

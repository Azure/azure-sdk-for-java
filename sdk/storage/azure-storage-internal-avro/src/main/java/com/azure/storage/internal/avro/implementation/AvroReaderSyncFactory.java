// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.internal.avro.implementation;

import java.nio.ByteBuffer;

/**
 * Factory class for {@link AvroSyncReader}.
 */
public class AvroReaderSyncFactory {

    /**
     * An internal static inner class that implements AvroSyncReader.
     */
    private static class InternalAvroSyncReader implements AvroSyncReader {
        private final AvroSyncParser parser;
        private final ByteBuffer avroHeader;
        private final ByteBuffer avroBody;
        private final long offset;
        private final long thresholdIndex;

        InternalAvroSyncReader(AvroSyncParser parser, ByteBuffer avroHeader, ByteBuffer avroBody, long offset, long thresholdIndex) {
            this.parser = parser;
            this.avroHeader = avroHeader;
            this.avroBody = avroBody;
            this.offset = offset;
            this.thresholdIndex = thresholdIndex;
        }

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
    }

    /**
     * Gets a new instance of {@link AvroSyncReader} with support for offset and thresholdIndex.
     */
    public AvroSyncReader getAvroReader(ByteBuffer avroHeader, ByteBuffer avroBody, long offset, long thresholdIndex) {
        AvroSyncParser parser = new AvroSyncParser(true);
        return new InternalAvroSyncReader(parser, avroHeader, avroBody, offset, thresholdIndex);
    }

    /**
     * Gets a new instance of {@link AvroSyncReader}.
     */
    public AvroSyncReader getAvroReader(ByteBuffer data) {
        AvroSyncParser parser = new AvroSyncParser(false);
        return () -> parser.parse(data);
    }
}

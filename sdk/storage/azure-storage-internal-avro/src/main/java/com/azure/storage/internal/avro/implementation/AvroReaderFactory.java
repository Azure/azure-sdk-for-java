// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.internal.avro.implementation;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

/**
 * Factory class for {@link AvroReader}.
 */
public class AvroReaderFactory {

    /**
     * Gets a new instance of {@link AvroReader}.
     *
     * @param avroHeader A reactive stream that contains the Avro header.
     * @param avroBody A reactive stream that starts at the offset and represents the start of a block.
     * @param offset The body offset.
     * @param thresholdIndex The inclusive index after which to start returning objects.
     * @return An AvroReader.
     */
    public AvroReader getAvroReader(Flux<ByteBuffer> avroHeader, Flux<ByteBuffer> avroBody, long offset,
        long thresholdIndex) {
        return () -> {
            AvroParser parser = new AvroParser(true);
            /* Parse the header. */
            return avroHeader.concatMap(parser::parse)
                /* Prepare the parser to read the body at an offset.*/
                .then(Mono.defer(() -> parser.prepareParserToReadBody(offset, thresholdIndex)))
                /* Parse the body. */
                .thenMany(avroBody.concatMap(parser::parse));
        };
    }

    /**
     * Gets a new instance of {@link AvroReader}.
     *
     * @param avro A reactive stream that contains the Avro data.
     * @return An AvroReader.
     */
    public AvroReader getAvroReader(Flux<ByteBuffer> avro) {
        return () -> {
            AvroParser parser = new AvroParser(false);
            /* Parse the header. */
            return avro.concatMap(parser::parse);
        };
    }
}

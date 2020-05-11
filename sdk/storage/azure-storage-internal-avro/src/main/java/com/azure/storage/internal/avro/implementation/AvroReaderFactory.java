package com.azure.storage.internal.avro.implementation;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

public class AvroReaderFactory {

    /* Expect in code. */
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

    public AvroReader getAvroReader(Flux<ByteBuffer> avro) {
        return () -> {
            AvroParser parser = new AvroParser(false);
            /* Parse the header. */
            return avro.concatMap(parser::parse);
        };
    }
}

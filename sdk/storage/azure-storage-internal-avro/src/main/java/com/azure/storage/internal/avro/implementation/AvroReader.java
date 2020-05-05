package com.azure.storage.internal.avro.implementation;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;

public class AvroReader {

    public static Flux<AvroObject> readAvro(Flux<ByteBuffer> avroHeader, Flux<ByteBuffer> avroBody, long bodyOffset) {
        AvroParser parser = new AvroParser(true);
                /* Parse the header. */
        return avroHeader.concatMap(parser::parse)
                /* Prepare the parser to read the body at an offset.*/
                .then(Mono.defer(() -> parser.prepareParserToReadBody(bodyOffset)))
                /* Parse the body. */
                .thenMany(avroBody.concatMap(parser::parse)); /* TODO: (gapra) Does this need to be deferred? */
    }

    public static Flux<AvroObject> readAvro(Flux<ByteBuffer> avro) {
        AvroParser parser = new AvroParser(false);
        return avro.concatMap(parser::parse);
    }
}

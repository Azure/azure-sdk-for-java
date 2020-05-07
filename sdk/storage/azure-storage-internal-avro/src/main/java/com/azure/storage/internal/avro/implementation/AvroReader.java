package com.azure.storage.internal.avro.implementation;

import reactor.core.publisher.Flux;

public interface AvroReader {
    Flux<AvroObject> readAvroObjects();
}

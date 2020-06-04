// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.internal.avro.implementation;

import reactor.core.publisher.Flux;

/**
 * An interface that represents an AvroReader.
 */
public interface AvroReader {
    Flux<AvroObject> read();
}

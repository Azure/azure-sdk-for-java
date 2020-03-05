// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.perf.test.core;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.List;
import java.util.Vector;

import reactor.core.publisher.Flux;

/**
 * Represents a stream with repeated values.
 */
public class CircularStream {
    /**
     * Creates a stream of {@code size}with repeated values of {@code byteArray}.
     * @param byteArray the array to create stream from.
     * @param size the size of the stream to create.
     * @return The created {@link InputStream}
     */
    public static InputStream create(byte[] byteArray, long size) {
        int remaining = byteArray.length;
        
        int quotient = (int) size / remaining;
        int remainder = (int) size % remaining;

        List<ByteArrayInputStream> list = Flux.range(0, quotient)
            .map(i -> new ByteArrayInputStream(byteArray))
            .concatWithValues(new ByteArrayInputStream(byteArray, 0, remainder))
            .collectList()
            .block();

        Vector<ByteArrayInputStream> vector = new Vector<>(list);

        return new SequenceInputStream(vector.elements());
    }
}

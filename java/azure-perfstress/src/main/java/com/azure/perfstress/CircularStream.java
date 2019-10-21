package com.azure.perfstress;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.List;
import java.util.Vector;

import reactor.core.publisher.Flux;

public class CircularStream {
    public static InputStream create(byte[] byteArray, int size) {
        int remaining = byteArray.length;
        
        int quotient = size / remaining;
        int remainder = size % remaining;

        List<ByteArrayInputStream> list = Flux.range(0, quotient)
            .map(i -> new ByteArrayInputStream(byteArray))
            .concatWithValues(new ByteArrayInputStream(byteArray, 0, remainder))
            .collectList()
            .block();

        Vector<ByteArrayInputStream> vector = new Vector<>(list);

        return new SequenceInputStream(vector.elements());
    }
}

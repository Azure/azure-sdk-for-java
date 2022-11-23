// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.test.shared;

import com.azure.core.util.BinaryData;
import reactor.core.publisher.Flux;

import java.io.ByteArrayInputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Stream;

public final class BinaryDataProvider {
    /**
     * Provides BinaryData instances initialized through various means, varying in source data type, replayability,
     * and whether a known length of the data is set.
     * @param length Optional length of BinaryData instances (not whether the instance exposes the length).
     * @param numChunks Number of chunks the data should be split into, for relevant data source types.
     * @param dataGenerator Generator to provide random data from.
     * @return Stream of BinaryData instances with this in mind.
     */
    public static Stream<BinaryDataParamWrapper> all(Integer length, Integer numChunks, Function<Integer, byte[]> dataGenerator) {
        Long longLength = length != null ? length.longValue() : null;
        return Stream.of(
            new BinaryDataParamWrapper(
                fromReplayableFlux(longLength, numChunks, dataGenerator, true),
                "Replayable flux known length"),
            new BinaryDataParamWrapper(
                fromReplayableFlux(longLength, numChunks, dataGenerator, false),
                "Replayable flux unknown length"),
            new BinaryDataParamWrapper(
                fromNonReplayableFlux(longLength, numChunks, dataGenerator, true),
                "Non-replayable flux known length"),
            new BinaryDataParamWrapper(
                fromNonReplayableFlux(longLength, numChunks, dataGenerator, false),
                "Non-replayable flux unknown length"),
            new BinaryDataParamWrapper(
                fromStream(length, dataGenerator, true),
                "Replayable stream known length"),
            new BinaryDataParamWrapper(
                fromStream(length, dataGenerator, false),
                "Replayable stream unknown length"),
            new BinaryDataParamWrapper(
                fromByteBuffer(length, dataGenerator),
                "ByteBuffer")
        );
    }

    public static BinaryData fromReplayableFlux(Long length, Integer numChunks, Function<Integer, byte[]> dataGenerator,
        boolean knownLength) {
        final long finalLength = lengthOrDefault(length);
        final int finalNumChunks = numChunksOrDefault(numChunks);
        final int bufSize = (int) Math.min(finalLength / finalNumChunks, Integer.MAX_VALUE / 2);

        List<ByteBuffer> data = new ArrayList<>(finalNumChunks);
        for (int i = 0; i < finalNumChunks; i++) {
            int actualSize = (int) Math.min(bufSize, finalLength - ((long) i * bufSize));
            data.add(ByteBuffer.wrap(dataGenerator.apply(actualSize)));
        }
        return BinaryData.wrapFlux(Flux.fromIterable(data).map(ByteBuffer::duplicate),
            knownLength ? finalLength : null, true);
    }

    public static BinaryData fromNonReplayableFlux(Long length, Integer numChunks,
        Function<Integer, byte[]> dataGenerator, boolean knownLength) {
        final long finalLength = lengthOrDefault(length);
        final int finalNumChunks = numChunksOrDefault(numChunks);
        final int bufSize = (int) Math.min(finalLength / finalNumChunks, Integer.MAX_VALUE - 8);

        if (finalLength <= 0) {
            throw new IllegalArgumentException("length must be positive.");
        }
        if (finalNumChunks <= 0) {
            throw new IllegalArgumentException("numChunks must be positive.");
        }

        AtomicBoolean subscribedToOnce = new AtomicBoolean(false);
        Flux<ByteBuffer> onlyOnceFlux = Flux.generate(
            () -> 0,
            (count, sink) -> {
                if (subscribedToOnce.get()) {
                    throw new RuntimeException("Attempted to replay non-replayable flux.");
                }

                if (count == finalNumChunks) {
                    sink.complete();
                    subscribedToOnce.set(true);
                } else {
                    int actualSize = (int) Math.min(bufSize, finalLength - ((long) count * bufSize));
                    sink.next(ByteBuffer.wrap(dataGenerator.apply(actualSize)));
                }

                return count + 1;
            });
        return BinaryData.wrapFlux(onlyOnceFlux, knownLength ? finalLength : null, false);
    }

    public static BinaryData fromStream(Integer length, Function<Integer, byte[]> dataGenerator, boolean knownLength) {
        final long finalLength = lengthOrDefault(length);
        return BinaryData.fromStream(
            new ByteArrayInputStream(dataGenerator.apply((int) finalLength)),
            knownLength ? finalLength : null);
    }

    public static BinaryData fromByteBuffer(Integer length, Function<Integer, byte[]> dataGenerator) {
        final long finalLength = lengthOrDefault(length);
        return BinaryData.fromByteBuffer(ByteBuffer.wrap(dataGenerator.apply((int) finalLength)));
    }

    private static long lengthOrDefault(Long length) {
        return length != null ? length : 1024;
    }

    private static int lengthOrDefault(Integer length) {
        return length != null ? length : 1024;
    }

    public static int numChunksOrDefault(Integer numChunks) {
        return numChunks != null ? numChunks : 16;
    }
}

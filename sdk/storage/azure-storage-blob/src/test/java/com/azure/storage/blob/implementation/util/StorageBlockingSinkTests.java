// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob.implementation.util;

import com.azure.storage.common.implementation.Constants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.nio.ByteBuffer;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class StorageBlockingSinkTests {

    // Use a delay of 5 ms to test.
    // This is more than long enough to ensure that nothing odd happens in the blocking sink.
    private static final int DELAY_MS = 5;

    @Test
    public void min() {
        StorageBlockingSink blockingSink = new StorageBlockingSink();

        blockingSink.emitNext(ByteBuffer.wrap(new byte[0]));
        blockingSink.emitCompleteOrThrow();

        Flux<ByteBuffer> flux = blockingSink.asFlux();
        StepVerifier.create(flux).expectNextMatches(buffer -> buffer.remaining() == 0).expectComplete().verify();
    }

    @ParameterizedTest
    @ValueSource(ints = { 5, 10, 50, 100 })
    public void producerDelayedConsumer(int num) {
        StorageBlockingSink blockingSink = new StorageBlockingSink();

        blockingSink.asFlux()
            .index()
            .delayElements(Duration.ofMillis(DELAY_MS)) // This simulates the slower network bound IO
            .doOnNext(tuple -> assertEquals(tuple.getT2().getLong(0), tuple.getT1())) // Check for data integrity
            .subscribe();

        for (int i = 0; i < num; i++) {
            // This simulates a customer writing really fast to the OutputStream
            blockingSink.emitNext(ByteBuffer.allocate(8).putLong(i));
        }
        assertDoesNotThrow(blockingSink::emitCompleteOrThrow);
    }

    @Test
    void producerDelayedConsumerRandomBuffers() {
        int num = 50;
        ByteBuffer[] buffers = new ByteBuffer[num];
        for (int i = 0; i < num; i++) {
            int size = ThreadLocalRandom.current().nextInt(8 * Constants.KB);
            byte[] b = new byte[size];
            ThreadLocalRandom.current().nextBytes(b);
            buffers[i] = ByteBuffer.wrap(b);
        }

        StorageBlockingSink blockingSink = new StorageBlockingSink();
        blockingSink.asFlux()
            .index()
            .delayElements(Duration.ofMillis(DELAY_MS)) // This simulates the slower network bound IO
            // Check for data integrity
            .doOnNext(tuple -> assertEquals(buffers[Math.toIntExact(tuple.getT1())], tuple.getT2()))
            .subscribe();

        for (int i = 0; i < num; i++) {
            blockingSink.emitNext(buffers[i]);
        }
        assertDoesNotThrow(blockingSink::emitCompleteOrThrow);
    }

    @ParameterizedTest
    @ValueSource(ints = { 5, 10, 50, 100 })
    void delayedProducerConsumer(int num) {
        StorageBlockingSink blockingSink = new StorageBlockingSink();

        blockingSink.asFlux()
            .index()
            .delayElements(Duration.ofMillis(DELAY_MS)) // This simulates the slower network bound IO
            .doOnNext(tuple -> assertEquals(tuple.getT2().getLong(0), tuple.getT1())) // Check for data integrity
            .subscribe();

        for (int i = 0; i < num; i++) {
            // This simulates a customer writing really slow to the OutputStream
            blockingSink.emitNext(ByteBuffer.allocate(8).putLong(i));
            sleep(DELAY_MS);
        }
        assertDoesNotThrow(blockingSink::emitCompleteOrThrow);
    }

    @Test
    void delayedProducerConsumerRandomBuffers() {
        int num = 50;
        ByteBuffer[] buffers = new ByteBuffer[num];
        for (int i = 0; i < num; i++) {
            int size = ThreadLocalRandom.current().nextInt(8 * Constants.KB);
            byte[] b = new byte[size];
            ThreadLocalRandom.current().nextBytes(b);
            buffers[i] = ByteBuffer.wrap(b);
        }

        StorageBlockingSink blockingSink = new StorageBlockingSink();

        blockingSink.asFlux()
            .index()
            // Check for data integrity
            .doOnNext(tuple -> assertEquals(buffers[Math.toIntExact(tuple.getT1())], tuple.getT2()))
            .subscribe();

        for (int i = 0; i < num; i++) {
            blockingSink.emitNext(buffers[i]);
            sleep(DELAY_MS); // This simulates a customer writing really slow to the OutputStream
        }
        assertDoesNotThrow(blockingSink::emitCompleteOrThrow);
    }

    @Test
    void errorTerminated() {
        StorageBlockingSink blockingSink = new StorageBlockingSink();

        blockingSink.asFlux().subscribe();

        blockingSink.emitNext(ByteBuffer.wrap(new byte[0]));
        blockingSink.emitCompleteOrThrow();
        StorageBlockingSink finalBlockingSink = blockingSink;
        IllegalStateException e
            = assertThrows(IllegalStateException.class, () -> finalBlockingSink.emitNext(ByteBuffer.wrap(new byte[0])));

        assertEquals(((Sinks.EmissionException) e.getCause()).getReason(), Sinks.EmitResult.FAIL_TERMINATED);

        blockingSink = new StorageBlockingSink();
        blockingSink.asFlux().subscribe();

        blockingSink.emitCompleteOrThrow();
        StorageBlockingSink finalBlockingSink1 = blockingSink;
        Sinks.EmissionException ex
            = assertThrows(Sinks.EmissionException.class, finalBlockingSink1::emitCompleteOrThrow);
        assertEquals(ex.getReason(), Sinks.EmitResult.FAIL_TERMINATED);
    }

    @Test
    void errorCancelled() throws InterruptedException {
        StorageBlockingSink blockingSink = new StorageBlockingSink();
        final CountDownLatch latch = new CountDownLatch(1);
        blockingSink.asFlux()
            .timeout(Duration.ofMillis(DELAY_MS))
            .doFinally(s -> latch.countDown())
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe();

        latch.await(1, TimeUnit.MINUTES);

        StorageBlockingSink finalBlockingSink = blockingSink;
        IllegalStateException e
            = assertThrows(IllegalStateException.class, () -> finalBlockingSink.emitNext(ByteBuffer.wrap(new byte[0])));

        assertEquals(((Sinks.EmissionException) e.getCause()).getReason(), Sinks.EmitResult.FAIL_CANCELLED);

        blockingSink = new StorageBlockingSink();
        final CountDownLatch latch2 = new CountDownLatch(1);
        blockingSink.asFlux()
            .timeout(Duration.ofMillis(DELAY_MS))
            .doFinally(s -> latch2.countDown())
            .subscribeOn(Schedulers.boundedElastic())
            .subscribe();

        latch2.await(1, TimeUnit.MINUTES);

        StorageBlockingSink finalBlockingSink1 = blockingSink;
        Sinks.EmissionException ex
            = assertThrows(Sinks.EmissionException.class, finalBlockingSink1::emitCompleteOrThrow);
        assertEquals(ex.getReason(), Sinks.EmitResult.FAIL_CANCELLED);
    }

    private static void sleep(int millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}

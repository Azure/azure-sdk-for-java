package com.azure.storage.blob.implementation.util


import reactor.test.StepVerifier
import spock.lang.Specification

import java.nio.ByteBuffer
import java.time.Duration

class StorageBlockingSinkTest extends Specification {

    def setup() {
        String fullTestName = specificationContext.getCurrentIteration().getName().replace(' ', '').toLowerCase()
        String className = specificationContext.getCurrentSpec().getName()
        // Print out the test name to create breadcrumbs in our test logging in case anything hangs.
        System.out.printf("========================= %s.%s =========================%n", className, fullTestName)
    }

    def "min"() {
        setup:
        def blockingSink = new StorageBlockingSink()

        when:
        blockingSink.tryEmitNext(ByteBuffer.wrap(new byte[0]))
        blockingSink.tryEmitCompleteOrThrow()

        then:
        StepVerifier.create(blockingSink.asFlux())
            .expectNextMatches({ buffer -> buffer.remaining() == 0})
            .expectComplete()
    }

    // This test can take a long time to execute
    def "producer consumer"() {
        setup:
        def blockingSink = new StorageBlockingSink()
        def delay = 1000
        def blockTime = 3

        when:
        blockingSink.asFlux()
            .index()
            .delayElements(Duration.ofMillis(delay))
            .doOnNext({ tuple ->
                assert tuple.getT2().getLong(0) == tuple.getT1() // Check for data integrity
            })
//            .onErrorStop()
            .subscribe()

        // timer around this and do math to check blocking happened.
//        def timeStart = new Date()
        for(int i = 0; i < num; i++) {
            blockingSink.tryEmitNext(ByteBuffer.allocate(8).putLong(i))
        }
        blockingSink.tryEmitCompleteOrThrow()
//        delay(200)
//        def timeStop = new Date()

        then:
        // We will block for 3 seconds for every other item (num / 2), and the block time is 3 seconds.
//        TimeDuration duration = TimeCategory.minus(new Date(), timeStart)
//        duration.toMilliseconds() / 1000 < blockTime * num / 2
//        duration.toMilliseconds() / 1000 > blockTime * ((num / 2) - 1)
        notThrown(Exception)

        where:
        num     || _
        5       || _
        10      || _
        50      || _
        100     || _ // Anything past this takes way too long, This takes around 2 min
    }
    // add test that generates random buffers


}

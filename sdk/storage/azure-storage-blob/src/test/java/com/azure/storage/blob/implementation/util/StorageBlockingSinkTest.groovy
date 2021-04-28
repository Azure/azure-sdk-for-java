package com.azure.storage.blob.implementation.util

import com.azure.storage.common.implementation.Constants
import reactor.core.publisher.Sinks
import reactor.test.StepVerifier
import spock.lang.Specification
import spock.lang.Unroll

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
        blockingSink.emitNext(ByteBuffer.wrap(new byte[0]))
        blockingSink.emitCompleteOrThrow()

        then:
        StepVerifier.create(blockingSink.asFlux())
            .expectNextMatches({ buffer -> buffer.remaining() == 0})
            .expectComplete()
    }

    // These next few test can take a long time to execute
    @Unroll
    def "producer, delayed consumer"() {
        setup:
        def blockingSink = new StorageBlockingSink()
        def delay = 1000

        when:
        blockingSink.asFlux()
            .index()
            .delayElements(Duration.ofMillis(delay)) // This simulates the slower network bound IO
            .doOnNext({ tuple ->
                assert tuple.getT2().getLong(0) == tuple.getT1() // Check for data integrity
            })
            .subscribe()

        for(int i = 0; i < num; i++) {
            blockingSink.emitNext(ByteBuffer.allocate(8).putLong(i)) // This simulates a customer writing really fast to the OutputStream
        }
        blockingSink.emitCompleteOrThrow()

        then:
        notThrown(Exception)

        where:
        num     || _
        5       || _
        10      || _
        50      || _
        100     || _
    }

    @Unroll
    def "producer, delayed consumer random buffers"() {
        setup:
        def blockingSink = new StorageBlockingSink()
        def delay = 1000
        def num = 50
        def rand = new Random()
        def buffers = new ByteBuffer[num]
        for(int i = 0; i < num; i++) {
            def size = rand.nextInt(8 * Constants.KB)
            def b = new byte[size]
            rand.nextBytes(b)
            buffers[i] = ByteBuffer.wrap(b)
        }

        when:
        blockingSink.asFlux()
            .index()
            .delayElements(Duration.ofMillis(delay)) // This simulates the slower network bound IO
            .doOnNext({ tuple ->
                assert tuple.getT2() == buffers[(int)tuple.getT1()] // Check for data integrity
            })
            .subscribe()

        for(int i = 0; i < num; i++) {
            blockingSink.emitNext(buffers[i]) // This simulates a customer writing really fast to the OutputStream
        }
        blockingSink.emitCompleteOrThrow()

        then:
        notThrown(Exception)
    }

    @Unroll
    def "delayed producer, consumer"() {
        setup:
        def blockingSink = new StorageBlockingSink()
        def delay = 1000

        when:
        blockingSink.asFlux()
            .index()
            .doOnNext({ tuple ->
                assert tuple.getT2().getLong(0) == tuple.getT1() // Check for data integrity
            })
            .subscribe()

        for(int i = 0; i < num; i++) {
            blockingSink.emitNext(ByteBuffer.allocate(8).putLong(i))
            sleep(delay) // This simulates a customer writing really slow to the OutputStream
        }
        blockingSink.emitCompleteOrThrow()

        then:
        notThrown(Exception)

        where:
        num     || _
        5       || _
        10      || _
        50      || _
        100     || _
    }

    @Unroll
    def "delayed producer, consumer random buffers"() {
        setup:
        def blockingSink = new StorageBlockingSink()
        def delay = 1000
        def num = 50
        def rand = new Random()
        def buffers = new ByteBuffer[num]
        for(int i = 0; i < num; i++) {
            def size = rand.nextInt(8 * Constants.KB)
            def b = new byte[size]
            rand.nextBytes(b)
            buffers[i] = ByteBuffer.wrap(b)
        }

        when:
        blockingSink.asFlux()
            .index()
            .doOnNext({ tuple ->
                assert tuple.getT2() == buffers[(int)tuple.getT1()] // Check for data integrity
            })
            .subscribe()

        for(int i = 0; i < num; i++) {
            blockingSink.emitNext(buffers[i])
            sleep(delay) // This simulates a customer writing really slow to the OutputStream
        }
        blockingSink.emitCompleteOrThrow()

        then:
        notThrown(Exception)
    }

    def "error terminated"() {
        setup:
        def blockingSink = new StorageBlockingSink()

        when:
        blockingSink.asFlux()
            .subscribe()

        blockingSink.emitNext(ByteBuffer.wrap(new byte[0]))
        blockingSink.emitCompleteOrThrow()
        blockingSink.emitNext(ByteBuffer.wrap(new byte[0]))

        then:
        def e = thrown(IllegalStateException)
        ((Sinks.EmissionException) e.getCause()).getReason() == Sinks.EmitResult.FAIL_TERMINATED

        when:
        blockingSink = new StorageBlockingSink()
        blockingSink.asFlux()
            .subscribe()

        blockingSink.emitCompleteOrThrow()
        blockingSink.emitCompleteOrThrow()

        then:
        e = thrown(Sinks.EmissionException)
        e.getReason() == Sinks.EmitResult.FAIL_TERMINATED
    }

    def "error cancelled"() {
        setup:
        def blockingSink = new StorageBlockingSink()

        when:
        blockingSink.asFlux()
            .timeout(Duration.ofMillis(100))
            .subscribe()

        sleep(200)

        blockingSink.emitNext(ByteBuffer.wrap(new byte[0]))

        then:
        def e = thrown(IllegalStateException)
        ((Sinks.EmissionException) e.getCause()).getReason() == Sinks.EmitResult.FAIL_CANCELLED

        when:
        blockingSink = new StorageBlockingSink()
        blockingSink.asFlux()
            .timeout(Duration.ofMillis(100))
            .subscribe()

        sleep(200)

        blockingSink.emitCompleteOrThrow()

        then:
        e = thrown(Sinks.EmissionException)
        e.getReason() == Sinks.EmitResult.FAIL_CANCELLED
    }
}

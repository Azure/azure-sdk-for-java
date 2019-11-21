package noop;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;

/**
 * Hello world!
 *
 */
public class App
{
    private static final long _iterations = 80_000_000;
    private static final int _parallel = 1;

    public static void main( String[] args )
    {
        long iterationsPerParallel = _iterations / _parallel;

        long startNanoTime = System.nanoTime();
        
        Flux.just(1)
            .parallel()
            .runOn(Schedulers.parallel())
            .flatMap(i -> Flux.just(1)
                    .repeat()
                    .doOnNext(j -> System.out.println(System.nanoTime()))
                    .take(Duration.ofSeconds(1)))
            .then()
            .block();

        long endNanoTime = System.nanoTime();

        double elapsedSeconds = ((double)(endNanoTime - startNanoTime)) / 1_000_000_000;
        double opsPerSecond = _iterations / elapsedSeconds;

        System.out.printf("Called %d functions in %.2f seconds (%.0f ops/s)", _iterations, elapsedSeconds, opsPerSecond);
    }

    private static Mono<Void> runLoop(long iterations) {
        return Flux.just(1)
                .repeat()
                .flatMap(i -> noopAsync().then(Mono.just(1)), 1)
                .take(Duration.ofSeconds(1))
                .then();
    }

    private static Mono<Void> noopAsync() {
        return Mono.empty();
    }

}

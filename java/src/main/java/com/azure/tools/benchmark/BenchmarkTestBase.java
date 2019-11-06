package com.azure.tools.benchmark;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

@State(Scope.Benchmark)
public class BenchmarkTestBase {
    private static AtomicInteger completedOperations = new AtomicInteger();
    private static Logger LOGGER = Logger.getLogger(BenchmarkTestBase.class.getName());
    public static int warmupIterations = 2;

    public List<BenchmarkResults> benchmarkResults;

    @Setup
    public void setup() {
        benchmarkResults = new ArrayList<>();
    }

    @TearDown
    public void tearDown() {
        benchmarkResults.clear();
    }

    public <T, R> BenchmarkResults runJobAsync(String benchmarkId, T client, Function<T, Mono<R>> testFunction,
                                               int parallel, Duration duration, Blackhole bh) {
        completedOperations.set(0);
        // Run Benchmark
       Flux.range(1, parallel)
                .flatMap(t -> Flux.just(1).repeat()
                            .flatMap(i -> testFunction.apply(client), 1)
                            .doOnNext(v -> completedOperations.incrementAndGet())
                            .take(duration), parallel).blockLast();

        return new BenchmarkResults(benchmarkId, parallel, duration, completedOperations.longValue());
    }


    public <T, R, V> BenchmarkResults runJobAsync(String benchmarkId, T client, Function<T, V> setupFunction,
                                                  BiFunction<T, V, Mono<R>> testFunction, int parallel,
                                                  Duration duration, Blackhole bh) {
        ArrayList<V> setupList = new ArrayList<>();
        IntStream.range(0, parallel).forEach(t -> setupList.add(setupFunction.apply(client)));

        completedOperations.set(0);
        // Run Benchmark
        Flux.fromIterable(setupList)
                .flatMap(t -> Flux.just(1).repeat()
                            .flatMap(i -> testFunction.apply(client, t), 1)
                            .doOnNext(v -> completedOperations.incrementAndGet())
                            .take(duration), parallel).blockLast();
        return new BenchmarkResults(benchmarkId, parallel, duration, completedOperations.longValue());
    }

    public <T, R> BenchmarkResults runJob(String benchmarkId, T client, Function<T, R> testFunction,
                                          int parallel, Duration duration, Blackhole bh) {
        completedOperations.set(0);
        ForkJoinPool forkJoinPool = new ForkJoinPool(parallel);
        long endNanoTime = System.nanoTime() + (duration.getSeconds() * 1000000000);

        try {

            forkJoinPool.submit(() -> {
                IntStream.rangeClosed(1, parallel).parallel().forEach(t -> {
                    while (System.nanoTime() < endNanoTime) {
                        bh.consume(testFunction.apply(client));
                        completedOperations.incrementAndGet();
                    }
                });
            }).get();

        } catch (InterruptedException | ExecutionException e) {

            throw new RuntimeException(e);

        }
        return new BenchmarkResults(benchmarkId, parallel, duration, completedOperations.longValue());
    }

    public <T, R, V> BenchmarkResults runJob(String benchmarkId, T client, Function<T, V> setupFunction,
                                             BiFunction<T, V, R> testFunction, int parallel, Duration duration,
                                             Blackhole bh) {
        ArrayList<V> setupList = new ArrayList<>();
        IntStream.range(0, parallel).forEach(t -> setupList.add(setupFunction.apply(client)));

        completedOperations.set(0);
        ForkJoinPool forkJoinPool = new ForkJoinPool(parallel);
        long endNanoTime = System.nanoTime() + (duration.getSeconds() * 1000000000);

        try {
            forkJoinPool.submit(() -> {
                setupList.stream().parallel().forEach(t -> {
                    while (System.nanoTime() < endNanoTime) {
                        bh.consume(testFunction.apply(client, t));
                        completedOperations.incrementAndGet();
                    }
                });
            }).get();
        } catch (InterruptedException | ExecutionException e) {

            throw new RuntimeException(e);

        }
        return new BenchmarkResults(benchmarkId, parallel, duration, completedOperations.longValue());
    }

     public void saveResults(BenchmarkResults results) {
        benchmarkResults.add(results);
        String label = "Results for Warmup Iteration: ";
        if (benchmarkResults.size() > AzureBenchmark.WARMUP_ITERATIONS) {
            label = "Results for Benchmark: ";
        }
         ObjectMapper mapper = new ObjectMapper();
         System.out.println("\n");
         LOGGER.log(Level.INFO, label + results.getBenchmarkId());
         String jsonInString = "";
         try {
             jsonInString = mapper.writeValueAsString(results);
         } catch (IOException e) {
             e.printStackTrace();
         }
         System.out.println(jsonInString + "\n");
    }
}
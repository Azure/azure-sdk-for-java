package com.microsoft.storageperf;

import com.azure.storage.blob.BlockBlobAsyncClient;
import io.netty.util.internal.MathUtil;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

public class BenchmarkingTool<T, R> {

    private T client;

    public BenchmarkingTool(T client) {
        this.client = client;
    }

    public BenchmarkResults runJobAsync(Function<T, Mono<R>> function, int parallel, Duration duration) {
        Long startTime = System.nanoTime();
        Long blobs = Flux.just(1).repeat().flatMap(i -> function.apply(client), parallel).take(duration).count().block();
        Long endTime = System.nanoTime();
        Long timeTaken = (endTime - startTime) / 1000000000;
        return new BenchmarkResults(Duration.ofSeconds(timeTaken), blobs, (blobs.floatValue() / timeTaken.floatValue()));
    }

    public BenchmarkResults runJob(Function<T, Mono<R>> function, int parallel, Duration duration) {
        AtomicReference<Integer> requests = new AtomicReference<>(0);
        ExecutorService executor = Executors.newFixedThreadPool(parallel);
        List<Callable<Integer>> callables = new ArrayList<>();
        for (int i = 0; i < parallel; i++) {
            Callable<Integer> worker = new Task(client, duration.getSeconds(), requests, function);
            callables.add(worker);
            // executor.execute(worker);
        }
        Long startTime = System.nanoTime();
        try {
            executor.invokeAll(callables, 10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        executor.shutdown();
        while(!executor.isTerminated()) { }

        Long endTime = System.nanoTime();

        System.out.println(requests.get());
        executor.shutdown();

        Long timeTaken = (endTime - startTime) / 1000000000;

        return new BenchmarkResults(Duration.ofSeconds(timeTaken), requests.get().longValue(), (requests.get().floatValue() / timeTaken.floatValue()));
    }


    public class Task implements Callable<Integer> {

        T client;
        long duration;
        AtomicReference<Integer> requests;
        Function<T, Mono<R>> function;

        public Task(T client, long duration, AtomicReference<Integer> requests, Function<T, Mono<R>> function) {
            this.client = client;
            this.duration = duration;
            this.requests = requests;
            this.function = function;
        }

        @Override
        public Integer call() throws Exception {
            long startTime = System.nanoTime();
            Long elapsedTime = 0l;
            while(elapsedTime < duration) {
                function.apply(client);
                requests.set(requests.get() + 1);
                elapsedTime = (System.nanoTime() - startTime) /  1000000000l;
            }
            //integer.set(integer.get() + downloads);
            return 0;
        }
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.rest;

import com.azure.core.http.HttpHeaders;
import com.azure.core.util.SharedExecutorService;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import reactor.core.publisher.Mono;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@Execution(ExecutionMode.SAME_THREAD)
public class PagedIterableConcurrencyTests {
    @ParameterizedTest
    @CsvSource({
        "false, false, 1",
        "false, false, 2",
        "false, true, 1",
        "false, true, 2",
        "true, false, 1",
        "true, false, 2",
        "true, true, 1",
        "true, true, 2" })
    public void pageFetchDoesNotPinCarriers(boolean async, boolean byPage, int carriers, @TempDir Path directory)
        throws Exception {
        int javaVersion = Integer.parseInt(System.getProperty("java.specification.version").split("\\.")[0]);
        assumeTrue(javaVersion >= 21, "Virtual threads require Java 21 or later.");

        Path output = directory.resolve("paging.log");
        String classPath = System.getProperty("surefire.test.class.path", System.getProperty("java.class.path"));
        // Surefire separates production classes and dependencies onto the module path on Java 9+.
        String modulePath = System.getProperty("jdk.module.path");
        if (modulePath != null && !modulePath.isEmpty()) {
            classPath += File.pathSeparator + modulePath;
        }
        // Carrier parallelism and shared-executor settings must be set before their first use.
        Process process = new ProcessBuilder(Paths.get(System.getProperty("java.home"), "bin", "java").toString(),
            "-Xmx128m", "-XX:ActiveProcessorCount=" + carriers, "-Djdk.virtualThreadScheduler.parallelism=" + carriers,
            "-Djdk.virtualThreadScheduler.maxPoolSize=" + carriers,
            "-Dazure.sdk.shared.threadpool.usevirtualthreads=true", "-Dazure.sdk.shared.threadpool.maxpoolsize=4",
            "-cp", classPath, PagingProcess.class.getName(), String.valueOf(async), String.valueOf(byPage),
            String.valueOf(carriers)).redirectErrorStream(true).redirectOutput(output.toFile()).start();

        boolean completed;
        try {
            completed = process.waitFor(30, TimeUnit.SECONDS);
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
                assertTrue(process.waitFor(10, TimeUnit.SECONDS), "The child JVM did not terminate.");
            }
        }

        String diagnostics = new String(Files.readAllBytes(output), UTF_8);
        assertTrue(completed, "Paging did not complete in the child JVM.\n" + diagnostics);
        assertEquals(0, process.exitValue(), diagnostics);
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void pageFetchReleasesLockAfterFailure(boolean async) throws Exception {
        AtomicInteger attempts = new AtomicInteger();
        RuntimeException failure = new IllegalStateException("Page fetch failed.");
        Iterator<Integer> iterator = pagedIterable(async, () -> page(1), () -> {
            if (attempts.getAndIncrement() == 0) {
                throw failure;
            }
            return page(2);
        }).iterator();
        assertEquals(1, iterator.next());
        assertSame(failure, assertThrows(IllegalStateException.class, iterator::hasNext));

        ExecutorService executor = newDaemonExecutor();
        try {
            assertTrue(executor.submit(iterator::hasNext).get(10, TimeUnit.SECONDS));
            assertEquals(2, iterator.next());
        } finally {
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    public void pageFetchReleasesLockAfterInterruption(boolean async) throws Exception {
        AtomicInteger attempts = new AtomicInteger();
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch blocked = new CountDownLatch(1);
        Iterator<Integer> iterator = pagedIterable(async, () -> page(1), () -> {
            if (attempts.getAndIncrement() == 0) {
                entered.countDown();
                await(blocked);
            }
            return page(2);
        }).iterator();
        assertEquals(1, iterator.next());

        ExecutorService waitingExecutor = newDaemonExecutor();
        ExecutorService nextExecutor = newDaemonExecutor();
        try {
            Future<Boolean> waiting = waitingExecutor.submit(iterator::hasNext);
            assertTrue(entered.await(10, TimeUnit.SECONDS));
            assertTrue(waiting.cancel(true));
            assertTrue(nextExecutor.submit(iterator::hasNext).get(10, TimeUnit.SECONDS));
            assertEquals(2, iterator.next());
        } finally {
            blocked.countDown();
            waitingExecutor.shutdownNow();
            nextExecutor.shutdownNow();
            assertTrue(waitingExecutor.awaitTermination(10, TimeUnit.SECONDS));
            assertTrue(nextExecutor.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    private static ExecutorService newDaemonExecutor() {
        return Executors.newSingleThreadExecutor(runnable -> {
            Thread thread = new Thread(runnable);
            thread.setDaemon(true);
            return thread;
        });
    }

    private static PagedIterable<Integer> pagedIterable(boolean async, Supplier<PagedResponse<Integer>> first,
        Supplier<PagedResponse<Integer>> next) {
        return async
            ? new PagedIterable<>(new PagedFlux<>(() -> Mono.fromSupplier(first), ignored -> Mono.fromSupplier(next)))
            : new PagedIterable<>(first, ignored -> next.get());
    }

    private static PagedResponse<Integer> page(int number) {
        return new PagedResponseBase<>(null, 200, new HttpHeaders(), Collections.singletonList(number),
            number == 1 ? "next" : null, null);
    }

    private static void await(CountDownLatch latch) {
        try {
            if (!latch.await(10, TimeUnit.SECONDS)) {
                throw new AssertionError("Concurrent page fetches did not reach the barrier.");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Page fetch interrupted.", e);
        }
    }

    public static final class PagingProcess {
        public static void main(String[] args) throws Exception {
            boolean async = Boolean.parseBoolean(args[0]);
            boolean byPage = Boolean.parseBoolean(args[1]);
            int callers = Integer.parseInt(args[2]);
            SharedExecutorService shared = SharedExecutorService.getInstance();
            Future<Boolean> virtual
                = shared.submit(() -> (Boolean) Thread.class.getMethod("isVirtual").invoke(Thread.currentThread()));
            assertTrue(virtual.get(10, TimeUnit.SECONDS),
                "Page completion must use the shared virtual-thread executor.");

            // Warm paging classes outside virtual callers to isolate page-request pinning from class initialization.
            pagedIterable(false, () -> page(2), () -> null).stream().count();
            pagedIterable(true, () -> page(2), () -> null).stream().count();

            CountDownLatch[] ready = { new CountDownLatch(callers), new CountDownLatch(callers) };
            AtomicInteger pages = new AtomicInteger();
            Function<Integer, CompletableFuture<PagedResponse<Integer>>> retrieve = number -> {
                ready[number - 1].countDown();
                await(ready[number - 1]);
                return CompletableFuture.supplyAsync(() -> {
                    pages.incrementAndGet();
                    return page(number);
                }, shared);
            };
            ExecutorService executor
                = (ExecutorService) Executors.class.getMethod("newVirtualThreadPerTaskExecutor").invoke(null);
            List<Future<List<Integer>>> results = new ArrayList<>();
            try {
                for (int i = 0; i < callers; i++) {
                    results.add(executor.submit(() -> {
                        PagedIterable<Integer> iterable = async
                            ? new PagedIterable<>(new PagedFlux<>(() -> Mono.fromFuture(retrieve.apply(1)),
                                ignored -> Mono.fromFuture(retrieve.apply(2))))
                            : pagedIterable(false, () -> waitForPage(retrieve.apply(1)),
                                () -> waitForPage(retrieve.apply(2)));
                        return byPage
                            ? iterable.streamByPage()
                                .flatMap(response -> response.getValue().stream())
                                .collect(Collectors.toList())
                            : iterable.stream().collect(Collectors.toList());
                    }));
                }
                for (Future<List<Integer>> result : results) {
                    assertEquals(Arrays.asList(1, 2), result.get(10, TimeUnit.SECONDS));
                }
                assertEquals(callers * 2, pages.get());
            } finally {
                for (Future<?> result : results) {
                    result.cancel(true);
                }
                executor.shutdownNow();
                try {
                    assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
                } finally {
                    shared.reset();
                }
            }
        }

        private static PagedResponse<Integer> waitForPage(CompletableFuture<PagedResponse<Integer>> result) {
            try {
                return result.get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Page fetch interrupted.", e);
            } catch (ExecutionException e) {
                throw new IllegalStateException("Page fetch failed.", e.getCause());
            }
        }
    }
}

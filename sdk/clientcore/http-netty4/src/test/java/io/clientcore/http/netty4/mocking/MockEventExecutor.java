// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.netty4.mocking;

import io.netty.util.concurrent.EventExecutor;
import io.netty.util.concurrent.EventExecutorGroup;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.ProgressivePromise;
import io.netty.util.concurrent.Promise;
import io.netty.util.concurrent.ScheduledFuture;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class MockEventExecutor implements EventExecutor {
    private final Map<String, AtomicInteger> scheduleAtFixedRateCallCount = new ConcurrentHashMap<>();
    private final Map<Long, AtomicInteger> scheduleCallCount = new ConcurrentHashMap<>();

    @Override
    public boolean isShuttingDown() {
        return false;
    }

    @Override
    public Future<?> shutdownGracefully() {
        return null;
    }

    @Override
    public Future<?> shutdownGracefully(long quietPeriod, long timeout, TimeUnit unit) {
        return null;
    }

    @Override
    public Future<?> terminationFuture() {
        return null;
    }

    @Override
    @Deprecated
    public void shutdown() {

    }

    @Override
    @Deprecated
    public List<Runnable> shutdownNow() {
        return null;
    }

    @Override
    public boolean isShutdown() {
        return false;
    }

    @Override
    public boolean isTerminated() {
        return false;
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public EventExecutor next() {
        return null;
    }

    @Override
    public Iterator<EventExecutor> iterator() {
        return null;
    }

    @Override
    public Future<?> submit(Runnable task) {
        return null;
    }

    @Override
    public <T> List<java.util.concurrent.Future<T>> invokeAll(Collection<? extends Callable<T>> tasks)
        throws InterruptedException {
        return null;
    }

    @Override
    public <T> List<java.util.concurrent.Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout,
        TimeUnit unit) throws InterruptedException {
        return null;
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
        return null;
    }

    @Override
    public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
        throws InterruptedException, ExecutionException, TimeoutException {
        return null;
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        return null;
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        return null;
    }

    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        scheduleCallCount.computeIfAbsent(delay, k -> new AtomicInteger()).incrementAndGet();
        return null;
    }

    public int getScheduleCallCount() {
        return scheduleCallCount.values().stream().mapToInt(AtomicInteger::get).sum();
    }

    public int getScheduleCallCount(long delay) {
        return scheduleCallCount.getOrDefault(delay, new AtomicInteger()).get();
    }

    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return null;
    }

    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        scheduleAtFixedRateCallCount.computeIfAbsent(initialDelay + "-" + period, k -> new AtomicInteger())
            .incrementAndGet();
        return null;
    }

    public int getScheduleAtFixedRateCallCount() {
        return scheduleAtFixedRateCallCount.values().stream().mapToInt(AtomicInteger::get).sum();
    }

    public int getScheduleAtFixedRateCallCount(long initialDelay, long period) {
        return scheduleAtFixedRateCallCount.getOrDefault(initialDelay + "-" + period, new AtomicInteger()).get();
    }

    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return null;
    }

    @Override
    public EventExecutorGroup parent() {
        return null;
    }

    @Override
    public boolean inEventLoop() {
        return false;
    }

    @Override
    public boolean inEventLoop(Thread thread) {
        return false;
    }

    @Override
    public <V> Promise<V> newPromise() {
        return null;
    }

    @Override
    public <V> ProgressivePromise<V> newProgressivePromise() {
        return null;
    }

    @Override
    public <V> Future<V> newSucceededFuture(V result) {
        return null;
    }

    @Override
    public <V> Future<V> newFailedFuture(Throwable cause) {
        return null;
    }

    @Override
    public void execute(Runnable command) {

    }
}

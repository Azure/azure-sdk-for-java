// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.rntbd;

import io.netty.channel.Channel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
import io.netty.util.concurrent.Promise;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

class ChannelPromiseWithExpiryTime implements Promise<Channel> {
    private final Promise<Channel> channelPromise;
    private final long expiryTimeInNanos;
    private final RntbdChannelAcquisitionContext channelAcquisitionContext;

    public ChannelPromiseWithExpiryTime(Promise<Channel> channelPromise, long expiryTimeInNanos) {
        this(channelPromise, expiryTimeInNanos, null);
    }

    public ChannelPromiseWithExpiryTime(
        Promise<Channel> channelPromise,
        long expiryTimeInNanos,
        RntbdChannelAcquisitionContext channelAcquisitionContext) {
        checkNotNull(channelPromise, "channelPromise must not be null");
        checkNotNull(expiryTimeInNanos, "expiryTimeInNanos must not be null");

        this.channelPromise = channelPromise;
        this.expiryTimeInNanos = expiryTimeInNanos;
        this.channelAcquisitionContext = channelAcquisitionContext;
    }

    public long getExpiryTimeInNanos() {
        return this.expiryTimeInNanos;
    }

    @Override
    public Promise<Channel> setSuccess(Channel result) {
        return this.channelPromise.setSuccess(result);
    }

    @Override
    public boolean trySuccess(Channel result) {
        return this.channelPromise.trySuccess(result);
    }

    @Override
    public Promise<Channel> setFailure(Throwable cause) {
        return this.channelPromise.setFailure(cause);
    }

    @Override
    public boolean tryFailure(Throwable cause) {
        return this.channelPromise.tryFailure(cause);
    }

    @Override
    public boolean setUncancellable() {
        return this.channelPromise.setUncancellable();
    }

    @Override
    public boolean isSuccess() {
        return this.channelPromise.isSuccess();
    }

    @Override
    public boolean isCancellable() {
        return this.channelPromise.isCancellable();
    }

    @Override
    public Throwable cause() {
        return this.channelPromise.cause();
    }

    @Override
    public Promise<Channel> addListener(
        GenericFutureListener<? extends Future<? super Channel>> listener) {

        return this.channelPromise.addListener(listener);
    }

    @SafeVarargs
    @Override
    @SuppressWarnings("varargs")
    public final Promise<Channel> addListeners(
        GenericFutureListener<? extends Future<? super Channel>>... listeners) {

        return this.channelPromise.addListeners(listeners);
    }

    @Override
    public Promise<Channel> removeListener(
        GenericFutureListener<? extends Future<? super Channel>> listener) {

        return this.channelPromise.removeListener(listener);
    }

    @SafeVarargs
    @Override
    @SuppressWarnings("varargs")
    public final Promise<Channel> removeListeners(
        GenericFutureListener<? extends Future<? super Channel>>... listeners) {

        return this.channelPromise.removeListeners(listeners);
    }

    @Override
    public Promise<Channel> await() throws InterruptedException {
        return this.channelPromise.await();
    }

    @Override
    public Promise<Channel> awaitUninterruptibly() {
        return this.channelPromise.awaitUninterruptibly();
    }

    @Override
    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return this.channelPromise.await(timeout, unit);
    }

    @Override
    public boolean await(long timeoutMillis) throws InterruptedException {
        return this.channelPromise.await(timeoutMillis);
    }

    @Override
    public boolean awaitUninterruptibly(long timeout, TimeUnit unit) {
        return this.channelPromise.awaitUninterruptibly(timeout, unit);
    }

    @Override
    public boolean awaitUninterruptibly(long timeoutMillis) {
        return this.channelPromise.awaitUninterruptibly(timeoutMillis);
    }

    @Override
    public Channel getNow() {
        return this.channelPromise.getNow();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return this.channelPromise.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return this.channelPromise.isCancelled();
    }

    @Override
    public boolean isDone() {
        return this.channelPromise.isDone();
    }

    @Override
    public Channel get() throws InterruptedException, ExecutionException {
        return this.channelPromise.get();
    }

    @SuppressWarnings("NullableProblems")
    @Override
    public Channel get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        return this.channelPromise.get(timeout, unit);
    }

    @Override
    public Promise<Channel> sync() throws InterruptedException {
        return this.channelPromise.sync();
    }

    @Override
    public Promise<Channel> syncUninterruptibly() {
        return this.channelPromise.syncUninterruptibly();
    }

    public RntbdChannelAcquisitionContext getChannelAcquisitionContext() {
        return this.channelAcquisitionContext;
    }
}

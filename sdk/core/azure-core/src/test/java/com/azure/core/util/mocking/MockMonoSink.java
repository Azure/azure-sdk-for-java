// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.mocking;

import reactor.core.Disposable;
import reactor.core.publisher.MonoSink;
import reactor.util.context.Context;

import java.util.function.LongConsumer;

/**
 * A Mono sink used for mocking in tests.
 */
@SuppressWarnings("deprecation")
public class MockMonoSink<T> implements MonoSink<T> {
    @Override
    public void success() {

    }

    @Override
    public void success(T value) {

    }

    @Override
    public void error(Throwable e) {

    }

    @Override
    public Context currentContext() {
        return null;
    }

    @Override
    public MonoSink<T> onRequest(LongConsumer consumer) {
        return null;
    }

    @Override
    public MonoSink<T> onCancel(Disposable d) {
        return null;
    }

    @Override
    public MonoSink<T> onDispose(Disposable d) {
        return null;
    }
}

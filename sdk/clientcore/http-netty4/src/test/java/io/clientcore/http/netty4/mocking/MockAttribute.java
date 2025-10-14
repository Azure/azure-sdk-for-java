// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.http.netty4.mocking;

import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

import java.util.concurrent.atomic.AtomicReference;

public class MockAttribute<T> implements Attribute<T> {
    private final AtomicReference<T> value;

    public MockAttribute(T value) {
        this.value = new AtomicReference<>(value);
    }

    @Override
    public AttributeKey<T> key() {
        return null;
    }

    @Override
    public T get() {
        return value.get();
    }

    @Override
    public void set(T value) {
        this.value.set(value);
    }

    @Override
    public T getAndSet(T value) {
        return this.value.getAndSet(value);
    }

    @Override
    public T setIfAbsent(T value) {
        return this.value.updateAndGet(currentValue -> currentValue == null ? value : currentValue);
    }

    @Override
    @Deprecated
    public T getAndRemove() {
        return null;
    }

    @Override
    public boolean compareAndSet(T oldValue, T newValue) {
        return value.compareAndSet(oldValue, newValue);
    }

    @Override
    @Deprecated
    public void remove() {

    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.eventhubs.impl;

public final class PassByRef<T extends Object> {

    T t;

    public T get() {
        return this.t;
    }

    public void set(final T t) {
        this.t = t;
    }
}

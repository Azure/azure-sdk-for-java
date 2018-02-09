/*
 * Copyright (c) Microsoft. All rights reserved.
 * Licensed under the MIT license. See LICENSE file in the project root for full license information.
 */
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

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

interface ExceptionThrowingConsumer<T> {
    void consume(T t) throws Exception;
}

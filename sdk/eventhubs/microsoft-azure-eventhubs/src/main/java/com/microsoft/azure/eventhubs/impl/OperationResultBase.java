// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.microsoft.azure.eventhubs.impl;

import java.util.Objects;
import java.util.function.Consumer;

class OperationResultBase<T, E extends Exception> implements OperationResult<T, E> {
    private final Consumer<T> onComplete;
    private final Consumer<Exception> onError;

    OperationResultBase(Consumer<T> onComplete, Consumer<Exception> onError) {
        Objects.requireNonNull(onComplete);
        Objects.requireNonNull(onError);

        this.onComplete = onComplete;
        this.onError = onError;
    }

    @Override
    public void onComplete(T result) {
        onComplete.accept(result);
    }

    @Override
    public void onError(E error) {
        onError.accept(error);
    }
}


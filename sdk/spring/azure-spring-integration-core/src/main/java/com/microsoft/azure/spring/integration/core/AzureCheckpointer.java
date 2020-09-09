// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.integration.core;

import com.microsoft.azure.spring.integration.core.api.Checkpointer;
import org.springframework.lang.NonNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

public class AzureCheckpointer implements Checkpointer {
    private final Supplier<CompletableFuture<Void>> success;
    private final Supplier<CompletableFuture<Void>> fail;

    public AzureCheckpointer(@NonNull Supplier<CompletableFuture<Void>> success) {
        this(success, null);
    }

    public AzureCheckpointer(@NonNull Supplier<CompletableFuture<Void>> success,
                             Supplier<CompletableFuture<Void>> fail) {
        this.success = success;
        this.fail = fail;
    }

    @Override
    public CompletableFuture<Void> success() {
        return this.success.get();
    }

    @Override
    public CompletableFuture<Void> failure() {
        if (this.fail == null) {
            throw new UnsupportedOperationException("Fail current message unsupported");
        }
        return this.fail.get();
    }
}

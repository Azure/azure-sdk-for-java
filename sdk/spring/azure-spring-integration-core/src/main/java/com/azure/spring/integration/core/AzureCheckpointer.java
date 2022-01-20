// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.core;

import com.azure.spring.integration.core.api.Checkpointer;
import org.springframework.lang.NonNull;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Azure implementation for check point callback.
 *
 * @deprecated {@link CompletableFuture} API will be dropped in version 4.0.0, please migrate to reactor API in
 * {@link com.azure.spring.integration.core.api.reactor.AzureCheckpointer}. From version 4.0.0, the reactor API support
 * will be moved to com.azure.spring.messaging.core.checkpoint.AzureCheckpointer.
 */
@Deprecated
public class AzureCheckpointer implements Checkpointer {
    private Supplier<CompletableFuture<Void>> success;
    private Supplier<CompletableFuture<Void>> fail;

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

    public Supplier<CompletableFuture<Void>> getSuccess() {
        return success;
    }

    public void setSuccess(Supplier<CompletableFuture<Void>> success) {
        this.success = success;
    }

    public Supplier<CompletableFuture<Void>> getFail() {
        return fail;
    }

    public void setFail(Supplier<CompletableFuture<Void>> fail) {
        this.fail = fail;
    }
}

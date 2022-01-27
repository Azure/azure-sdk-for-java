// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.checkpoint;

import org.springframework.lang.NonNull;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

/**
 * Azure implementation for check point callback.
 */
public class AzureCheckpointer implements Checkpointer {

    private Supplier<Mono<Void>> success;
    private Supplier<Mono<Void>> fail;

    /**
     * Construct the checkpointer with the {@link Supplier} for a success operation.
     * @param success the {@link Supplier} for a success operation.
     */
    public AzureCheckpointer(@NonNull Supplier<Mono<Void>> success) {
        this(success, null);
    }

    /**
     * Construct the checkpointer with {@link Supplier}s for a success operation and failure operation.
     * @param success the {@link Supplier} for a success operation.
     * @param fail the {@link Supplier} for a failure operation.
     */
    public AzureCheckpointer(@NonNull Supplier<Mono<Void>> success,
                             Supplier<Mono<Void>> fail) {
        this.success = success;
        this.fail = fail;
    }

    @Override
    public Mono<Void> success() {
        return this.success.get();
    }

    @Override
    public Mono<Void> failure() {
        if (this.fail == null) {
            throw new UnsupportedOperationException("Fail current message unsupported");
        }
        return this.fail.get();
    }

    /**
     * Get the {@link Supplier} for a success operation.
     * @return the {@link Supplier} for a success operation.
     */
    public Supplier<Mono<Void>> getSuccess() {
        return success;
    }

    /**
     * Set the {@link Supplier} for a success operation.
     * @param success the {@link Supplier} for a success operation.
     */
    public void setSuccess(Supplier<Mono<Void>> success) {
        this.success = success;
    }

    /**
     * Get the {@link Supplier} for a failure operation.
     * @return the {@link Supplier} for a failure operation.
     */
    public Supplier<Mono<Void>> getFail() {
        return fail;
    }

    /**
     * Set the {@link Supplier} for a failure operation.
     * @param fail the {@link Supplier} for a failure operation.
     */
    public void setFail(Supplier<Mono<Void>> fail) {
        this.fail = fail;
    }
}

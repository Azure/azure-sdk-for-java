// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.implementation.checkpoint;

import com.azure.spring.messaging.checkpoint.Checkpointer;
import org.springframework.lang.NonNull;
import reactor.core.publisher.Mono;

import java.util.function.Supplier;

/**
 * Azure implementation for check point callback.
 */
public class AzureCheckpointer implements Checkpointer {

    private final Supplier<Mono<Void>> success;
    private final Supplier<Mono<Void>> fail;

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

}

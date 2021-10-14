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

    private final Supplier<Mono<Void>> success;
    private final Supplier<Mono<Void>> fail;

    public AzureCheckpointer(@NonNull Supplier<Mono<Void>> success) {
        this(success, null);
    }

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

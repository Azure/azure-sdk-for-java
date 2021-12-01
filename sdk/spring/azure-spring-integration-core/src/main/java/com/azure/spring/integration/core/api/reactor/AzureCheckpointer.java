// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.core.api.reactor;

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
     *
     * @param success The success supplier.
     */
    public AzureCheckpointer(@NonNull Supplier<Mono<Void>> success) {
        this(success, null);
    }

    /**
     *
     * @param success The success supplier
     * @param fail The fail supplier
     */
    public AzureCheckpointer(@NonNull Supplier<Mono<Void>> success,
                             Supplier<Mono<Void>> fail) {
        this.success = success;
        this.fail = fail;
    }

    /**
     *
     * @return The success mono.
     */
    @Override
    public Mono<Void> success() {
        return this.success.get();
    }

    /**
     *
     * @return The failure mono.
     */
    @Override
    public Mono<Void> failure() {
        if (this.fail == null) {
            throw new UnsupportedOperationException("Fail current message unsupported");
        }
        return this.fail.get();
    }
}

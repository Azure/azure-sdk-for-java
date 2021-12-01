// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.core.api;

/**
 * Support Batch Consuming by setting {@link Batchable}
 *
 */
public interface Batchable {

    /**
     *
     * @return The BatchConsumerConfig.
     */
    BatchConsumerConfig getBatchConsumerConfig();

    /**
     *
     * @param batchConsumerConfig The batchConsumerConfig.
     */
    void setBatchConsumerConfig(BatchConsumerConfig batchConsumerConfig);
    
}

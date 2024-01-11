// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.core;

import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * Schedulers for azure-spring-data-cosmos
 */
class CosmosSchedulers {

    /**
     * Spring custom parallel scheduler name
     */
    private static final String SPRING_DATA_COSMOS_PARALLEL_THREAD_NAME =  "spring-data-cosmos-parallel";

    /**
     * Using a custom parallel scheduler to be able to schedule retries etc.
     *     without being vulnerable to scenarios where applications abuse the
     *     Parallel scheduler and cause thread starvation on Reactor Core parallel scheduler
     */
    public static final Scheduler SPRING_DATA_COSMOS_PARALLEL = Schedulers.newParallel(
        SPRING_DATA_COSMOS_PARALLEL_THREAD_NAME,
        Schedulers.DEFAULT_POOL_SIZE,
        true);
}

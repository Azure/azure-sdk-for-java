// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.data.cosmos.common;

import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

/**
 * Schedulers for azure-spring-data-cosmos
 */
public class CosmosSchedulers {

    /**
     * Spring custom parallel scheduler name
     */
    private static final String COSMOS_SPRING_PARALLEL_THREAD_NAME =  "cosmos-spring-parallel";

    // Using a custom parallel scheduler to be able to schedule retries etc.
    // without being vulnerable to scenarios where applications abuse the
    // Parallel scheduler and cause thread starvation on Reactor Core parallel scheduler
    public static final Scheduler COSMOS_SPRING_PARALLEL = Schedulers.newParallel(
        COSMOS_SPRING_PARALLEL_THREAD_NAME,
        Schedulers.DEFAULT_POOL_SIZE,
        true);
}

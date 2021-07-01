// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public class CosmosSchedulers {
    private final static String COSMOS_PARALLEL_THREAD_NAME =  "cosmos-parallel";

    // Using a custom parallel scheduler to be able to schedule retries etc.
    // without being vulnerable to scenarios where applications abuse the
    // Parallel scheduler and cause thread starvation on Reactor Core parallel scheduler
    public final static Scheduler COSMOS_PARALLEL = Schedulers.newParallel(
        COSMOS_PARALLEL_THREAD_NAME,
        Schedulers.DEFAULT_POOL_SIZE,
        true);
}

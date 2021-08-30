// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public class CosmosSchedulers {
    private final static String COSMOS_PARALLEL_THREAD_NAME =  "cosmos-parallel";
    private final static String TRANSPORT_CLIENT_BOUNDED_ELASTIC_THREAD_NAME = "transport-client-bounded-elastic";

    // Using a custom parallel scheduler to be able to schedule retries etc.
    // without being vulnerable to scenarios where applications abuse the
    // Parallel scheduler and cause thread starvation on Reactor Core parallel scheduler
    public final static Scheduler COSMOS_PARALLEL = Schedulers.newParallel(
        COSMOS_PARALLEL_THREAD_NAME,
        Schedulers.DEFAULT_POOL_SIZE,
        true);

    // Custom bounded elastic scheduler to switch off IO thread as soon as possible.
    public final static Scheduler TRANSPORT_CLIENT_BOUNDED_ELASTIC = Schedulers.newBoundedElastic(
        Schedulers.DEFAULT_BOUNDED_ELASTIC_SIZE,
        Schedulers.DEFAULT_BOUNDED_ELASTIC_QUEUESIZE,
        TRANSPORT_CLIENT_BOUNDED_ELASTIC_THREAD_NAME
    );
}

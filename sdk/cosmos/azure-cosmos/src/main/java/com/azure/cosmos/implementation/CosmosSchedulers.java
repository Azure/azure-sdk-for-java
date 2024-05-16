// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public class CosmosSchedulers {
    private final static String COSMOS_PARALLEL_THREAD_NAME =  "cosmos-parallel";
    private final static String TRANSPORT_RESPONSE_BOUNDED_ELASTIC_THREAD_NAME = "transport-response-bounded-elastic";
    private final static String BULK_EXECUTOR_BOUNDED_ELASTIC_THREAD_NAME = "bulk-executor-bounded-elastic";
    private final static String BULK_EXECUTOR_FLUSH_BOUNDED_ELASTIC_THREAD_NAME = "bulk-executor-flush-bounded-elastic";
    private final static String CLIENT_TELEMETRY_BOUNDED_ELASTIC_THREAD_NAME = "client-telemetry-bounded-elastic";
    private final static String OPEN_CONNECTIONS_BOUNDED_ELASTIC_THREAD_NAME = "open-connections-bounded-elastic";
    private final static String ASYNC_CACHE_BACKGROUND_REFRESH_THREAD_NAME = "async-cache-background-refresh-bounded-elastic";
    private final static String FAULT_INJECTION_CONNECTION_ERROR_THREAD_NAME = "fault-injection-connection-error-bounded-elastic";

    private final static int TTL_FOR_SCHEDULER_WORKER_IN_SECONDS = 60; // same as BoundedElasticScheduler.DEFAULT_TTL_SECONDS

    // Using a custom parallel scheduler to be able to schedule retries etc.
    // without being vulnerable to scenarios where applications abuse the
    // Parallel scheduler and cause thread starvation on Reactor Core parallel scheduler
    public final static Scheduler COSMOS_PARALLEL = Schedulers.newParallel(
        COSMOS_PARALLEL_THREAD_NAME,
        Schedulers.DEFAULT_POOL_SIZE,
        true);

    // Custom bounded elastic scheduler to switch off IO thread to process response.
    public final static Scheduler TRANSPORT_RESPONSE_BOUNDED_ELASTIC = Schedulers.newBoundedElastic(
        Schedulers.DEFAULT_BOUNDED_ELASTIC_SIZE,
        Schedulers.DEFAULT_BOUNDED_ELASTIC_QUEUESIZE,
        TRANSPORT_RESPONSE_BOUNDED_ELASTIC_THREAD_NAME,
        TTL_FOR_SCHEDULER_WORKER_IN_SECONDS,
        true
    );

    // Custom bounded elastic scheduler process bulk execution tasks
    public final static Scheduler BULK_EXECUTOR_BOUNDED_ELASTIC = Schedulers.newBoundedElastic(
        2 * Schedulers.DEFAULT_BOUNDED_ELASTIC_SIZE,
        Schedulers.DEFAULT_BOUNDED_ELASTIC_QUEUESIZE,
        BULK_EXECUTOR_BOUNDED_ELASTIC_THREAD_NAME,
        TTL_FOR_SCHEDULER_WORKER_IN_SECONDS,
        true
    );

    public final static Scheduler BULK_EXECUTOR_FLUSH_BOUNDED_ELASTIC = Schedulers.newBoundedElastic(
        Schedulers.DEFAULT_BOUNDED_ELASTIC_SIZE,
        Schedulers.DEFAULT_BOUNDED_ELASTIC_QUEUESIZE,
        BULK_EXECUTOR_FLUSH_BOUNDED_ELASTIC_THREAD_NAME,
        TTL_FOR_SCHEDULER_WORKER_IN_SECONDS,
        true
    );

    public final static Scheduler CLIENT_TELEMETRY_BOUNDED_ELASTIC = Schedulers.newBoundedElastic(
        Schedulers.DEFAULT_BOUNDED_ELASTIC_SIZE,
        Schedulers.DEFAULT_BOUNDED_ELASTIC_QUEUESIZE,
        CLIENT_TELEMETRY_BOUNDED_ELASTIC_THREAD_NAME,
        TTL_FOR_SCHEDULER_WORKER_IN_SECONDS
    );

    // Custom bounded elastic scheduler for open connections
    public final static Scheduler OPEN_CONNECTIONS_BOUNDED_ELASTIC = Schedulers.newBoundedElastic(
            Schedulers.DEFAULT_BOUNDED_ELASTIC_SIZE,
            Schedulers.DEFAULT_BOUNDED_ELASTIC_QUEUESIZE,
            OPEN_CONNECTIONS_BOUNDED_ELASTIC_THREAD_NAME,
            TTL_FOR_SCHEDULER_WORKER_IN_SECONDS,
            true
    );

    // Custom bounded elastic scheduler for async cache background refresh task
    public final static Scheduler ASYNC_CACHE_BACKGROUND_REFRESH_BOUNDED_ELASTIC = Schedulers.newBoundedElastic(
            Schedulers.DEFAULT_BOUNDED_ELASTIC_SIZE,
            Schedulers.DEFAULT_BOUNDED_ELASTIC_QUEUESIZE,
            ASYNC_CACHE_BACKGROUND_REFRESH_THREAD_NAME,
            TTL_FOR_SCHEDULER_WORKER_IN_SECONDS,
            true
    );


    // Custom bounded elastic scheduler for fault injection connection erros
    public final static Scheduler FAULT_INJECTION_CONNECTION_ERROR_BOUNDED_ELASTIC = Schedulers.newBoundedElastic(
        Schedulers.DEFAULT_BOUNDED_ELASTIC_SIZE,
        Schedulers.DEFAULT_BOUNDED_ELASTIC_QUEUESIZE,
        FAULT_INJECTION_CONNECTION_ERROR_THREAD_NAME,
        TTL_FOR_SCHEDULER_WORKER_IN_SECONDS,
        true
    );
}

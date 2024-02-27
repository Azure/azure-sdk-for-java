// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation;

import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

public class KafkaCosmosSchedulers {
    private final static String SINK_BOUNDED_ELASTIC_THREAD_NAME = "sink-bounded-elastic";
    private final static int TTL_FOR_SCHEDULER_WORKER_IN_SECONDS = 60; // same as BoundedElasticScheduler.DEFAULT_TTL_SECONDS
    public final static Scheduler SINK_BOUNDED_ELASTIC = Schedulers.newBoundedElastic(
        Schedulers.DEFAULT_BOUNDED_ELASTIC_SIZE,
        Schedulers.DEFAULT_BOUNDED_ELASTIC_QUEUESIZE,
        SINK_BOUNDED_ELASTIC_THREAD_NAME,
        TTL_FOR_SCHEDULER_WORKER_IN_SECONDS,
        true
    );
}

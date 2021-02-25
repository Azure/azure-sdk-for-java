// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.CosmosAsyncContainer;
import com.azure.cosmos.CosmosAsyncDatabase;
import com.azure.cosmos.benchmark.BenchmarkHelper;
import com.azure.cosmos.benchmark.Configuration;
import com.azure.cosmos.benchmark.linkedin.data.Key;
import com.azure.cosmos.benchmark.linkedin.impl.Accessor;
import com.azure.cosmos.benchmark.linkedin.impl.CosmosDBDataAccessor;
import com.azure.cosmos.benchmark.linkedin.impl.DocumentTransformer;
import com.azure.cosmos.benchmark.linkedin.impl.IdentityDocumentTransformer;
import com.azure.cosmos.benchmark.linkedin.impl.OperationsLogger;
import com.azure.cosmos.benchmark.linkedin.impl.ResponseHandler;
import com.azure.cosmos.benchmark.linkedin.impl.datalocator.StaticDataLocator;
import com.azure.cosmos.benchmark.linkedin.impl.exceptions.AccessorException;
import com.azure.cosmos.benchmark.linkedin.impl.keyextractor.KeyExtractor;
import com.azure.cosmos.benchmark.linkedin.impl.keyextractor.KeyExtractorImpl;
import com.azure.cosmos.benchmark.linkedin.impl.metrics.MetricsFactory;
import com.azure.cosmos.benchmark.linkedin.impl.models.CollectionKey;
import com.azure.cosmos.benchmark.linkedin.impl.models.GetRequestOptions;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.common.base.Preconditions;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The test implementation for GETs/pointed lookups against CosmosDB. It takes in
 * the testData and submits upto N get operations at a time, leveraging the ExecutorService
 * for the parallelism control.
 *
 * The Latency tracking and reposting is handled within the Accessor, and not in this method.
 */
public class GetTestRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetTestRunner.class);
    private static final Duration TERMINATION_WAIT_DURATION = Duration.ofSeconds(60);

    private final Configuration _configuration;
    private final Accessor<Key, JsonNode> _accessor;
    private final ExecutorService _executorService;
    private final AtomicLong _successCount;
    private final AtomicLong _errorCount;
    private final Semaphore _semaphore;

    GetTestRunner(final Configuration configuration,
        final CosmosAsyncClient client,
        final MetricRegistry metricsRegistry) {
        Preconditions.checkNotNull(configuration,
            "The Workload configuration defining the parameters can not be null");
        Preconditions.checkNotNull(client,
            "Need a non-null client for setting up the Database and containers for the test");
        Preconditions.checkNotNull(metricsRegistry,
            "The MetricsRegistry can not be null");

        _configuration = configuration;
        _accessor = createAccessor(configuration, client, metricsRegistry);
        _executorService = Executors.newFixedThreadPool(configuration.getConcurrency());
        _successCount = new AtomicLong(0);
        _errorCount = new AtomicLong(0);
        _semaphore = new Semaphore(configuration.getConcurrency());
    }

    public void run(final Set<Key> testKeys) {
        final ArrayList<Key> keys = new ArrayList<>(testKeys);
        Collections.shuffle(keys);
        final long runStartTime = System.currentTimeMillis();
        long i = 0;
        for (; BenchmarkHelper.shouldContinue(runStartTime, i, _configuration); i++) {
            int index = (int) ((i % keys.size()) % Integer.MAX_VALUE);
            final Key key = keys.get(index);
            try {
                _semaphore.acquire();
            } catch (InterruptedException e) {
                // Continue the execution, since the check above handles checks for error thresholds
                _errorCount.incrementAndGet();
                continue;
            }
            _executorService.submit(() -> runOperation(key));
        }

        // Log the completion details
        final Instant runEndTime = Instant.now();
        LOGGER.info("Number of iterations: {}, Errors: {}, Runtime: {} millis",
            _successCount.get(),
            _errorCount.get(),
            runEndTime.minusMillis(runStartTime).toEpochMilli());
    }

    public void cleanup() {
        try {
            _executorService.awaitTermination(TERMINATION_WAIT_DURATION.getSeconds(), TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.error("Error awaiting the completion of all tasks", e);
        }

        _executorService.shutdown();
    }

    private void runOperation(final Key key) {
        try {
            _accessor.get(key, GetRequestOptions.EMPTY_REQUEST_OPTIONS);
            _successCount.getAndIncrement();
        } catch (AccessorException e) {
            LOGGER.error("Received running exception", e);
            _errorCount.getAndIncrement();
        } finally {
            _semaphore.release();
        }
    }

    private Accessor<Key, JsonNode> createAccessor(final Configuration configuration,
        final CosmosAsyncClient client,
        final MetricRegistry metricsRegistry) {

        final StaticDataLocator dataLocator = createDataLocator(configuration, client);
        final KeyExtractor<Key> keyExtractor = new KeyExtractorImpl();
        final DocumentTransformer<JsonNode, JsonNode> documentTransformer = new IdentityDocumentTransformer<>();
        final Clock clock = Clock.systemUTC();
        return new CosmosDBDataAccessor<>(dataLocator,
            keyExtractor,
            new ResponseHandler<>(documentTransformer, keyExtractor),
            new MetricsFactory(metricsRegistry, clock),
            clock,
            new OperationsLogger(Duration.ofSeconds(10)));
    }

    private StaticDataLocator createDataLocator(Configuration configuration, CosmosAsyncClient client) {
        final CollectionKey collectionKey = new CollectionKey(configuration.getServiceEndpoint(),
            configuration.getDatabaseId(),
            configuration.getCollectionId());
        final CosmosAsyncDatabase database = client.getDatabase(configuration.getDatabaseId());
        final CosmosAsyncContainer container = database.getContainer(configuration.getCollectionId());
        return new StaticDataLocator(collectionKey, container);
    }
}

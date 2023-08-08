// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.benchmark.Configuration;
import com.azure.cosmos.benchmark.linkedin.data.EntityConfiguration;
import com.azure.cosmos.benchmark.linkedin.data.Key;
import com.azure.cosmos.benchmark.linkedin.impl.exceptions.AccessorException;
import com.azure.cosmos.benchmark.linkedin.impl.models.GetRequestOptions;
import com.azure.cosmos.benchmark.linkedin.impl.models.QueryOptions;
import com.codahale.metrics.MetricRegistry;
import java.util.Random;

/**
 * The composite test runner is configured to execute 85% GET queries (lookup by id/partitioningKey)
 * and 15% SQL QUERIES (lookup by partitioningKey) for the entity
 */
public class CompositeReadTestRunner extends TestRunner {

    private static final float CUTOFF_THRESHOLD = 0.85f;

    private final QueryTestRunner.QueryGenerator _queryGenerator;
    /**
     * Random number generator to determine whether to execute a GET or a QUERY call
     * to the underlying API
     */
    private final Random _randomNumberGenerator;

    CompositeReadTestRunner(final Configuration configuration,
        final CosmosAsyncClient client,
        final MetricRegistry metricsRegistry,
        final EntityConfiguration entityConfiguration) {
        super(configuration, client, metricsRegistry, entityConfiguration);
        _queryGenerator = new QueryTestRunner.QueryGenerator();
        _randomNumberGenerator = new Random(System.currentTimeMillis());
    }

    @Override
    protected void testOperation(final Key key) throws AccessorException {
        final float generatedValue = _randomNumberGenerator.nextFloat();
        if (generatedValue <= CUTOFF_THRESHOLD) {
            _accessor.get(key, GetRequestOptions.EMPTY_REQUEST_OPTIONS);
        } else {
            final QueryOptions queryOptions = _queryGenerator.generateQuery(key);
            _accessor.query(queryOptions);
        }
    }
}

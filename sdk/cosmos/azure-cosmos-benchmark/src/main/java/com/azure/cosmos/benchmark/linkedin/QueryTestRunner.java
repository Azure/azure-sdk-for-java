// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.benchmark.Configuration;
import com.azure.cosmos.benchmark.linkedin.data.EntityConfiguration;
import com.azure.cosmos.benchmark.linkedin.data.Key;
import com.azure.cosmos.benchmark.linkedin.impl.Constants;
import com.azure.cosmos.benchmark.linkedin.impl.exceptions.AccessorException;
import com.azure.cosmos.benchmark.linkedin.impl.models.QueryOptions;
import com.azure.cosmos.models.SqlParameter;
import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The test implementation for GETs/pointed lookups against CosmosDB. It takes in
 * the testData and submits upto N get operations at a time, leveraging the ExecutorService
 * for the parallelism control.
 *
 * The Latency tracking and reposting is handled within the Accessor, and not in this method.
 */
public class QueryTestRunner extends TestRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(QueryTestRunner.class);
    private static final String PARTITIONING_KEY_BINDING_PARAMETER = "@" + Constants.PARTITION_KEY;
    private static final String QUERY = String.format("SELECT * FROM c WHERE c.%s = %s",
        Constants.PARTITION_KEY, PARTITIONING_KEY_BINDING_PARAMETER);

    QueryTestRunner(final Configuration configuration,
        final CosmosAsyncClient client,
        final MetricRegistry metricsRegistry,
        final EntityConfiguration entityConfiguration) {
        super(configuration, client, metricsRegistry, entityConfiguration);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void testOperation(final Key key) throws AccessorException {
        final SqlParameter sqlParameter = new SqlParameter(PARTITIONING_KEY_BINDING_PARAMETER,
            key.getPartitioningKey());
        final QueryOptions queryOptions = new QueryOptions.Builder()
            .setDocumentDBQuery(QUERY)
            .setSqlParameterList(ImmutableList.of(sqlParameter))
            .setPartitioningKey(key.getPartitioningKey())
            .build();
        _accessor.query(queryOptions);
    }
}

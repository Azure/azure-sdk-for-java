// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.benchmark.Configuration;
import com.azure.cosmos.benchmark.linkedin.data.EntityConfiguration;
import com.azure.cosmos.benchmark.linkedin.data.Key;
import com.azure.cosmos.benchmark.linkedin.impl.exceptions.AccessorException;
import com.azure.cosmos.benchmark.linkedin.impl.models.GetRequestOptions;
import com.codahale.metrics.MetricRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * The test implementation for GETs/pointed lookups against CosmosDB. It takes in
 * the testData and submits upto N get operations at a time, leveraging the ExecutorService
 * for the parallelism control.
 *
 * The Latency tracking and reposting is handled within the Accessor, and not in this method.
 */
public class GetTestRunner extends TestRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(GetTestRunner.class);

    GetTestRunner(final Configuration configuration,
        final CosmosAsyncClient client,
        final MetricRegistry metricsRegistry,
        final EntityConfiguration entityConfiguration) {
        super(configuration, client, metricsRegistry, entityConfiguration);
    }

    @Override
    public void testOperation(final Key key) throws AccessorException {
        _accessor.get(key, GetRequestOptions.EMPTY_REQUEST_OPTIONS);
    }
}

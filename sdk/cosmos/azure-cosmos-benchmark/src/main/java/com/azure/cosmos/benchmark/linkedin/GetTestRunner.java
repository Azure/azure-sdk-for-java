// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.benchmark.linkedin;

import com.azure.cosmos.CosmosAsyncClient;
import com.azure.cosmos.benchmark.TenantWorkloadConfig;
import com.azure.cosmos.benchmark.linkedin.data.EntityConfiguration;
import com.azure.cosmos.benchmark.linkedin.data.Key;
import com.azure.cosmos.benchmark.linkedin.impl.exceptions.AccessorException;
import com.azure.cosmos.benchmark.linkedin.impl.models.GetRequestOptions;
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

    GetTestRunner(final TenantWorkloadConfig workloadConfig,
        final CosmosAsyncClient client,
        final EntityConfiguration entityConfiguration) {
        super(workloadConfig, client, entityConfiguration);
    }

    @Override
    public void testOperation(final Key key) throws AccessorException {
        _accessor.get(key, GetRequestOptions.EMPTY_REQUEST_OPTIONS);
    }
}

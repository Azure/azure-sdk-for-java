// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.developer.loadtesting;

import com.azure.core.annotation.ServiceClient;
import com.azure.developer.loadtesting.implementation.LoadTestingClientImpl;

/** Initializes a new instance of the asynchronous LoadTestingClient type. */
@ServiceClient(builder = LoadTestingClientBuilder.class, isAsync = true)
public final class LoadTestingAsyncClient {

    /**
     * {@link LoadTestAdministrationAsyncClient} contains AppComponent, ServerMetrics and
     * Test operations.
     */
    private final LoadTestAdministrationAsyncClient administration;

    /**
     * {@link TestRunAsyncClient} involves operations for running a test
     */
    private final TestRunAsyncClient testRun;

    /**
     * Initializes an instance of LoadTestingAsyncClient class.
     *
     * @param serviceClient the service client implementation.
     */
    LoadTestingAsyncClient(LoadTestingClientImpl serviceClient) {
        this.administration = new LoadTestAdministrationAsyncClient(serviceClient.getLoadTestAdministrations());
        this.testRun = new TestRunAsyncClient(serviceClient.getTestRuns());
    }

    /**
     * Returns the instance of LoadTestAdministrationAsyncClient class.
     *
     * @return {@link LoadTestAdministrationAsyncClient} object.
     */
    public LoadTestAdministrationAsyncClient getLoadTestAdministration() {
        return this.administration;
    }

    /**
     * Returns the instance of TestRunAsyncClient class.
     *
     * @return {@link TestRunAsyncClient} object.
     */
    public TestRunAsyncClient getLoadTestRun() {
        return this.testRun;
    }
}

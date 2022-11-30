// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.developer.loadtesting;

import com.azure.core.annotation.ServiceClient;

/** Initializes a new instance of the synchronous LoadTestingClient type. */
@ServiceClient(builder = LoadTestingClientBuilder.class)
public final class LoadTestingClient {

    /**
     * {@link LoadTestAdministrationClient} contains AppComponent, ServerMetrics and
     * Test operations.
     */
    private final LoadTestAdministrationClient administration;

    /**
     * {@link TestRunClient} involves operations for running a test
     */
    private final TestRunClient testRun;

    /**
     * Initializes an instance of LoadTestingClient class.
     *
     * @param serviceClient the service client implementation.
     */
    LoadTestingClient(LoadTestingAsyncClient client) {
        this.administration = new LoadTestAdministrationClient(client.getLoadTestAdministrationAsyncClient());
        this.testRun = new TestRunClient(client.getLoadTestRunAsyncClient());
    }

    /**
     * Returns the instance of LoadTestAdministrationClient class.
     *
     * @return {@link LoadTestAdministrationClient} object.
     */
    public LoadTestAdministrationClient getLoadTestAdministrationClient() {
        return this.administration;
    }

    /**
     * Returns the instance of TestRunClient class.
     *
     * @return {@link TestRunClient} object.
     */
    public TestRunClient getLoadTestRunClient() {
        return this.testRun;
    }
}

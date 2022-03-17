// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.perf.core;

import com.azure.perf.test.core.PerfStressOptions;
import com.beust.jcommander.Parameter;

public class CorePerfStressOptions extends PerfStressOptions {

    @Parameter(names = { "-e", "--endpoint" }, description = "The base endpoint for rest proxy tests")
    private String endpoint = "http://unused";

    @Parameter(names = { "--http-client" }, description = "The http client to use. Can be netty, okhttp. "
        + "Must be specified if non-mock backend type is used otherwise is ignored")
    private HttpClientType httpClient = null;

    @Parameter(names = { "--backend-type"}, description = "The backend type used for tests. "
        + "Options are mock, blobs or wiremock. "
        + "Defaults to mock.")
    private BackendType backendType = BackendType.MOCK;

    @Parameter(names = { "--include-pipeline-policies" },
        description = "Includes a bunch of core pipeline policies in the test")
    private boolean includePipelinePolicies;

    /**
     * The base endpoint for rest proxy tests. See {@link MyRestProxyService}.
     * @return The base endpoint for rest proxy tests.
     */
    public String getEndpoint() {
        return endpoint;
    }

    /**
     * The http client to use. Can be netty, okhttp.
     * Must be specified if non-mock backend type is used otherwise is ignored
     * @return The http client to use.
     */
    public HttpClientType getHttpClient() {
        return httpClient;
    }

    /**
     * The backend type used for tests. Options are mock, blobs or wiremock. Defaults to mock.
     * @return The backend type used for tests.
     */
    public BackendType getBackendType() {
        return backendType;
    }

    /**
     * Includes a bunch of core pipeline policies in the test
     * @return Whether to include a bunch of core pipeline policies in the test
     */
    public boolean isIncludePipelinePolicies() {
        return includePipelinePolicies;
    }

    public enum HttpClientType {
        NETTY, OKHTTP
    }

    public enum BackendType {
        MOCK, BLOBS, WIREMOCK
    }
}

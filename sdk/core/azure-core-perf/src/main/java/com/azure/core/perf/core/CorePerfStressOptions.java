// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.perf.core;

import com.azure.perf.test.core.PerfStressOptions;
import com.beust.jcommander.Parameter;

public class CorePerfStressOptions extends PerfStressOptions {

    @Parameter(names = { "--backend-type"}, description = "The backend type used for tests. "
        + "Options are mock, blobs or wiremock. "
        + "Defaults to mock.")
    private BackendType backendType = BackendType.MOCK;

    @Parameter(names = { "--binary-data-source"}, description =
        "The binary data source used for tests that use BinaryData. "
        + "Options are bytes, file or stream. "
        + "Defaults to file.")
    private BinaryDataSource binaryDataSource = BinaryDataSource.FILE;

    @Parameter(names = { "--include-pipeline-policies" },
        description = "Includes a bunch of core pipeline policies in the test")
    private boolean includePipelinePolicies;

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

    /**
     * The binary data source used for tests that use BinaryData.
     * Options are bytes, file or stream.
     * Defaults to file.
     * @return The binary data source used for tests that use BinaryData.
     */
    public BinaryDataSource getBinaryDataSource() {
        return binaryDataSource;
    }

    public enum BackendType {
        MOCK, BLOBS, WIREMOCK
    }

    public enum BinaryDataSource {
        // TODO (kasobol-msft) add FLUX when there's an option to provide it lazily.
        BYTES, FILE, STREAM
    }
}

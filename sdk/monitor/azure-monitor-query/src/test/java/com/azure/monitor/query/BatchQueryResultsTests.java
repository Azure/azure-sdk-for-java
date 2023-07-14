// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.monitor.query;

import com.azure.monitor.query.implementation.logs.models.BatchQueryResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

/**
 * Unit test using JSON from a live test run where {@link BatchQueryResponse} failed to deserialize.
 */
public class BatchQueryResponseTests {
    @Test
    public void failingJsonDeserialization() throws IOException {
        try (InputStream json = BatchQueryResponseTests.class.getResourceAsStream("FailingBatchQueryResultsJson.json")) {

        }
    }
}

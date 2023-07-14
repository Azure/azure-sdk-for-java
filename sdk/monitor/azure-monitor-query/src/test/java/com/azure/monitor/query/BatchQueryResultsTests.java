// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.monitor.query;

import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.monitor.query.implementation.logs.models.BatchQueryResults;
import com.azure.monitor.query.implementation.logs.models.BatchResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;

/**
 * Unit test using JSON from a live test run where {@link BatchQueryResults} failed to deserialize.
 */
public class BatchQueryResultsTests {
    @Test
    public void failingJsonDeserialization() throws IOException {
        ClassLoader classLoader = getClass().getClassLoader();
        try (InputStream json = classLoader.getResourceAsStream("FailingBatchQueryResultsJson.json");
             JsonReader jsonReader = JsonProviders.createReader(json)) {
            BatchResponse batchQueryResults = BatchResponse.fromJson(jsonReader);
        }
    }
}

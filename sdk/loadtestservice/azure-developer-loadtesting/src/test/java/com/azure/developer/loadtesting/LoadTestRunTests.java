// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.developer.loadtesting;

import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public final class LoadTestRunTests extends LoadTestingClientTestBase {
    private Map<String, Object> getTestRunBodyFromDict() {
        Map<String, Object> testRunMap = new HashMap<String, Object>();
        testRunMap.put("testId", existingTestId);
        testRunMap.put("displayName", "Java SDK Sample Test Run");
        testRunMap.put("description", "Sample Test Run");

        return testRunMap;
    }

    @Test
    @Order(1)
    public void createOrUpdateTestRun() {
        BinaryData file = BinaryData.fromObject(getTestRunBodyFromDict());
        Response<BinaryData> response = builder.buildLoadTestRunClient().createOrUpdateTestRunWithResponse(newTestRunId, file, null);
        Assertions.assertTrue(Arrays.asList(200, 201).contains(response.getStatusCode()));
    }

    @Test
    @Order(2)
    public void listTestRuns() {
        RequestOptions reqOpts = new RequestOptions()
                                    .addQueryParam("testId", existingTestId);
        PagedIterable<BinaryData> response = builder.buildLoadTestRunClient().listTestRuns(reqOpts);
        boolean found = response.stream().anyMatch((testRunBinary) -> {
            try {
                JsonNode testRun = new ObjectMapper().readTree(testRunBinary.toString());
                if (testRun.get("testRunId").asText().equals(newTestRunId) && testRun.get("testId").asText().equals(existingTestId)) {
                    return true;
                }
            } catch (Exception e) {
                // no-op
            }
            return false;
        });
        Assertions.assertTrue(found);
    }

    @Test
    @Order(3)
    public void deleteTestRun() {
        Assertions.assertDoesNotThrow(() -> {
            builder.buildLoadTestRunClient().deleteTestRunWithResponse(newTestRunId, null);
        });
    }
}

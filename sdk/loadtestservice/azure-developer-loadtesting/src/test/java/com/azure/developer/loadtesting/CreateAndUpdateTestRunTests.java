// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.developer.loadtesting;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class CreateAndUpdateTestRunTests extends LoadTestingClientTestBase {
    private final String CREATE_TEST_RUN_BODY_JSON = "{\"testId\":\"" + DEFAULT_TEST_ID + "\""
        + ",\"description\":\"Sample Test Run\",\"displayName\":\"Java SDK Sample Test Run\"}";

    private Map<String, Object> getTestRunBodyFromDict() {
        Map<String, Object> testRunMap = new HashMap<String, Object>();
        testRunMap.put("testId", DEFAULT_TEST_ID);
        testRunMap.put("displayName", "Java SDK Sample Test Run");
        testRunMap.put("description", "Sample Test Run");

        return testRunMap;
    }

    @Test
    public void createAndUpdateTestRunString() {
        BinaryData file = BinaryData.fromString(CREATE_TEST_RUN_BODY_JSON);
        RequestOptions requestOptions = new RequestOptions();
        Response<BinaryData> response = client.getTestRun().createAndUpdateTestRunWithResponse(DEFAULT_TEST_RUN_ID, file, requestOptions);
        Assertions.assertEquals(200, response.getStatusCode());
    }

    @Test
    public void createAndUpdateTestRunDict() {
        BinaryData file = BinaryData.fromObject(getTestRunBodyFromDict());
        RequestOptions requestOptions = new RequestOptions();
        Response<BinaryData> response = client.getTestRun().createAndUpdateTestRunWithResponse(DEFAULT_TEST_RUN_ID, file, requestOptions);
        Assertions.assertEquals(200, response.getStatusCode());
    }
}

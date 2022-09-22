// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.developer.loadtesting;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class CreateOrUpdateTestTests extends LoadTestingClientTestBase {
    private final String createTestBodyJson = "{\"description\":\"Sample Test\",\"displayName\":\"Java SDK Sample Test\","
        + "\"environmentVariables\":{\"threads_per_engine\":1,\"ramp_up_time\":0,\"duration_in_sec\":10,\"domain\":\"azure.microsoft.com\",\"protocol\":\"https\"},"
        + "\"loadTestConfig\":{\"engineInstances\":1}}";

    private Map<String, Object> getTestBodyFromDict() {
        Map<String, Object> testMap = new HashMap<String, Object>();
        testMap.put("displayName", "Java SDK Sample Test");
        testMap.put("description", "Sample Test");

        Map<String, Object> loadTestConfigMap = new HashMap<String, Object>();
        loadTestConfigMap.put("engineInstances", 1);
        testMap.put("loadTestConfig", loadTestConfigMap);

        Map<String, Object> envVarMap = new HashMap<String, Object>();
        envVarMap.put("threads_per_engine", 1);
        envVarMap.put("ramp_up_time", 0);
        envVarMap.put("duration_in_sec", 10);
        envVarMap.put("domain", "azure.microsoft.com");
        envVarMap.put("protocol", "https");
        testMap.put("environmentVariables", envVarMap);

        return testMap;
    }

    @Test
    public void createOrUpdateTestString() {
        BinaryData body = BinaryData.fromString(createTestBodyJson);
        RequestOptions requestOptions = new RequestOptions();
        Response<BinaryData> response = client.getLoadTestAdministration().createOrUpdateTestWithResponse(defaultTestId, body, requestOptions);
        Assertions.assertTrue(Arrays.asList(200, 201).contains(response.getStatusCode()));
    }

    @Test
    public void createOrUpdateTestDictDict() {
        BinaryData body = BinaryData.fromObject(getTestBodyFromDict());
        RequestOptions requestOptions = new RequestOptions();
        Response<BinaryData> response = client.getLoadTestAdministration().createOrUpdateTestWithResponse(defaultTestId, body, requestOptions);
        Assertions.assertTrue(Arrays.asList(200, 201).contains(response.getStatusCode()));
    }
}

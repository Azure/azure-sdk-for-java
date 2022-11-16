// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.developer.loadtesting;

import com.azure.core.exception.ResourceNotFoundException;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public final class LoadTestAdministrationTests extends LoadTestingClientTestBase {
    private Map<String, Object> getTestBodyFromDict() {
        Map<String, Object> testMap = new HashMap<String, Object>();
        testMap.put("displayName", "Java SDK Sample Test");
        testMap.put("description", "Sample Test");

        Map<String, Object> loadTestConfigMap = new HashMap<String, Object>();
        loadTestConfigMap.put("engineInstances", 1);
        testMap.put("loadTestConfiguration", loadTestConfigMap);

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
    @Order(1)
    public void createOrUpdateLoadTest() {
        BinaryData body = BinaryData.fromObject(getTestBodyFromDict());
        Response<BinaryData> response = builder.buildLoadTestAdministrationClient().createOrUpdateLoadTestWithResponse(newTestId, body, null);
        Assertions.assertTrue(Arrays.asList(200, 201).contains(response.getStatusCode()));
    }


    @Test
    @Order(2)
    public void uploadTestFileValid() {
        URL fileJmxUrl = LoadTestAdministrationTests.class.getClassLoader().getResource(defaultUploadFileName);
        BinaryData file = BinaryData.fromFile(new File(fileJmxUrl.getPath()).toPath());
        RequestOptions requestOptions = new RequestOptions().addQueryParam("fileType", "JMX_FILE");
        Response<BinaryData> response = builder.buildLoadTestAdministrationClient().uploadFileWithResponse(
                                                newTestId,
                                                defaultUploadFileName,
                                                file,
                                                requestOptions);
        Assertions.assertEquals(201, response.getStatusCode());
    }

    @Test
    @Order(3)
    public void uploadTestFileResourceNotFound() {
        URL fileJmxUrl = LoadTestAdministrationTests.class.getClassLoader().getResource(defaultUploadFileName);
        BinaryData file = BinaryData.fromFile(new File(fileJmxUrl.getPath()).toPath());
        RequestOptions requestOptions = new RequestOptions().addQueryParam("fileType", "JMX_FILE");
        Assertions.assertThrows(ResourceNotFoundException.class, () -> {
            builder.buildLoadTestAdministrationClient().uploadFileWithResponse(
                                                "fake_test_id",
                                                defaultUploadFileName,
                                                file,
                                                requestOptions);
        });
    }

    @Test
    @Order(4)
    public void listLoadTests() {
        RequestOptions reqOpts = new RequestOptions()
                                    .addQueryParam("orderBy", "lastModifiedDateTime desc");
        PagedIterable<BinaryData> response = builder.buildLoadTestAdministrationClient().listLoadTests(reqOpts);
        boolean found = response.stream().anyMatch((testBinary) -> {
            try {
                JsonNode test = new ObjectMapper().readTree(testBinary.toString());
                if (test.get("testId").asText().equals(newTestId)) {
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
    @Order(5)
    public void deleteTest() {
        Assertions.assertDoesNotThrow(() -> {
            builder.buildLoadTestAdministrationClient().deleteLoadTestWithResponse(newTestId, null);
        });
    }
}

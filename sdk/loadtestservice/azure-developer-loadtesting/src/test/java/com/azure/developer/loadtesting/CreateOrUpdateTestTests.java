// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.developer.loadtesting;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import java.util.Arrays;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class CreateOrUpdateTestTests extends LoadTestingClientTestBase {
    private final String CREATE_TEST_BODY_JSON = "{\"description\":\"Sample Test\",\"displayName\":\"Java SDK Sample Test\","
        + "\"environmentVariables\":{\"threads_per_engine\":1,\"ramp_up_time\":0,\"duration_in_sec\":10,\"domain\":\"azure.microsoft.com\",\"protocol\":\"https\"},"
        + "\"loadTestConfig\":{\"engineInstances\":1}}";

    @Test
    public void simpleCreateOrUpdateTest() {
        BinaryData body = BinaryData.fromString(CREATE_TEST_BODY_JSON);
        RequestOptions requestOptions = new RequestOptions();
        Response<BinaryData> response = testClient.createOrUpdateTestWithResponse(DEFAULT_TEST_ID, body, requestOptions);
        Assertions.assertTrue(Arrays.asList(200, 201).contains(response.getStatusCode()));
    }
}

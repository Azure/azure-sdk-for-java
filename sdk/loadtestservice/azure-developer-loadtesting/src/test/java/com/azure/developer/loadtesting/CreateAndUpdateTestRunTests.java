// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.developer.loadtesting;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class CreateAndUpdateTestRunTests extends LoadTestingClientTestBase {
    private final String CREATE_TEST_RUN_BODY_JSON = "{\"testId\":\"" + DEFAULT_TEST_ID + "\""
        + ",\"description\":\"Sample Test Run\",\"displayName\":\"Java SDK Sample Test Run\"}";

    @Test
    public void simpleCreateAndUpdateTestRun() {
        BinaryData file = BinaryData.fromString(CREATE_TEST_RUN_BODY_JSON);
        RequestOptions requestOptions = new RequestOptions();
        Response<BinaryData> response = testRunClient.createAndUpdateTestWithResponse(DEFAULT_TEST_RUN_ID, file, requestOptions);
        Assertions.assertEquals(200, response.getStatusCode());
    }
}

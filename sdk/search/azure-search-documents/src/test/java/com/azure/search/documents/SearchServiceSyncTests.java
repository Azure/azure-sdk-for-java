// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.search.documents.models.RequestOptions;
import com.azure.search.documents.models.ServiceStatistics;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static com.azure.search.documents.TestHelpers.assertObjectEquals;

public class SearchServiceSyncTests extends SearchServiceTestBase {

    @Test
    public void getServiceStatsReturnsCorrectDefinition() {
        SearchServiceClient serviceClient = getSearchServiceClientBuilder().buildClient();

        ServiceStatistics serviceStatistics = serviceClient.getServiceStatistics();
        assertObjectEquals(serviceStatistics, getExpectedServiceStatistics(), true);
    }

    @Test
    public void getServiceStatsReturnsCorrectDefinitionWithResponse() {
        SearchServiceClient serviceClient = getSearchServiceClientBuilder().buildClient();

        ServiceStatistics serviceStatistics = serviceClient.getServiceStatisticsWithResponse(generateRequestOptions(), Context.NONE).getValue();
        assertObjectEquals(serviceStatistics, getExpectedServiceStatistics(), true);
    }

    @Test
    public void getServiceStatsReturnsRequestId() {
        SearchServiceClient serviceClient = getSearchServiceClientBuilder().buildClient();

        RequestOptions requestOptions = new RequestOptions().setXMsClientRequestId(UUID.randomUUID());
        Response<ServiceStatistics> response = serviceClient.getServiceStatisticsWithResponse(requestOptions, Context.NONE);

        /*
         * The service will always return a request-id and will conditionally return client-request-id if
         * return-client-request-id is set to true. If client-request-id is sent in the request then request-id will
         * have the same value. This test validates that client-request-id is returned and that request-id is equal to
         * it.
         */
        String actualRequestId = response.getHeaders().getValue("request-id");
        String actualClientRequestId = response.getHeaders().getValue("client-request-id");

        Assertions.assertNotNull(actualClientRequestId);
        Assertions.assertEquals(actualClientRequestId, actualRequestId);
        assertObjectEquals(response.getValue(), getExpectedServiceStatistics(), true);
    }
}

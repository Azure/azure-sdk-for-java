// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.search.models.RequestOptions;
import com.azure.search.models.ServiceStatistics;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;
import static org.unitils.reflectionassert.ReflectionComparatorMode.IGNORE_DEFAULTS;

public class SearchServiceSyncTests extends SearchServiceTestBase {

    @Test
    public void getServiceStatsReturnsCorrectDefinition() {
        SearchServiceClient serviceClient = getSearchServiceClientBuilder().buildClient();

        ServiceStatistics serviceStatistics = serviceClient.getServiceStatistics();
        assertReflectionEquals(serviceStatistics, getExpectedServiceStatistics(), IGNORE_DEFAULTS);
    }

    @Test
    public void getServiceStatsReturnsCorrectDefinitionWithResponse() {
        SearchServiceClient serviceClient = getSearchServiceClientBuilder().buildClient();

        ServiceStatistics serviceStatistics = serviceClient.getServiceStatisticsWithResponse(generateRequestOptions(), Context.NONE).getValue();
        assertReflectionEquals(serviceStatistics, getExpectedServiceStatistics(), IGNORE_DEFAULTS);
    }

    @Test
    public void getServiceStatsReturnsRequestId() {
        SearchServiceClient serviceClient = getSearchServiceClientBuilder().buildClient();

        UUID expectedUuid = UUID.randomUUID();
        RequestOptions requestOptions = new RequestOptions().setClientRequestId(expectedUuid);
        Response<ServiceStatistics> response = serviceClient.getServiceStatisticsWithResponse(requestOptions, Context.NONE);

        assertEquals(expectedUuid.toString(), response.getHeaders().getValue("client-request-id"));
        assertReflectionEquals(response.getValue(), getExpectedServiceStatistics(), IGNORE_DEFAULTS);
    }
}

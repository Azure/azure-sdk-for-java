// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents.indexes;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.search.documents.SearchTestBase;
import com.azure.search.documents.indexes.models.SearchServiceCounters;
import com.azure.search.documents.indexes.models.SearchServiceStatistics;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SearchServiceSyncTests extends SearchTestBase {

    @Test
    public void getServiceStatsReturnsCorrectDefinition() {
        SearchIndexClient serviceClient = getSearchIndexClientBuilder().buildClient();

        validateServiceStatistics(serviceClient.getServiceStatistics());
    }

    @Test
    public void getServiceStatsReturnsCorrectDefinitionWithResponse() {
        SearchIndexClient serviceClient = getSearchIndexClientBuilder().buildClient();

        SearchServiceStatistics searchServiceStatistics = serviceClient.getServiceStatisticsWithResponse(Context.NONE)
            .getValue();
        validateServiceStatistics(searchServiceStatistics);
    }

    @Test
    public void getServiceStatsReturnsRequestId() {
        SearchIndexClient serviceClient = getSearchIndexClientBuilder().buildClient();

        Response<SearchServiceStatistics> response = serviceClient.getServiceStatisticsWithResponse(Context.NONE);

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
        validateServiceStatistics(response.getValue());
    }

    private static void validateServiceStatistics(SearchServiceStatistics searchServiceStatistics) {
        SearchServiceCounters searchServiceCounters = searchServiceStatistics.getCounters();
        assertTrue(searchServiceCounters.getIndexCounter().getQuota() >= 1);
        assertTrue(searchServiceCounters.getIndexerCounter().getQuota() >= 1);
        assertTrue(searchServiceCounters.getDataSourceCounter().getQuota() >= 1);
        assertTrue(searchServiceCounters.getStorageSizeCounter().getQuota() >= 1);
        assertTrue(searchServiceCounters.getSynonymMapCounter().getQuota() >= 1);
    }
}

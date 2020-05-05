// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static com.azure.search.documents.TestHelpers.assertObjectEquals;


public class SearchServiceAsyncTests extends SearchServiceTestBase {

    @Test
    public void getServiceStatsReturnsCorrectDefinition() {
        SearchServiceAsyncClient serviceClient = getSearchServiceClientBuilder().buildAsyncClient();

        StepVerifier.create(serviceClient.getServiceStatistics())
            .assertNext(serviceStatistics -> assertObjectEquals(getExpectedServiceStatistics(), serviceStatistics, true))
            .verifyComplete();
    }

    @Test
    public void getServiceStatsReturnsCorrectDefinitionWithResponse() {
        SearchServiceAsyncClient serviceClient = getSearchServiceClientBuilder().buildAsyncClient();

        StepVerifier.create(serviceClient.getServiceStatisticsWithResponse(generateRequestOptions()))
            .assertNext(serviceStatistics -> assertObjectEquals(getExpectedServiceStatistics(),
                serviceStatistics.getValue(), true))
            .verifyComplete();
    }
}

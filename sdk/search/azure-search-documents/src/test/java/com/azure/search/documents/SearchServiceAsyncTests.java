// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import static com.azure.search.documents.TestHelpers.DISPLAY_NAME_WITH_ARGUMENTS;
import static com.azure.search.documents.TestHelpers.assertObjectEquals;


public class SearchServiceAsyncTests extends SearchServiceTestBase {

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.search.documents.TestHelpers#getTestParameters")
    public void getServiceStatsReturnsCorrectDefinition() {
        SearchServiceAsyncClient serviceClient = getSearchServiceClientBuilder().buildAsyncClient();

        StepVerifier.create(serviceClient.getServiceStatistics())
            .assertNext(serviceStatistics -> assertObjectEquals(getExpectedServiceStatistics(), serviceStatistics, true))
            .verifyComplete();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.search.documents.TestHelpers#getTestParameters")
    public void getServiceStatsReturnsCorrectDefinitionWithResponse() {
        SearchServiceAsyncClient serviceClient = getSearchServiceClientBuilder().buildAsyncClient();

        StepVerifier.create(serviceClient.getServiceStatisticsWithResponse(generateRequestOptions()))
            .assertNext(serviceStatistics -> assertObjectEquals(getExpectedServiceStatistics(),
                serviceStatistics.getValue(), true))
            .verifyComplete();
    }
}

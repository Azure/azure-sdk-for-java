// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search;

import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static org.unitils.reflectionassert.ReflectionAssert.assertReflectionEquals;
import static org.unitils.reflectionassert.ReflectionComparatorMode.IGNORE_DEFAULTS;

public class SearchServiceAsyncTests extends SearchServiceTestBase {

    @Test
    public void getServiceStatsReturnsCorrectDefinition() {
        SearchServiceAsyncClient serviceClient = getSearchServiceClientBuilder().buildAsyncClient();

        StepVerifier.create(serviceClient.getServiceStatistics())
            .assertNext(serviceStatistics -> assertReflectionEquals(serviceStatistics, getExpectedServiceStatistics(), IGNORE_DEFAULTS))
            .verifyComplete();
    }

    @Test
    public void getServiceStatsReturnsCorrectDefinitionWithResponse() {
        SearchServiceAsyncClient serviceClient = getSearchServiceClientBuilder().buildAsyncClient();

        StepVerifier.create(serviceClient.getServiceStatisticsWithResponse(generateRequestOptions()))
            .assertNext(serviceStatistics -> assertReflectionEquals(serviceStatistics.getValue(),
                getExpectedServiceStatistics(), IGNORE_DEFAULTS))
            .verifyComplete();
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.search.documents;

import org.junit.jupiter.api.Test;
import org.unitils.reflectionassert.ReflectionAssert;
import reactor.test.StepVerifier;

import static org.unitils.reflectionassert.ReflectionComparatorMode.IGNORE_DEFAULTS;

public class SearchServiceAsyncTests extends SearchServiceTestBase {

    @Test
    public void getServiceStatsReturnsCorrectDefinition() {
        SearchServiceAsyncClient serviceClient = getSearchServiceClientBuilder().buildAsyncClient();

        StepVerifier.create(serviceClient.getServiceStatistics())
            .assertNext(serviceStatistics -> ReflectionAssert.assertReflectionEquals(serviceStatistics, getExpectedServiceStatistics(), IGNORE_DEFAULTS))
            .verifyComplete();
    }

    @Test
    public void getServiceStatsReturnsCorrectDefinitionWithResponse() {
        SearchServiceAsyncClient serviceClient = getSearchServiceClientBuilder().buildAsyncClient();

        StepVerifier.create(serviceClient.getServiceStatisticsWithResponse(generateRequestOptions()))
            .assertNext(serviceStatistics -> ReflectionAssert.assertReflectionEquals(serviceStatistics.getValue(),
                getExpectedServiceStatistics(), IGNORE_DEFAULTS))
            .verifyComplete();
    }
}

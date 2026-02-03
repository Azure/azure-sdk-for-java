// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.ConsistencyLevel;
import com.azure.cosmos.CosmosException;
import com.azure.cosmos.ReadConsistencyStrategy;
import com.azure.cosmos.implementation.HttpConstants;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.fail;

public class RequestHelperTest {

    @DataProvider(name = "validateGetReadConsistencyStrategyToUseParamsProvider")
    public Object[][] validateGetReadConsistencyStrategyToUseParamsProvider() {

        return new Object[][] {
            { null, null, null, ConsistencyLevel.SESSION, ReadConsistencyStrategy.SESSION, null, null, false },
            { null, null, null, ConsistencyLevel.BOUNDED_STALENESS, ReadConsistencyStrategy.LATEST_COMMITTED, null, null, false },
            { "Session", null, null, ConsistencyLevel.BOUNDED_STALENESS, ReadConsistencyStrategy.SESSION, "Session", null, false },
            { "Session", "Eventual", null, ConsistencyLevel.BOUNDED_STALENESS, ReadConsistencyStrategy.EVENTUAL, "Eventual", "Eventual", false },
            { "Session", "LatestCommitted", null, ConsistencyLevel.BOUNDED_STALENESS, ReadConsistencyStrategy.LATEST_COMMITTED, "BoundedStaleness", "LatestCommitted", false },
            { "Session", "LatestCommitted", ReadConsistencyStrategy.EVENTUAL, ConsistencyLevel.BOUNDED_STALENESS, ReadConsistencyStrategy.EVENTUAL, "Eventual", "Eventual", false },
            { "Session", "Eventual", ReadConsistencyStrategy.LATEST_COMMITTED, ConsistencyLevel.BOUNDED_STALENESS, ReadConsistencyStrategy.LATEST_COMMITTED, "BoundedStaleness", "LatestCommitted", false },
            { "Session", null, ReadConsistencyStrategy.EVENTUAL, ConsistencyLevel.BOUNDED_STALENESS, ReadConsistencyStrategy.EVENTUAL, "Eventual", "Eventual", false },
            { "Session", null, null, ConsistencyLevel.BOUNDED_STALENESS, ReadConsistencyStrategy.SESSION, "Session", null, false },
            { "InvalidConsistencyLevel", null, null, null, null, null, null, true },
            { null, "InvalidReadConsistencyStrategy", null, null, null, null, null, true },
            { null, "InvalidReadConsistencyStrategy", ReadConsistencyStrategy.SESSION, ConsistencyLevel.BOUNDED_STALENESS, ReadConsistencyStrategy.SESSION, "Session", "Session", false },
            { "InvalidConsistencyLevel", "InvalidReadConsistencyStrategy", null, null, null, null, null, true },
            { null, "LatestCommitted", ReadConsistencyStrategy.EVENTUAL, ConsistencyLevel.BOUNDED_STALENESS, ReadConsistencyStrategy.EVENTUAL, "Eventual", "Eventual", false },
            { null, "LatestCommitted", ReadConsistencyStrategy.DEFAULT, ConsistencyLevel.BOUNDED_STALENESS, ReadConsistencyStrategy.LATEST_COMMITTED, "BoundedStaleness", "LatestCommitted", false },
            { null, "GlobalStrong", ReadConsistencyStrategy.DEFAULT, ConsistencyLevel.BOUNDED_STALENESS, null, null, null, true },
            { null, "GlobalStrong", ReadConsistencyStrategy.DEFAULT, ConsistencyLevel.STRONG, ReadConsistencyStrategy.GLOBAL_STRONG, "Strong", "GlobalStrong", false },
            { null, "LatestCommitted", ReadConsistencyStrategy.DEFAULT, ConsistencyLevel.STRONG, ReadConsistencyStrategy.LATEST_COMMITTED, "BoundedStaleness", "LatestCommitted", false },
        };
    }
    @Test(groups = "unit", dataProvider = "validateGetReadConsistencyStrategyToUseParamsProvider")
    public void validateGetReadConsistencyStrategyToUse(
        String inputRequestHeaderConsistencyLevel,
        String inputRequestHeaderReadConsistencyStrategy,
        ReadConsistencyStrategy requestOrClientLevelReadConsistencyStrategy,
        ConsistencyLevel defaultConsistencySnapshot,
        ReadConsistencyStrategy expectedReadConsistencyStrategy,
        String expectedRequestHeaderConsistencyLevel,
        String expectedRequestHeaderReadConsistencyStrategy,
        boolean expectsError) {

        Map<String, String> headers = new HashMap<>();

        if (inputRequestHeaderConsistencyLevel != null) {
            headers.put(
                HttpConstants.HttpHeaders.CONSISTENCY_LEVEL,
                inputRequestHeaderConsistencyLevel);
        }

        if (inputRequestHeaderReadConsistencyStrategy != null) {
            headers.put(
                HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY,
                inputRequestHeaderReadConsistencyStrategy);
        }

        try {
            ReadConsistencyStrategy actualReadConsistencyStrategy = RequestHelper.getReadConsistencyStrategyToUse(
                headers,
                requestOrClientLevelReadConsistencyStrategy,
                defaultConsistencySnapshot);

            if (expectsError) {
                fail("Expected the invocation of getReadConsistencyStrategyToUse to fail.");
            }

            assertThat(actualReadConsistencyStrategy).isEqualTo(expectedReadConsistencyStrategy);
            if (expectedRequestHeaderConsistencyLevel != null) {
                assertThat(headers.get(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL))
                    .isEqualTo(expectedRequestHeaderConsistencyLevel);
            } else {
                assertThat(headers.containsKey(HttpConstants.HttpHeaders.CONSISTENCY_LEVEL)).isFalse();
            }

            if (expectedRequestHeaderReadConsistencyStrategy != null) {
                assertThat(headers.get(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY))
                    .isEqualTo(expectedRequestHeaderReadConsistencyStrategy);
            } else {
                assertThat(headers.containsKey(HttpConstants.HttpHeaders.READ_CONSISTENCY_STRATEGY)).isFalse();
            }
        } catch (CosmosException cosmosError) {
            if (cosmosError.getStatusCode() != 400) {
                throw cosmosError;
            }

            assertThat(expectsError).isTrue();
        }
    }
}

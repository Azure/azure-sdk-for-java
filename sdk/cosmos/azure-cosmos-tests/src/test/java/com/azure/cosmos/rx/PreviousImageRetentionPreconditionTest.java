// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.rx;

import com.azure.cosmos.implementation.DocumentCollection;
import com.azure.cosmos.models.CosmosChangeFeedPreviousImageRetentionMode;
import com.azure.cosmos.models.CosmosContainerProperties;
import com.azure.cosmos.models.ModelBridgeInternal;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class PreviousImageRetentionPreconditionTest {
    @Test(groups = "unit")
    public void acceptsModeOnlyPolicy() throws JsonProcessingException {
        CosmosContainerProperties properties = new CosmosContainerProperties("container", "/pk")
            .setChangeFeedPreviousImageRetentionMode(
                CosmosChangeFeedPreviousImageRetentionMode.ENABLED_FOR_ALL_OPERATIONS);

        PreviousImageRetentionTest.assertIncludedPathsOmitted(properties);
    }

    @DataProvider
    public Object[][] explicitIncludedPaths() {
        return new Object[][] {
            { "[\"/version\"]" },
            { "[]" },
            { "null" }
        };
    }

    @Test(groups = "unit", dataProvider = "explicitIncludedPaths")
    public void rejectsExplicitFeaturePaths(String includedPaths) {
        CosmosContainerProperties properties = fromPolicy("{\"supportedFeatures\":{"
            + "\"allVersionsAndDeletes\":{\"mode\":3,\"includedPaths\":" + includedPaths + "}}}");

        assertThatThrownBy(() -> PreviousImageRetentionTest.assertIncludedPathsOmitted(properties))
            .isInstanceOf(AssertionError.class)
            .hasMessageContaining("Omitting includedPaths requests full previous documents");
    }

    @Test(groups = "unit")
    public void ignoresPathsOutsideAllVersionsAndDeletes() throws JsonProcessingException {
        CosmosContainerProperties properties = fromPolicy("{\"includedPaths\":[\"/policyMetadata\"],"
            + "\"supportedFeatures\":{\"allVersionsAndDeletes\":{\"mode\":3},"
            + "\"globalSecondaryIndex\":{\"mode\":1,\"includedPaths\":[\"/id\"]}}}");

        PreviousImageRetentionTest.assertIncludedPathsOmitted(properties);
    }

    private static CosmosContainerProperties fromPolicy(String policy) {
        DocumentCollection collection = new DocumentCollection("{\"id\":\"container\","
            + "\"partitionKey\":{\"paths\":[\"/pk\"],\"kind\":\"Hash\"},"
            + "\"previousImageRetentionPolicy\":" + policy + "}");
        return ModelBridgeInternal.getCosmosContainerPropertiesFromV2Results(Collections.singletonList(collection)).get(0);
    }
}

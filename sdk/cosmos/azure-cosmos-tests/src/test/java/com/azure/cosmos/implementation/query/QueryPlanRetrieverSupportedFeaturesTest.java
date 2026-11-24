// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.implementation.query;

import org.testng.annotations.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

public class QueryPlanRetrieverSupportedFeaturesTest {

    @Test(groups = {"unit"})
    public void supportedFeaturesExcludeUnsupportedAggregates() throws Exception {
        String supportedFeatures = getQueryPlanRetrieverString("SUPPORTED_QUERY_FEATURES");

        assertThat(supportedFeatures)
            .doesNotContain(QueryFeature.CountIf.name())
            .doesNotContain("HybridSearchSkipOrderByRewrite")
            .doesNotContain("ListAndSetAggregate");
    }

    private static String getQueryPlanRetrieverString(String fieldName) throws Exception {
        Class<?> queryPlanRetrieverClass =
            Class.forName("com.azure.cosmos.implementation.query.QueryPlanRetriever");
        Field field = queryPlanRetrieverClass.getDeclaredField(fieldName);
        field.setAccessible(true);
        return (String) field.get(null);
    }
}

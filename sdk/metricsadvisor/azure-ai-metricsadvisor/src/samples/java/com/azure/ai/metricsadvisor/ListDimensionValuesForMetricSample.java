// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.ListMetricDimensionValuesOptions;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;
import com.azure.core.util.Context;

/**
 * Sample for listing dimension values for a give metric filtered using specific set of dimension combinations.
 */
public class ListDimensionValuesForMetricSample {
    public static void main(String[] args) {
        final MetricsAdvisorClient advisorClient =
            new MetricsAdvisorClientBuilder()
                .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
                .credential(new MetricsAdvisorKeyCredential("subscription_key", "api_key"))
                .buildClient();

        final String metricId = "2dgfbbbb-41ec-a637-677e77b81455";

        advisorClient.listMetricDimensionValues(metricId, "category",
            new ListMetricDimensionValuesOptions()
                .setMaxPageSize(10)
                .setDimensionValueToFilter("Electronics"), Context.NONE)
            .forEach(System.out::println);
    }
}

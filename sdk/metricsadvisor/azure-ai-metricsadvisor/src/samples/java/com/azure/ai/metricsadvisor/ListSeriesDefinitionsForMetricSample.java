// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.ListMetricSeriesDefinitionOptions;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;
import com.azure.core.util.Context;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;


/**
 * Sample for listing series definition for a metric filtered using specific set of dimension combinations.
 */
public class ListSeriesDefinitionsForMetricSample {
    public static void main(String[] args) {
        final MetricsAdvisorClient advisorClient =
            new MetricsAdvisorClientBuilder()
                .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
                .credential(new MetricsAdvisorKeyCredential("subscription_key", "api_key"))
                .buildClient();

        String metricId = "b460abfc-7a58-47d7-9d99-21ee21fdfc6e";
        final OffsetDateTime activeSince = OffsetDateTime.parse("2020-07-10T00:00:00Z");
        final ListMetricSeriesDefinitionOptions options
            = new ListMetricSeriesDefinitionOptions()
            .setMaxPageSize(10)
            .setDimensionCombinationToFilter(new HashMap<String, List<String>>() {{
                    put("city", Collections.singletonList("Redmond"));
                }});

        advisorClient.listMetricSeriesDefinitions(metricId, activeSince, options, Context.NONE)
            .forEach(metricSeriesDefinition -> {
                System.out.printf("Data Feed Metric Id : %s%n", metricSeriesDefinition.getMetricId());
                System.out.printf("Series Key: %s%n", metricSeriesDefinition.getSeriesKey().asMap());
            });
    }
}

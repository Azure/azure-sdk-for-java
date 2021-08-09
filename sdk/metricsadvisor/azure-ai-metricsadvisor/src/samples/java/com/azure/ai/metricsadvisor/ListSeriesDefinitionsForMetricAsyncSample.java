// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.ListMetricSeriesDefinitionOptions;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Async sample for listing series definition for a metric filtered using specific set of dimension combinations.
 */
public class ListSeriesDefinitionsForMetricAsyncSample {
    public static void main(String[] args) {
        final MetricsAdvisorAsyncClient advisorAsyncClient =
            new MetricsAdvisorClientBuilder()
                .endpoint("https://{endpoint}.cognitiveservices.azure.com/")
                .credential(new MetricsAdvisorKeyCredential("subscription_key", "api_key"))
                .buildAsyncClient();

        final String metricId = "b460abfc-7a58-47d7-9d99-21ee21fdfc6e";
        final OffsetDateTime activeSince = OffsetDateTime.parse("2020-07-10T00:00:00Z");

        final ListMetricSeriesDefinitionOptions options
            = new ListMetricSeriesDefinitionOptions()
            .setMaxPageSize(10)
            .setDimensionCombinationToFilter(new HashMap<String, List<String>>() {{
                    put("city", Collections.singletonList("Redmond"));
                }});

        advisorAsyncClient.listMetricSeriesDefinitions(metricId, activeSince, options)
            .doOnNext(metricSeriesDefinition -> {
                System.out.printf("Data Feed Metric Id : %s%n", metricSeriesDefinition.getMetricId());
                System.out.printf("Series Key: %s%n", metricSeriesDefinition.getSeriesKey().asMap());
            }).blockLast();
            /*
              'blockLast()' will block until all the above CRUD on operation on detection is completed.
              This is strongly discouraged for use in production as it eliminates the benefits of
              asynchronous IO. It is used here to ensure the sample runs to completion.
             */
    }
}

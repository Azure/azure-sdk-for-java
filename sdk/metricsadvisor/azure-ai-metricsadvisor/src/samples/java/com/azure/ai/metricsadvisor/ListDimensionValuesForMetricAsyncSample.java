// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor;

import com.azure.ai.metricsadvisor.models.ListMetricDimensionValuesOptions;
import com.azure.ai.metricsadvisor.models.MetricsAdvisorKeyCredential;
import com.azure.core.util.Context;

/**
 * Async sample for listing dimension values for a give metric filtered using specific set of dimension combinations.
 */
public class ListDimensionValuesForMetricAsyncSample {
    public static void main(String[] args) {
        final MetricsAdvisorAsyncClient advisorAsyncClient = new MetricsAdvisorClientBuilder()
            .credential(new MetricsAdvisorKeyCredential("subscription_key", "api_key"))
            .endpoint("{endpoint}")
            .buildAsyncClient();

        final String metricId = "2dgfbbbb-41ec-a637-677e77b81455";

        advisorAsyncClient.listMetricDimensionValues(metricId, "category",
            new ListMetricDimensionValuesOptions()
                .setMaxPageSize(10)
                .setDimensionValueToFilter("Electronics"), Context.NONE)
            .doOnNext(System.out::println)
            .blockLast();
        /*
          'blockLast()' will block until all the above CRUD on operation on detection is completed.
          This is strongly discouraged for use in production as it eliminates the benefits of
          asynchronous IO. It is used here to ensure the sample runs to completion.
         */
    }
}

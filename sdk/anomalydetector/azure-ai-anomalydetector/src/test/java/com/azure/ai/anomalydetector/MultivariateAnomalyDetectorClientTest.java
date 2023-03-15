// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.anomalydetector;

import com.azure.ai.anomalydetector.models.AnomalyDetectionModel;
import com.azure.core.http.rest.PagedIterable;
import org.junit.jupiter.api.Test;


public class MultivariateAnomalyDetectorClientTest extends AnomalyDetectorClientTestBase {
    private AnomalyDetectorClient getClient() {
        return getClientBuilder().buildClient();
    }

    private static void getModelList(AnomalyDetectorClient client, Integer skip, Integer top) {
        PagedIterable<AnomalyDetectionModel> response = client.listMultivariateModels(skip, top);

        System.out.println("ModelList: ");
        response.streamByPage().forEach(models -> {
            for (AnomalyDetectionModel item : models.getValue()) {
                System.out.println("\t" + item.getModelId());
            }
        });
    }

    @Test
    public void testDetect() {
        testDetectEntireSeriesWithResponse(request -> {
            AnomalyDetectorClient client = getClient();

            Integer skip = 0;
            Integer top = 5;
            getModelList(client, skip, top);

        });

    }
}

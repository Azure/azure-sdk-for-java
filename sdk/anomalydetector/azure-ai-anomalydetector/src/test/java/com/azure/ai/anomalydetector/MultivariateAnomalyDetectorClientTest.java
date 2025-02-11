// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.anomalydetector;

import com.azure.ai.anomalydetector.models.AnomalyDetectionModel;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import org.junit.jupiter.api.Test;

public class MultivariateAnomalyDetectorClientTest extends AnomalyDetectorClientTestBase {
    private static final ClientLogger LOGGER = new ClientLogger(MultivariateAnomalyDetectorClientTest.class);

    private MultivariateClient getClient() {
        return getClientBuilder().buildMultivariateClient();
    }

    private static void getModelList(MultivariateClient client, Integer skip, Integer top) {
        PagedIterable<AnomalyDetectionModel> response = client.listMultivariateModels(skip, top);

        LOGGER.info("ModelList: ");
        response.forEach(model -> LOGGER.log(LogLevel.INFORMATIONAL, () -> "\t" + model.getModelId()));
    }

    @Test
    public void testDetect() {
        testDetectEntireSeriesWithResponse(request -> {
            MultivariateClient client = getClient();

            Integer skip = 0;
            Integer top = 5;
            getModelList(client, skip, top);
        });

    }
}

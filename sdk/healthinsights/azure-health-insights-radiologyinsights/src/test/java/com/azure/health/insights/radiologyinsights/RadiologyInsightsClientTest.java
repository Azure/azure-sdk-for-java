// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.health.insights.radiologyinsights;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.azure.core.util.serializer.TypeReference;
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsInferenceResult;
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsPatientResult;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for {@link RadiologyInsightsClient}.
 */
public class RadiologyInsightsClientTest extends RadiologyInsightsClientTestBase {

    private RadiologyInsightsClient getClient() {
        return getClientBuilder().buildClient();
    }

    @Test
    public void test() {
        try {
            testRadiologyInsightsgWithResponse(request -> {

                BinaryData responseValue = setPlaybackSyncPollerPollInterval(
                        getClient().beginInferRadiologyInsights(request, new RequestOptions())).waitForCompletion().getValue();
                RadiologyInsightsInferenceResult riResponse = responseValue
                        .toObject(TypeReference.createInstance(RadiologyInsightsInferenceResult.class));
                List<RadiologyInsightsPatientResult> patients = riResponse.getPatientResults();
                assertEquals(1, patients.size());
                RadiologyInsightsPatientResult patient = patients.get(0);
                assertTrue(patient.getInferences().size() > 0, "at least one inference should be returned");
            });

        } catch (Throwable t) {
            String message = t.toString() + "\n" + Arrays.toString(t.getStackTrace());
            t.printStackTrace();
            Assertions.fail(message);
            return;
        }
    }
}

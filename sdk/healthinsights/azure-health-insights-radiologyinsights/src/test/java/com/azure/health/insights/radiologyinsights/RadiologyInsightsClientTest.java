// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.health.insights.radiologyinsights;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsInferenceResult;
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsJob;
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsPatientResult;

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

                RadiologyInsightsJob riJobResponse = setPlaybackSyncPollerPollInterval(
                        getClient().beginInferRadiologyInsights("job1",request)).getFinalResult();

                List<RadiologyInsightsPatientResult> patients = riJobResponse.getResult().getPatientResults();
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

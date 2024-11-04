// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.health.insights.radiologyinsights;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsInferenceResult;
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsInferenceType;
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsPatientResult;

/**
 * Unit tests for {@link RadiologyInsightsClient}.
 */
public class RadiologyInsightsClientTest extends RadiologyInsightsClientTestBase {

    private RadiologyInsightsClient getClient() {
        return getClientBuilder().buildClient();
    }

    @Test
    public void test() {
        setDocumentContent("Findings: There is a total hip prosthesis.");
        setInferenceType(RadiologyInsightsInferenceType.LATERALITY_DISCREPANCY);
        setOrderCode("MVLW");
        setOrderDescription("IH Hip 1 View Left");
        try {
            testRadiologyInsightsWithResponse(request -> {

                RadiologyInsightsInferenceResult riResponse = setPlaybackSyncPollerPollInterval(
                    getClient().beginInferRadiologyInsights("job1715007505099", request)).getFinalResult();

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

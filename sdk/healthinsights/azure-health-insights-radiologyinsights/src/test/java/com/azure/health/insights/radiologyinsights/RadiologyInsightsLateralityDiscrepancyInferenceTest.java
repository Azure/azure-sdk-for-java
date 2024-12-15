// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.health.insights.radiologyinsights;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.azure.health.insights.radiologyinsights.models.FhirR4Extension;
import com.azure.health.insights.radiologyinsights.models.LateralityDiscrepancyInference;
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsInference;
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsInferenceResult;
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsInferenceType;
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsPatientResult;

/**
 * Unit tests for {@link RadiologyInsightsClient}.
 */
public class RadiologyInsightsLateralityDiscrepancyInferenceTest extends RadiologyInsightsClientTestBase {

    private RadiologyInsightsClient getClient() {
        return getClientBuilder().buildClient();
    }

    @Test
    public void test() {
        String documentContent = "Exam:   US LT BREAST TARGETED"
            + "\r\nTechnique:  Targeted imaging of the  right breast  is performed."
            + "\r\nFindings: Targeted imaging of the left breast is performed from the 6:00 to the 9:00 position.  "
            + "\r\nAt the 6:00 position, 5 cm from the nipple, there is a 3 x 2 x 4 mm minimally hypoechoic mass with a peripheral calcification. "
            + "This may correspond to the mammographic finding. No other cystic or solid masses visualized." + "\r\n";
        setDocumentContent(documentContent);
        setInferenceType(RadiologyInsightsInferenceType.LATERALITY_DISCREPANCY);
        setOrderCode("26688-1");
        setOrderDescription("US BREAST - LEFT LIMITED");

        try {
            testRadiologyInsightsWithResponse(request -> {
                RadiologyInsightsInferenceResult riResponse = setPlaybackSyncPollerPollInterval(
                    getClient().beginInferRadiologyInsights("job1715007617997", request)).getFinalResult();

                List<RadiologyInsightsPatientResult> patients = riResponse.getPatientResults();
                assertEquals(1, patients.size());

                RadiologyInsightsPatientResult patient = patients.get(0);
                List<RadiologyInsightsInference> inferences = patient.getInferences();
                assertEquals(1, inferences.size());

                RadiologyInsightsInference inference = inferences.get(0);
                assertTrue(inference instanceof LateralityDiscrepancyInference,
                    "Inference should be an instance of LateralityDiscrepancyInference");

                LateralityDiscrepancyInference lateralityDiscrepancyInference
                    = (LateralityDiscrepancyInference) inference;
                List<FhirR4Extension> extensions = lateralityDiscrepancyInference.getExtension();

            });

        } catch (Throwable t) {
            String message = t.toString() + "\n" + Arrays.toString(t.getStackTrace());
            t.printStackTrace();
            Assertions.fail(message);
            return;
        }
    }

}

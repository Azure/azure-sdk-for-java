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
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsInference;
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsInferenceResult;
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsInferenceType;
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsPatientResult;
import com.azure.health.insights.radiologyinsights.models.RadiologyProcedureInference;

/**
 * Unit tests for {@link RadiologyInsightsClient}.
 */
public class RadiologyInsightsRadiologyProcedureTest extends RadiologyInsightsClientTestBase {

    private RadiologyInsightsClient getClient() {
        return getClientBuilder().buildClient();
    }

    @Test
    public void test() {
        String documentContent = "CLINICAL HISTORY:   "
            + "\r\n20-year-old female presenting with abdominal pain. Surgical history significant for appendectomy."
            + "\r\n " + "\r\nCOMPARISON:   " + "\r\nRight upper quadrant sonographic performed 1 day prior." + "\r\n "
            + "\r\nTECHNIQUE:   " + "\r\nTransabdominal grayscale pelvic sonography with duplex color Doppler "
            + "\r\nand spectral waveform analysis of the ovaries." + "\r\n " + "\r\nFINDINGS:   "
            + "\r\nThe uterus is unremarkable given the transabdominal technique with "
            + "\r\nendometrial echo complex within physiologic normal limits. The "
            + "\r\novaries are symmetric in size, measuring 2.5 x 1.2 x 3.0 cm and the "
            + "\r\nleft measuring 2.8 x 1.5 x 1.9 cm.\n \r\nOn duplex imaging, Doppler signal is symmetric." + "\r\n "
            + "\r\nIMPRESSION:   " + "\r\n1. Normal pelvic sonography. Findings of testicular torsion."
            + "\r\n\nA new US pelvis within the next 6 months is recommended."
            + "\n\nThese results have been discussed with Dr. Jones at 3 PM on November 5 2020.\n " + "\r\n";
        setDocumentContent(documentContent);
        setInferenceType(RadiologyInsightsInferenceType.RADIOLOGY_PROCEDURE);
        setOrderCode("USPELVIS");
        setOrderDescription("US PELVIS COMPLETE");

        try {
            testRadiologyInsightsWithResponse(request -> {
                RadiologyInsightsInferenceResult riResponse = setPlaybackSyncPollerPollInterval(
                    getClient().beginInferRadiologyInsights("job1715007662332", request)).getFinalResult();

                List<RadiologyInsightsPatientResult> patients = riResponse.getPatientResults();
                assertEquals(1, patients.size());

                RadiologyInsightsPatientResult patient = patients.get(0);
                List<RadiologyInsightsInference> inferences = patient.getInferences();
                assertEquals(1, inferences.size());

                RadiologyInsightsInference inference = inferences.get(0);
                assertTrue(inference instanceof RadiologyProcedureInference,
                    "Inference should be an instance of RadiologyProcedureInference");

                RadiologyProcedureInference radiologyProcedureInference = (RadiologyProcedureInference) inference;
                List<FhirR4Extension> extensions = radiologyProcedureInference.getExtension();

            });

        } catch (Throwable t) {
            String message = t.toString() + "\n" + Arrays.toString(t.getStackTrace());
            t.printStackTrace();
            Assertions.fail(message);
            return;
        }
    }

}

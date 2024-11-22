// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.health.insights.radiologyinsights;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import com.azure.health.insights.radiologyinsights.models.CompleteOrderDiscrepancyInference;
import com.azure.health.insights.radiologyinsights.models.FhirR4CodeableConcept;
import com.azure.health.insights.radiologyinsights.models.FhirR4Coding;
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsInference;
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsInferenceResult;
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsInferenceType;
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsPatientResult;

/**
 * Unit tests for {@link RadiologyInsightsClient}.
 */
public class RadiologyInsightsCompleteOrderDiscrepancyTest extends RadiologyInsightsClientTestBase {

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
        setInferenceType(RadiologyInsightsInferenceType.COMPLETE_ORDER_DISCREPANCY);
        setOrderCode("USPELVIS");
        setOrderDescription("US PELVIS COMPLETE");

        try {
            testRadiologyInsightsWithResponse(request -> {
                RadiologyInsightsInferenceResult riResponse = setPlaybackSyncPollerPollInterval(
                    getClient().beginInferRadiologyInsights("job1715007526876", request)).getFinalResult();

                List<RadiologyInsightsPatientResult> patients = riResponse.getPatientResults();
                assertEquals(1, patients.size());
                RadiologyInsightsPatientResult patient = patients.get(0);

                List<RadiologyInsightsInference> inferences = patient.getInferences();
                assertEquals(1, inferences.size());
                RadiologyInsightsInference inference = inferences.get(0);

                if (inference instanceof CompleteOrderDiscrepancyInference) {
                    CompleteOrderDiscrepancyInference completeOrderDiscrepancyInference
                        = (CompleteOrderDiscrepancyInference) inference;
                    FhirR4CodeableConcept orderType = completeOrderDiscrepancyInference.getOrderType();
                    Set<String> orderTypeCodes = getCodeStrings(orderType);
                    Set<String> expectedOrderTypeCodes = new HashSet<>();
                    expectedOrderTypeCodes.add("Coding: 24869-0, US Pelvis (http://loinc.org)");
                    assertEquals(expectedOrderTypeCodes, orderTypeCodes);

                    List<FhirR4Coding> codingList = orderType.getCoding();
                    assertNotNull(codingList);
                    assertEquals(1, codingList.size());

                    FhirR4Coding fhirR4Coding = codingList.get(0);
                    assertFhirR4Coding(fhirR4Coding, "24869-0", "US Pelvis", "http://loinc.org");

                    List<FhirR4CodeableConcept> missingBodyParts
                        = completeOrderDiscrepancyInference.getMissingBodyParts();
                    assertEquals(0, missingBodyParts.size());

                    List<FhirR4CodeableConcept> missingBodyPartMeasurements
                        = completeOrderDiscrepancyInference.getMissingBodyPartMeasurements();
                    //                    System.out.println("   Missing body part measurements:");
                    //                    for (FhirR4CodeableConcept missingBodyPartMeasurement : missingBodyPartMeasurements) {
                    //                        System.out.println(getCodeStrings(missingBodyPartMeasurement));
                    //                    }
                    assertEquals(2, missingBodyPartMeasurements.size());
                }
            });

        } catch (Throwable t) {
            String message = t.toString() + "\n" + Arrays.toString(t.getStackTrace());
            t.printStackTrace();
            Assertions.fail(message);
            return;
        }
    }

    private void assertFhirR4Coding(FhirR4Coding fhirR4Coding, String code, String display, String system) {
        assertEquals(code, fhirR4Coding.getCode());
        assertEquals(display, fhirR4Coding.getDisplay());
        assertEquals(system, fhirR4Coding.getSystem());
    }

}

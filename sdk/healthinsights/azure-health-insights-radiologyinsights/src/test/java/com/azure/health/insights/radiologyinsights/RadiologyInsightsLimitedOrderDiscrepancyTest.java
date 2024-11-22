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
import com.azure.health.insights.radiologyinsights.models.LimitedOrderDiscrepancyInference;
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsInference;
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsInferenceResult;
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsInferenceType;
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsPatientResult;

/**
 * Unit tests for {@link RadiologyInsightsClient}.
 */
public class RadiologyInsightsLimitedOrderDiscrepancyTest extends RadiologyInsightsClientTestBase {

    private RadiologyInsightsClient getClient() {
        return getClientBuilder().buildClient();
    }

    @Test
    public void test() {
        String documentContent
            = "\\nHISTORY: male with a history of tuberous sclerosis presenting with epigastric pain and diffuse tenderness. "
                + "The patient was found to have pericholecystic haziness on CT; evaluation for acute cholecystitis."
                + "\\n\\nTECHNIQUE: Ultrasound evaluation of the abdomen was performed. " + "\\n\\nFINDINGS:"
                + "\\n\\nThe liver is elongated, measuring 19.3 cm craniocaudally, and is homogeneous in echotexture without evidence of focal mass lesion. "
                + "The liver contour is smooth on high resolution images. "
                + "There is no appreciable intra- or extrahepatic biliary ductal dilatation, with the visualized extrahepatic bile duct measuring up to 6 mm. "
                + "There are multiple shadowing gallstones, including within the gallbladder neck, which do not appear particularly mobile. "
                + "In addition, there is thickening of the gallbladder wall up to approximately 7 mm with probable mild mural edema. "
                + "There is no pericholecystic fluid. No sonographic Murphy's sign was elicited; however the patient reportedly received pain medications in the emergency department. "
                + "\\n\\nThe pancreatic head, body and visualized portions of the tail are unremarkable. The spleen is normal in size, measuring 9.9 cm in length."
                + "\\n\\nThe kidneys are normal in size. The right kidney measures 11.5 x 5.2 x 4.3 cm and the left kidney measuring 11.8 x 5.3 x 5.1 cm. "
                + "There are again multiple bilateral echogenic renal masses consistent with angiomyolipomas, in keeping with the patient's history of tuberous sclerosis. "
                + "The largest echogenic mass on the right is located in the upper pole and measures 1.2 x 1.3 x 1.3 cm. "
                + "The largest echogenic mass on the left is located within the renal sinus and measures approximately 2.6 x 2.7 x 4.6 cm. "
                + "Additional indeterminate renal lesions are present bilaterally and are better characterized on CT. There is no hydronephrosis."
                + "\\n\\nNo ascites is identified within the upper abdomen.\\n\\nThe visualized portions of the upper abdominal aorta and IVC are normal in caliber.";
        setDocumentContent(documentContent);
        setInferenceType(RadiologyInsightsInferenceType.LIMITED_ORDER_DISCREPANCY);
        setOrderCode("30704-1");
        setOrderDescription("US ABDOMEN LIMITED");

        try {
            testRadiologyInsightsWithResponse(request -> {
                RadiologyInsightsInferenceResult riResponse = setPlaybackSyncPollerPollInterval(
                    getClient().beginInferRadiologyInsights("job1715007637970", request)).getFinalResult();

                List<RadiologyInsightsPatientResult> patients = riResponse.getPatientResults();
                assertEquals(1, patients.size());

                RadiologyInsightsPatientResult patient = patients.get(0);
                List<RadiologyInsightsInference> inferences = patient.getInferences();
                assertEquals(1, inferences.size());

                RadiologyInsightsInference inference = inferences.get(0);
                assertTrue(inference instanceof LimitedOrderDiscrepancyInference,
                    "Inference should be an instance of LimitedOrderDiscrepancyInference");

                LimitedOrderDiscrepancyInference limitedOrderDiscrepancyInference
                    = (LimitedOrderDiscrepancyInference) inference;
                List<FhirR4Extension> extensions = limitedOrderDiscrepancyInference.getExtension();

            });

        } catch (Throwable t) {
            String message = t.toString() + "\n" + Arrays.toString(t.getStackTrace());
            t.printStackTrace();
            Assertions.fail(message);
            return;
        }
    }

}

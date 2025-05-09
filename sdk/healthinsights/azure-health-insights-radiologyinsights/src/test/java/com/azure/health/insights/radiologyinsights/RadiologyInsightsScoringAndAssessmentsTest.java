// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.health.insights.radiologyinsights;

import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsInference;
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsInferenceResult;
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsInferenceType;
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsPatientResult;
import com.azure.health.insights.radiologyinsights.models.ScoringAndAssessmentCategoryType;
import com.azure.health.insights.radiologyinsights.models.ScoringAndAssessmentInference;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

public class RadiologyInsightsScoringAndAssessmentsTest extends RadiologyInsightsClientTestBase {

    private RadiologyInsightsClient getClient() {
        return getClientBuilder().buildClient();
    }

    @Test
    public void test() {
        String documentContent = "Exam: US THYROID\r\n" + "\r\n"
            + "Clinical History: Thyroid nodules. 76 year old patient.\r\n" + "\r\n" + "Comparison: none.\r\n" + "\r\n"
            + "FINDINGS: \r\n" + "\r\n" + "Right lobe: 4.8 x 1.6 x 1.4 cm\r\n" + "\r\n"
            + "Left Lobe: 4.1 x 1.3 x 1.3 cm\r\n" + "\r\n" + "Isthmus: 4 mm\r\n" + "\r\n"
            + "There are multiple cystic and partly cystic sub-5 mm nodules noted within the right lobe (TIRADS 2).\r\n"
            + "\r\n"
            + "In the lower pole of the left lobe there is a 9 x 8 x 6 mm predominantly solid isoechoic nodule (TIRADS 3).\r\n"
            + "Impression:\r\n" + "\r\n"
            + "Multiple bilateral small cystic benign thyroid nodules. A low suspicion 9 mm left lobe thyroid nodule (TI-RADS 3) which, given its small size, does not warrant follow-up.";
        setDocumentContent(documentContent);
        setInferenceType(RadiologyInsightsInferenceType.SCORING_AND_ASSESSMENT);
        setOrderCode("USTHY");
        setOrderDescription("US THYROID");

        try {
            testRadiologyInsightsWithResponse(request -> {
                RadiologyInsightsInferenceResult riResponse = setPlaybackSyncPollerPollInterval(
                    getClient().beginInferRadiologyInsights("job1715007451741", request)).getFinalResult();

                List<RadiologyInsightsPatientResult> patients = riResponse.getPatientResults();
                assertEquals(1, patients.size());

                RadiologyInsightsPatientResult patient = patients.get(0);
                List<RadiologyInsightsInference> inferences = patient.getInferences();
                assertEquals(3, inferences.size());

                RadiologyInsightsInference inference = inferences.get(0);
                assertInstanceOf(ScoringAndAssessmentInference.class, inference,
                    "Inference should be an instance of ScoringAndAssessmentInference");

                ScoringAndAssessmentInference scoringAndAssessmentInference = (ScoringAndAssessmentInference) inference;
                ScoringAndAssessmentCategoryType scoringAndAssessmentCategoryType
                    = scoringAndAssessmentInference.getCategory();
                assertEquals("TI-RADS", scoringAndAssessmentCategoryType.getValue(),
                    "Expected ScoringAndAssessmentCategoryType is TI-RADS");

                String scoringAndAssessmentCategoryDescription = scoringAndAssessmentInference.getCategoryDescription();
                assertEquals("ACR THYROID IMAGING REPORTING AND DATA SYSTEM", scoringAndAssessmentCategoryDescription,
                    "Expected ScoringAndAssessmentCategoryDescription is ACR THYROID IMAGING REPORTING AND DATA SYSTEM");

                String scoringAndAssessmentSingleValue = scoringAndAssessmentInference.getSingleValue();
                assertEquals("2", scoringAndAssessmentSingleValue,
                    "Expected ScoringAndAssessmentCategoryDescription is 2");
            });

        } catch (Throwable t) {
            String message = t.toString() + "\n" + Arrays.toString(t.getStackTrace());
            t.printStackTrace();
            Assertions.fail(message);
        }
    }

}

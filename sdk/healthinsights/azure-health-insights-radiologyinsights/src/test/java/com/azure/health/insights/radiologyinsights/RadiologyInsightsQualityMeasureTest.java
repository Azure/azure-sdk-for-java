// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.health.insights.radiologyinsights;

import com.azure.health.insights.radiologyinsights.models.QualityMeasureComplianceType;
import com.azure.health.insights.radiologyinsights.models.QualityMeasureInference;
import com.azure.health.insights.radiologyinsights.models.QualityMeasureType;
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsInference;
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsInferenceResult;
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsInferenceType;
import com.azure.health.insights.radiologyinsights.models.RadiologyInsightsPatientResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link RadiologyInsightsClient}.
 */
public class RadiologyInsightsQualityMeasureTest extends RadiologyInsightsClientTestBase {

    private RadiologyInsightsClient getClient() {
        return getClientBuilder().buildClient();
    }

    @Test
    public void test() {
        String documentContent = "EXAM: CT CHEST WO CONTRAST\r\n" + "\r\n"
            + "INDICATION: abnormal lung findings. History of emphysema.\r\n" + "\r\n"
            + "TECHNIQUE: Helical CT images through the chest, without contrast. This exam was performed using one or more of the following dose reduction techniques: Automated exposure control, adjustment of the mA and/or kV according to patient size, and/or use of iterative reconstruction technique. \r\n"
            + "\r\n" + "COMPARISON: Chest CT dated 6/21/2022.\r\n" + "\r\n"
            + "Number of previous CT examinations or cardiac nuclear medicine (myocardial perfusion) examinations performed in the preceding 12-months: 2\r\n"
            + "\r\n" + "FINDINGS: \r\n" + "\r\n"
            + "Heart size is normal. No pericardial effusion. Thoracic aorta as well as pulmonary arteries are normal in caliber. There are dense coronary artery calcifications. No enlarged axillary, mediastinal, or hilar lymph nodes by CT size criteria. Central airways are widely patent. No bronchial wall thickening. No pneumothorax, pleural effusion or pulmonary edema. The previously identified posterior right upper lobe nodules are no longer seen. However, there are multiple new small pulmonary nodules. An 8 mm nodule in the right upper lobe, image #15 series 4. New posterior right upper lobe nodule measuring 6 mm, image #28 series 4. New 1.2 cm pulmonary nodule, right upper lobe, image #33 series 4. New 4 mm pulmonary nodule left upper lobe, image #22 series 4. New 8 mm pulmonary nodule in the left upper lobe adjacent to the fissure, image #42 series 4. A few new tiny 2 to 3 mm pulmonary nodules are also noted in the left lower lobe. As before there is a background of severe emphysema. No evidence of pneumonia.\r\n"
            + "\r\n" + "Limited evaluation of the upper abdomen shows no concerning abnormality.\r\n" + "\r\n"
            + "Review of bone windows shows no aggressive appearing osseous lesions.\r\n" + "\r\n" + "\r\n"
            + "IMPRESSION:\r\n" + "\r\n"
            + "1. Previously identified small pulmonary nodules in the right upper lobe have resolved, but there are multiple new small nodules scattered throughout both lungs. Recommend short-term follow-up with noncontrast chest CT in 3 months as per current  Current guidelines (2017 Fleischner Society).\r\n"
            + "2. Severe emphysema.\r\n" + "\r\n" + "Findings communicated to Dr. Jane Smith.";
        setDocumentContent(documentContent);
        setInferenceType(RadiologyInsightsInferenceType.QUALITY_MEASURE);
        setOrderCode("CTCHWO");
        setOrderDescription("CT CHEST WO CONTRAST");
        setQualityMeasureOptions(new QualityMeasureType[] { QualityMeasureType.MIPS_364 });

        try {
            testRadiologyInsightsWithResponse(request -> {
                RadiologyInsightsInferenceResult riResponse = setPlaybackSyncPollerPollInterval(
                    getClient().beginInferRadiologyInsights("job1715007451740", request)).getFinalResult();

                List<RadiologyInsightsPatientResult> patients = riResponse.getPatientResults();
                assertEquals(1, patients.size());

                RadiologyInsightsPatientResult patient = patients.get(0);
                List<RadiologyInsightsInference> inferences = patient.getInferences();
                assertEquals(1, inferences.size());

                RadiologyInsightsInference inference = inferences.get(0);
                assertInstanceOf(QualityMeasureInference.class, inference,
                    "Inference should be an instance of QualityMeasureInference");

                QualityMeasureInference qualityMeasureInference = (QualityMeasureInference) inference;
                String qualityMeasureDenominator = qualityMeasureInference.getQualityMeasureDenominator();
                assertEquals("INCIDENTAL PULMONARY NODULE", qualityMeasureDenominator,
                    "Expected QualityMeasureDenominator is INCIDENTAL PULMONARY NODULE");

                QualityMeasureComplianceType qualityMeasureComplianceType = qualityMeasureInference.getComplianceType();
                assertEquals("performanceMet", qualityMeasureComplianceType.getValue(),
                    "Expected QualityMeasureComplianceType is performanceMet");

                List<String> qualityCriteriaList = qualityMeasureInference.getQualityCriteria();
                assertEquals("APPROPRIATE FOLLOW-UP RECOMMENDATION", qualityCriteriaList.get(0),
                    "Expected QualityCriteria is FOLLOW-UP RECOMMENDATION");
            });

        } catch (Throwable t) {
            String message = t.toString() + "\n" + Arrays.toString(t.getStackTrace());
            t.printStackTrace();
            Assertions.fail(message);
        }
    }

}

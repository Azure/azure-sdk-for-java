// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.contentsafety;

import com.azure.ai.contentsafety.models.DetectOutcome;
import com.azure.ai.contentsafety.models.DetectProvenanceOptions;
import com.azure.ai.contentsafety.models.DetectProvenanceResult;
import com.azure.ai.contentsafety.models.DetectedProvenance;
import com.azure.ai.contentsafety.models.DetectedProvenanceType;
import com.azure.ai.contentsafety.models.ProvenanceContent;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public final class ContentProvenanceTests extends ContentSafetyClientTestBase {
    @Override
    protected void beforeTest() {
        super.beforeTest();
        // AZSDK2030 rewrites operation-location to https://example.com, which breaks LRO polling in playback.
        interceptorManager.removeSanitizers("AZSDK2030");
    }

    @Test
    public void testDetectProvenanceInSignedMedia() {
        DetectProvenanceResult response
            = contentProvenanceClient.beginDetect(new DetectProvenanceOptions(new ProvenanceContent(signedMediaUri)))
                .getFinalResult();

        Assertions.assertNotNull(response);
        Assertions.assertEquals(DetectOutcome.PROVENANCE_DETECTED, response.getOutcome());
        Assertions.assertNotNull(response.getResults());
        Assertions.assertFalse(response.getResults().isEmpty());
        for (DetectedProvenance detected : response.getResults()) {
            Assertions.assertTrue(DetectedProvenanceType.C2PA.equals(detected.getType())
                || DetectedProvenanceType.WATERMARK.equals(detected.getType()));
            Assertions.assertNotNull(detected.getProvider());
            Assertions.assertNotNull(detected.getModelName());
        }
    }

    @Test
    public void testDetectNoProvenanceInUnsignedMedia() {
        DetectProvenanceResult response
            = contentProvenanceClient.beginDetect(new DetectProvenanceOptions(new ProvenanceContent(unsignedMediaUri)))
                .getFinalResult();

        Assertions.assertNotNull(response);
        Assertions.assertEquals(DetectOutcome.NO_PROVENANCE_DETECTED, response.getOutcome());
        Assertions.assertTrue(response.getResults() == null || response.getResults().isEmpty());
    }
}

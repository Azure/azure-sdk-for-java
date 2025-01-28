// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.health.insights.clinicalmatching;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.health.insights.clinicalmatching.models.TrialMatcherPatientResult;
import com.azure.health.insights.clinicalmatching.models.TrialMatcherResults;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Unit tests for {@link ClinicalMatchingClient}.
 */
public class ClinicalMatchingClientTest extends ClinicalMatchingClientTestBase {

    private ClinicalMatchingClient getClient() {
        return getClientBuilder().buildClient();
    }

    @Test
    public void test() {
        // playback
        testTMWithResponse(request -> {
            BinaryData responseValue
                = setPlaybackSyncPollerPollInterval(getClient().beginMatchTrials(request, new RequestOptions()))
                    .getFinalResult();
            TrialMatcherResults tmRespone = responseValue.toObject(TrialMatcherResults.class);
            List<TrialMatcherPatientResult> patients = tmRespone.getPatients();
            assertEquals(1, patients.size());
            TrialMatcherPatientResult patient = patients.get(0);
            assertFalse(patient.getInferences().isEmpty(), "at least one inference should be returned");
        });
    }
}

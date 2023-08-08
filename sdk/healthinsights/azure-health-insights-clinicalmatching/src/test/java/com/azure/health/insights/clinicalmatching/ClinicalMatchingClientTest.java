// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.health.insights.clinicalmatching;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.azure.health.insights.clinicalmatching.models.TrialMatcherResult;
import com.azure.health.insights.clinicalmatching.models.TrialMatcherPatientResult;
import com.azure.core.util.serializer.TypeReference;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

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
        try {
            testTMWithResponse(request -> {

                BinaryData responseValue = setPlaybackSyncPollerPollInterval(getClient().beginMatchTrials(request, new RequestOptions())).waitForCompletion().getValue();
                TrialMatcherResult tmRespone = responseValue.toObject(TypeReference.createInstance(TrialMatcherResult.class));
                List<TrialMatcherPatientResult> patients = tmRespone.getResults().getPatients();
                assertEquals(1, patients.size());
                TrialMatcherPatientResult patient = patients.get(0);
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

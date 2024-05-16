// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.health.insights.cancerprofiling;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.azure.health.insights.cancerprofiling.models.OncoPhenotypePatientResult;
import com.azure.health.insights.cancerprofiling.models.OncoPhenotypeResult;

import com.azure.core.util.serializer.TypeReference;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Unit tests for {@link CancerProfilingClient}.
 */
public class CancerProfilingClientTest extends CancerProfilingClientTestBase {

    private CancerProfilingClient getClient() {
        return getClientBuilder().buildClient();
    }

    @Test
    public void test() {
        try {
            testCancerProfilingWithResponse(request -> {
                BinaryData responseValue = setPlaybackSyncPollerPollInterval(getClient().beginInferCancerProfile(request, new RequestOptions())).waitForCompletion().getValue();
                OncoPhenotypeResult respone = responseValue.toObject(TypeReference.createInstance(OncoPhenotypeResult.class));

                List<OncoPhenotypePatientResult> patients = respone.getResults().getPatients();
                assertEquals(1, patients.size());
                OncoPhenotypePatientResult patient = patients.get(0);
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

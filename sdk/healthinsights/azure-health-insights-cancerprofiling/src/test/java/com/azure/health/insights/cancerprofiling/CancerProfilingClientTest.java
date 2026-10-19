// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.health.insights.cancerprofiling;

import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.health.insights.cancerprofiling.models.OncoPhenotypePatientResult;
import com.azure.health.insights.cancerprofiling.models.OncoPhenotypeResults;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Unit tests for {@link CancerProfilingClient}.
 */
public class CancerProfilingClientTest extends CancerProfilingClientTestBase {

    private CancerProfilingClient getClient() {
        return getClientBuilder().buildClient();
    }

    @Test
    public void test() {
        testCancerProfilingWithResponse(request -> {
            BinaryData responseValue
                = setPlaybackSyncPollerPollInterval(getClient().beginInferCancerProfile(request, new RequestOptions()))
                    .getFinalResult();
            OncoPhenotypeResults response = responseValue.toObject(OncoPhenotypeResults.class);

            List<OncoPhenotypePatientResult> patients = response.getPatients();
            assertEquals(1, patients.size());
            OncoPhenotypePatientResult patient = patients.get(0);
            assertFalse(patient.getInferences().isEmpty(), "at least one inference should be returned");
        });
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.health.insights.clinicalmatching;

import java.io.IOException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AllSamples {
    String endpoint;
    String apiKey;

    @BeforeEach
    public void init() {
        endpoint = System.getenv("SDK_JAVA_HDS_ENDPOINT");
        apiKey = System.getenv("SDK_JAVA_HDS_APIKEY");
    }

    @Test
    public void matchTrialCustomTrial() throws InterruptedException, IOException {
        SampleMatchTrialCustomTrial.runSample(endpoint, apiKey);
    }

    // @Test
    public void matchTrialGradualMatching() throws InterruptedException, IOException {
        SampleMatchTrialGradualMatching.runSample(endpoint, apiKey);
    }

    // @Test
    public void matchTrialPatientFhir() throws InterruptedException, IOException {
        SampleMatchTrialPatientFhir.runSample(endpoint, apiKey);
    }

    // @Test
    public void matchTrialsSync() throws InterruptedException, IOException {
        SampleMatchTrialSync.runSample(endpoint, apiKey);
    }

    // @Test
    public void matchTrialAsync() throws InterruptedException, IOException {
        SampleMatchTrialAsync.runSample(endpoint, apiKey);
    }

    // @Test
    public void matchTrialUnstructuredClinicalNote() throws InterruptedException, IOException {
        SampleMatchTrialUnstructuredClinicalNote.runSample(endpoint, apiKey);
    }

}

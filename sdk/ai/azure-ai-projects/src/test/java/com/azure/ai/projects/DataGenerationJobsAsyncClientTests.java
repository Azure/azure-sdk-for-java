// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.agents.models.PageOrder;
import com.azure.ai.projects.models.FoundryFeaturesOptInKeys;
import com.azure.core.http.HttpClient;
import com.azure.core.test.annotation.LiveOnly;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import static com.azure.ai.projects.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;

public class DataGenerationJobsAsyncClientTests extends ClientTestBase {
    private static final FoundryFeaturesOptInKeys DATA_GENERATION_PREVIEW
        = FoundryFeaturesOptInKeys.DATA_GENERATION_JOBS_V1_PREVIEW;

    @LiveOnly
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void dataGenerationJobsListAsyncSample(HttpClient httpClient, AIProjectsServiceVersion serviceVersion) {
        DataGenerationJobsAsyncClient dataGenerationJobsAsyncClient
            = getClientBuilder(httpClient, serviceVersion).buildDataGenerationJobsAsyncClient();

        StepVerifier.create(dataGenerationJobsAsyncClient
            .listGenerationJobs(DATA_GENERATION_PREVIEW, 5, PageOrder.DESC, null, null, null, null)
            .take(5)
            .doOnNext(job -> {
                Assertions.assertNotNull(job);
                Assertions.assertNotNull(job.getId());
            })
            .then()).verifyComplete();
    }
}

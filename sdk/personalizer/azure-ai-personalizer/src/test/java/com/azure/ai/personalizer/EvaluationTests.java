// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer;

import com.azure.ai.personalizer.models.EvaluationOperationResult;
import com.azure.ai.personalizer.models.PersonalizerEvaluation;
import com.azure.ai.personalizer.models.PersonalizerEvaluationJobStatus;
import com.azure.ai.personalizer.models.PersonalizerEvaluationOptions;
import com.azure.ai.personalizer.models.PersonalizerPolicy;
import com.azure.ai.personalizer.models.PersonalizerPolicySource;
import com.azure.core.http.HttpClient;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.polling.SyncPoller;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.OffsetDateTime;
import java.util.ArrayList;

import static com.azure.ai.personalizer.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.*;

public class EvaluationTests extends PersonalizerTestBase {

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.personalizer.TestUtils#getTestParameters")
    public final void listEvaluationsTest(HttpClient httpClient, PersonalizerServiceVersion serviceVersion) {
        PersonalizerAdminClient client = getAdministrationClient(httpClient, serviceVersion, true);
        client.getEvaluations();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.personalizer.TestUtils#getTestParameters")
    public final void runEvaluationLifecycleTest(HttpClient httpClient, PersonalizerServiceVersion serviceVersion) {
        PersonalizerEvaluationOptions evaluationOptions = new PersonalizerEvaluationOptions()
            .setName("JavaSDKTestEvaluation")
            .setEnableOfflineExperimentation(true)
            .setStartTime(OffsetDateTime.now())
            .setEndTime(OffsetDateTime.now().minusDays(1))
            .setPolicies(new ArrayList<PersonalizerPolicy>());
        PersonalizerAdminAsyncClient client = getAdministrationAsyncClient(httpClient, serviceVersion, true);
        SyncPoller<EvaluationOperationResult, PersonalizerEvaluation> syncPoller = client.createEvaluation(evaluationOptions).getSyncPoller();
        syncPoller.waitForCompletion();

        PersonalizerEvaluation evaluationResult = syncPoller.getFinalResult();
        assertNotNull(evaluationResult);
        assertEquals(PersonalizerEvaluationJobStatus.COMPLETED, evaluationResult.getStatus());
        assertTrue(evaluationResult.getPolicyResults().stream().anyMatch(p -> p.getPolicySource().equals(PersonalizerPolicySource.ONLINE)));
        assertFalse(CoreUtils.isNullOrEmpty(evaluationResult.getOptimalPolicy()));

        client.deleteEvaluation(evaluationResult.getId());
    }
}

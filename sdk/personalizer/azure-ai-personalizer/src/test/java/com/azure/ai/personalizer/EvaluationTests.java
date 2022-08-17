// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer;

import com.azure.ai.personalizer.models.EvaluationOperationResult;
import com.azure.ai.personalizer.models.PersonalizerEvaluation;
import com.azure.ai.personalizer.models.PersonalizerEvaluationJobStatus;
import com.azure.ai.personalizer.models.PersonalizerEvaluationOptions;
import com.azure.ai.personalizer.models.PersonalizerPolicySource;
import com.azure.core.http.HttpClient;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.polling.PollerFlux;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

import static com.azure.ai.personalizer.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.*;

public class EvaluationTests extends PersonalizerTestBase {
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.personalizer.TestUtils#getTestParameters")
    public final void runEvaluationTest(HttpClient httpClient, PersonalizerServiceVersion serviceVersion) {
        PersonalizerEvaluationOptions evaluationOptions = new PersonalizerEvaluationOptions()
            .setName("JavaSDKTestEvaluation")
            .setEnableOfflineExperimentation(true)
            .setStartTime(OffsetDateTime.now())
            .setEndTime(OffsetDateTime.now().minusDays(1));
        PersonalizerAdminAsyncClient client = getAdministrationAsyncClient(httpClient, serviceVersion, true);
        PollerFlux<EvaluationOperationResult, PersonalizerEvaluation> evaluationPoller =
            client.createEvaluation(evaluationOptions);

        Mono<PersonalizerEvaluation> evaluationPollerResult = evaluationPoller
            .last()
            .flatMap(pollResponse -> {
                if (pollResponse.getStatus().isComplete()) {
                    return pollResponse.getFinalResult();
                } else {
                    return Mono.error(new RuntimeException("Polling completed unsuccessfully with status:"
                        + pollResponse.getStatus()));
                }
            });

        evaluationPollerResult.subscribe(evaluationResult -> {
            assertEquals(PersonalizerEvaluationJobStatus.COMPLETED, evaluationResult.getStatus());
            assertTrue(evaluationResult.getPolicyResults().stream().anyMatch(p -> p.getPolicySource().equals(PersonalizerPolicySource.ONLINE)));
            assertFalse(CoreUtils.isNullOrEmpty(evaluationResult.getOptimalPolicy()));
        });
    }
}

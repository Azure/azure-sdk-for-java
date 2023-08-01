// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter;

import com.azure.communication.jobrouter.models.CancelExceptionAction;
import com.azure.communication.jobrouter.models.CreateExceptionPolicyOptions;
import com.azure.communication.jobrouter.models.ExceptionAction;
import com.azure.communication.jobrouter.models.ExceptionPolicy;
import com.azure.communication.jobrouter.models.ExceptionRule;
import com.azure.communication.jobrouter.models.QueueLengthExceptionTrigger;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExceptionPolicyLiveTests extends JobRouterTestBase {
    private JobRouterAdministrationClient routerAdminClient;

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createExceptionPolicy(HttpClient httpClient) {
        // Setup
        routerAdminClient = getRouterAdministrationClient(httpClient);
        String exceptionPolicyId = String.format("%s-CreateExceptionPolicy-ExceptionPolicy", JAVA_LIVE_TESTS);
        String exceptionPolicyName = String.format("%s-Name", exceptionPolicyId);

        CancelExceptionAction exceptionAction = new CancelExceptionAction()
            .setDispositionCode("CancelledDueToMaxQueueLengthReached")
            .setNote("Job Cancelled as maximum queue length is reached.");

        Map<String, ExceptionAction> exceptionActions = new HashMap<String, ExceptionAction>() {
            {
                put("CancelledDueToMaxQueueLengthReached", exceptionAction);
            }
        };

        ExceptionRule exceptionRule = new ExceptionRule(new QueueLengthExceptionTrigger(1), exceptionActions);

        Map<String, ExceptionRule> exceptionRules = new HashMap<String, ExceptionRule>() {
            {
                put(exceptionPolicyId, exceptionRule);
            }
        };

        CreateExceptionPolicyOptions createExceptionPolicyOptions = new CreateExceptionPolicyOptions(
            exceptionPolicyId, exceptionRules)
            .setName(exceptionPolicyName);

        // Action
        ExceptionPolicy result = routerAdminClient.createExceptionPolicy(createExceptionPolicyOptions);

        // Verify
        assertEquals(exceptionPolicyId, result.getId());

        // Cleanup
        routerAdminClient.deleteExceptionPolicy(exceptionPolicyId);
    }
}

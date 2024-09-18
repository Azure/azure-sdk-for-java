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
import com.azure.core.test.annotation.LiveOnly;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ExceptionPolicyLiveTests extends JobRouterTestBase {
    private JobRouterAdministrationClient routerAdminClient;

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    @LiveOnly // Remove after azure-core-test 1.26.0-beta.1 is released.
    public void createExceptionPolicy(HttpClient httpClient) {
        // Setup
        routerAdminClient = getRouterAdministrationClient(httpClient);
        String exceptionPolicyId = String.format("%s-CreateExceptionPolicy-ExceptionPolicy", JAVA_LIVE_TESTS);
        String exceptionPolicyName = String.format("%s-Name", exceptionPolicyId);

        CancelExceptionAction exceptionAction = new CancelExceptionAction()
            .setDispositionCode("CancelledDueToMaxQueueLengthReached")
            .setNote("Job Cancelled as maximum queue length is reached.");

        List<ExceptionAction> exceptionActions = new ArrayList<ExceptionAction>() {
            {
                add(exceptionAction);
            }
        };

        ExceptionRule exceptionRule = new ExceptionRule("CancelledDueToMaxQueueLengthReached", new QueueLengthExceptionTrigger(1), exceptionActions);

        List<ExceptionRule> exceptionRules = new ArrayList<ExceptionRule>() {
            {
                add(exceptionRule);
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

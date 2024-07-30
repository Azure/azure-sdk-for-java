// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.jobrouter;

import com.azure.communication.jobrouter.models.CancelExceptionAction;
import com.azure.communication.jobrouter.models.CreateExceptionPolicyOptions;
import com.azure.communication.jobrouter.models.ExceptionAction;
import com.azure.communication.jobrouter.models.ExceptionPolicy;
import com.azure.communication.jobrouter.models.ExceptionRule;
import com.azure.communication.jobrouter.models.LabelOperator;
import com.azure.communication.jobrouter.models.ManualReclassifyExceptionAction;
import com.azure.communication.jobrouter.models.QueueLengthExceptionTrigger;
import com.azure.communication.jobrouter.models.ReclassifyExceptionAction;
import com.azure.communication.jobrouter.models.RouterValue;
import com.azure.communication.jobrouter.models.RouterWorkerSelector;
import com.azure.communication.jobrouter.models.WaitTimeExceptionTrigger;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ExceptionPolicyLiveTests extends JobRouterTestBase {

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createExceptionPolicy(HttpClient httpClient) {
        // Setup
        JobRouterAdministrationClient routerAdminClient = getRouterAdministrationClient(httpClient);
        String exceptionPolicyId = String.format("%s-CreateExceptionPolicy-ExceptionPolicy", JAVA_LIVE_TESTS);
        String exceptionPolicyName = String.format("%s-Name", exceptionPolicyId);

        CancelExceptionAction exceptionAction = new CancelExceptionAction()
            .setDispositionCode("CancelledDueToMaxQueueLengthReached")
            .setNote("Job Cancelled as maximum queue length is reached.");

        List<ExceptionAction> exceptionActions = Arrays.asList(exceptionAction, new ManualReclassifyExceptionAction()
            .setPriority(5)
            .setWorkerSelectors(Collections.singletonList(new RouterWorkerSelector("IntValue", LabelOperator.EQUAL, new RouterValue(5)))));

        ExceptionRule exceptionRule = new ExceptionRule("CancelledDueToMaxQueueLengthReached", new QueueLengthExceptionTrigger(1), exceptionActions);

        List<ExceptionRule> exceptionRules = Arrays.asList(exceptionRule,
            new ExceptionRule("rule2", new WaitTimeExceptionTrigger(Duration.ofSeconds(100)),
                Collections.singletonList(new ReclassifyExceptionAction()
                    .setLabelsToUpsert(Collections.singletonMap("Label1", new RouterValue(true))))));

        CreateExceptionPolicyOptions createExceptionPolicyOptions = new CreateExceptionPolicyOptions(
            exceptionPolicyId, exceptionRules).setName(exceptionPolicyName);

        // Action
        ExceptionPolicy result = routerAdminClient.createExceptionPolicy(createExceptionPolicyOptions);

        // Verify
        assertEquals(exceptionPolicyId, result.getId());
        assertEquals(exceptionPolicyName, result.getName());
        assertNotNull(result.getEtag());
        assertEquals(2, result.getExceptionRules().size());
        assertEquals(2, result.getExceptionRules().get(0).getActions().size());
        assertEquals(1, result.getExceptionRules().get(1).getActions().size());

        Response<BinaryData> binaryResponse = routerAdminClient.getExceptionPolicyWithResponse(result.getId(), null);
        ExceptionPolicy deserialized = binaryResponse.getValue().toObject(ExceptionPolicy.class);

        assertEquals(exceptionPolicyId, deserialized.getId());
        assertEquals(exceptionPolicyName, deserialized.getName());
        assertEquals(result.getEtag(), deserialized.getEtag());
        assertEquals(2, deserialized.getExceptionRules().size());
        assertEquals(2, deserialized.getExceptionRules().get(0).getActions().size());
        assertEquals(1, deserialized.getExceptionRules().get(1).getActions().size());

        deserialized.setExceptionRules(new ArrayList<>());
        ExceptionPolicy updatedPolicy = routerAdminClient.updateExceptionPolicy(
            deserialized.getId(), deserialized);

        assertEquals(exceptionPolicyId, updatedPolicy.getId());
        assertEquals(exceptionPolicyName, updatedPolicy.getName());
        assertNotEquals(result.getEtag(), updatedPolicy.getEtag());
        assertEquals(0, deserialized.getExceptionRules().size());

        // Cleanup
        routerAdminClient.deleteExceptionPolicy(exceptionPolicyId);
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 * DESCRIPTION:
 *     These samples demonstrates how to create Exception Policy used in ACS JobRouter.
 *     You need a valid connection string to an Azure Communication Service to execute the sample
 * NOTES:
 *     Set the environment variables with your own values before running the sample:
 *     1) AZURE_TEST_JOBROUTER_CONNECTION_STRING - Communication Service connection string
 */

package com.azure.communication.jobrouter;

import com.azure.communication.jobrouter.implementation.models.CancelExceptionAction;
import com.azure.communication.jobrouter.implementation.models.ExceptionAction;
import com.azure.communication.jobrouter.models.ExceptionPolicy;
import com.azure.communication.jobrouter.implementation.models.ExceptionRule;
import com.azure.communication.jobrouter.implementation.models.PagedExceptionPolicy;
import com.azure.communication.jobrouter.implementation.models.QueueLengthExceptionTrigger;
import com.azure.communication.jobrouter.implementation.models.WaitTimeExceptionTrigger;
import com.azure.communication.jobrouter.models.CreateExceptionPolicyOptions;
import com.azure.communication.jobrouter.models.UpdateExceptionPolicyOptions;
import com.azure.core.http.rest.PagedIterable;

import java.util.Collections;

public class ExceptionPolicyExample {
    private final String exceptionPolicyId;

    public ExceptionPolicyExample(String exceptionPolicyId) {
        this.exceptionPolicyId = exceptionPolicyId;
    }

    private void createAndUpdateExceptionPolicy() {
        String connectionString = System.getenv("AZURE_TEST_JOBROUTER_CONNECTION_STRING");
        RouterClient routerClient = new RouterClientBuilder()
            .connectionString(connectionString)
            .buildClient();

        /**
         * Define an exception trigger.
         * This sets off exception when there are at least 10 jobs in a queue.
         */
        QueueLengthExceptionTrigger exceptionTrigger = new QueueLengthExceptionTrigger()
            .setThreshold(10);

        /**
         * Define an exception action.
         * This sets up what action to take when an exception trigger condition is fulfilled.
         */
        ExceptionAction exceptionAction = new CancelExceptionAction();

        /**
         * Defining exception rule combining the trigger and action.
         */
        ExceptionRule exceptionRule = new ExceptionRule()
            .setActions(Collections.singletonMap("CancelJobActionWhenQueueIsFull", exceptionAction))
            .setTrigger(exceptionTrigger);

        /**
         * Create the exception policy.
         */
        CreateExceptionPolicyOptions createExceptionPolicyOptions = new CreateExceptionPolicyOptions(exceptionPolicyId,
            Collections.singletonMap("TriggerJobCancellationWhenQueueLenIs10", exceptionRule));
        routerClient.createExceptionPolicy(createExceptionPolicyOptions);

        System.out.printf("Successfully created exception policy with id: %s %n", exceptionPolicyId);

        /**
         * Add additional exception rule to policy.
         */
        WaitTimeExceptionTrigger waitTimeExceptionTrigger = new WaitTimeExceptionTrigger();
        waitTimeExceptionTrigger.setThreshold("PT1H");

        ExceptionRule waitTimeExceptionRule = new ExceptionRule();
        waitTimeExceptionRule.setTrigger(waitTimeExceptionTrigger);
        waitTimeExceptionRule.setActions(Collections.singletonMap("CancelJobActionWhenJobInQFor1Hr", exceptionAction));

        UpdateExceptionPolicyOptions updateExceptionPolicyOptions = new UpdateExceptionPolicyOptions(createExceptionPolicyOptions.getId())
            .setExceptionRules(Collections.singletonMap("CancelJobWhenInQueueFor1Hr", waitTimeExceptionRule));

        /**
         * Update policy using routerClient.
         */
        routerClient.updateExceptionPolicy(updateExceptionPolicyOptions);

        System.out.println("Exception policy has been successfully updated.");
    }

    private void getExceptionPolicy() {
        String connectionString = System.getenv("AZURE_TEST_JOBROUTER_CONNECTION_STRING");
        RouterClient routerClient = new RouterClientBuilder()
            .connectionString(connectionString)
            .buildClient();

        ExceptionPolicy exceptionPolicyResult = routerClient.getExceptionPolicy(exceptionPolicyId);
        System.out.printf("Successfully fetched exception policy with id: %s %n", exceptionPolicyResult.getId());
    }

    private void listExceptionPolicies() {
        String connectionString = System.getenv("AZURE_TEST_JOBROUTER_CONNECTION_STRING");
        RouterClient routerClient = new RouterClientBuilder()
            .connectionString(connectionString)
            .buildClient();

        PagedIterable<PagedExceptionPolicy> exceptionPolicyPagedIterable = routerClient.listExceptionPolicies();
        exceptionPolicyPagedIterable.iterableByPage().forEach(resp -> {
            System.out.printf("Response headers are %s. Url %s  and status code %d %n", resp.getHeaders(),
                resp.getRequest().getUrl(), resp.getStatusCode());
            resp.getElements().forEach(exceptionPolicy -> {
                System.out.printf("Retrieved exception policy with id %s %n.", exceptionPolicy.getId());
            });
        });
    }

    private void cleanUp() {
        String connectionString = System.getenv("AZURE_TEST_JOBROUTER_CONNECTION_STRING");
        RouterClient routerClient = new RouterClientBuilder()
            .connectionString(connectionString)
            .buildClient();
        routerClient.deleteExceptionPolicy(exceptionPolicyId);
    }

    public static void main() {
        String exceptionPolicyId = "sample_exception_policy";
        ExceptionPolicyExample example = new ExceptionPolicyExample(exceptionPolicyId);
        example.createAndUpdateExceptionPolicy();
        example.cleanUp();
    }
}

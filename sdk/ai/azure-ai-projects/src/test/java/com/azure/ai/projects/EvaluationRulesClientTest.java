// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.projects;

import com.azure.ai.projects.models.DailyRecurrenceSchedule;
import com.azure.ai.projects.models.EvaluationRule;
import com.azure.ai.projects.models.EvaluationRuleActionType;
import com.azure.ai.projects.models.EvaluationRuleEventType;
import com.azure.ai.projects.models.RecurrenceTrigger;
import com.azure.ai.projects.models.ScheduleProvisioningStatus;
import com.azure.ai.projects.models.TraceEvaluationRuleAction;
import com.azure.ai.projects.models.TraceEvaluationSource;
import com.azure.ai.projects.models.TracesEvaluationRunDataSource;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.http.HttpClient;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Collections;

import static com.azure.ai.projects.TestUtils.DISPLAY_NAME_WITH_ARGUMENTS;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EvaluationRulesClientTest extends ClientTestBase {
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("com.azure.ai.projects.TestUtils#getTestParameters")
    public void scheduledRuleLifecycle(HttpClient httpClient, AIProjectsServiceVersion serviceVersion) {
        EvaluationRulesClient client = getClientBuilder(httpClient, serviceVersion).buildEvaluationRulesClient();
        String id = testResourceNamer.randomName("scheduled-trace-", 64);
        TraceEvaluationRuleAction action
            = new TraceEvaluationRuleAction(recordedSetting("FOUNDRY_EVALUATION_ID", "eval_recorded"),
                TraceEvaluationSource.ofTraces(new TracesEvaluationRunDataSource()
                    .setAgentName(recordedSetting("FOUNDRY_AGENT_NAME", "recorded-agent"))));
        EvaluationRule rule = new EvaluationRule(action, EvaluationRuleEventType.TIMER_TRIGGER, true)
            .setDisplayName("Recorded trace rule")
            .setSchedule(
                new RecurrenceTrigger(1, new DailyRecurrenceSchedule().setHours(Collections.singletonList(9))));

        assertEquals(id, client.createOrUpdateEvaluationRule(id, rule).getId());
        try {
            assertEquals(id, client.getEvaluationRule(id).getId());
            assertTrue(client
                .listEvaluationRules(EvaluationRuleActionType.TRACE_EVALUATION,
                    recordedSetting("FOUNDRY_AGENT_NAME", "recorded-agent"), true)
                .stream()
                .anyMatch(item -> id.equals(item.getId())));

            waitForProvisioning(client, id);
            assertEquals("Updated trace rule",
                client.createOrUpdateEvaluationRule(id, rule.setDisplayName("Updated trace rule")).getDisplayName());
        } finally {
            for (int attempt = 0; attempt < 30; attempt++) {
                try {
                    client.deleteEvaluationRule(id);
                    break;
                } catch (ResourceModifiedException e) {
                    if (attempt == 29) {
                        throw e;
                    }
                    sleepIfRunningAgainstService(2000);
                }
            }
        }
    }

    private void waitForProvisioning(EvaluationRulesClient client, String id) {
        for (int attempt = 0; attempt < 30; attempt++) {
            ScheduleProvisioningStatus status = client.getEvaluationRule(id).getProvisioningStatus();
            if (ScheduleProvisioningStatus.SUCCEEDED.equals(status)
                || ScheduleProvisioningStatus.FAILED.equals(status)) {
                assertEquals(ScheduleProvisioningStatus.SUCCEEDED, status);
                return;
            }
            sleepIfRunningAgainstService(2000);
        }
        throw new AssertionError("Rule did not finish provisioning: " + id);
    }
}

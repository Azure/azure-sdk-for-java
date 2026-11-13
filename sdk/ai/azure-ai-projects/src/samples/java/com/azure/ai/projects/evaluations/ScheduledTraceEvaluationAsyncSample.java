// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects.evaluations;

import com.azure.ai.projects.AIProjectClientBuilder;
import com.azure.ai.projects.EvaluationRulesAsyncClient;
import com.azure.ai.projects.models.DailyRecurrenceSchedule;
import com.azure.ai.projects.models.EvaluationRule;
import com.azure.ai.projects.models.EvaluationRuleEventType;
import com.azure.ai.projects.models.RecurrenceTrigger;
import com.azure.ai.projects.models.TraceEvaluationRuleAction;
import com.azure.ai.projects.models.TraceEvaluationSource;
import com.azure.ai.projects.models.TracesEvaluationRunDataSource;
import com.azure.identity.DefaultAzureCredentialBuilder;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.UUID;

/**
 * Asynchronously creates and inspects a daily agent-trace evaluation rule, then removes it.
 * Requires an existing evaluation that accepts agent traces and connected Application Insights.
 * Set {@code FOUNDRY_PROJECT_ENDPOINT}, {@code FOUNDRY_EVALUATION_ID}, and {@code FOUNDRY_AGENT_NAME}.
 * No preview opt-in is needed for timer-triggered evaluation rules.
 */
public class ScheduledTraceEvaluationAsyncSample {
    public static void main(String[] args) {
        EvaluationRulesAsyncClient rules = new AIProjectClientBuilder()
            .endpoint(EvaluationSampleUtils.requiredSetting("FOUNDRY_PROJECT_ENDPOINT"))
            .credential(new DefaultAzureCredentialBuilder().build())
            .buildEvaluationRulesAsyncClient();

        String agentName = EvaluationSampleUtils.requiredSetting("FOUNDRY_AGENT_NAME");
        String id = "java-scheduled-trace-" + UUID.randomUUID();
        EvaluationRule rule = new EvaluationRule(
            new TraceEvaluationRuleAction(EvaluationSampleUtils.requiredSetting("FOUNDRY_EVALUATION_ID"),
                TraceEvaluationSource.ofTraces(new TracesEvaluationRunDataSource().setAgentName(agentName))),
            EvaluationRuleEventType.TIMER_TRIGGER, true)
            .setDisplayName("Daily agent-trace evaluation")
            .setSchedule(new RecurrenceTrigger(1,
                new DailyRecurrenceSchedule().setHours(Collections.singletonList(9))));

        Mono.usingWhen(
            rules.createOrUpdateEvaluationRule(id, rule),
            created -> {
                System.out.printf("Created rule: %s; provisioning: %s%n",
                    created.getId(), created.getProvisioningStatus());
                return rules.getEvaluationRule(id)
                    .doOnNext(retrieved -> System.out.printf("Retrieved rule: %s%n", retrieved.getDisplayName()))
                    .thenMany(rules.listEvaluationRules()
                        .doOnNext(listed -> System.out.printf("Rule: %s%n", listed.getId())))
                    .then();
            },
            created -> EvaluationSampleUtils.deleteRule(rules, id)
        ).block();
    }
}

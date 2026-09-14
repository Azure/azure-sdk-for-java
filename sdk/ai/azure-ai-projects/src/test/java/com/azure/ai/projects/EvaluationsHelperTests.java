// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects;

import com.azure.ai.projects.models.AzureAIAgentTarget;
import com.azure.ai.projects.models.AzureAIEvaluationDataSource;
import com.azure.ai.projects.models.TestingCriterionAzureAIEvaluator;
import com.azure.core.util.BinaryData;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.openai.core.ObjectMappers;
import com.openai.models.evals.EvalCreateParams;
import com.openai.models.evals.runs.CreateEvalCompletionsRunDataSource;
import java.util.Collections;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EvaluationsHelperTests {
    @Test
    public void azureDataSourcesPreserveTheirWireShape() throws java.io.IOException {
        CreateEvalCompletionsRunDataSource.Source source = CreateEvalCompletionsRunDataSource.Source
            .ofFileId(CreateEvalCompletionsRunDataSource.Source.FileId.builder().id("file-123").build());
        CreateEvalCompletionsRunDataSource.InputMessages input = CreateEvalCompletionsRunDataSource.InputMessages
            .ofItemReference(CreateEvalCompletionsRunDataSource.InputMessages.ItemReference.builder()
                .itemReference("item.messages")
                .build());
        AzureAIAgentTarget target = new AzureAIAgentTarget("agent");
        AzureAIEvaluationDataSource[] sources = {
            AzureAIEvaluationDataSource.csv("file-123"),
            AzureAIEvaluationDataSource.targetCompletions(source, target, input),
            AzureAIEvaluationDataSource.responses(source, Collections.singletonMap("response_id", "item.response_id"))
                .setMaxNumTurns(4)
                .setMaxRunsHourly(10)
                .setEventConfigurationId("events"),
            AzureAIEvaluationDataSource.benchmark(target).setInputMessages(input),
            AzureAIEvaluationDataSource.redTeam(BinaryData.fromString("{\"type\":\"synthetic\"}"), target),
            AzureAIEvaluationDataSource.traces()
                .setTraceIds(Collections.singletonList("trace"))
                .setAgentId("agent-id")
                .setAgentName("agent")
                .setLookbackHours(24)
                .setMaxTraces(10)
                .setIngestionDelaySeconds(30)
                .setEndTime(java.time.OffsetDateTime.parse("2026-01-01T00:00:00Z")) };
        String[] types = {
            "csv",
            "azure_ai_target_completions",
            "azure_ai_responses",
            "azure_ai_benchmark_preview",
            "azure_ai_red_team",
            "azure_ai_traces_preview" };
        for (int index = 0; index < sources.length; index++) {
            com.fasterxml.jackson.databind.JsonNode expected
                = ObjectMappers.jsonMapper().readTree(sources[index].toJsonString());
            com.fasterxml.jackson.databind.JsonNode actual = ObjectMappers.jsonMapper()
                .readTree(
                    ObjectMappers.jsonMapper().writeValueAsString(EvaluationsHelper.toDataSource(sources[index])));
            Assertions.assertEquals(types[index], actual.path("type").asText(),
                "Before conversion: " + expected + "; after conversion: " + actual);
            Assertions.assertEquals(expected, actual);
        }
        com.fasterxml.jackson.databind.JsonNode config = ObjectMappers.jsonMapper()
            .readTree(ObjectMappers.jsonMapper()
                .writeValueAsString(EvaluationsHelper.createDataSourceConfig("traces_preview")));
        Assertions.assertEquals("azure_ai_source", config.get("type").asText());
        Assertions.assertEquals("traces_preview", config.get("scenario").asText());
        Assertions.assertThrows(IllegalStateException.class,
            () -> AzureAIEvaluationDataSource.csv("file").setMaxTraces(1));
    }

    @Test
    public void convertsAzureAIEvaluatorToTestingCriterion() throws JsonProcessingException {
        TestingCriterionAzureAIEvaluator evaluator
            = new TestingCriterionAzureAIEvaluator("coherence", "builtin.coherence")
                .setInitializationParameters(
                    Collections.singletonMap("deployment_name", BinaryData.fromObject("gpt-4o-mini")))
                .setDataMapping(Collections.singletonMap("response", "{{sample.output_text}}"));

        EvalCreateParams.TestingCriterion testingCriterion = EvaluationsHelper.toTestingCriterion(evaluator);

        Assertions.assertNotNull(testingCriterion);
        String json = ObjectMappers.jsonMapper().writeValueAsString(testingCriterion);
        Assertions.assertTrue(json.contains("\"type\":\"azure_ai_evaluator\""));
        Assertions.assertTrue(json.contains("\"evaluator_name\":\"builtin.coherence\""));
        Assertions.assertTrue(json.contains("\"deployment_name\":\"gpt-4o-mini\""));
        Assertions.assertTrue(json.contains("\"response\":\"{{sample.output_text}}\""));
    }

    @Test
    public void toTestingCriterionRequiresEvaluator() {
        Assertions.assertThrows(NullPointerException.class, () -> EvaluationsHelper.toTestingCriterion(null));
    }
}

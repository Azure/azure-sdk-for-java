// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects.models;

import com.azure.ai.agents.models.EvaluationModelConfiguration;
import com.azure.ai.agents.models.UserConversationSimulationTestCase;
import com.azure.core.util.BinaryData;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.fasterxml.jackson.databind.JsonNode;
import com.openai.core.ObjectMappers;
import com.openai.models.evals.runs.CreateEvalCompletionsRunDataSource;
import com.openai.models.evals.runs.CreateEvalCompletionsRunDataSource.InputMessages.ItemReference;
import com.openai.models.evals.runs.CreateEvalJsonlRunDataSource;
import com.openai.models.evals.runs.CreateEvalJsonlRunDataSource.Source;
import com.openai.models.evals.runs.CreateEvalJsonlRunDataSource.Source.FileContent;
import com.openai.models.evals.runs.CreateEvalJsonlRunDataSource.Source.FileId;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EvaluationSourceSerializationTests {
    private static final String FILE_CONTENT = "{\"type\":\"file_content\",\"content\":["
        + "{\"item\":{\"query\":\"hello\"},\"sample\":{\"output\":\"world\"},\"future_content\":true}],"
        + "\"future_source\":\"preserved\"}";
    private static final String FILE_ID = "{\"type\":\"file_id\",\"id\":\"file-1\",\"future_source\":\"preserved\"}";

    @Test
    void polymorphicBaseConstructorsAreProtected() throws NoSuchMethodException {
        for (Class<?> type : new Class<?>[] {
            EvaluationRunDataSource.class,
            ItemGenerationParameters.class,
            TraceSource.class }) {
            assertTrue(Modifier.isProtected(type.getDeclaredConstructor().getModifiers()), type.getSimpleName());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = { "red_team_seed_prompts", "red_team_taxonomy" })
    void fileContentRoundTripsThroughPolymorphicParent(String type) throws IOException {
        FileContent source = ObjectMappers.jsonMapper().readValue(FILE_CONTENT, FileContent.class);
        ItemGenerationParameters original = "red_team_seed_prompts".equals(type)
            ? new RedTeamSeedPromptsItemGenerationParameters(Collections.emptyList(), 7, source)
            : new RedTeamTaxonomyItemGenerationParameters(Collections.emptyList(), 7, source);

        BinaryData serialized = BinaryData.fromObject(original);

        assertEquals(ObjectMappers.jsonMapper().readTree(FILE_CONTENT),
            ObjectMappers.jsonMapper().readTree(serialized.toString()).get("source"));
        assertEquals(7, ObjectMappers.jsonMapper().readTree(serialized.toString()).get("num_turns").asInt());

        ItemGenerationParameters restored = serialized.toObject(ItemGenerationParameters.class);
        assertEquals(original.getClass(), restored.getClass());
        FileContent restoredSource = restored instanceof RedTeamSeedPromptsItemGenerationParameters
            ? ((RedTeamSeedPromptsItemGenerationParameters) restored).getSource()
            : ((RedTeamTaxonomyItemGenerationParameters) restored).getSource();
        assertEquals(source, restoredSource);
        int numberOfTurns = restored instanceof RedTeamSeedPromptsItemGenerationParameters
            ? ((RedTeamSeedPromptsItemGenerationParameters) restored).getNumberOfTurns()
            : ((RedTeamTaxonomyItemGenerationParameters) restored).getNumberOfTurns();
        assertEquals(7, numberOfTurns);
        assertEquals(serialized.toString(), BinaryData.fromObject(restored).toString());
    }

    @ParameterizedTest
    @ValueSource(strings = { "red_team_seed_prompts", "red_team_taxonomy" })
    void fileContentPreservesNullAndAbsentSources(String type) {
        for (String sourceField : Arrays.asList("", ",\"source\":null")) {
            String json = "{\"type\":\"" + type + "\",\"attack_strategies\":[],\"num_turns\":20" + sourceField + "}";
            ItemGenerationParameters restored = BinaryData.fromString(json).toObject(ItemGenerationParameters.class);
            FileContent source = restored instanceof RedTeamSeedPromptsItemGenerationParameters
                ? ((RedTeamSeedPromptsItemGenerationParameters) restored).getSource()
                : ((RedTeamTaxonomyItemGenerationParameters) restored).getSource();

            assertNull(source);
            assertEquals(BinaryData.fromObject(restored).toString(),
                BinaryData.fromObject(BinaryData.fromObject(restored).toObject(ItemGenerationParameters.class))
                    .toString());
        }
    }

    @Test
    void fileIdsRoundTripWithNullListElements() throws IOException {
        FileId file = FileId.builder().id("file-1").build();
        SyntheticDataGenerationItemGenerationParameters original
            = new SyntheticDataGenerationItemGenerationParameters(3, "deployment", Arrays.asList(file, null))
                .setPrompt("Generate examples")
                .setOutputDatasetName("examples");

        BinaryData serialized = BinaryData.fromObject(original);
        JsonNode sources = ObjectMappers.jsonMapper().readTree(serialized.toString()).get("sources");
        assertEquals("file_id", sources.get(0).get("type").asText());
        assertEquals("file-1", sources.get(0).get("id").asText());
        assertTrue(sources.get(1).isNull());

        ItemGenerationParameters restored = serialized.toObject(ItemGenerationParameters.class);
        assertEquals(SyntheticDataGenerationItemGenerationParameters.class, restored.getClass());
        assertEquals(original.getSources(), ((SyntheticDataGenerationItemGenerationParameters) restored).getSources());
        assertEquals(serialized.toString(), BinaryData.fromObject(restored).toString());
    }

    @ParameterizedTest
    @ValueSource(strings = { "", ",\"sources\":null", ",\"sources\":[]" })
    void fileIdsPreserveAbsentNullAndEmptyLists(String sourcesField) {
        String json = "{\"type\":\"synthetic_data_gen\",\"samples_count\":3,"
            + "\"model_deployment_name\":\"deployment\"" + sourcesField + "}";
        SyntheticDataGenerationItemGenerationParameters restored
            = BinaryData.fromString(json).toObject(SyntheticDataGenerationItemGenerationParameters.class);

        if (sourcesField.endsWith("[]")) {
            assertTrue(restored.getSources().isEmpty());
        } else {
            assertNull(restored.getSources());
        }
        SyntheticDataGenerationItemGenerationParameters roundTripped
            = BinaryData.fromObject(restored).toObject(SyntheticDataGenerationItemGenerationParameters.class);
        assertEquals(restored.getSources(), roundTripped.getSources());
    }

    @ParameterizedTest
    @ValueSource(strings = { FILE_CONTENT, FILE_ID })
    void targetCompletionSourceAndItemReferenceRoundTripThroughPolymorphicParent(String sourceJson) throws IOException {
        ItemReference inputMessages = ObjectMappers.jsonMapper()
            .readValue("{\"type\":\"item_reference\",\"item_reference\":\"item.messages\",\"future_setting\":true}",
                ItemReference.class);
        Source source = ObjectMappers.jsonMapper().readValue(sourceJson, Source.class);
        TargetCompletionEvaluationRunDataSource original
            = new TargetCompletionEvaluationRunDataSource(source, new AzureAIModelTarget().setModel("deployment"))
                .setInputMessages(inputMessages)
                .setItemGenerationParameters(new TargetCompletionItemGenerationParameters().setMaxConcurrency(3));

        BinaryData serialized = BinaryData.fromObject(original);
        JsonNode json = ObjectMappers.jsonMapper().readTree(serialized.toString());
        assertEquals(ObjectMappers.jsonMapper().readTree(sourceJson), json.get("source"));
        assertEquals("item_reference", json.get("input_messages").get("type").asText());
        assertEquals("item.messages", json.get("input_messages").get("item_reference").asText());
        assertTrue(json.get("input_messages").get("future_setting").asBoolean());
        assertEquals("target_completion", json.get("item_generation_params").get("type").asText());
        assertEquals(3, json.get("item_generation_params").get("max_concurrency").asInt());

        EvaluationRunDataSource restored = serialized.toObject(EvaluationRunDataSource.class);
        assertEquals(TargetCompletionEvaluationRunDataSource.class, restored.getClass());
        assertEquals(source, ((TargetCompletionEvaluationRunDataSource) restored).getSource());
        assertEquals(inputMessages, ((TargetCompletionEvaluationRunDataSource) restored).getInputMessages());
        assertEquals(TargetCompletionItemGenerationParameters.class,
            ((TargetCompletionEvaluationRunDataSource) restored).getItemGenerationParameters().getClass());
        assertEquals(serialized.toString(), BinaryData.fromObject(restored).toString());
    }

    @ParameterizedTest
    @ValueSource(strings = { FILE_CONTENT, FILE_ID })
    void responseRetrievalSourceRoundTripsThroughPolymorphicParent(String sourceJson) throws IOException {
        Source source = ObjectMappers.jsonMapper().readValue(sourceJson, Source.class);
        ResponseRetrievalItemGenerationParameters original = new ResponseRetrievalItemGenerationParameters(7,
            Collections.singletonMap("response_id", "{{item.resp_id}}"), source);

        BinaryData serialized = BinaryData.fromObject(original);
        JsonNode json = ObjectMappers.jsonMapper().readTree(serialized.toString());
        assertEquals(ObjectMappers.jsonMapper().readTree(sourceJson), json.get("source"));
        assertEquals(7, json.get("max_num_turns").asInt());
        assertEquals("{{item.resp_id}}", json.get("data_mapping").get("response_id").asText());

        ItemGenerationParameters restored = serialized.toObject(ItemGenerationParameters.class);
        assertEquals(ResponseRetrievalItemGenerationParameters.class, restored.getClass());
        assertEquals(source, ((ResponseRetrievalItemGenerationParameters) restored).getSource());
        assertEquals(serialized.toString(), BinaryData.fromObject(restored).toString());
    }

    @ParameterizedTest
    @ValueSource(strings = { "", ",\"source\":null" })
    void unionSourcesPreserveNullAndAbsentValues(String sourceField) {
        ResponseRetrievalItemGenerationParameters parameters = BinaryData
            .fromString("{\"type\":\"response_retrieval\",\"max_num_turns\":7,\"data_mapping\":{}" + sourceField + "}")
            .toObject(ResponseRetrievalItemGenerationParameters.class);
        TargetCompletionEvaluationRunDataSource dataSource = BinaryData.fromString(
            "{\"type\":\"azure_ai_target_completions\",\"target\":{\"type\":\"azure_ai_model\"}" + sourceField + "}")
            .toObject(TargetCompletionEvaluationRunDataSource.class);

        assertNull(parameters.getSource());
        assertNull(dataSource.getSource());
        assertNull(
            BinaryData.fromObject(parameters).toObject(ResponseRetrievalItemGenerationParameters.class).getSource());
        assertNull(
            BinaryData.fromObject(dataSource).toObject(TargetCompletionEvaluationRunDataSource.class).getSource());
    }

    @ParameterizedTest
    @ValueSource(strings = { "", ",\"input_messages\":null" })
    void itemReferencePreservesNullAndAbsentValues(String inputMessagesField) {
        String json = "{\"type\":\"azure_ai_target_completions\",\"source\":{\"type\":\"file_id\",\"id\":\"file-1\"},"
            + "\"target\":{\"type\":\"azure_ai_model\",\"model\":\"deployment\"}" + inputMessagesField + "}";
        TargetCompletionEvaluationRunDataSource restored
            = BinaryData.fromString(json).toObject(TargetCompletionEvaluationRunDataSource.class);

        assertNull(restored.getInputMessages());
        assertNull(
            BinaryData.fromObject(restored).toObject(TargetCompletionEvaluationRunDataSource.class).getInputMessages());
    }

    @ParameterizedTest
    @ValueSource(strings = { "jsonl", "completions" })
    void datasetUnionPreservesExternalSources(String type) throws IOException {
        String sourceJson = "{\"source\":" + FILE_ID + ",\"type\":\"" + type
            + "\",\"model\":\"deployment\",\"input_messages\":{\"type\":\"item_reference\","
            + "\"item_reference\":\"item.messages\"},\"future_dataset\":true}";
        DatasetEvaluationSource source = "jsonl".equals(type)
            ? DatasetEvaluationSource
                .of(ObjectMappers.jsonMapper().readValue(sourceJson, CreateEvalJsonlRunDataSource.class))
            : DatasetEvaluationSource
                .of(ObjectMappers.jsonMapper().readValue(sourceJson, CreateEvalCompletionsRunDataSource.class));
        DatasetEvaluationRuleAction original = new DatasetEvaluationRuleAction("eval-1", source);

        BinaryData serialized = BinaryData.fromObject(original);
        assertEquals(ObjectMappers.jsonMapper().readTree(sourceJson),
            ObjectMappers.jsonMapper().readTree(serialized.toString()).get("source"));

        EvaluationRuleAction restored = serialized.toObject(EvaluationRuleAction.class);
        DatasetEvaluationSource restoredSource = ((DatasetEvaluationRuleAction) restored).getSource();
        if ("jsonl".equals(type)) {
            assertEquals(source.getJsonl(), restoredSource.getJsonl());
            assertNull(restoredSource.getCompletions());
        } else {
            assertEquals(source.getCompletions(), restoredSource.getCompletions());
            assertNull(restoredSource.getJsonl());
        }
        assertEquals(ObjectMappers.jsonMapper().readTree(serialized.toString()),
            ObjectMappers.jsonMapper().readTree(BinaryData.fromObject(restored).toString()));
    }

    @Test
    void datasetUnionPreservesCsvAndTargetCompletionSources() {
        CsvEvaluationRunDataSource csv = new CsvEvaluationRunDataSource(new EvaluationCsvFileIdSource("csv-file"));
        DatasetEvaluationRuleAction csvAction
            = new DatasetEvaluationRuleAction("eval-1", DatasetEvaluationSource.of(csv));
        DatasetEvaluationRuleAction restoredCsv
            = BinaryData.fromObject(csvAction).toObject(DatasetEvaluationRuleAction.class);
        assertEquals("csv-file", restoredCsv.getSource().getCsv().getSource().getId());
        assertNull(restoredCsv.getSource().getTargetCompletions());

        TargetCompletionEvaluationRunDataSource target
            = new TargetCompletionEvaluationRunDataSource(Source.ofFileId(FileId.builder().id("target-file").build()),
                new AzureAIModelTarget().setModel("deployment"));
        DatasetEvaluationRuleAction targetAction
            = new DatasetEvaluationRuleAction("eval-1", DatasetEvaluationSource.of(target));
        DatasetEvaluationRuleAction restoredTarget
            = BinaryData.fromObject(targetAction).toObject(DatasetEvaluationRuleAction.class);
        assertEquals(target.getSource(), restoredTarget.getSource().getTargetCompletions().getSource());
        assertNull(restoredTarget.getSource().getCsv());
    }

    @ParameterizedTest
    @ValueSource(booleans = { false, true })
    void simulationUnionPreservesExternalContentForBothDataSources(boolean preview) throws IOException {
        FileContent content = ObjectMappers.jsonMapper().readValue(FILE_CONTENT, FileContent.class);
        UserConversationSimulationSource source = UserConversationSimulationSource.of(content);
        Target target = new AzureAIModelTarget().setModel("target-model");
        EvaluationModelConfiguration model = new EvaluationModelConfiguration("simulation-model");
        EvaluationRunDataSource original = preview
            ? new UserConversationSimulationPreviewEvaluationRunDataSource(source, target, model)
            : new UserConversationSimulationEvaluationRunDataSource(source, target, model);

        BinaryData serialized = BinaryData.fromObject(original);
        assertEquals(ObjectMappers.jsonMapper().readTree(FILE_CONTENT),
            ObjectMappers.jsonMapper().readTree(serialized.toString()).get("source"));
        EvaluationRunDataSource restored = serialized.toObject(EvaluationRunDataSource.class);
        UserConversationSimulationSource restoredSource = preview
            ? ((UserConversationSimulationPreviewEvaluationRunDataSource) restored).getSource()
            : ((UserConversationSimulationEvaluationRunDataSource) restored).getSource();
        assertEquals(content, restoredSource.getFileContent());
        assertNull(restoredSource.getInlineTestCases());
    }

    @Test
    void datasetUnionPreservesStandardAndPreviewSimulationVariants() {
        Target target = new AzureAIModelTarget().setModel("target-model");
        EvaluationModelConfiguration model = new EvaluationModelConfiguration("simulation-model");
        UserConversationSimulationSource fileSource
            = UserConversationSimulationSource.of(FileId.builder().id("simulation-file").build());
        DatasetEvaluationRuleAction standard = new DatasetEvaluationRuleAction("eval-1", DatasetEvaluationSource
            .of(new UserConversationSimulationEvaluationRunDataSource(fileSource, target, model)));
        DatasetEvaluationRuleAction restoredStandard
            = BinaryData.fromObject(standard).toObject(DatasetEvaluationRuleAction.class);
        assertEquals("simulation-file",
            restoredStandard.getSource().getUserConversationSimulation().getSource().getFileId().id());
        assertNull(restoredStandard.getSource().getUserConversationSimulationPreview());

        UserConversationSimulationSource inlineSource = UserConversationSimulationSource
            .of(new UserConversationSimulationInlineTestCasesSource(Collections.singletonList(
                new UserConversationSimulationTestCase().setTestCaseDescription("Ask about the weather"))));
        DatasetEvaluationRuleAction preview = new DatasetEvaluationRuleAction("eval-1", DatasetEvaluationSource
            .of(new UserConversationSimulationPreviewEvaluationRunDataSource(inlineSource, target, model)));
        DatasetEvaluationRuleAction restoredPreview
            = BinaryData.fromObject(preview).toObject(DatasetEvaluationRuleAction.class);
        assertEquals("Ask about the weather",
            restoredPreview.getSource()
                .getUserConversationSimulationPreview()
                .getSource()
                .getInlineTestCases()
                .getTestCases()
                .get(0)
                .getTestCaseDescription());
        assertNull(restoredPreview.getSource().getUserConversationSimulation());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "azure_ai_traces",
            "azure_ai_trace_data_source",
            "azure_ai_traces_preview",
            "azure_ai_trace_data_source_preview" })
    void traceUnionDispatchesStandardAndPreviewSources(String type) throws IOException {
        String json = "{\"evalId\":\"eval-1\",\"source\":{\"trace_source\":{\"type\":\"trace_id_source\","
            + "\"trace_ids\":[\"trace-1\"]},\"agent_name\":\"agent-1\",\"type\":\"" + type
            + "\"},\"type\":\"traceEvaluation\"}";
        TraceEvaluationRuleAction restored
            = (TraceEvaluationRuleAction) BinaryData.fromString(json).toObject(EvaluationRuleAction.class);
        TraceEvaluationSource source = restored.getSource();
        TraceEvaluationSource reconstructed;
        if ("azure_ai_traces".equals(type)) {
            assertEquals("agent-1", source.getTraces().getAgentName());
            assertNull(source.getTraceSource());
            reconstructed = TraceEvaluationSource.of(source.getTraces());
        } else if ("azure_ai_trace_data_source".equals(type)) {
            assertNotNull(source.getTraceSource().getTraceSource());
            assertNull(source.getTraces());
            reconstructed = TraceEvaluationSource.of(source.getTraceSource());
        } else if ("azure_ai_traces_preview".equals(type)) {
            assertEquals("agent-1", source.getTracesPreview().getAgentName());
            assertNull(source.getTraceSourcePreview());
            reconstructed = TraceEvaluationSource.of(source.getTracesPreview());
        } else {
            assertNotNull(source.getTraceSourcePreview().getTraceSource());
            assertNull(source.getTracesPreview());
            reconstructed = TraceEvaluationSource.of(source.getTraceSourcePreview());
        }
        JsonNode serialized = ObjectMappers.jsonMapper()
            .readTree(BinaryData.fromObject(new TraceEvaluationRuleAction("eval-1", reconstructed)).toString());
        assertEquals(type, serialized.get("source").get("type").asText());
        if ("azure_ai_trace_data_source".equals(type) || "azure_ai_trace_data_source_preview".equals(type)) {
            assertEquals("trace-1", serialized.get("source").get("trace_source").get("trace_ids").get(0).asText());
        }
        assertEquals(ObjectMappers.jsonMapper().readTree(BinaryData.fromObject(source).toString()),
            serialized.get("source"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "{}", "{\"type\":\"unsupported\"}" })
    void evaluationUnionsRejectMissingAndUnsupportedTypes(String json) throws IOException {
        try (JsonReader datasetReader = JsonProviders.createReader(json);
            JsonReader traceReader = JsonProviders.createReader(json);
            JsonReader simulationReader = JsonProviders.createReader(json)) {
            assertThrows(IllegalStateException.class, () -> DatasetEvaluationSource.fromJson(datasetReader));
            assertThrows(IllegalStateException.class, () -> TraceEvaluationSource.fromJson(traceReader));
            assertThrows(IllegalStateException.class,
                () -> UserConversationSimulationSource.fromJson(simulationReader));
        }
    }
}

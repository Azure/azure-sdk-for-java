// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects.models;

import com.azure.ai.projects.implementation.OpenAIJsonHelper;
import com.azure.core.util.BinaryData;
import com.fasterxml.jackson.databind.JsonNode;
import com.openai.core.ObjectMappers;
import com.openai.models.evals.runs.CreateEvalCompletionsRunDataSource.InputMessages.ItemReference;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

class EvaluationSourceSerializationTests {
    private static final String FILE_CONTENT = "{\"type\":\"file_content\",\"content\":["
        + "{\"item\":{\"query\":\"hello\"},\"sample\":{\"output\":\"world\"},\"future_content\":true}],"
        + "\"future_source\":\"preserved\"}";

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

    @Test
    void itemReferenceRoundTripsThroughPolymorphicParent() throws IOException {
        ItemReference inputMessages = ObjectMappers.jsonMapper()
            .readValue("{\"type\":\"item_reference\",\"item_reference\":\"item.messages\",\"future_setting\":true}",
                ItemReference.class);
        BinaryData source = OpenAIJsonHelper.toBinaryData(FileId.builder().id("file-1").build());
        TargetCompletionEvaluationRunDataSource original
            = new TargetCompletionEvaluationRunDataSource(source, new AzureAIModelTarget().setModel("deployment"))
                .setInputMessages(inputMessages)
                .setItemGenerationParameters(new TargetCompletionItemGenerationParameters().setMaxConcurrency(3));

        BinaryData serialized = BinaryData.fromObject(original);
        JsonNode json = ObjectMappers.jsonMapper().readTree(serialized.toString());
        assertEquals(ObjectMappers.jsonMapper().readTree(source.toString()), json.get("source"));
        assertEquals("item_reference", json.get("input_messages").get("type").asText());
        assertEquals("item.messages", json.get("input_messages").get("item_reference").asText());
        assertTrue(json.get("input_messages").get("future_setting").asBoolean());
        assertEquals("target_completion", json.get("item_generation_params").get("type").asText());
        assertEquals(3, json.get("item_generation_params").get("max_concurrency").asInt());

        EvaluationRunDataSource restored = serialized.toObject(EvaluationRunDataSource.class);
        assertEquals(TargetCompletionEvaluationRunDataSource.class, restored.getClass());
        assertEquals(inputMessages, ((TargetCompletionEvaluationRunDataSource) restored).getInputMessages());
        assertEquals(TargetCompletionItemGenerationParameters.class,
            ((TargetCompletionEvaluationRunDataSource) restored).getItemGenerationParameters().getClass());
        assertEquals(serialized.toString(), BinaryData.fromObject(restored).toString());
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
}

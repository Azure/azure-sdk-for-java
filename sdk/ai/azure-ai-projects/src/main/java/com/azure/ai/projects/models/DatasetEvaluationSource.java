// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects.models;

import com.azure.ai.projects.implementation.OpenAIJsonHelper;
import com.azure.ai.projects.implementation.utils.Beta;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.openai.models.evals.runs.CreateEvalCompletionsRunDataSource;
import com.openai.models.evals.runs.CreateEvalJsonlRunDataSource;
import java.io.IOException;
import java.util.Objects;

/**
 * Dataset inputs for a scheduled evaluation. Use the overloaded {@code of(...)} factories to select a source.
 * Preview sources may appear in responses for existing schedules but are rejected on create and update.
 */
@Immutable
public final class DatasetEvaluationSource implements JsonSerializable<DatasetEvaluationSource> {
    private static final ClientLogger LOGGER = new ClientLogger(DatasetEvaluationSource.class);

    private final Object value;

    private DatasetEvaluationSource(Object value) {
        this.value = Objects.requireNonNull(value, "'value' cannot be null.");
    }

    /**
     * Creates a source containing JSONL inputs.
     * @param source the JSONL source.
     * @return the dataset evaluation source.
     * @throws NullPointerException if {@code source} is null.
     */
    public static DatasetEvaluationSource of(CreateEvalJsonlRunDataSource source) {
        return new DatasetEvaluationSource(source);
    }

    /**
     * Creates a source containing CSV inputs.
     * @param source the CSV source.
     * @return the dataset evaluation source.
     * @throws NullPointerException if {@code source} is null.
     */
    public static DatasetEvaluationSource of(CsvEvaluationRunDataSource source) {
        return new DatasetEvaluationSource(source);
    }

    /**
     * Creates a source that generates model completions before scoring.
     * @param source the completions source.
     * @return the dataset evaluation source.
     * @throws NullPointerException if {@code source} is null.
     */
    public static DatasetEvaluationSource of(CreateEvalCompletionsRunDataSource source) {
        return new DatasetEvaluationSource(source);
    }

    /**
     * Creates a source that generates evaluation-target responses before scoring.
     * @param source the target-completion source.
     * @return the dataset evaluation source.
     * @throws NullPointerException if {@code source} is null.
     */
    public static DatasetEvaluationSource of(TargetCompletionEvaluationRunDataSource source) {
        return new DatasetEvaluationSource(source);
    }

    /**
     * Creates a source containing simulated user conversations.
     * @param source the user-conversation simulation source.
     * @return the dataset evaluation source.
     * @throws NullPointerException if {@code source} is null.
     */
    public static DatasetEvaluationSource of(UserConversationSimulationEvaluationRunDataSource source) {
        return new DatasetEvaluationSource(source);
    }

    /**
     * Creates a response-only source containing preview simulated user conversations.
     * @param source the preview user-conversation simulation source.
     * @return the dataset evaluation source.
     * @throws NullPointerException if {@code source} is null.
     */
    @Beta(warningText = "Preview API. Evaluations=V1Preview")
    public static DatasetEvaluationSource of(UserConversationSimulationPreviewEvaluationRunDataSource source) {
        return new DatasetEvaluationSource(source);
    }

    /**
     * Gets the JSONL variant.
     * @return the JSONL source, or null if this source contains another variant.
     */
    public CreateEvalJsonlRunDataSource getJsonl() {
        return value instanceof CreateEvalJsonlRunDataSource ? (CreateEvalJsonlRunDataSource) value : null;
    }

    /**
     * Gets the CSV variant.
     * @return the CSV source, or null if this source contains another variant.
     */
    public CsvEvaluationRunDataSource getCsv() {
        return value instanceof CsvEvaluationRunDataSource ? (CsvEvaluationRunDataSource) value : null;
    }

    /**
     * Gets the completions variant.
     * @return the completions source, or null if this source contains another variant.
     */
    public CreateEvalCompletionsRunDataSource getCompletions() {
        return value instanceof CreateEvalCompletionsRunDataSource ? (CreateEvalCompletionsRunDataSource) value : null;
    }

    /**
     * Gets the target-completion variant.
     * @return the target-completion source, or null if this source contains another variant.
     */
    public TargetCompletionEvaluationRunDataSource getTargetCompletions() {
        return value instanceof TargetCompletionEvaluationRunDataSource
            ? (TargetCompletionEvaluationRunDataSource) value
            : null;
    }

    /**
     * Gets the user-conversation simulation variant.
     * @return the simulation source, or null if this source contains another variant.
     */
    public UserConversationSimulationEvaluationRunDataSource getUserConversationSimulation() {
        return value instanceof UserConversationSimulationEvaluationRunDataSource
            ? (UserConversationSimulationEvaluationRunDataSource) value
            : null;
    }

    /**
     * Gets the response-only preview user-conversation simulation variant.
     * @return the preview simulation source, or null if this source contains another variant.
     */
    @Beta(warningText = "Preview API. Evaluations=V1Preview")
    public UserConversationSimulationPreviewEvaluationRunDataSource getUserConversationSimulationPreview() {
        return value instanceof UserConversationSimulationPreviewEvaluationRunDataSource
            ? (UserConversationSimulationPreviewEvaluationRunDataSource) value
            : null;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        if (value instanceof JsonSerializable<?>) {
            return ((JsonSerializable<?>) value).toJson(jsonWriter);
        }
        OpenAIJsonHelper.toBinaryData(value).writeTo(jsonWriter);
        return jsonWriter;
    }

    /**
     * Reads a dataset evaluation source using the {@code type} discriminator.
     * @param jsonReader the JSON reader.
     * @return the source, or null for JSON null.
     * @throws IOException if the JSON cannot be read.
     * @throws IllegalStateException if the source type is missing or unsupported.
     */
    public static DatasetEvaluationSource fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            try (JsonReader bufferedReader = reader.bufferObject()) {
                String type = null;
                bufferedReader.nextToken();
                while (bufferedReader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = bufferedReader.getFieldName();
                    bufferedReader.nextToken();
                    if ("type".equals(fieldName)) {
                        type = bufferedReader.getString();
                        break;
                    }
                    bufferedReader.skipChildren();
                }
                if ("jsonl".equals(type) || "completions".equals(type)) {
                    try (JsonReader sourceReader = bufferedReader.reset()) {
                        sourceReader.nextToken();
                        BinaryData data = BinaryData.fromObject(sourceReader.readUntyped());
                        return "jsonl".equals(type)
                            ? of(OpenAIJsonHelper.fromBinaryData(data, CreateEvalJsonlRunDataSource.class))
                            : of(OpenAIJsonHelper.fromBinaryData(data, CreateEvalCompletionsRunDataSource.class));
                    }
                } else if ("csv".equals(type)) {
                    return of(CsvEvaluationRunDataSource.fromJson(bufferedReader.reset()));
                } else if ("azure_ai_target_completions".equals(type)) {
                    return of(TargetCompletionEvaluationRunDataSource.fromJson(bufferedReader.reset()));
                } else if ("azure_ai_user_conversation_simulation".equals(type)) {
                    return of(UserConversationSimulationEvaluationRunDataSource.fromJson(bufferedReader.reset()));
                } else if ("azure_ai_user_conversation_simulation_preview".equals(type)) {
                    return of(
                        UserConversationSimulationPreviewEvaluationRunDataSource.fromJson(bufferedReader.reset()));
                }
                throw LOGGER.logExceptionAsError(
                    new IllegalStateException("Unsupported dataset evaluation source type: " + type));
            }
        });
    }
}

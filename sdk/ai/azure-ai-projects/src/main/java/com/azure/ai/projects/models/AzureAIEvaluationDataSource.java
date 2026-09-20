// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects.models;

import com.azure.ai.projects.implementation.OpenAIJsonHelper;
import com.azure.core.annotation.Fluent;
import com.azure.core.util.BinaryData;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;
import com.openai.models.evals.runs.CreateEvalCompletionsRunDataSource;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/** Azure-specific evaluation run data sources, convertible with {@code EvaluationsHelper.toDataSource}. */
@Fluent
public final class AzureAIEvaluationDataSource implements JsonSerializable<AzureAIEvaluationDataSource> {
    private static final com.azure.core.util.logging.ClientLogger LOGGER
        = new com.azure.core.util.logging.ClientLogger(AzureAIEvaluationDataSource.class);
    private final Map<String, Object> properties = new LinkedHashMap<>();

    private AzureAIEvaluationDataSource(String type) {
        properties.put("type", type);
    }

    /**
     * Gets the wire discriminator.
     * @return the wire discriminator.
     */
    public String getType() {
        return (String) properties.get("type");
    }

    /**
     * Creates a CSV file data source.
     * @param fileId uploaded CSV file ID.
     * @return a CSV data source.
     */
    public static AzureAIEvaluationDataSource csv(String fileId) {
        Map<String, Object> source = new LinkedHashMap<>();
        source.put("type", "file_id");
        source.put("id", Objects.requireNonNull(fileId, "fileId"));
        AzureAIEvaluationDataSource result = new AzureAIEvaluationDataSource("csv");
        result.properties.put("source", source);
        return result;
    }

    /**
     * Creates a target-completion data source.
     * @param source native inline or file-ID source.
     * @param target model or agent target.
     * @param inputMessages native input-message configuration.
     * @return the target-completion data source.
     */
    public static AzureAIEvaluationDataSource targetCompletions(CreateEvalCompletionsRunDataSource.Source source,
        Target target, CreateEvalCompletionsRunDataSource.InputMessages inputMessages) {
        AzureAIEvaluationDataSource result = new AzureAIEvaluationDataSource("azure_ai_target_completions");
        result.properties.put("source", nativeValue(Objects.requireNonNull(source, "source")));
        result.properties.put("target", azureValue(Objects.requireNonNull(target, "target")));
        return result.setInputMessages(Objects.requireNonNull(inputMessages, "inputMessages"));
    }

    /**
     * Creates a continuous-response retrieval data source.
     * @param source native inline or file-ID source.
     * @param dataMapping source-field mapping including response ID.
     * @return the response-retrieval data source.
     */
    public static AzureAIEvaluationDataSource responses(CreateEvalCompletionsRunDataSource.Source source,
        Map<String, String> dataMapping) {
        AzureAIEvaluationDataSource result = new AzureAIEvaluationDataSource("azure_ai_responses");
        Map<String, Object> generation = new LinkedHashMap<>();
        generation.put("type", "response_retrieval");
        generation.put("source", nativeValue(Objects.requireNonNull(source, "source")));
        generation.put("data_mapping", new LinkedHashMap<>(Objects.requireNonNull(dataMapping, "dataMapping")));
        result.properties.put("item_generation_params", generation);
        return result;
    }

    /**
     * Creates a benchmark data source. Model sampling parameters must be omitted for benchmark targets.
     * @param target model or agent target.
     * @return the benchmark data source.
     */
    public static AzureAIEvaluationDataSource benchmark(Target target) {
        AzureAIEvaluationDataSource result = new AzureAIEvaluationDataSource("azure_ai_benchmark_preview");
        result.properties.put("target", azureValue(Objects.requireNonNull(target, "target")));
        return result;
    }

    /**
     * Creates a red-team data source.
     * @param itemGenerationParams JSON item-generation settings.
     * @param target model or agent target.
     * @return the red-team data source.
     */
    public static AzureAIEvaluationDataSource redTeam(BinaryData itemGenerationParams, Target target) {
        AzureAIEvaluationDataSource result = new AzureAIEvaluationDataSource("azure_ai_red_team");
        result.properties.put("item_generation_params",
            Objects.requireNonNull(itemGenerationParams, "itemGenerationParams").toObject(Map.class));
        result.properties.put("target", azureValue(Objects.requireNonNull(target, "target")));
        return result;
    }

    /**
     * Creates a traces-preview data source.
     * @return a traces-preview data source with service-default query settings.
     */
    public static AzureAIEvaluationDataSource traces() {
        return new AzureAIEvaluationDataSource("azure_ai_traces_preview");
    }

    /**
    * Sets the input-message configuration.
    * @param value input messages for target completions or benchmarks.
     * @return this source.
     */
    public AzureAIEvaluationDataSource setInputMessages(CreateEvalCompletionsRunDataSource.InputMessages value) {
        requireType("azure_ai_target_completions", "azure_ai_benchmark_preview");
        put("input_messages", value == null ? null : nativeValue(value));
        return this;
    }

    /**
    * Sets the maximum retrieved conversation turns for response evaluation.
    * @param value maximum retrieved conversation turns.
     * @return this source.
     */
    public AzureAIEvaluationDataSource setMaxNumTurns(Integer value) {
        requireType("azure_ai_responses");
        Map<?, ?> generation = (Map<?, ?>) properties.get("item_generation_params");
        Map<String, Object> updated = new LinkedHashMap<>();
        generation.forEach((name, setting) -> updated.put(name.toString(), setting));
        if (value == null) {
            updated.remove("max_num_turns");
        } else {
            updated.put("max_num_turns", value);
        }
        properties.put("item_generation_params", updated);
        return this;
    }

    /**
    * Sets the hourly response-evaluation run limit.
    * @param value hourly run limit for response evaluation.
     * @return this source.
     */
    public AzureAIEvaluationDataSource setMaxRunsHourly(Integer value) {
        requireType("azure_ai_responses");
        put("max_runs_hourly", value);
        return this;
    }

    /**
    * Sets the response event configuration ID.
    * @param value response event configuration ID.
     * @return this source.
     */
    public AzureAIEvaluationDataSource setEventConfigurationId(String value) {
        requireType("azure_ai_responses");
        put("event_configuration_id", value);
        return this;
    }

    /**
    * Sets the trace IDs to evaluate.
    * @param value trace IDs to evaluate.
     * @return this source.
     */
    public AzureAIEvaluationDataSource setTraceIds(List<String> value) {
        requireType("azure_ai_traces_preview");
        put("trace_ids", value == null ? null : new java.util.ArrayList<>(value));
        return this;
    }

    /**
    * Sets the agent ID for trace filtering.
    * @param value agent ID for trace filtering.
     * @return this source.
     */
    public AzureAIEvaluationDataSource setAgentId(String value) {
        requireType("azure_ai_traces_preview");
        put("agent_id", value);
        return this;
    }

    /**
    * Sets the agent name for trace filtering.
    * @param value agent name for trace filtering.
     * @return this source.
     */
    public AzureAIEvaluationDataSource setAgentName(String value) {
        requireType("azure_ai_traces_preview");
        put("agent_name", value);
        return this;
    }

    /**
    * Sets the trace lookback window.
    * @param value trace lookback window in hours.
     * @return this source.
     */
    public AzureAIEvaluationDataSource setLookbackHours(Integer value) {
        requireType("azure_ai_traces_preview");
        put("lookback_hours", value);
        return this;
    }

    /**
    * Sets the end of the trace query window.
    * @param value end of the trace query window, serialized as Unix seconds.
     * @return this source.
     */
    public AzureAIEvaluationDataSource setEndTime(OffsetDateTime value) {
        requireType("azure_ai_traces_preview");
        put("end_time", value == null ? null : value.toEpochSecond());
        return this;
    }

    /**
    * Sets the maximum traces to evaluate.
    * @param value maximum traces to evaluate.
     * @return this source.
     */
    public AzureAIEvaluationDataSource setMaxTraces(Integer value) {
        requireType("azure_ai_traces_preview");
        put("max_traces", value);
        return this;
    }

    /**
    * Sets the trace ingestion delay.
    * @param value trace ingestion delay in seconds.
     * @return this source.
     */
    public AzureAIEvaluationDataSource setIngestionDelaySeconds(Integer value) {
        requireType("azure_ai_traces_preview");
        put("ingestion_delay_seconds", value);
        return this;
    }

    private void requireType(String... types) {
        for (String type : types) {
            if (type.equals(getType())) {
                return;
            }
        }
        throw LOGGER.logExceptionAsError(new IllegalStateException("This option is not supported for " + getType()));
    }

    private void put(String name, Object value) {
        if (value == null) {
            properties.remove(name);
        } else {
            properties.put(name, value);
        }
    }

    private static Object nativeValue(Object value) {
        return OpenAIJsonHelper.toBinaryData(value).toObject(Object.class);
    }

    private static Object azureValue(Target value) {
        return BinaryData.fromObject(value).toObject(Object.class);
    }

    @Override
    public JsonWriter toJson(JsonWriter writer) throws IOException {
        return writer.writeMap(properties, JsonWriter::writeUntyped);
    }

    /**
     * Reads an Azure evaluation source while preserving extension fields.
     * @param reader JSON reader.
     * @return the source, or null for JSON null.
     * @throws IOException if the JSON cannot be read.
     */
    public static AzureAIEvaluationDataSource fromJson(com.azure.json.JsonReader reader) throws IOException {
        return reader.readObject(objectReader -> {
            Map<String, Object> fields = objectReader.readMap(com.azure.json.JsonReader::readUntyped);
            AzureAIEvaluationDataSource source = new AzureAIEvaluationDataSource((String) fields.get("type"));
            source.properties.putAll(fields);
            return source;
        });
    }
}

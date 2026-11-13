// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.projects.models;

import com.azure.ai.projects.implementation.OpenAIJsonHelper;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.openai.models.evals.runs.CreateEvalJsonlRunDataSource.Source.FileContent;
import com.openai.models.evals.runs.CreateEvalJsonlRunDataSource.Source.FileId;
import java.io.IOException;
import java.util.Objects;

/**
 * Test scenarios for user-conversation simulation, shared by the standard and preview evaluation run data sources.
 * Use the typed factories to supply JSONL content, a file ID, or inline test cases.
 */
@Immutable
public final class UserConversationSimulationSource implements JsonSerializable<UserConversationSimulationSource> {
    private static final ClientLogger LOGGER = new ClientLogger(UserConversationSimulationSource.class);

    private final Object value;

    private UserConversationSimulationSource(Object value) {
        this.value = Objects.requireNonNull(value, "'value' cannot be null.");
    }

    /**
     * Creates a source containing JSONL file content.
     * @param source the file content.
     * @return the user-conversation simulation source.
     * @throws NullPointerException if {@code source} is null.
     */
    public static UserConversationSimulationSource ofFileContent(FileContent source) {
        return new UserConversationSimulationSource(source);
    }

    /**
     * Creates a source referencing a JSONL file.
     * @param source the file ID source.
     * @return the user-conversation simulation source.
     * @throws NullPointerException if {@code source} is null.
     */
    public static UserConversationSimulationSource ofFileId(FileId source) {
        return new UserConversationSimulationSource(source);
    }

    /**
     * Creates a source containing inline test cases.
     * @param source the inline test cases.
     * @return the user-conversation simulation source.
     * @throws NullPointerException if {@code source} is null.
     */
    public static UserConversationSimulationSource
        ofInlineTestCases(UserConversationSimulationInlineTestCasesSource source) {
        return new UserConversationSimulationSource(source);
    }

    /**
     * Gets the JSONL file-content variant.
     * @return the file content, or null if this source contains another variant.
     */
    public FileContent getFileContent() {
        return value instanceof FileContent ? (FileContent) value : null;
    }

    /**
     * Gets the JSONL file-ID variant.
     * @return the file ID source, or null if this source contains another variant.
     */
    public FileId getFileId() {
        return value instanceof FileId ? (FileId) value : null;
    }

    /**
     * Gets the inline test-case variant.
     * @return the inline test cases, or null if this source contains another variant.
     */
    public UserConversationSimulationInlineTestCasesSource getInlineTestCases() {
        return value instanceof UserConversationSimulationInlineTestCasesSource
            ? (UserConversationSimulationInlineTestCasesSource) value
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
     * Reads a simulation source using the {@code type} discriminator.
     * @param jsonReader the JSON reader.
     * @return the source, or null for JSON null.
     * @throws IOException if the JSON cannot be read.
     * @throws IllegalStateException if the source type is missing or unsupported.
     */
    public static UserConversationSimulationSource fromJson(JsonReader jsonReader) throws IOException {
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
                if ("file_content".equals(type) || "file_id".equals(type)) {
                    try (JsonReader sourceReader = bufferedReader.reset()) {
                        sourceReader.nextToken();
                        BinaryData data = BinaryData.fromObject(sourceReader.readUntyped());
                        return "file_content".equals(type)
                            ? ofFileContent(OpenAIJsonHelper.fromBinaryData(data, FileContent.class))
                            : ofFileId(OpenAIJsonHelper.fromBinaryData(data, FileId.class));
                    }
                } else if ("inline_user_conversation_simulation".equals(type)) {
                    return ofInlineTestCases(
                        UserConversationSimulationInlineTestCasesSource.fromJson(bufferedReader.reset()));
                }
                throw LOGGER.logExceptionAsError(
                    new IllegalStateException("Unsupported user-conversation simulation source type: " + type));
            }
        });
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.azure.ai.agents.implementation.OpenAIJsonHelper;
import com.azure.ai.agents.implementation.utils.Beta;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;
import com.openai.models.responses.ResponseCreateParams;
import com.openai.models.responses.ToolChoiceFunction;
import com.openai.models.responses.ToolChoiceMcp;
import com.openai.models.responses.ToolChoiceOptions;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * Tool-selection behavior for a voice agent: a mode, a specific function, or a specific MCP tool.
 *
 * <p>Create a choice using {@link #ofMode(ToolChoiceOptions)}, {@link #ofFunction(ToolChoiceFunction)}, or
 * {@link #ofMcp(ToolChoiceMcp)}. Pass {@code null} to a model's {@code setToolChoice} method to clear the property;
 * the factories do not accept {@code null}.</p>
 *
 * <p>Use {@link #getMode()}, {@link #getFunction()}, or {@link #getMcp()} to read the selected variant. Getters for
 * other variants return {@code null}. Unrecognized choices read from JSON are preserved when serialized again;
 * all three getters return {@code null} for an unrecognized choice.</p>
 */
@Immutable
@Beta(warningText = "Preview API. VoiceAgents=V1Preview")
public final class VoiceAgentToolChoice implements JsonSerializable<VoiceAgentToolChoice> {
    private static final ClientLogger LOGGER = new ClientLogger(VoiceAgentToolChoice.class);

    private final ResponseCreateParams.ToolChoice value;

    private final BinaryData unknownValue;

    private VoiceAgentToolChoice(ResponseCreateParams.ToolChoice value) {
        this.value = value;
        this.unknownValue = null;
    }

    private VoiceAgentToolChoice(BinaryData unknownValue) {
        this.value = null;
        this.unknownValue = unknownValue;
    }

    /**
     * Creates a choice using a tool-selection mode, such as {@link ToolChoiceOptions#AUTO}.
     *
     * @param mode the tool-selection mode.
     * @return the tool choice.
     * @throws NullPointerException if {@code mode} is null.
     */
    public static VoiceAgentToolChoice ofMode(ToolChoiceOptions mode) {
        return new VoiceAgentToolChoice(
            ResponseCreateParams.ToolChoice.ofOptions(Objects.requireNonNull(mode, "'mode' cannot be null.")));
    }

    /**
     * Creates a choice that selects a specific function tool.
     *
     * @param function the function tool to select.
     * @return the tool choice.
     * @throws NullPointerException if {@code function} is null.
     */
    public static VoiceAgentToolChoice ofFunction(ToolChoiceFunction function) {
        return new VoiceAgentToolChoice(
            ResponseCreateParams.ToolChoice.ofFunction(Objects.requireNonNull(function, "'function' cannot be null.")));
    }

    /**
     * Creates a choice that selects a specific MCP tool.
     *
     * @param mcp the MCP tool to select.
     * @return the tool choice.
     * @throws NullPointerException if {@code mcp} is null.
     */
    public static VoiceAgentToolChoice ofMcp(ToolChoiceMcp mcp) {
        return new VoiceAgentToolChoice(
            ResponseCreateParams.ToolChoice.ofMcp(Objects.requireNonNull(mcp, "'mcp' cannot be null.")));
    }

    /**
     * Gets the tool-selection mode.
     *
     * @return the mode, or {@code null} if this choice holds another variant.
     */
    public ToolChoiceOptions getMode() {
        return value == null ? null : value.options().orElse(null);
    }

    /**
     * Gets the specific function tool selected by this choice.
     *
     * @return the function tool, or {@code null} if this choice holds another variant.
     */
    public ToolChoiceFunction getFunction() {
        return value == null ? null : value.function().orElse(null);
    }

    /**
     * Gets the specific MCP tool selected by this choice.
     *
     * @return the MCP tool, or {@code null} if this choice holds another variant.
     */
    public ToolChoiceMcp getMcp() {
        return value == null ? null : value.mcp().orElse(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        if (unknownValue != null) {
            unknownValue.writeTo(jsonWriter);
        } else {
            OpenAIJsonHelper.toBinaryData(value).writeTo(jsonWriter);
        }
        return jsonWriter;
    }

    /**
     * Reads a tool choice from a JSON string or object.
     *
     * @param jsonReader the JSON reader.
     * @return the tool choice, or null for JSON null.
     * @throws IOException if the JSON cannot be read.
     */
    public static VoiceAgentToolChoice fromJson(JsonReader jsonReader) throws IOException {
        Object payload = jsonReader.readUntyped();
        if (payload == null) {
            return null;
        }
        BinaryData data = BinaryData.fromObject(payload);
        if (payload instanceof String) {
            return ofMode(OpenAIJsonHelper.fromBinaryData(data, ToolChoiceOptions.class));
        }
        if (payload instanceof Map) {
            Object type = ((Map<?, ?>) payload).get("type");
            if ("function".equals(type)) {
                return ofFunction(OpenAIJsonHelper.fromBinaryData(data, ToolChoiceFunction.class));
            }
            if ("mcp".equals(type)) {
                return ofMcp(OpenAIJsonHelper.fromBinaryData(data, ToolChoiceMcp.class));
            }
            return new VoiceAgentToolChoice(data);
        }
        throw LOGGER.logExceptionAsError(new IllegalStateException("Tool choice must be a JSON string or object."));
    }
}

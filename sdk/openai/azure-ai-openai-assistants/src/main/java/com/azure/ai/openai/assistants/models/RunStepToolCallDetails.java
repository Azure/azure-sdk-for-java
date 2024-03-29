// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) TypeSpec Code Generator.
package com.azure.ai.openai.assistants.models;

import com.azure.core.annotation.Generated;
import com.azure.core.annotation.Immutable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;
import java.util.List;

/**
 * The detailed information associated with a run step calling tools.
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
@JsonTypeName("tool_calls")
@Immutable
public final class RunStepToolCallDetails extends RunStepDetails {

    /*
     * A list of tool call details for this run step.
     */
    @Generated
    @JsonProperty(value = "tool_calls")
    private List<RunStepToolCall> toolCalls;

    /**
     * Creates an instance of RunStepToolCallDetails class.
     *
     * @param toolCalls the toolCalls value to set.
     */
    @Generated
    @JsonCreator
    private RunStepToolCallDetails(@JsonProperty(value = "tool_calls") List<RunStepToolCall> toolCalls) {
        this.toolCalls = toolCalls;
    }

    /**
     * Get the toolCalls property: A list of tool call details for this run step.
     *
     * @return the toolCalls value.
     */
    @Generated
    public List<RunStepToolCall> getToolCalls() {
        return this.toolCalls;
    }
}

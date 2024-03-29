// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) TypeSpec Code Generator.
package com.azure.ai.openai.assistants.models;

import com.azure.core.annotation.Generated;
import com.azure.core.annotation.Immutable;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.JsonTypeName;

/**
 * An abstract representation of an input tool definition that an assistant can use.
 */
@JsonTypeInfo(
    use = JsonTypeInfo.Id.NAME,
    include = JsonTypeInfo.As.PROPERTY,
    property = "type",
    defaultImpl = ToolDefinition.class)
@JsonTypeName("ToolDefinition")
@JsonSubTypes({
    @JsonSubTypes.Type(name = "code_interpreter", value = CodeInterpreterToolDefinition.class),
    @JsonSubTypes.Type(name = "retrieval", value = RetrievalToolDefinition.class),
    @JsonSubTypes.Type(name = "function", value = FunctionToolDefinition.class) })
@Immutable
public class ToolDefinition {

    /**
     * Creates an instance of ToolDefinition class.
     */
    @Generated
    public ToolDefinition() {
    }
}

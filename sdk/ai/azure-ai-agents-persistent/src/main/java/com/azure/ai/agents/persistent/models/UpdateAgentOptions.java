// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) TypeSpec Code Generator.
package com.azure.ai.agents.persistent.models;

import com.azure.core.annotation.Fluent;
import com.azure.core.annotation.Generated;
import com.azure.core.util.BinaryData;
import java.util.List;
import java.util.Map;

/**
 * Options for updateAgent API.
 */
@Fluent
public final class UpdateAgentOptions {

    /*
     * The ID of the agent to modify.
     */
    @Generated
    private final String assistantId;

    /*
     * The ID of the model to use.
     */
    @Generated
    private String model;

    /*
     * The modified name for the agent to use.
     */
    @Generated
    private String name;

    /*
     * The modified description for the agent to use.
     */
    @Generated
    private String description;

    /*
     * The modified system instructions for the new agent to use.
     */
    @Generated
    private String instructions;

    /*
     * The modified collection of tools to enable for the agent.
     */
    @Generated
    private List<ToolDefinition> tools;

    /*
     * A set of resources that are used by the agent's tools. The resources are specific to the type of tool. For
     * example,
     * the `code_interpreter` tool requires a list of file IDs, while the `file_search` tool requires a list of vector
     * store IDs.
     */
    @Generated
    private ToolResources toolResources;

    /*
     * What sampling temperature to use, between 0 and 2. Higher values like 0.8 will make the output more random,
     * while lower values like 0.2 will make it more focused and deterministic.
     */
    @Generated
    private Double temperature;

    /*
     * An alternative to sampling with temperature, called nucleus sampling, where the model considers the results of
     * the tokens with top_p probability mass.
     * So 0.1 means only the tokens comprising the top 10% probability mass are considered.
     * 
     * We generally recommend altering this or temperature but not both.
     */
    @Generated
    private Double topP;

    /*
     * The response format of the tool calls used by this agent.
     */
    @Generated
    private BinaryData responseFormat;

    /*
     * A set of up to 16 key/value pairs that can be attached to an object, used for storing additional information
     * about that object in a structured format. Keys may be up to 64 characters in length and values may be up to 512
     * characters in length.
     */
    @Generated
    private Map<String, String> metadata;

    /**
     * Creates an instance of UpdateAgentOptions class.
     *
     * @param assistantId the assistantId value to set.
     */
    @Generated
    public UpdateAgentOptions(String assistantId) {
        this.assistantId = assistantId;
    }

    /**
     * Get the assistantId property: The ID of the agent to modify.
     *
     * @return the assistantId value.
     */
    @Generated
    public String getAssistantId() {
        return this.assistantId;
    }

    /**
     * Get the model property: The ID of the model to use.
     *
     * @return the model value.
     */
    @Generated
    public String getModel() {
        return this.model;
    }

    /**
     * Set the model property: The ID of the model to use.
     *
     * @param model the model value to set.
     * @return the UpdateAgentOptions object itself.
     */
    @Generated
    public UpdateAgentOptions setModel(String model) {
        this.model = model;
        return this;
    }

    /**
     * Get the name property: The modified name for the agent to use.
     *
     * @return the name value.
     */
    @Generated
    public String getName() {
        return this.name;
    }

    /**
     * Set the name property: The modified name for the agent to use.
     *
     * @param name the name value to set.
     * @return the UpdateAgentOptions object itself.
     */
    @Generated
    public UpdateAgentOptions setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the description property: The modified description for the agent to use.
     *
     * @return the description value.
     */
    @Generated
    public String getDescription() {
        return this.description;
    }

    /**
     * Set the description property: The modified description for the agent to use.
     *
     * @param description the description value to set.
     * @return the UpdateAgentOptions object itself.
     */
    @Generated
    public UpdateAgentOptions setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Get the instructions property: The modified system instructions for the new agent to use.
     *
     * @return the instructions value.
     */
    @Generated
    public String getInstructions() {
        return this.instructions;
    }

    /**
     * Set the instructions property: The modified system instructions for the new agent to use.
     *
     * @param instructions the instructions value to set.
     * @return the UpdateAgentOptions object itself.
     */
    @Generated
    public UpdateAgentOptions setInstructions(String instructions) {
        this.instructions = instructions;
        return this;
    }

    /**
     * Get the tools property: The modified collection of tools to enable for the agent.
     *
     * @return the tools value.
     */
    @Generated
    public List<ToolDefinition> getTools() {
        return this.tools;
    }

    /**
     * Set the tools property: The modified collection of tools to enable for the agent.
     *
     * @param tools the tools value to set.
     * @return the UpdateAgentOptions object itself.
     */
    @Generated
    public UpdateAgentOptions setTools(List<ToolDefinition> tools) {
        this.tools = tools;
        return this;
    }

    /**
     * Get the toolResources property: A set of resources that are used by the agent's tools. The resources are specific
     * to the type of tool. For example,
     * the `code_interpreter` tool requires a list of file IDs, while the `file_search` tool requires a list of vector
     * store IDs.
     *
     * @return the toolResources value.
     */
    @Generated
    public ToolResources getToolResources() {
        return this.toolResources;
    }

    /**
     * Set the toolResources property: A set of resources that are used by the agent's tools. The resources are specific
     * to the type of tool. For example,
     * the `code_interpreter` tool requires a list of file IDs, while the `file_search` tool requires a list of vector
     * store IDs.
     *
     * @param toolResources the toolResources value to set.
     * @return the UpdateAgentOptions object itself.
     */
    @Generated
    public UpdateAgentOptions setToolResources(ToolResources toolResources) {
        this.toolResources = toolResources;
        return this;
    }

    /**
     * Get the temperature property: What sampling temperature to use, between 0 and 2. Higher values like 0.8 will make
     * the output more random,
     * while lower values like 0.2 will make it more focused and deterministic.
     *
     * @return the temperature value.
     */
    @Generated
    public Double getTemperature() {
        return this.temperature;
    }

    /**
     * Set the temperature property: What sampling temperature to use, between 0 and 2. Higher values like 0.8 will make
     * the output more random,
     * while lower values like 0.2 will make it more focused and deterministic.
     *
     * @param temperature the temperature value to set.
     * @return the UpdateAgentOptions object itself.
     */
    @Generated
    public UpdateAgentOptions setTemperature(Double temperature) {
        this.temperature = temperature;
        return this;
    }

    /**
     * Get the topP property: An alternative to sampling with temperature, called nucleus sampling, where the model
     * considers the results of the tokens with top_p probability mass.
     * So 0.1 means only the tokens comprising the top 10% probability mass are considered.
     *
     * We generally recommend altering this or temperature but not both.
     *
     * @return the topP value.
     */
    @Generated
    public Double getTopP() {
        return this.topP;
    }

    /**
     * Set the topP property: An alternative to sampling with temperature, called nucleus sampling, where the model
     * considers the results of the tokens with top_p probability mass.
     * So 0.1 means only the tokens comprising the top 10% probability mass are considered.
     *
     * We generally recommend altering this or temperature but not both.
     *
     * @param topP the topP value to set.
     * @return the UpdateAgentOptions object itself.
     */
    @Generated
    public UpdateAgentOptions setTopP(Double topP) {
        this.topP = topP;
        return this;
    }

    /**
     * Get the responseFormat property: The response format of the tool calls used by this agent.
     *
     * @return the responseFormat value.
     */
    @Generated
    public BinaryData getResponseFormat() {
        return this.responseFormat;
    }

    /**
     * Set the responseFormat property: The response format of the tool calls used by this agent.
     *
     * @param responseFormat the responseFormat value to set.
     * @return the UpdateAgentOptions object itself.
     */
    @Generated
    public UpdateAgentOptions setResponseFormat(BinaryData responseFormat) {
        this.responseFormat = responseFormat;
        return this;
    }

    /**
     * Get the metadata property: A set of up to 16 key/value pairs that can be attached to an object, used for storing
     * additional information about that object in a structured format. Keys may be up to 64 characters in length and
     * values may be up to 512 characters in length.
     *
     * @return the metadata value.
     */
    @Generated
    public Map<String, String> getMetadata() {
        return this.metadata;
    }

    /**
     * Set the metadata property: A set of up to 16 key/value pairs that can be attached to an object, used for storing
     * additional information about that object in a structured format. Keys may be up to 64 characters in length and
     * values may be up to 512 characters in length.
     *
     * @param metadata the metadata value to set.
     * @return the UpdateAgentOptions object itself.
     */
    @Generated
    public UpdateAgentOptions setMetadata(Map<String, String> metadata) {
        this.metadata = metadata;
        return this;
    }
}

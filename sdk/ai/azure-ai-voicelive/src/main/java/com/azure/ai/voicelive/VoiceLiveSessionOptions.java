// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive;

import com.azure.ai.voicelive.models.InputAudioFormat;
import com.azure.ai.voicelive.models.Modality;
import com.azure.ai.voicelive.models.OutputAudioFormat;
import com.azure.ai.voicelive.models.Tool;
import com.azure.ai.voicelive.models.TurnDetection;
import com.azure.ai.voicelive.models.VoiceProvider;

import java.util.List;

/**
 * Configuration options for a VoiceLive session.
 */
public final class VoiceLiveSessionOptions {
    private String model;
    private List<Modality> modalities;
    private String instructions;
    private VoiceProvider voice;
    private InputAudioFormat inputAudioFormat;
    private OutputAudioFormat outputAudioFormat;
    private TurnDetection turnDetection;
    private List<Tool> tools;
    private String toolChoice;
    private Double temperature;
    private Integer maxResponseOutputTokens;

    /**
     * Creates a new instance of VoiceLiveSessionOptions.
     *
     * @param model The model to use for the session.
     */
    public VoiceLiveSessionOptions(String model) {
        this.model = model;
    }

    /**
     * Gets the model to use for the session.
     *
     * @return The model name.
     */
    public String getModel() {
        return model;
    }

    /**
     * Sets the model to use for the session.
     *
     * @param model The model name.
     * @return This VoiceLiveSessionOptions instance.
     */
    public VoiceLiveSessionOptions setModel(String model) {
        this.model = model;
        return this;
    }

    /**
     * Gets the modalities for the session.
     *
     * @return The list of modalities.
     */
    public List<Modality> getModalities() {
        return modalities;
    }

    /**
     * Sets the modalities for the session.
     *
     * @param modalities The list of modalities.
     * @return This VoiceLiveSessionOptions instance.
     */
    public VoiceLiveSessionOptions setModalities(List<Modality> modalities) {
        this.modalities = modalities;
        return this;
    }

    /**
     * Gets the instructions for the session.
     *
     * @return The instructions.
     */
    public String getInstructions() {
        return instructions;
    }

    /**
     * Sets the instructions for the session.
     *
     * @param instructions The instructions.
     * @return This VoiceLiveSessionOptions instance.
     */
    public VoiceLiveSessionOptions setInstructions(String instructions) {
        this.instructions = instructions;
        return this;
    }

    /**
     * Gets the voice configuration for the session.
     *
     * @return The voice configuration.
     */
    public VoiceProvider getVoice() {
        return voice;
    }

    /**
     * Sets the voice configuration for the session.
     *
     * @param voice The voice configuration (e.g., OpenAIVoice, AzureVoice).
     * @return This VoiceLiveSessionOptions instance.
     */
    public VoiceLiveSessionOptions setVoice(VoiceProvider voice) {
        this.voice = voice;
        return this;
    }

    /**
     * Gets the input audio format for the session.
     *
     * @return The input audio format.
     */
    public InputAudioFormat getInputAudioFormat() {
        return inputAudioFormat;
    }

    /**
     * Sets the input audio format for the session.
     *
     * @param inputAudioFormat The input audio format.
     * @return This VoiceLiveSessionOptions instance.
     */
    public VoiceLiveSessionOptions setInputAudioFormat(InputAudioFormat inputAudioFormat) {
        this.inputAudioFormat = inputAudioFormat;
        return this;
    }

    /**
     * Gets the output audio format for the session.
     *
     * @return The output audio format.
     */
    public OutputAudioFormat getOutputAudioFormat() {
        return outputAudioFormat;
    }

    /**
     * Sets the output audio format for the session.
     *
     * @param outputAudioFormat The output audio format.
     * @return This VoiceLiveSessionOptions instance.
     */
    public VoiceLiveSessionOptions setOutputAudioFormat(OutputAudioFormat outputAudioFormat) {
        this.outputAudioFormat = outputAudioFormat;
        return this;
    }

    /**
     * Gets the turn detection configuration.
     *
     * @return The turn detection configuration.
     */
    public TurnDetection getTurnDetection() {
        return turnDetection;
    }

    /**
     * Sets the turn detection configuration.
     *
     * @param turnDetection The turn detection configuration.
     * @return This VoiceLiveSessionOptions instance.
     */
    public VoiceLiveSessionOptions setTurnDetection(TurnDetection turnDetection) {
        this.turnDetection = turnDetection;
        return this;
    }

    /**
     * Gets the tools available for the session.
     *
     * @return The list of tools.
     */
    public List<Tool> getTools() {
        return tools;
    }

    /**
     * Sets the tools available for the session.
     *
     * @param tools The list of tools.
     * @return This VoiceLiveSessionOptions instance.
     */
    public VoiceLiveSessionOptions setTools(List<Tool> tools) {
        this.tools = tools;
        return this;
    }

    /**
     * Gets the tool choice configuration.
     *
     * @return The tool choice.
     */
    public String getToolChoice() {
        return toolChoice;
    }

    /**
     * Sets the tool choice configuration.
     *
     * @param toolChoice The tool choice.
     * @return This VoiceLiveSessionOptions instance.
     */
    public VoiceLiveSessionOptions setToolChoice(String toolChoice) {
        this.toolChoice = toolChoice;
        return this;
    }

    /**
     * Gets the temperature for response generation.
     *
     * @return The temperature.
     */
    public Double getTemperature() {
        return temperature;
    }

    /**
     * Sets the temperature for response generation.
     *
     * @param temperature The temperature value between 0 and 2.
     * @return This VoiceLiveSessionOptions instance.
     */
    public VoiceLiveSessionOptions setTemperature(Double temperature) {
        this.temperature = temperature;
        return this;
    }

    /**
     * Gets the maximum number of output tokens for responses.
     *
     * @return The maximum output tokens.
     */
    public Integer getMaxResponseOutputTokens() {
        return maxResponseOutputTokens;
    }

    /**
     * Sets the maximum number of output tokens for responses.
     *
     * @param maxResponseOutputTokens The maximum output tokens.
     * @return This VoiceLiveSessionOptions instance.
     */
    public VoiceLiveSessionOptions setMaxResponseOutputTokens(Integer maxResponseOutputTokens) {
        this.maxResponseOutputTokens = maxResponseOutputTokens;
        return this;
    }
}

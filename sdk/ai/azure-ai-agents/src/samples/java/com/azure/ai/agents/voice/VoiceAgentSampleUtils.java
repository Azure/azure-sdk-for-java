// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.voice;

import com.azure.ai.agents.models.VoiceAgentAudioConfig;
import com.azure.ai.agents.models.VoiceAgentAudioOutputConfig;
import com.azure.ai.agents.models.VoiceAgentDefinition;
import com.azure.ai.agents.models.VoiceModelType;
import com.azure.ai.agents.models.VoiceOutputModality;
import com.azure.ai.agents.models.VoiceType;

import java.util.Collections;

final class VoiceAgentSampleUtils {
    private VoiceAgentSampleUtils() {
    }

    static VoiceAgentDefinition createDefinition(VoiceModelType modelType, String model, String instructions) {
        VoiceAgentAudioOutputConfig output = new VoiceAgentAudioOutputConfig()
            .setVoice("en-US-AvaNeural")
            .setVoiceType(VoiceType.AZURE_STANDARD);
        return new VoiceAgentDefinition(modelType, model)
            .setInstructions(instructions)
            .setAudio(new VoiceAgentAudioConfig().setOutput(output))
            .setOutputModalities(Collections.singletonList(VoiceOutputModality.AUDIO))
            .setStore(true);
    }
}

package com.azure.ai.openai.assistants.implementation;

import com.azure.ai.openai.assistants.models.ApiResponseFormat;
import com.azure.ai.openai.assistants.models.AssistantCreationOptions;
import com.azure.ai.openai.assistants.models.AssistantsApiResponseFormat;
import com.azure.ai.openai.assistants.models.AssistantsApiResponseFormatOption;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AssistantApiFormatSerializerTests {

    @Test
    public void assistantCreationOptionTextFormat() {
        AssistantCreationOptions assistantCreationOptions = new AssistantCreationOptions("gpt-4");
        assistantCreationOptions.setResponseFormat(new AssistantsApiResponseFormatOption(new AssistantsApiResponseFormat(ApiResponseFormat.TEXT)));

        String json = BinaryData.fromObject(assistantCreationOptions).toString();
        assertTrue(json.contains("\"response_format\":{\"type\":\"text\"}"));
        assertTrue(json.contains("\"model\":\"gpt-4\""));

    }
}

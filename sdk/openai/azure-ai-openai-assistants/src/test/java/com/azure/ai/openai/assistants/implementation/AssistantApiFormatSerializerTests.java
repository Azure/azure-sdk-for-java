package com.azure.ai.openai.assistants.implementation;

import com.azure.ai.openai.assistants.models.ApiResponseFormat;
import com.azure.ai.openai.assistants.models.AssistantCreationOptions;
import com.azure.ai.openai.assistants.models.AssistantsApiResponseFormat;
import com.azure.ai.openai.assistants.models.AssistantsApiResponseFormatMode;
import com.azure.ai.openai.assistants.models.AssistantsApiResponseFormatOption;
import com.azure.ai.openai.assistants.models.CreateRunOptions;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class AssistantApiFormatSerializerTests {

    @Test
    public void assistantCreationOptionTextFormat() {
        AssistantCreationOptions assistantCreationOptions = new AssistantCreationOptions("gpt-4");
        assistantCreationOptions.setResponseFormat(new AssistantsApiResponseFormatOption(new AssistantsApiResponseFormat(ApiResponseFormat.TEXT)));

        BinaryData jsonBinaryData = BinaryData.fromObject(assistantCreationOptions);
        String json = jsonBinaryData.toString();
        assertTrue(json.contains("\"response_format\":{\"type\":\"text\"}"));
        assertTrue(json.contains("\"model\":\"gpt-4\""));

    }

    @Test
    public void assistantCreationOptionJsonObjectFormat() {
        AssistantCreationOptions assistantCreationOptions = new AssistantCreationOptions("gpt-4");
        assistantCreationOptions.setResponseFormat(new AssistantsApiResponseFormatOption(new AssistantsApiResponseFormat(ApiResponseFormat.JSON_OBJECT)));

        BinaryData jsonBinaryData = BinaryData.fromObject(assistantCreationOptions);
        String json = jsonBinaryData.toString();
        assertTrue(json.contains("\"response_format\":{\"type\":\"json_object\"}"));
        assertTrue(json.contains("\"model\":\"gpt-4\""));

    }

    @Test
    public void assistantCreationOptionAutoMode() {
        AssistantCreationOptions assistantCreationOptions = new AssistantCreationOptions("gpt-4");
        assistantCreationOptions.setResponseFormat(new AssistantsApiResponseFormatOption(AssistantsApiResponseFormatMode.AUTO));

        BinaryData jsonBinaryData = BinaryData.fromObject(assistantCreationOptions);
        String json = jsonBinaryData.toString();
        assertTrue(json.contains("\"response_format\":\"auto\"}"));
        assertTrue(json.contains("\"model\":\"gpt-4\""));
    }

    @Test
    public void assistantCreationOptionNoneMode() {
        AssistantCreationOptions assistantCreationOptions = new AssistantCreationOptions("gpt-4");
        assistantCreationOptions.setResponseFormat(new AssistantsApiResponseFormatOption(AssistantsApiResponseFormatMode.NONE));

        BinaryData jsonBinaryData = BinaryData.fromObject(assistantCreationOptions);
        String json = jsonBinaryData.toString();
        assertTrue(json.contains("\"response_format\":\"none\"}"));
        assertTrue(json.contains("\"model\":\"gpt-4\""));
    }

    @Test
    public void createRunOptionTextFormat() {
        CreateRunOptions createRunOptions = new CreateRunOptions("abc123");
        createRunOptions.setResponseFormat(new AssistantsApiResponseFormatOption(new AssistantsApiResponseFormat(ApiResponseFormat.TEXT)));

        BinaryData jsonBinaryData = BinaryData.fromObject(createRunOptions);
        String json = jsonBinaryData.toString();
        assertTrue(json.contains("\"response_format\":{\"type\":\"text\"}"));
        assertTrue(json.contains("\"assistant_id\":\"abc123\""));
    }

    @Test
    public void createRunOptionJsonObjectFormat() {
        CreateRunOptions createRunOptions = new CreateRunOptions("abc123");
        createRunOptions.setResponseFormat(new AssistantsApiResponseFormatOption(new AssistantsApiResponseFormat(ApiResponseFormat.JSON_OBJECT)));

        BinaryData jsonBinaryData = BinaryData.fromObject(createRunOptions);
        String json = jsonBinaryData.toString();
        assertTrue(json.contains("\"response_format\":{\"type\":\"json_object\"}"));
        assertTrue(json.contains("\"assistant_id\":\"abc123\""));
    }

    @Test
    public void createRunOptionAutoMode() {
        CreateRunOptions createRunOptions = new CreateRunOptions("abc123");
        createRunOptions.setResponseFormat(new AssistantsApiResponseFormatOption(AssistantsApiResponseFormatMode.AUTO));

        BinaryData jsonBinaryData = BinaryData.fromObject(createRunOptions);
        String json = jsonBinaryData.toString();
        assertTrue(json.contains("\"response_format\":\"auto\"}"));
        assertTrue(json.contains("\"assistant_id\":\"abc123\""));
    }

    @Test
    public void createRunOptionNoneMode() {
        CreateRunOptions createRunOptions = new CreateRunOptions("abc123");
        createRunOptions.setResponseFormat(new AssistantsApiResponseFormatOption(AssistantsApiResponseFormatMode.NONE));

        BinaryData jsonBinaryData = BinaryData.fromObject(createRunOptions);
        String json = jsonBinaryData.toString();
        assertTrue(json.contains("\"response_format\":\"none\"}"));
        assertTrue(json.contains("\"assistant_id\":\"abc123\""));
    }
}

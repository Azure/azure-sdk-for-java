// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants.implementation;

import com.azure.ai.openai.assistants.models.ApiResponseFormat;
import com.azure.ai.openai.assistants.models.Assistant;
import com.azure.ai.openai.assistants.models.AssistantCreationOptions;
import com.azure.ai.openai.assistants.models.AssistantsApiResponseFormat;
import com.azure.ai.openai.assistants.models.AssistantsApiResponseFormatMode;
import com.azure.ai.openai.assistants.models.AssistantsApiResponseFormatOption;
import com.azure.ai.openai.assistants.models.CreateAndRunThreadOptions;
import com.azure.ai.openai.assistants.models.CreateRunOptions;
import com.azure.ai.openai.assistants.models.ThreadRun;
import com.azure.ai.openai.assistants.models.UpdateAssistantOptions;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// It may seem odd to test the getter for some methods, but there is actually some degree of logic involved in it, to
// prevent having to leak BinaryData as a type that would have to be used by someone consuming the SDK.
public class AssistantApiFormatSerializerTests {

    @Test
    public void assistantCreationOptionTextFormat() {
        AssistantCreationOptions assistantCreationOptions = new AssistantCreationOptions("gpt-4");
        assistantCreationOptions.setResponseFormat(new AssistantsApiResponseFormatOption(new AssistantsApiResponseFormat(ApiResponseFormat.TEXT)));
        AssistantsApiResponseFormatOption responseFormat = assistantCreationOptions.getResponseFormat();

        BinaryData jsonBinaryData = BinaryData.fromObject(assistantCreationOptions);
        String json = jsonBinaryData.toString();

        assertEquals(ApiResponseFormat.TEXT, responseFormat.getFormat().getType());
        assertTrue(json.contains("\"response_format\":{\"type\":\"text\"}"));
        assertTrue(json.contains("\"model\":\"gpt-4\""));

    }

    @Test
    public void assistantCreationOptionJsonObjectFormat() {
        AssistantCreationOptions assistantCreationOptions = new AssistantCreationOptions("gpt-4");
        assistantCreationOptions.setResponseFormat(new AssistantsApiResponseFormatOption(new AssistantsApiResponseFormat(ApiResponseFormat.JSON_OBJECT)));
        AssistantsApiResponseFormatOption responseFormat = assistantCreationOptions.getResponseFormat();

        BinaryData jsonBinaryData = BinaryData.fromObject(assistantCreationOptions);
        String json = jsonBinaryData.toString();

        assertEquals(ApiResponseFormat.JSON_OBJECT, responseFormat.getFormat().getType());
        assertTrue(json.contains("\"response_format\":{\"type\":\"json_object\"}"));
        assertTrue(json.contains("\"model\":\"gpt-4\""));

    }

    @Test
    public void assistantCreationOptionAutoMode() {
        AssistantCreationOptions assistantCreationOptions = new AssistantCreationOptions("gpt-4");
        assistantCreationOptions.setResponseFormat(new AssistantsApiResponseFormatOption(AssistantsApiResponseFormatMode.AUTO));
        AssistantsApiResponseFormatOption responseFormat = assistantCreationOptions.getResponseFormat();

        BinaryData jsonBinaryData = BinaryData.fromObject(assistantCreationOptions);
        String json = jsonBinaryData.toString();

        assertEquals(AssistantsApiResponseFormatMode.AUTO, responseFormat.getMode());
        assertTrue(json.contains("\"response_format\":\"auto\"}"));
        assertTrue(json.contains("\"model\":\"gpt-4\""));
    }

    @Test
    public void assistantCreationOptionNoneMode() {
        AssistantCreationOptions assistantCreationOptions = new AssistantCreationOptions("gpt-4");
        assistantCreationOptions.setResponseFormat(new AssistantsApiResponseFormatOption(AssistantsApiResponseFormatMode.NONE));
        AssistantsApiResponseFormatOption responseFormat = assistantCreationOptions.getResponseFormat();

        BinaryData jsonBinaryData = BinaryData.fromObject(assistantCreationOptions);
        String json = jsonBinaryData.toString();

        assertEquals(AssistantsApiResponseFormatMode.NONE, responseFormat.getMode());
        assertTrue(json.contains("\"response_format\":\"none\"}"));
        assertTrue(json.contains("\"model\":\"gpt-4\""));
    }

    @Test
    public void createRunOptionTextFormat() {
        CreateRunOptions createRunOptions = new CreateRunOptions("abc123");
        createRunOptions.setResponseFormat(new AssistantsApiResponseFormatOption(new AssistantsApiResponseFormat(ApiResponseFormat.TEXT)));
        AssistantsApiResponseFormatOption responseFormat = createRunOptions.getResponseFormat();


        BinaryData jsonBinaryData = BinaryData.fromObject(createRunOptions);
        String json = jsonBinaryData.toString();

        assertEquals(ApiResponseFormat.TEXT, responseFormat.getFormat().getType());
        assertTrue(json.contains("\"response_format\":{\"type\":\"text\"}"));
        assertTrue(json.contains("\"assistant_id\":\"abc123\""));
    }

    @Test
    public void createRunOptionJsonObjectFormat() {
        CreateRunOptions createRunOptions = new CreateRunOptions("abc123");
        createRunOptions.setResponseFormat(new AssistantsApiResponseFormatOption(new AssistantsApiResponseFormat(ApiResponseFormat.JSON_OBJECT)));
        AssistantsApiResponseFormatOption responseFormat = createRunOptions.getResponseFormat();

        BinaryData jsonBinaryData = BinaryData.fromObject(createRunOptions);
        String json = jsonBinaryData.toString();

        assertEquals(ApiResponseFormat.JSON_OBJECT, responseFormat.getFormat().getType());
        assertTrue(json.contains("\"response_format\":{\"type\":\"json_object\"}"));
        assertTrue(json.contains("\"assistant_id\":\"abc123\""));
    }

    @Test
    public void createRunOptionAutoMode() {
        CreateRunOptions createRunOptions = new CreateRunOptions("abc123");
        createRunOptions.setResponseFormat(new AssistantsApiResponseFormatOption(AssistantsApiResponseFormatMode.AUTO));
        AssistantsApiResponseFormatOption responseFormat = createRunOptions.getResponseFormat();

        BinaryData jsonBinaryData = BinaryData.fromObject(createRunOptions);
        String json = jsonBinaryData.toString();

        assertEquals(AssistantsApiResponseFormatMode.AUTO, responseFormat.getMode());
        assertTrue(json.contains("\"response_format\":\"auto\"}"));
        assertTrue(json.contains("\"assistant_id\":\"abc123\""));
    }

    @Test
    public void createRunOptionNoneMode() {
        CreateRunOptions createRunOptions = new CreateRunOptions("abc123");
        createRunOptions.setResponseFormat(new AssistantsApiResponseFormatOption(AssistantsApiResponseFormatMode.NONE));
        AssistantsApiResponseFormatOption responseFormat = createRunOptions.getResponseFormat();

        BinaryData jsonBinaryData = BinaryData.fromObject(createRunOptions);
        String json = jsonBinaryData.toString();

        assertEquals(AssistantsApiResponseFormatMode.NONE, responseFormat.getMode());
        assertTrue(json.contains("\"response_format\":\"none\"}"));
        assertTrue(json.contains("\"assistant_id\":\"abc123\""));
    }

    @Test
    public void createAndRunThreadOptionTextFormat() {
        CreateAndRunThreadOptions options = new CreateAndRunThreadOptions("abc123");
        options.setResponseFormat(new AssistantsApiResponseFormatOption(new AssistantsApiResponseFormat(ApiResponseFormat.TEXT)));
        AssistantsApiResponseFormatOption responseFormat = options.getResponseFormat();

        BinaryData jsonBinaryData = BinaryData.fromObject(options);
        String json = jsonBinaryData.toString();

        assertEquals(ApiResponseFormat.TEXT, responseFormat.getFormat().getType());
        assertTrue(json.contains("\"response_format\":{\"type\":\"text\"}"));
        assertTrue(json.contains("\"assistant_id\":\"abc123\""));
    }

    @Test
    public void createAndRunThreadOptionJsonObjectFormat() {
        CreateAndRunThreadOptions options = new CreateAndRunThreadOptions("abc123");
        options.setResponseFormat(new AssistantsApiResponseFormatOption(new AssistantsApiResponseFormat(ApiResponseFormat.JSON_OBJECT)));
        AssistantsApiResponseFormatOption responseFormat = options.getResponseFormat();

        BinaryData jsonBinaryData = BinaryData.fromObject(options);
        String json = jsonBinaryData.toString();

        assertEquals(ApiResponseFormat.JSON_OBJECT, responseFormat.getFormat().getType());
        assertTrue(json.contains("\"response_format\":{\"type\":\"json_object\"}"));
        assertTrue(json.contains("\"assistant_id\":\"abc123\""));
    }

    @Test
    public void createAndRunThreadOptionAutoMode() {
        CreateAndRunThreadOptions options = new CreateAndRunThreadOptions("abc123");
        options.setResponseFormat(new AssistantsApiResponseFormatOption(AssistantsApiResponseFormatMode.AUTO));
        AssistantsApiResponseFormatOption responseFormat = options.getResponseFormat();

        BinaryData jsonBinaryData = BinaryData.fromObject(options);
        String json = jsonBinaryData.toString();

        assertEquals(AssistantsApiResponseFormatMode.AUTO, responseFormat.getMode());
        assertTrue(json.contains("\"response_format\":\"auto\""));
        assertTrue(json.contains("\"assistant_id\":\"abc123\""));
    }

    @Test
    public void createAndRunThreadOptionNoneMode() {
        CreateAndRunThreadOptions options = new CreateAndRunThreadOptions("abc123");
        options.setResponseFormat(new AssistantsApiResponseFormatOption(AssistantsApiResponseFormatMode.NONE));
        AssistantsApiResponseFormatOption responseFormat = options.getResponseFormat();

        BinaryData jsonBinaryData = BinaryData.fromObject(options);
        String json = jsonBinaryData.toString();

        assertEquals(AssistantsApiResponseFormatMode.NONE, responseFormat.getMode());
        assertTrue(json.contains("\"response_format\":\"none\""));
        assertTrue(json.contains("\"assistant_id\":\"abc123\""));
    }

    @Test
    public void updateAssistantOptionTextFormat() {
        UpdateAssistantOptions options = new UpdateAssistantOptions();
        options.setResponseFormat(new AssistantsApiResponseFormatOption(new AssistantsApiResponseFormat(ApiResponseFormat.TEXT)));
        AssistantsApiResponseFormatOption responseFormat = options.getResponseFormat();

        BinaryData jsonBinaryData = BinaryData.fromObject(options);
        String json = jsonBinaryData.toString();

        assertEquals(ApiResponseFormat.TEXT, responseFormat.getFormat().getType());
        assertTrue(json.contains("\"response_format\":{\"type\":\"text\"}"));
    }

    @Test
    public void updateAssistantOptionJsonObjectFormat() {
        UpdateAssistantOptions options = new UpdateAssistantOptions();
        options.setResponseFormat(new AssistantsApiResponseFormatOption(new AssistantsApiResponseFormat(ApiResponseFormat.JSON_OBJECT)));
        AssistantsApiResponseFormatOption responseFormat = options.getResponseFormat();

        BinaryData jsonBinaryData = BinaryData.fromObject(options);
        String json = jsonBinaryData.toString();

        assertEquals(ApiResponseFormat.JSON_OBJECT, responseFormat.getFormat().getType());
        assertTrue(json.contains("\"response_format\":{\"type\":\"json_object\"}"));
    }

    @Test
    public void updateAssistantOptionAutoMode() {
        UpdateAssistantOptions options = new UpdateAssistantOptions();
        options.setResponseFormat(new AssistantsApiResponseFormatOption(AssistantsApiResponseFormatMode.AUTO));
        AssistantsApiResponseFormatOption responseFormat = options.getResponseFormat();

        BinaryData jsonBinaryData = BinaryData.fromObject(options);
        String json = jsonBinaryData.toString();

        assertEquals(AssistantsApiResponseFormatMode.AUTO, responseFormat.getMode());
        assertTrue(json.contains("\"response_format\":\"auto\""));
    }

    @Test
    public void updateAssistantOptionNoneMode() {
        UpdateAssistantOptions options = new UpdateAssistantOptions();
        options.setResponseFormat(new AssistantsApiResponseFormatOption(AssistantsApiResponseFormatMode.NONE));
        AssistantsApiResponseFormatOption responseFormat = options.getResponseFormat();

        BinaryData jsonBinaryData = BinaryData.fromObject(options);
        String json = jsonBinaryData.toString();

        assertEquals(AssistantsApiResponseFormatMode.NONE, responseFormat.getMode());
        assertTrue(json.contains("\"response_format\":\"none\""));
    }

    @Test
    public void assistantTextFormat() {
        Assistant assistant = BinaryData.fromString("{\"response_format\":{\"type\":\"text\"}}").toObject(Assistant.class);
        AssistantsApiResponseFormatOption responseFormat = assistant.getResponseFormat();

        assertNull(responseFormat.getMode());
        assertNotNull(responseFormat.getFormat());
        assertEquals(ApiResponseFormat.TEXT, responseFormat.getFormat().getType());
    }

    @Test
    public void assistantJsonObjectFormat() {
        Assistant assistant = BinaryData.fromString("{\"response_format\":{\"type\":\"json_object\"}}").toObject(Assistant.class);
        AssistantsApiResponseFormatOption responseFormat = assistant.getResponseFormat();

        assertNull(responseFormat.getMode());
        assertNotNull(responseFormat.getFormat());
        assertEquals(ApiResponseFormat.JSON_OBJECT, responseFormat.getFormat().getType());
    }

    @Test
    public void assistantAutoMode() {
        Assistant assistant = BinaryData.fromString("{\"response_format\":\"auto\"}").toObject(Assistant.class);
        AssistantsApiResponseFormatOption responseFormat = assistant.getResponseFormat();

        assertNotNull(responseFormat.getMode());
        assertNull(responseFormat.getFormat());
        assertEquals(AssistantsApiResponseFormatMode.AUTO, responseFormat.getMode());
    }

    @Test
    public void assistantNoneMode() {
        Assistant assistant = BinaryData.fromString("{\"response_format\":\"none\"}").toObject(Assistant.class);
        AssistantsApiResponseFormatOption responseFormat = assistant.getResponseFormat();

        assertNotNull(responseFormat.getMode());
        assertNull(responseFormat.getFormat());
        assertEquals(AssistantsApiResponseFormatMode.NONE, responseFormat.getMode());
    }

    @Test
    public void assistantNull() {
        Assistant assistant = BinaryData.fromString("{\"response_format\":null}").toObject(Assistant.class);
        AssistantsApiResponseFormatOption responseFormat = assistant.getResponseFormat();

        assertNull(responseFormat);
    }

    @Test
    public void threadRunTextFormat() {
        ThreadRun threadRun = BinaryData.fromString("{\"response_format\":{\"type\":\"text\"}}").toObject(ThreadRun.class);
        AssistantsApiResponseFormatOption responseFormat = threadRun.getResponseFormat();

        assertNull(responseFormat.getMode());
        assertNotNull(responseFormat.getFormat());
        assertEquals(ApiResponseFormat.TEXT, responseFormat.getFormat().getType());
    }

    @Test
    public void threadRunJsonObjectFormat() {
        ThreadRun threadRun = BinaryData.fromString("{\"response_format\":{\"type\":\"json_object\"}}").toObject(ThreadRun.class);
        AssistantsApiResponseFormatOption responseFormat = threadRun.getResponseFormat();

        assertNull(responseFormat.getMode());
        assertNotNull(responseFormat.getFormat());
        assertEquals(ApiResponseFormat.JSON_OBJECT, responseFormat.getFormat().getType());
    }

    @Test
    public void threadRunAutoMode() {
        ThreadRun threadRun = BinaryData.fromString("{\"response_format\":\"auto\"}").toObject(ThreadRun.class);
        AssistantsApiResponseFormatOption responseFormat = threadRun.getResponseFormat();

        assertNotNull(responseFormat.getMode());
        assertNull(responseFormat.getFormat());
        assertEquals(AssistantsApiResponseFormatMode.AUTO, responseFormat.getMode());
    }

    @Test
    public void threadRunNoneMode() {
        ThreadRun threadRun = BinaryData.fromString("{\"response_format\":\"none\"}").toObject(ThreadRun.class);
        AssistantsApiResponseFormatOption responseFormat = threadRun.getResponseFormat();

        assertNotNull(responseFormat.getMode());
        assertNull(responseFormat.getFormat());
        assertEquals(AssistantsApiResponseFormatMode.NONE, responseFormat.getMode());
    }

    @Test
    public void threadRunNull() {
        ThreadRun threadRun = BinaryData.fromString("{\"response_format\":null}").toObject(ThreadRun.class);
        AssistantsApiResponseFormatOption responseFormat = threadRun.getResponseFormat();

        assertNull(responseFormat);
    }
}

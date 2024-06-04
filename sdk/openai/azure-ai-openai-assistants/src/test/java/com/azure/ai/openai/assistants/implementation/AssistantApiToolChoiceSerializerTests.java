// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants.implementation;

import com.azure.ai.openai.assistants.models.AssistantsApiToolChoiceOption;
import com.azure.ai.openai.assistants.models.AssistantsApiToolChoiceOptionMode;
import com.azure.ai.openai.assistants.models.AssistantsNamedToolChoice;
import com.azure.ai.openai.assistants.models.AssistantsNamedToolChoiceType;
import com.azure.ai.openai.assistants.models.CreateAndRunThreadOptions;
import com.azure.ai.openai.assistants.models.CreateRunOptions;
import com.azure.ai.openai.assistants.models.FunctionName;
import com.azure.ai.openai.assistants.models.ThreadRun;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// It may seem odd to test the getter for some methods, but there is actually some degree of logic involved in it, to
// prevent having to leak BinaryData as a type that would have to be used by someone consuming the SDK.
public class AssistantApiToolChoiceSerializerTests {

    @Test
    public void threadRunNull() {
        ThreadRun threadRun = BinaryData.fromString("{\"tool_choice\":null}").toObject(ThreadRun.class);
        AssistantsApiToolChoiceOption toolChoice = threadRun.getToolChoice();

        assertNull(toolChoice);
    }

    @Test
    public void threadRunAutoMode() {
        ThreadRun threadRun = BinaryData.fromString("{\"tool_choice\":\"auto\"}").toObject(ThreadRun.class);
        AssistantsApiToolChoiceOption toolChoice = threadRun.getToolChoice();

        assertNotNull(toolChoice.getMode());
        assertNull(toolChoice.getToolChoice());
        assertEquals(AssistantsApiToolChoiceOptionMode.AUTO, toolChoice.getMode());
    }

    @Test
    public void threadRunNoneMode() {
        ThreadRun threadRun = BinaryData.fromString("{\"tool_choice\":\"none\"}").toObject(ThreadRun.class);
        AssistantsApiToolChoiceOption toolChoice = threadRun.getToolChoice();

        assertNotNull(toolChoice.getMode());
        assertNull(toolChoice.getToolChoice());
        assertEquals(AssistantsApiToolChoiceOptionMode.NONE, toolChoice.getMode());
    }

    @Test
    public void threadRunFunctionCallToolChoice() {
        ThreadRun threadRun = BinaryData.fromString("{\"tool_choice\":{\"type\":\"function\",\"function\":{\"name\":\"my_function\"}}}")
            .toObject(ThreadRun.class);
        AssistantsApiToolChoiceOption toolChoice = threadRun.getToolChoice();

        assertNull(toolChoice.getMode());
        assertNotNull(toolChoice.getToolChoice());
        assertEquals(AssistantsNamedToolChoiceType.FUNCTION, toolChoice.getToolChoice().getType());
        assertEquals("my_function", toolChoice.getToolChoice().getFunction().getName());
    }

    @Test
    public void threadRunFileSearchToolChoice() {
        ThreadRun threadRun = BinaryData.fromString("{\"tool_choice\":{\"type\":\"file_search\"}}").toObject(ThreadRun.class);
        AssistantsApiToolChoiceOption toolChoice = threadRun.getToolChoice();

        assertNull(toolChoice.getMode());
        assertNotNull(toolChoice.getToolChoice());
        assertEquals(AssistantsNamedToolChoiceType.FILE_SEARCH, toolChoice.getToolChoice().getType());
    }

    @Test
    public void threadRunCodeInterpreterToolChoice() {
        ThreadRun threadRun = BinaryData.fromString("{\"tool_choice\":{\"type\":\"code_interpreter\"}}").toObject(ThreadRun.class);
        AssistantsApiToolChoiceOption toolChoice = threadRun.getToolChoice();

        assertNull(toolChoice.getMode());
        assertNotNull(toolChoice.getToolChoice());
        assertEquals(AssistantsNamedToolChoiceType.CODE_INTERPRETER, toolChoice.getToolChoice().getType());
    }

    @Test
    public void createAndRunThreadOptionsAutoMode() {
        CreateAndRunThreadOptions options = new CreateAndRunThreadOptions("abc123");
        options.setToolChoice(new AssistantsApiToolChoiceOption(AssistantsApiToolChoiceOptionMode.AUTO));
        AssistantsApiToolChoiceOption toolChoice = options.getToolChoice();

        BinaryData jsonBinaryData = BinaryData.fromObject(options);
        String json = jsonBinaryData.toString();

        assertEquals(AssistantsApiToolChoiceOptionMode.AUTO, toolChoice.getMode());
        assertTrue(json.contains("\"tool_choice\":\"auto\""));
        assertTrue(json.contains("\"assistant_id\":\"abc123\""));
    }

    @Test
    public void createAndRunThreadOptionsNoneMode() {
        CreateAndRunThreadOptions options = new CreateAndRunThreadOptions("abc123");
        options.setToolChoice(new AssistantsApiToolChoiceOption(AssistantsApiToolChoiceOptionMode.NONE));
        AssistantsApiToolChoiceOption toolChoice = options.getToolChoice();

        BinaryData jsonBinaryData = BinaryData.fromObject(options);
        String json = jsonBinaryData.toString();

        assertEquals(AssistantsApiToolChoiceOptionMode.NONE, toolChoice.getMode());
        assertTrue(json.contains("\"tool_choice\":\"none\""));
        assertTrue(json.contains("\"assistant_id\":\"abc123\""));
    }

    @Test
    public void createAndRunThreadOptionsFunctionCallToolChoice() {
        CreateAndRunThreadOptions options = new CreateAndRunThreadOptions("abc123");
        options.setToolChoice(new AssistantsApiToolChoiceOption(
            new AssistantsNamedToolChoice(AssistantsNamedToolChoiceType.FUNCTION)
                .setFunction(new FunctionName("my_function"))));
        AssistantsApiToolChoiceOption toolChoice = options.getToolChoice();

        BinaryData jsonBinaryData = BinaryData.fromObject(options);
        String json = jsonBinaryData.toString();

        assertNull(toolChoice.getMode());
        assertNotNull(toolChoice.getToolChoice());
        assertEquals(AssistantsNamedToolChoiceType.FUNCTION, toolChoice.getToolChoice().getType());
        assertEquals("my_function", toolChoice.getToolChoice().getFunction().getName());
        assertTrue(json.contains("\"tool_choice\":{\"type\":\"function\",\"function\":{\"name\":\"my_function\"}}"));
        assertTrue(json.contains("\"assistant_id\":\"abc123\""));
    }

    @Test
    public void createAndRunThreadOptionsFileSearchToolChoice() {
        CreateAndRunThreadOptions options = new CreateAndRunThreadOptions("abc123");
        options.setToolChoice(new AssistantsApiToolChoiceOption(
            new AssistantsNamedToolChoice(AssistantsNamedToolChoiceType.FILE_SEARCH)));
        AssistantsApiToolChoiceOption toolChoice = options.getToolChoice();

        BinaryData jsonBinaryData = BinaryData.fromObject(options);
        String json = jsonBinaryData.toString();

        assertNull(toolChoice.getMode());
        assertNotNull(toolChoice.getToolChoice());
        assertEquals(AssistantsNamedToolChoiceType.FILE_SEARCH, toolChoice.getToolChoice().getType());
        assertTrue(json.contains("\"tool_choice\":{\"type\":\"file_search\"}"));
        assertTrue(json.contains("\"assistant_id\":\"abc123\""));
    }

    @Test
    public void createAndRunThreadOptionsCodeInterpreterToolChoice() {
        CreateAndRunThreadOptions options = new CreateAndRunThreadOptions("abc123");
        options.setToolChoice(new AssistantsApiToolChoiceOption(
            new AssistantsNamedToolChoice(AssistantsNamedToolChoiceType.CODE_INTERPRETER)));
        AssistantsApiToolChoiceOption toolChoice = options.getToolChoice();

        BinaryData jsonBinaryData = BinaryData.fromObject(options);
        String json = jsonBinaryData.toString();

        assertNull(toolChoice.getMode());
        assertNotNull(toolChoice.getToolChoice());
        assertEquals(AssistantsNamedToolChoiceType.CODE_INTERPRETER, toolChoice.getToolChoice().getType());
        assertTrue(json.contains("\"tool_choice\":{\"type\":\"code_interpreter\"}"));
        assertTrue(json.contains("\"assistant_id\":\"abc123\""));
    }

    @Test
    public void createRunOptionsAutoMode() {
        CreateRunOptions options = new CreateRunOptions("abc123");
        options.setToolChoice(new AssistantsApiToolChoiceOption(AssistantsApiToolChoiceOptionMode.AUTO));
        AssistantsApiToolChoiceOption toolChoice = options.getToolChoice();

        BinaryData jsonBinaryData = BinaryData.fromObject(options);
        String json = jsonBinaryData.toString();

        assertEquals(AssistantsApiToolChoiceOptionMode.AUTO, toolChoice.getMode());
        assertTrue(json.contains("\"tool_choice\":\"auto\""));
        assertTrue(json.contains("\"assistant_id\":\"abc123\""));
    }

    @Test
    public void createRunOptionsNoneMode() {
        CreateRunOptions options = new CreateRunOptions("abc123");
        options.setToolChoice(new AssistantsApiToolChoiceOption(AssistantsApiToolChoiceOptionMode.NONE));
        AssistantsApiToolChoiceOption toolChoice = options.getToolChoice();

        BinaryData jsonBinaryData = BinaryData.fromObject(options);
        String json = jsonBinaryData.toString();

        assertEquals(AssistantsApiToolChoiceOptionMode.NONE, toolChoice.getMode());
        assertTrue(json.contains("\"tool_choice\":\"none\""));
        assertTrue(json.contains("\"assistant_id\":\"abc123\""));
    }

    @Test
    public void createRunOptionsFunctionCallToolChoice() {
        CreateRunOptions options = new CreateRunOptions("abc123");
        options.setToolChoice(new AssistantsApiToolChoiceOption(
            new AssistantsNamedToolChoice(AssistantsNamedToolChoiceType.FUNCTION)
                .setFunction(new FunctionName("my_function"))));
        AssistantsApiToolChoiceOption toolChoice = options.getToolChoice();

        BinaryData jsonBinaryData = BinaryData.fromObject(options);
        String json = jsonBinaryData.toString();

        assertNull(toolChoice.getMode());
        assertNotNull(toolChoice.getToolChoice());
        assertEquals(AssistantsNamedToolChoiceType.FUNCTION, toolChoice.getToolChoice().getType());
        assertEquals("my_function", toolChoice.getToolChoice().getFunction().getName());
        assertTrue(json.contains("\"tool_choice\":{\"type\":\"function\",\"function\":{\"name\":\"my_function\"}}"));
        assertTrue(json.contains("\"assistant_id\":\"abc123\""));
    }

    @Test
    public void createRunOptionsFileSearchToolChoice() {
        CreateRunOptions options = new CreateRunOptions("abc123");
        options.setToolChoice(new AssistantsApiToolChoiceOption(
            new AssistantsNamedToolChoice(AssistantsNamedToolChoiceType.FILE_SEARCH)));
        AssistantsApiToolChoiceOption toolChoice = options.getToolChoice();

        BinaryData jsonBinaryData = BinaryData.fromObject(options);
        String json = jsonBinaryData.toString();

        assertNull(toolChoice.getMode());
        assertNotNull(toolChoice.getToolChoice());
        assertEquals(AssistantsNamedToolChoiceType.FILE_SEARCH, toolChoice.getToolChoice().getType());
        assertTrue(json.contains("\"tool_choice\":{\"type\":\"file_search\"}"));
        assertTrue(json.contains("\"assistant_id\":\"abc123\""));
    }

    @Test
    public void createRunOptionsCodeInterpreterToolChoice() {
        CreateRunOptions options = new CreateRunOptions("abc123");
        options.setToolChoice(new AssistantsApiToolChoiceOption(
            new AssistantsNamedToolChoice(AssistantsNamedToolChoiceType.CODE_INTERPRETER)));
        AssistantsApiToolChoiceOption toolChoice = options.getToolChoice();

        BinaryData jsonBinaryData = BinaryData.fromObject(options);
        String json = jsonBinaryData.toString();

        assertNull(toolChoice.getMode());
        assertNotNull(toolChoice.getToolChoice());
        assertEquals(AssistantsNamedToolChoiceType.CODE_INTERPRETER, toolChoice.getToolChoice().getType());
        assertTrue(json.contains("\"tool_choice\":{\"type\":\"code_interpreter\"}"));
        assertTrue(json.contains("\"assistant_id\":\"abc123\""));
    }
}

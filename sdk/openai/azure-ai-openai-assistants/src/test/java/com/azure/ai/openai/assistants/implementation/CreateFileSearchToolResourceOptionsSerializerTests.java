// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants.implementation;

import com.azure.ai.openai.assistants.models.AssistantCreationOptions;
import com.azure.ai.openai.assistants.models.AssistantThreadCreationOptions;
import com.azure.ai.openai.assistants.models.CreateCodeInterpreterToolResourceOptions;
import com.azure.ai.openai.assistants.models.CreateFileSearchToolResourceOptions;
import com.azure.ai.openai.assistants.models.CreateFileSearchToolResourceVectorStoreIds;
import com.azure.ai.openai.assistants.models.CreateToolResourcesOptions;
import com.azure.core.util.BinaryData;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CreateFileSearchToolResourceOptionsSerializerTests {

    @Test
    public void assistantCreationOptionToolResourceAbsent() {
        AssistantCreationOptions assistantCreationOptions = new AssistantCreationOptions("gpt-4");

        BinaryData jsonBinaryData = BinaryData.fromObject(assistantCreationOptions);
        String json = jsonBinaryData.toString();

        assertFalse(json.contains("\"tool_resources\""));
        assertTrue(json.contains("\"model\":\"gpt-4\""));
    }

    @Test
    public void assistantCreationOptionToolResourceNull() {
        AssistantCreationOptions assistantCreationOptions = new AssistantCreationOptions("gpt-4");
        assistantCreationOptions.setToolResources(null);

        BinaryData jsonBinaryData = BinaryData.fromObject(assistantCreationOptions);
        String json = jsonBinaryData.toString();

        // Explicit nullability is not encoded by the generated SDK code
        assertFalse(json.contains("\"tool_resources\":null"));
        assertTrue(json.contains("\"model\":\"gpt-4\""));
    }

    @Test
    public void assistantCreationOptionCodeInterpreter() {
        AssistantCreationOptions assistantCreationOptions = new AssistantCreationOptions("gpt-4");
        CreateToolResourcesOptions toolResourcesOptions = new CreateToolResourcesOptions();
        CreateCodeInterpreterToolResourceOptions codeInterpreterToolResources = new CreateCodeInterpreterToolResourceOptions();
        codeInterpreterToolResources.setFileIds(Arrays.asList("file1", "file2"));
        toolResourcesOptions.setCodeInterpreter(codeInterpreterToolResources);
        assistantCreationOptions.setToolResources(toolResourcesOptions);

        BinaryData jsonBinaryData = BinaryData.fromObject(assistantCreationOptions);
        String json = jsonBinaryData.toString();

        assertTrue(json.contains("\"tool_resources\":{\"code_interpreter\":{\"file_ids\":[\"file1\",\"file2\"]}}"));
        assertTrue(json.contains("\"model\":\"gpt-4\""));
    }

    @Test
    public void assistantCreationOptionFileSearchVectorStoreIDs() {
        AssistantCreationOptions assistantCreationOptions = new AssistantCreationOptions("gpt-4");
        CreateToolResourcesOptions toolResourcesOptions = new CreateToolResourcesOptions();
        CreateFileSearchToolResourceOptions codeInterpreterToolResources =
            new CreateFileSearchToolResourceOptions(
                new CreateFileSearchToolResourceVectorStoreIds(Arrays.asList("vectorStoreId1", "vectorStoreId2")));
        toolResourcesOptions.setFileSearch(codeInterpreterToolResources);
        assistantCreationOptions.setToolResources(toolResourcesOptions);

        BinaryData jsonBinaryData = BinaryData.fromObject(assistantCreationOptions);
        String json = jsonBinaryData.toString();

        assertTrue(json.contains("\"tool_resources\":{\"file_search\":{\"vector_store_ids\":[\"vectorStoreId1\",\"vectorStoreId2\"]}}"));
        assertTrue(json.contains("\"model\":\"gpt-4\""));
    }

    @Test
    public void assistantThreadCreationOptionToolResourceAbsent() {
        AssistantThreadCreationOptions assistantThreadCreationOptions = new AssistantThreadCreationOptions();

        BinaryData jsonBinaryData = BinaryData.fromObject(assistantThreadCreationOptions);
        String json = jsonBinaryData.toString();

        assertFalse(json.contains("\"tool_resources\""));
    }

    @Test
    public void assistantThreadCreationOptionToolResourceNull() {
        AssistantThreadCreationOptions assistantThreadCreationOptions = new AssistantThreadCreationOptions();
        assistantThreadCreationOptions.setToolResources(null);

        BinaryData jsonBinaryData = BinaryData.fromObject(assistantThreadCreationOptions);
        String json = jsonBinaryData.toString();

        // Explicit nullability is not encoded by the generated SDK code
        assertFalse(json.contains("\"tool_resources\":null"));
    }

    @Test
    public void assistantThreadCreationOptionCodeInterpreter() {
        AssistantThreadCreationOptions assistantThreadCreationOptions = new AssistantThreadCreationOptions();
        CreateToolResourcesOptions toolResourcesOptions = new CreateToolResourcesOptions();
        CreateCodeInterpreterToolResourceOptions codeInterpreterToolResources = new CreateCodeInterpreterToolResourceOptions();
        codeInterpreterToolResources.setFileIds(Arrays.asList("file1", "file2"));
        toolResourcesOptions.setCodeInterpreter(codeInterpreterToolResources);
        assistantThreadCreationOptions.setToolResources(toolResourcesOptions);

        BinaryData jsonBinaryData = BinaryData.fromObject(assistantThreadCreationOptions);
        String json = jsonBinaryData.toString();

        assertTrue(json.contains("\"tool_resources\":{\"code_interpreter\":{\"file_ids\":[\"file1\",\"file2\"]}}"));
    }

    @Test
    public void assistantThreadCreationOptionFileSearchVectorStoreIDs() {
        AssistantThreadCreationOptions assistantThreadCreationOptions = new AssistantThreadCreationOptions();
        CreateToolResourcesOptions toolResourcesOptions = new CreateToolResourcesOptions();
        CreateFileSearchToolResourceOptions codeInterpreterToolResources =
            new CreateFileSearchToolResourceOptions(
                new CreateFileSearchToolResourceVectorStoreIds(Arrays.asList("vectorStoreId1", "vectorStoreId2")));
        toolResourcesOptions.setFileSearch(codeInterpreterToolResources);
        assistantThreadCreationOptions.setToolResources(toolResourcesOptions);

        BinaryData jsonBinaryData = BinaryData.fromObject(assistantThreadCreationOptions);
        String json = jsonBinaryData.toString();

        assertTrue(json.contains("\"tool_resources\":{\"file_search\":{\"vector_store_ids\":[\"vectorStoreId1\",\"vectorStoreId2\"]}}"));
    }
}

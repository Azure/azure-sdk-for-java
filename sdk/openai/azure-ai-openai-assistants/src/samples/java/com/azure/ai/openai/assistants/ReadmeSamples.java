// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.Assistant;
import com.azure.ai.openai.assistants.models.AssistantCreationOptions;
import com.azure.ai.openai.assistants.models.AssistantThread;
import com.azure.ai.openai.assistants.models.AssistantThreadCreationOptions;
import com.azure.ai.openai.assistants.models.CreateAndRunThreadOptions;
import com.azure.ai.openai.assistants.models.CreateRunOptions;
import com.azure.ai.openai.assistants.models.FileDetails;
import com.azure.ai.openai.assistants.models.FilePurpose;
import com.azure.ai.openai.assistants.models.FunctionDefinition;
import com.azure.ai.openai.assistants.models.FunctionToolDefinition;
import com.azure.ai.openai.assistants.models.MessageContent;
import com.azure.ai.openai.assistants.models.MessageImageFileContent;
import com.azure.ai.openai.assistants.models.MessageRole;
import com.azure.ai.openai.assistants.models.MessageTextContent;
import com.azure.ai.openai.assistants.models.MessageTextDetails;
import com.azure.ai.openai.assistants.models.OpenAIFile;
import com.azure.ai.openai.assistants.models.OpenAIPageableListOfThreadMessage;
import com.azure.ai.openai.assistants.models.RequiredFunctionToolCall;
import com.azure.ai.openai.assistants.models.RequiredToolCall;
import com.azure.ai.openai.assistants.models.RetrievalToolDefinition;
import com.azure.ai.openai.assistants.models.RunStatus;
import com.azure.ai.openai.assistants.models.SubmitToolOutputsAction;
import com.azure.ai.openai.assistants.models.ThreadInitializationMessage;
import com.azure.ai.openai.assistants.models.ThreadMessage;
import com.azure.ai.openai.assistants.models.ThreadRun;
import com.azure.ai.openai.assistants.models.ToolOutput;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.KeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.core.util.serializer.TypeReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ReadmeSamples {
    private AssistantsClient client = new AssistantsClientBuilder().buildClient();

    public void createSyncClientKeyCredential() {
        // BEGIN: readme-sample-createSyncClientKeyCredential
        AssistantsClient client = new AssistantsClientBuilder()
                .credential(new AzureKeyCredential("{key}"))
                .endpoint("{endpoint}")
                .buildClient();
        // END: readme-sample-createSyncClientKeyCredential
    }

    public void createAsyncClientKeyCredential() {
        // BEGIN: readme-sample-createAsyncClientKeyCredential
        AssistantsAsyncClient client = new AssistantsClientBuilder()
                .credential(new AzureKeyCredential("{key}"))
                .endpoint("{endpoint}")
                .buildAsyncClient();
        // END: readme-sample-createAsyncClientKeyCredential
    }

    public void createNonAzureSyncClientWithApiKey() {
        // BEGIN: readme-sample-createNonAzureAssistantSyncClientApiKey
        AssistantsClient client = new AssistantsClientBuilder()
                .credential(new KeyCredential("{openai-secret-key}"))
                .buildClient();
        // END: readme-sample-createNonAzureAssistantSyncClientApiKey
    }

    public void createNonAzureAsyncClientWithApiKey() {
        // BEGIN: readme-sample-createNonAzureAssistantAsyncClientApiKey
        AssistantsAsyncClient client = new AssistantsClientBuilder()
                .credential(new KeyCredential("{openai-secret-key}"))
                .buildAsyncClient();
        // END: readme-sample-createNonAzureAssistantAsyncClientApiKey
    }

    public void createAssistant() {
        // BEGIN: readme-sample-createAssistant
        AssistantCreationOptions assistantCreationOptions = new AssistantCreationOptions("{deploymentOrModelId}")
                .setName("Math Tutor")
                .setInstructions("You are a personal math tutor. Answer questions briefly, in a sentence or less.");
        Assistant assistant = client.createAssistant(assistantCreationOptions);
        // END: readme-sample-createAssistant
    }

    @Test
    public void simpleMathAssistantOperations() throws InterruptedException {
        String apiKey = Configuration.getGlobalConfiguration().get("NON_AZURE_OPENAI_KEY");
        String deploymentOrModelId = "gpt-4-1106-preview";
        client = new AssistantsClientBuilder()
                .credential(new KeyCredential(apiKey))
                .buildClient();
        // Create a new assistant
        AssistantCreationOptions assistantCreationOptions = new AssistantCreationOptions(deploymentOrModelId)
                .setName("Math Tutor")
                .setInstructions("You are a personal math tutor. Answer questions briefly, in a sentence or less.");
        Assistant assistant = client.createAssistant(assistantCreationOptions);
        System.out.printf("Assistant ID = \"%s\" is created at %s.%n", assistant.getId(), assistant.getCreatedAt());
        String assistantId = assistant.getId();

        // BEGIN: readme-sample-createThread
        AssistantThread thread = client.createThread(new AssistantThreadCreationOptions());
        String threadId = thread.getId();
        // END: readme-sample-createThread

        // BEGIN: readme-sample-createMessage
        String userMessage = "I need to solve the equation `3x + 11 = 14`. Can you help me?";
        ThreadMessage threadMessage = client.createMessage(threadId, MessageRole.USER, userMessage);
        // END: readme-sample-createMessage

        // BEGIN: readme-sample-createRun
        ThreadRun run = client.createRun(threadId, new CreateRunOptions(assistantId));
        // END: readme-sample-createRun

        // BEGIN: readme-sample-createThreadAndRun
        CreateAndRunThreadOptions createAndRunThreadOptions = new CreateAndRunThreadOptions(assistantId)
                .setThread(new AssistantThreadCreationOptions()
                        .setMessages(Arrays.asList(new ThreadInitializationMessage(MessageRole.USER,
                                "I need to solve the equation `3x + 11 = 14`. Can you help me?"))));
        run = client.createThreadAndRun(createAndRunThreadOptions);
        // END: readme-sample-createThreadAndRun

        // BEGIN: readme-sample-pollRun
        do {
            run = client.getRun(run.getThreadId(), run.getId());
            Thread.sleep(1000);
        } while (run.getStatus() == RunStatus.QUEUED || run.getStatus() == RunStatus.IN_PROGRESS);
        // END: readme-sample-pollRun

        // BEGIN: readme-sample-listMessagesAfterRun
        OpenAIPageableListOfThreadMessage messages = client.listMessages(run.getThreadId());
        List<ThreadMessage> data = messages.getData();
        for (int i = 0; i < data.size(); i++) {
            ThreadMessage dataMessage = data.get(i);
            MessageRole role = dataMessage.getRole();
            for (MessageContent messageContent : dataMessage.getContent()) {
                MessageTextContent messageTextContent = (MessageTextContent) messageContent;
                System.out.println(i + ": Role = " + role + ", content = " + messageTextContent.getText().getValue());
            }
        }
        // END: readme-sample-listMessagesAfterRun
    }

    @Test
    public void simpleRetrievalOperation() throws InterruptedException {
        String apiKey = Configuration.getGlobalConfiguration().get("NON_AZURE_OPENAI_KEY");
        String deploymentOrModelId = "gpt-4-1106-preview";
        String fileName = "retrieval_sample_java_aoai_sdk.txt";
        client = new AssistantsClientBuilder()
            .credential(new KeyCredential(apiKey))
            .buildClient();

        // BEGIN: readme-sample-uploadFile
        Path filePath = Paths.get("src", "samples", "resources", fileName);
        BinaryData fileData = BinaryData.fromFile(filePath);
        FileDetails fileDetails = new FileDetails(fileData).setFilename(fileName);

        OpenAIFile openAIFile = client.uploadFile(fileDetails, FilePurpose.ASSISTANTS);
        // END: readme-sample-uploadFile

        // BEGIN: readme-sample-createRetrievalAssistant
        Assistant assistant = client.createAssistant(
            new AssistantCreationOptions(deploymentOrModelId)
                .setName("Java SDK Retrieval Sample")
                .setInstructions("You are a helpful assistant that can help fetch data from files you know about.")
                .setTools(Arrays.asList(new RetrievalToolDefinition()))
                .setFileIds(Arrays.asList(openAIFile.getId()))
        );
        // END: readme-sample-createRetrievalAssistant

        AssistantThread thread = client.createThread(new AssistantThreadCreationOptions());

        // Assign message to thread
        client.createMessage(
            thread.getId(),
            MessageRole.USER,
            "Can you give me the documented codes for 'banana' and 'orange'?");

        // Pass the message to the assistant and start the run
        ThreadRun run = client.createRun(thread, assistant);

        do {
            Thread.sleep(500);
            run = client.getRun(thread.getId(), run.getId());
        } while (run.getStatus() == RunStatus.IN_PROGRESS
            || run.getStatus() == RunStatus.QUEUED);

        OpenAIPageableListOfThreadMessage messages = client.listMessages(thread.getId());
        for (ThreadMessage message : messages.getData()) {
            message.getContent().forEach(content -> {
                if (content instanceof MessageTextContent) {
                    MessageTextDetails messageTextDetails = ((MessageTextContent) content).getText();
                    System.out.println(messageTextDetails.getValue());
                    messageTextDetails.getAnnotations().forEach(annotation ->
                        System.out.println("\tAnnotation start: " + annotation.getStartIndex()
                            + " ,end: " + annotation.getEndIndex() + " ,text: \"" + annotation.getText() + "\""));
                } else if (content instanceof MessageImageFileContent) {
                    System.out.print("Image file ID: ");
                    System.out.println(((MessageImageFileContent) content).getImageFile().getFileId());
                }
            });
        }
    }

    @Test
    public void simpleFunctionCallOperation() throws InterruptedException {
        String apiKey = Configuration.getGlobalConfiguration().get("NON_AZURE_OPENAI_KEY");
        String deploymentOrModelId = "gpt-4-1106-preview";
        client = new AssistantsClientBuilder()
            .credential(new KeyCredential(apiKey))
            .buildClient();

        // BEGIN: readme-sample-createAssistantFunctionCall
        AssistantCreationOptions assistantCreationOptions = new AssistantCreationOptions(deploymentOrModelId)
            .setName("Java Assistants SDK Function Tool Sample Assistant")
            .setInstructions("You are a weather bot. Use the provided functions to help answer questions. "
                + "Customize your responses to the user's preferences as much as possible and use friendly "
                + "nicknames for cities whenever possible.")
            .setTools(Arrays.asList(
                getUserFavoriteCityToolDefinition()
//                getCityNicknameToolDefinition(),
//                getWeatherAtLocationToolDefinition()
            ));

        Assistant assistant = client.createAssistant(assistantCreationOptions);
        // END: readme-sample-createAssistantFunctionCall

        AssistantThread thread = client.createThread(new AssistantThreadCreationOptions());

        // capture user input
        client.createMessage(thread.getId(), MessageRole.USER, "What is the weather like in my favorite city?");

        // Create a run
        ThreadRun run = client.createRun(thread, assistant);

        // BEGIN: readme-sample-functionHandlingRunPolling
        do {
            Thread.sleep(500);
            run = client.getRun(thread.getId(), run.getId());

            if (run.getStatus() == RunStatus.REQUIRES_ACTION
                && run.getRequiredAction() instanceof SubmitToolOutputsAction) {
                SubmitToolOutputsAction requiredAction = (SubmitToolOutputsAction) run.getRequiredAction();
                List<ToolOutput> toolOutputs = new ArrayList<>();

                for (RequiredToolCall toolCall : requiredAction.getSubmitToolOutputs().getToolCalls()) {
                    toolOutputs.add(getResolvedToolOutput(toolCall));
                }
                run = client.submitToolOutputsToRun(thread.getId(), run.getId(), toolOutputs);
            }
        } while (run.getStatus() == RunStatus.QUEUED || run.getStatus() == RunStatus.IN_PROGRESS);
        // END: readme-sample-functionHandlingRunPolling

        OpenAIPageableListOfThreadMessage messagesPage = client.listMessages(thread.getId());
        List<ThreadMessage> messages = messagesPage.getData();

        for (ThreadMessage message : messages) {
            for (MessageContent contentItem : message.getContent()) {
                if (contentItem instanceof MessageTextContent) {
                    System.out.println(((MessageTextContent) contentItem).getText().getValue());
                } else if (contentItem instanceof MessageImageFileContent) {
                    System.out.println(((MessageImageFileContent) contentItem).getImageFile().getFileId());
                }
            }
        }
    }

    public static final String GET_USER_FAVORITE_CITY = "getUserFavoriteCity";
    public static final String GET_CITY_NICKNAME = "getCityNickname";
    public static final String GET_WEATHER_AT_LOCATION = "getWeatherAtLocation";

    // BEGIN: readme-sample-functionDefinition
    private FunctionToolDefinition getUserFavoriteCityToolDefinition() {

        class UserFavoriteCityParameters {

            @JsonProperty("type")
            private String type = "object";

            @JsonProperty("properties")
            private Map<String, Object> properties = new HashMap<>();
        }

        return new FunctionToolDefinition(
            new FunctionDefinition(
                GET_USER_FAVORITE_CITY,
                BinaryData.fromObject(new UserFavoriteCityParameters()
                )
            ).setDescription("Gets the user's favorite city."));
    }
    // END: readme-sample-functionDefinition

    // BEGIN: readme-sample-userDefinedFunctions
    private static String getUserFavoriteCity() {
        return "Seattle, WA";
    }

    private static String getCityNickname(String location) {
        return "The Emerald City";
    }

    private static String getWeatherAtLocation(String location, String temperatureUnit) {
        return temperatureUnit.equals("f") ? "70f" : "21c";
    }
    // END: readme-sample-userDefinedFunctions

    // BEGIN: readme-sample-resolveToolOutput
    private ToolOutput getResolvedToolOutput(RequiredToolCall toolCall) {
        if (toolCall instanceof RequiredFunctionToolCall) {
            RequiredFunctionToolCall functionToolCall = (RequiredFunctionToolCall) toolCall;
            if (functionToolCall.getFunction().getName().equals(GET_USER_FAVORITE_CITY)) {
                return new ToolOutput().setToolCallId(toolCall.getId())
                    .setOutput(getUserFavoriteCity());
            }
            if (functionToolCall.getFunction().getName().equals(GET_CITY_NICKNAME)) {
                Map<String, String> parameters = BinaryData.fromString(
                        functionToolCall.getFunction().getArguments())
                    .toObject(new TypeReference<Map<String, String>>() {});
                String location = parameters.get("location");
                return new ToolOutput().setToolCallId(toolCall.getId())
                    .setOutput(getCityNickname(location));
            }
            if (functionToolCall.getFunction().getName().equals(GET_WEATHER_AT_LOCATION)) {
                Map<String, String> parameters = BinaryData.fromString(
                        functionToolCall.getFunction().getArguments())
                    .toObject(new TypeReference<Map<String, String>>() {});
                String location = parameters.get("location");
                // unit was not marked as required on our Function tool definition, so we need to handle its absence
                String unit = parameters.getOrDefault("unit", "c");
                return new ToolOutput().setToolCallId(toolCall.getId())
                    .setOutput(getWeatherAtLocation(location, unit));
            }
        }
        return null;
    }
    // END: readme-sample-resolveToolOutput
}

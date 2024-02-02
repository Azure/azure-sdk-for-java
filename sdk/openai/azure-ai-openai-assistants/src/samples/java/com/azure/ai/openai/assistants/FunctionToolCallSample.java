// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.Assistant;
import com.azure.ai.openai.assistants.models.AssistantCreationOptions;
import com.azure.ai.openai.assistants.models.AssistantThread;
import com.azure.ai.openai.assistants.models.AssistantThreadCreationOptions;
import com.azure.ai.openai.assistants.models.FunctionDefinition;
import com.azure.ai.openai.assistants.models.FunctionParameters;
import com.azure.ai.openai.assistants.models.FunctionProperties;
import com.azure.ai.openai.assistants.models.FunctionToolCall;
import com.azure.ai.openai.assistants.models.FunctionToolCallDetails;
import com.azure.ai.openai.assistants.models.FunctionToolDefinition;
import com.azure.ai.openai.assistants.models.MessageContent;
import com.azure.ai.openai.assistants.models.MessageImageFileContent;
import com.azure.ai.openai.assistants.models.MessageRole;
import com.azure.ai.openai.assistants.models.MessageTextContent;
import com.azure.ai.openai.assistants.models.OpenAIPageableListOfThreadMessage;
import com.azure.ai.openai.assistants.models.RequiredFunctionToolCall;
import com.azure.ai.openai.assistants.models.RequiredToolCall;
import com.azure.ai.openai.assistants.models.RunStatus;
import com.azure.ai.openai.assistants.models.SubmitToolOutputsAction;
import com.azure.ai.openai.assistants.models.ThreadMessage;
import com.azure.ai.openai.assistants.models.ThreadRun;
import com.azure.ai.openai.assistants.models.ToolOutput;
import com.azure.core.credential.KeyCredential;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.core.util.serializer.TypeReference;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FunctionToolCallSample {

    public static final String GET_USER_FAVORITE_CITY = "getUserFavoriteCity";
    public static final String GET_CITY_NICKNAME = "getCityNickname";
    public static final String GET_WEATHER_AT_LOCATION = "getWeatherAtLocation";

    public static void main(String[] args) throws InterruptedException {
        String apiKey = Configuration.getGlobalConfiguration().get("NON_AZURE_OPENAI_KEY");
        String deploymentOrModelId = "gpt-4-1106-preview";

        AssistantsClient client = new AssistantsClientBuilder()
                .httpLogOptions(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS))
            .credential(new KeyCredential(apiKey))
            .buildClient();

        // Create assistant and thread to be used for the run
        Assistant assistant = createFunctionAssistant(client, deploymentOrModelId);
        AssistantThread thread = createAssistantThread(client);

        // capture user input
        sendUserMessage("What is the weather like in my favorite city?", thread.getId(), client);

        // Create a run
        ThreadRun run = runUserRequest(assistant, thread, client);
        // Pool the run and call methods as requested by the service
        do {
            // We sleep to prevent requesting too many times for an update
            Thread.sleep(500);
            run = client.getRun(thread.getId(), run.getId());
            List<ToolOutput> toolOutputs = new ArrayList<>();

            if (run.getStatus() == RunStatus.REQUIRES_ACTION && run.getRequiredAction() instanceof SubmitToolOutputsAction) {
                SubmitToolOutputsAction requiredAction = (SubmitToolOutputsAction) run.getRequiredAction();
                requiredAction
                        .getSubmitToolOutputs()
                        .getToolCalls()
                        .forEach(toolCall -> {
                            toolOutputs.add(new ToolOutput()
                                    .setToolCallId(toolCall.getId())
                                    .setOutput(toolCall.getFunction().getArguments()));
                        });

                run = client.submitToolOutputsToRun(thread.getId(), run.getId(), toolOutputs);
            }
        } while (run.getStatus() == RunStatus.QUEUED || run.getStatus() == RunStatus.IN_PROGRESS);

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

        // cleanup
        cleanUp(assistant, thread, client);
    }

    private static void cleanUp(Assistant assistant, AssistantThread thread, AssistantsClient client) {
        client.deleteAssistant(assistant.getId());
        client.deleteThread(thread.getId());
    }

    private static ThreadRun runUserRequest(Assistant assistant, AssistantThread thread, AssistantsClient client) {
        return client.createRun(thread, assistant);
    }
    private static void sendUserMessage(String userMessage, String threadId, AssistantsClient client) {
        client.createMessage(threadId, MessageRole.USER, userMessage);
    }

    private static AssistantThread createAssistantThread(AssistantsClient client) {
        return client.createThread(new AssistantThreadCreationOptions());
    }

    private static Assistant createFunctionAssistant(AssistantsClient client, String deploymentModelOrModelId) {
        AssistantCreationOptions assistantCreationOptions = new AssistantCreationOptions(deploymentModelOrModelId)
            .setName("Java Assistants SDK Function Tool Sample Assistant")
            .setInstructions("You are a weather bot. Use the provided functions to help answer questions. "
                + "Customize your responses to the user's preferences as much as possible and use friendly "
                + "nicknames for cities whenever possible.")
            .setTools(Arrays.asList(
                getUserFavoriteCityToolDefinition(),
                getCityNicknameToolDefinition(),
                getWeatherAtLocationToolDefinition()
            ));
        return client.createAssistant(assistantCreationOptions);
    }

    // region getUserFavoriteCity
    private static String getUserFavoriteCity() {
        return "Seattle, WA";
    }

    /**
     * Sample function return user's favorite city. This function is an example of a function with no parameters.
     *
     * @return Function definition for a method that returns the user's favorite city
     */
    private static FunctionToolDefinition getUserFavoriteCityToolDefinition() {
        return new FunctionToolDefinition(new FunctionDefinition(
                GET_USER_FAVORITE_CITY,
                BinaryData.fromObject(getUserFavoriteCityParameters())
        ).setDescription("Gets the user's favorite city."));
    }

    private static FunctionParameters getUserFavoriteCityParameters() {
        FunctionProperties location = new FunctionProperties()
                .setType("string")
                .setDescription("The city and state, e.g. San Francisco, CA");

        Map<String, FunctionProperties> props = new HashMap<>();
        props.put("location", location);

        return new FunctionParameters()
                .setType("object")
                .setProperties(props);
    }

    // region getCityNickName
    /**
     * Mock function for getCityNickname
     * @param location for which the nickname will be retrieved
     * @return nickname of the desired location
     */
    private static String getCityNickname(String location) {
        // This function is just a mock.
        return "The Emerald City is the city nick name of " + location;
    }

    /**
     * Sample function returning the nickname of a city. This function serves as an example of a single mandatory parameter function
     *
     * @return Function tool definition for the getCityNickName function
     */
    private static FunctionToolDefinition getCityNicknameToolDefinition() {
        return new FunctionToolDefinition(new FunctionDefinition(
            GET_CITY_NICKNAME,
            BinaryData.fromObject(getCityNicknameParameters())
        ).setDescription("Gets the nickname of a city, e.g. 'LA' for 'Los Angeles, CA'."));
    }

    private static FunctionParameters getCityNicknameParameters() {
        FunctionProperties location = new FunctionProperties()
                .setType("string")
                .setDescription("The city and state, e.g. San Francisco, CA");

        Map<String, FunctionProperties> props = new HashMap<>();
        props.put("location", location);

        return new FunctionParameters()
                .setType("object")
                .setRequiredPropertyNames(Arrays.asList("location"))
                .setProperties(props);
    }

    // endregion

    // region getWeatherAtLocation
    /**
     * Mock function for getWeatherAtLocation
     * @param location for which the temperature will be retrieved
     * @param temperatureUnit unit of the temperature measurement
     * @return temperature in the requested unit for the desired location
     */
    private static String getWeatherAtLocation(String location, String temperatureUnit) {
        return temperatureUnit.equals("f") ? "70f" : "21c";
    }

    /**
     * Sample function returning the temperature of a city.
     * This function serves as an example of a single mandatory parameter function and an enum value.
     *
     * @return Function tool definition for the getCityNickName function
     */
    private static FunctionToolDefinition getWeatherAtLocationToolDefinition() {
        return new FunctionToolDefinition(new FunctionDefinition(
                GET_WEATHER_AT_LOCATION,
                BinaryData.fromObject(getCurrentWeatherFunctionParameters())
        ).setDescription("Gets the current weather at a provided location."));
    }

    private static FunctionParameters getCurrentWeatherFunctionParameters() {
        FunctionProperties location = new FunctionProperties()
                .setType("string")
                .setDescription("The city and state, e.g. San Francisco, CA");

        FunctionProperties unit = new FunctionProperties()
                .setType("string")
                .setEnumString(Arrays.asList("celsius", "fahrenheit"))
                .setDescription("The temperature unit to use. Infer this from the user's location.");

        Map<String, FunctionProperties> props = new HashMap<>();
        props.put("location", location);
        props.put("unit", unit);

        return new FunctionParameters()
                .setType("object")
                .setRequiredPropertyNames(Arrays.asList("location", "unit"))
                .setProperties(props);
    }
}

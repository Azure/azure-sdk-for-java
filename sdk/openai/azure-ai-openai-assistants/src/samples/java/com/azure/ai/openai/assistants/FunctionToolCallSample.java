// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.ai.openai.assistants;

import com.azure.ai.openai.assistants.models.Assistant;
import com.azure.ai.openai.assistants.models.AssistantCreationOptions;
import com.azure.ai.openai.assistants.models.AssistantThread;
import com.azure.ai.openai.assistants.models.AssistantThreadCreationOptions;
import com.azure.ai.openai.assistants.models.FunctionDefinition;
import com.azure.ai.openai.assistants.models.FunctionToolDefinition;
import com.azure.ai.openai.assistants.models.MessageContent;
import com.azure.ai.openai.assistants.models.MessageImageFileContent;
import com.azure.ai.openai.assistants.models.MessageRole;
import com.azure.ai.openai.assistants.models.MessageTextContent;
import com.azure.ai.openai.assistants.models.PageableList;
import com.azure.ai.openai.assistants.models.RequiredFunctionToolCall;
import com.azure.ai.openai.assistants.models.RequiredFunctionToolCallDetails;
import com.azure.ai.openai.assistants.models.RequiredToolCall;
import com.azure.ai.openai.assistants.models.RunStatus;
import com.azure.ai.openai.assistants.models.SubmitToolOutputsAction;
import com.azure.ai.openai.assistants.models.ThreadMessage;
import com.azure.ai.openai.assistants.models.ThreadMessageOptions;
import com.azure.ai.openai.assistants.models.ThreadRun;
import com.azure.ai.openai.assistants.models.ToolOutput;
import com.azure.core.credential.KeyCredential;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Configuration;
import com.azure.core.util.serializer.TypeReference;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FunctionToolCallSample {

    public static final String GET_USER_FAVORITE_CITY = "getUserFavoriteCity";
    public static final String GET_CITY_NICKNAME = "getCityNickname";
    public static final String GET_WEATHER_AT_LOCATION = "getWeatherAtLocation";

    public static void main(String[] args) throws InterruptedException {
        String apiKey = Configuration.getGlobalConfiguration().get("NON_AZURE_OPENAI_KEY");
        String deploymentOrModelId = "gpt-4-1106-preview";

        AssistantsClient client = new AssistantsClientBuilder()
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
            Thread.sleep(1000);
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

        PageableList<ThreadMessage> messagesPage = client.listMessages(thread.getId());
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

    private static ToolOutput getResolvedToolOutput(RequiredToolCall toolCall) {
        if (toolCall instanceof RequiredFunctionToolCall) {
            RequiredFunctionToolCall functionToolCall = (RequiredFunctionToolCall) toolCall;
            RequiredFunctionToolCallDetails functionCallDetails = functionToolCall.getFunction();
            String name = functionCallDetails.getName();
            String arguments = functionCallDetails.getArguments();
            ToolOutput toolOutput = new ToolOutput().setToolCallId(toolCall.getId());
            if (GET_USER_FAVORITE_CITY.equals(name)) {
                toolOutput.setOutput(getUserFavoriteCity());
            } else if (GET_CITY_NICKNAME.equals(name)) {
                Map<String, String> parameters = BinaryData.fromString(arguments)
                        .toObject(new TypeReference<Map<String, String>>() {});
                String location = parameters.get("location");

                toolOutput.setOutput(getCityNickname(location));
            } else if (GET_WEATHER_AT_LOCATION.equals(name)) {
                Map<String, String> parameters = BinaryData.fromString(arguments)
                        .toObject(new TypeReference<Map<String, String>>() {});
                String location = parameters.get("location");
                // unit was not marked as required on our Function tool definition, so we need to handle its absence
                String unit = parameters.getOrDefault("unit", "c");

                toolOutput.setOutput(getWeatherAtLocation(location, unit));
            }
            return toolOutput;
        }
        throw new IllegalArgumentException("Tool call not supported: " + toolCall.getClass());
    }

    private static void cleanUp(Assistant assistant, AssistantThread thread, AssistantsClient client) {
        client.deleteAssistant(assistant.getId());
        client.deleteThread(thread.getId());
    }

    private static ThreadRun runUserRequest(Assistant assistant, AssistantThread thread, AssistantsClient client) {
        return client.createRun(thread, assistant);
    }
    private static void sendUserMessage(String userMessage, String threadId, AssistantsClient client) {
        client.createMessage(threadId, new ThreadMessageOptions(MessageRole.USER, userMessage));
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
                BinaryData.fromObject(new UserFavoriteCityParameters())
        ).setDescription("Gets the user's favorite city."));
    }

    /**
     * Convenience class defining the parameters for the getUserFavoriteCity method.
     * This is used for the sole purpose of obtaining a JSON representation of the parameters.
     */
    private static class UserFavoriteCityParameters implements JsonSerializable<UserFavoriteCityParameters> {

        private String type = "object";

        private Map<String, JsonSerializable<?>> properties = new HashMap<>();

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            jsonWriter.writeStartObject();
            jsonWriter.writeStringField("type", this.type);
            jsonWriter.writeStartObject("properties");
            for (Map.Entry<String, JsonSerializable<?>> entry : this.properties.entrySet()) {
                jsonWriter.writeFieldName(entry.getKey());
                entry.getValue().toJson(jsonWriter);
            }
            jsonWriter.writeEndObject();
            return jsonWriter.writeEndObject();
        }
    }
    // endregion

    // region getCityNickName
    /**
     * Mock function for getCityNickname
     * @param location for which the nickname will be retrieved
     * @return nickname of the desired location
     */
    private static String getCityNickname(String location) {
        // This function is just a mock.
        return "The Emerald City";
    }

    /**
     * Sample function returning the nickname of a city. This function serves as an example of a single mandatory parameter function
     *
     * @return Function tool definition for the getCityNickName function
     */
    static FunctionToolDefinition getCityNicknameToolDefinition() {
        return new FunctionToolDefinition(new FunctionDefinition(
            GET_CITY_NICKNAME,
            BinaryData.fromObject(new CityNicknameParameters())
        ).setDescription("Gets the nickname of a city, e.g. 'LA' for 'Los Angeles, CA'."));
    }

    /**
     * Convenience class defining the parameters for the getCityNickname method.
     * This is used for the sole purpose of obtaining a JSON representation of the parameters.
     */
    private static class CityNicknameParameters implements JsonSerializable<CityNicknameParameters> {

        private String type = "object";

        private List<String> required = Arrays.asList("location");

        private Map<String, StringParameter> properties;

        CityNicknameParameters() {
            this.properties = new HashMap<>();

            this.properties.put("location", new StringParameter("The city and state, e.g. San Francisco, CA"));
        }

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            jsonWriter.writeStartObject();
            jsonWriter.writeStringField("type", this.type);
            jsonWriter.writeArrayField("required", this.required, (writer, s) -> writer.writeString(s));
            jsonWriter.writeStartObject();

            jsonWriter.writeEndObject();
            return jsonWriter.writeEndObject();
        }
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
    static FunctionToolDefinition getWeatherAtLocationToolDefinition() {
        return new FunctionToolDefinition(new FunctionDefinition(
            GET_WEATHER_AT_LOCATION,
            BinaryData.fromObject(new WeatherAtLocationParameters())
        ).setDescription("Gets the current weather at a provided location."));
    }

    /**
     * Convenience class defining the parameters for the getWeatherAtLocation method.
     * This is used for the sole purpose of obtaining a JSON representation of the parameters.
     */
    private static class WeatherAtLocationParameters implements JsonSerializable<WeatherAtLocationParameters> {

        private final String type = "object";

        private final List<String> required = Arrays.asList("location");

        private final Map<String, JsonSerializable<?>> properties;

        WeatherAtLocationParameters() {
            this.properties = new HashMap<>();

            this.properties.put("location", new StringParameter("The city and state, e.g. San Francisco, CA"));
            this.properties.put("unit", new EnumParameter());
        }

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            jsonWriter.writeStartObject();
            jsonWriter.writeStringField("type", this.type);
            jsonWriter.writeArrayField("required", this.required, (writer, s) -> writer.writeString(s));
            jsonWriter.writeStartObject("properties");
            for (Map.Entry<String, JsonSerializable<?>> entry : this.properties.entrySet()) {
                jsonWriter.writeFieldName(entry.getKey());
                entry.getValue().toJson(jsonWriter);
            }
            jsonWriter.writeEndObject();
            return jsonWriter.writeEndObject();
        }
    }
    // endregion

    // region FunctionToolCall Helpers
    /**
     * Function Tool call definition helper class for String parameters
     */
    private static class StringParameter implements JsonSerializable<StringParameter> {

        private final String type = "string";

        private final String description;

        StringParameter(String description) {
            this.description = description;
        }

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            jsonWriter.writeStartObject();
            jsonWriter.writeStringField("type", this.type);
            jsonWriter.writeStringField("description", this.description);
            return jsonWriter.writeEndObject();
        }
    }

    /**
     * Function Tool call definition helper class for enum parameters
     */
    private static class EnumParameter implements JsonSerializable<EnumParameter> {

        private final String type = "string";

        private final List<String> enumValues = Arrays.asList("c", "f");

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            jsonWriter.writeStartObject();
            jsonWriter.writeStringField("type", this.type);
            jsonWriter.writeArrayField("enum", this.enumValues, (writer, s) -> writer.writeString(s));
            return jsonWriter.writeEndObject();
        }
    }
    // endregion
}

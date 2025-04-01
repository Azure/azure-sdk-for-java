package com.azure.ai.openai.implementation.mappers;

import com.azure.ai.openai.implementation.mappers.visitors.ChatCompletionMessageParamVisitor;
import com.azure.ai.openai.implementation.mappers.visitors.ChatCompletionRequestMessageVisitor;
import com.azure.ai.openai.models.AzureChatDataSource;
import com.azure.ai.openai.models.AzureCreateChatCompletionRequest;
import com.azure.ai.openai.models.ChatCompletionRequestMessage;
import com.azure.core.util.BinaryData;
import com.azure.json.JsonProviders;
import com.azure.json.JsonWriter;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.openai.core.JsonArray;
import com.openai.core.JsonValue;
import com.openai.core.ObjectMappers;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionMessageParam;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class AzureChatMapper {

    public static ChatCompletionCreateParams from(AzureCreateChatCompletionRequest request, String deploymentId) {
        return from(request, deploymentId, false);
    }

    public static ChatCompletionCreateParams from(AzureCreateChatCompletionRequest request, String deploymentId, boolean isStream) {
        if (request == null) {
            return null;
        }

        ChatCompletionCreateParams.Builder builder = ChatCompletionCreateParams.builder()
            .model(deploymentId);

        // Map messages
        List<ChatCompletionMessageParam> messageParams = request.getMessages().stream()
            .map(ChatCompletionRequestMessageVisitor::accept)
            .collect(Collectors.toList());
        builder.messages(messageParams);

        // Map optional fields
        if (request.getTemperature() != null) {
            builder.temperature(request.getTemperature());
        }
        if (request.getTopP() != null) {
            builder.topP(request.getTopP());
        }
        if (request.getN() != null) {
            builder.n(request.getN());
        }
        if (request.isStream() != null) {
            // TODO jpalvarezl: this type is totally different in OpenAI
//            builder.streamOptions(new ChatCompletionStreamOptions.Builder().)
//            builder.stream(request.isStream());
        }
        if (request.getStop() != null) {
//            try {
//                builder.stop()
//                builder.stop(request.getStop().toObject(Object.class));
//            } catch (IOException e) {
//                //TODO jpalvarezl: Handle stop conversion error
//            }
        }
        if (request.getUser() != null) {
            builder.user(request.getUser());
        }
        if (request.getPresencePenalty() != null) {
            builder.presencePenalty(request.getPresencePenalty());
        }
        if (request.getFrequencyPenalty() != null) {
            builder.frequencyPenalty(request.getFrequencyPenalty());
        }
        if (request.getLogitBias() != null) {
            ChatCompletionCreateParams.LogitBias.Builder logitBiasBuilder = new ChatCompletionCreateParams.LogitBias.Builder();
            for (Map.Entry<String, Integer> entry : request.getLogitBias().entrySet()) {
                logitBiasBuilder.putAdditionalProperty(entry.getKey(), JsonValue.from(entry.getValue()));
            }
            builder.logitBias(logitBiasBuilder.build());
        }
        if (request.isLogprobs() != null) {
            builder.logprobs(request.isLogprobs());
        }
        if (request.getMaxTokens() != null) {
            builder.maxTokens(request.getMaxTokens());
        }
        if (request.getStreamOptions() != null) {
            //TODO jpalvarezl: Map ChatCompletionStreamOptions to appropriate type
        }
        if (request.getTools() != null) {
            //TODO jpalvarezl: Map List<ChatCompletionTool> to appropriate type
        }
        if (request.getToolChoice() != null) {
//            try {
//                builder.toolChoice(request.getToolChoice().toObject(Object.class));
//            } catch (IOException e) {
//                //TODO jpalvarezl: Handle toolChoice conversion error
//            }
        }
        if (request.isParallelToolCalls() != null) {
            builder.parallelToolCalls(request.isParallelToolCalls());
        }
        if (request.getFunctionCall() != null) {
//            try {
//                builder.functionCall(request.getFunctionCall().toObject(Object.class));
//            } catch (IOException e) {
//                //TODO jpalvarezl: Handle functionCall conversion error
//            }
        }
        if (request.getFunctions() != null) {
            //TODO jpalvarezl: Map List<ChatCompletionFunctions> to appropriate type
        }

        // Azure-specific fields that may need special handling
        if (request.getDataSources() != null) {
            //TODO jpalvarezl: Handle Azure-specific data sources
        }
        if (request.getUserSecurityContext() != null) {
            //TODO jpalvarezl: Handle Azure-specific user security context
        }
        if (request.getModalities() != null) {
            //TODO jpalvarezl: Handle Azure-specific modalities
        }
        if (request.getReasoningEffort() != null) {
            //TODO jpalvarezl: Handle Azure-specific reasoning effort
        }
        if (request.getMaxCompletionTokens() != null) {
            //TODO jpalvarezl: Handle Azure-specific max completion tokens
        }
        if (request.getWebSearchOptions() != null) {
            //TODO jpalvarezl: Handle Azure-specific web search options
        }
        if (request.getTopLogprobs() != null) {
            //TODO jpalvarezl: Handle Azure-specific top logprobs
        }
        if (request.getResponseFormat() != null) {
            //TODO jpalvarezl: Handle Azure-specific response format
        }
        if (request.getAudio() != null) {
            //TODO jpalvarezl: Handle Azure-specific audio options
        }
        if (request.isStore() != null) {
            //TODO jpalvarezl: Handle Azure-specific store option
        }
        if (request.getPrediction() != null) {
            //TODO jpalvarezl: Handle Azure-specific prediction
        }
        if (request.getSeed() != null) {
            builder.seed(request.getSeed());
        }

        // ---------------- Additional Azure fields:
//        byte[] dataSourcesJsonBytes = BinaryData.fromObject(request.getDataSources())
//            .toBytes();

        JsonMapper mapper = ObjectMappers.jsonMapper();
        Map<String, JsonValue> additionalBodyProperties = new HashMap<>();

        // THIS kind of works
//        List<JsonValue> dataSourceJsonValue = Arrays.asList();
//
//        try {
//            for (AzureChatDataSource dataSource : request.getDataSources()) {
//                InputStream a = BinaryData.fromObject(dataSource).toStream();
//                JsonValue b = mapper.readValue(a, new TypeReference<>() {
//                });
//                dataSourceJsonValue.add(b);
//            }
//        } catch (Exception e){}

//        JsonNode node = mapper.valueToTree(request.getDataSources());
//        additionalBodyProperties.put("data_sources", JsonValue.from(dataSourceJsonValue));
        BinaryData dataSourcesBinaryData = BinaryData.fromObject(request.getDataSources());
        try {
            JsonValue  dataSourceJsonValue = mapper.readValue(dataSourcesBinaryData.toStream(), new TypeReference<>() {});
            additionalBodyProperties.put("data_sources", dataSourceJsonValue);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        builder.additionalBodyProperties(additionalBodyProperties);

        return builder.build();
    }

//    public static AzureCreateChatCompletionRequest from(ChatCompletionCreateParams request) {
//        request.messages();
//        if (request == null) {
//            return null;
//        }
//        AzureCreateChatCompletionRequest azureCreateChatCompletionRequest = new AzureCreateChatCompletionRequest(from(request.messages()));
////        azureCreateChatCompletionRequest.setModel(createChatCompletionRequest.getModel());
//
//        request.temperature().ifPresent(azureCreateChatCompletionRequest::setTemperature);
//        request.topP().ifPresent(azureCreateChatCompletionRequest::setTopP);
////        request.n().ifPresent(azureCreateChatCompletionRequest::setN);
////        request.streamOptions().ifPresent(azureCreateChatCompletionRequest::setStream);
////        request.stop().ifPresent(azureCreateChatCompletionRequest::setStop);
//        request.user().ifPresent(azureCreateChatCompletionRequest::setUser);
//        request.presencePenalty().ifPresent(azureCreateChatCompletionRequest::setPresencePenalty);
//        request.frequencyPenalty().ifPresent(azureCreateChatCompletionRequest::setFrequencyPenalty);
////        request.logitBias().ifPresent(azureCreateChatCompletionRequest::setLogitBias);
//        request.logprobs().ifPresent(azureCreateChatCompletionRequest::setLogprobs);
//
//        return azureCreateChatCompletionRequest;
//    }

    public static List<ChatCompletionRequestMessage> from(List<ChatCompletionMessageParam> messages) {
        if (messages == null) {
            return null;
        }
        return messages.stream()
            .map(AzureChatMapper::from)
            .collect(Collectors.toList());
    }

    public static ChatCompletionRequestMessage from(ChatCompletionMessageParam message) {
        if (message == null) {
            return null;
        }
        ChatCompletionMessageParamVisitor chatMessageVisitor = new ChatCompletionMessageParamVisitor();
        return message.accept(chatMessageVisitor);
    }
}

package com.azure.ai.openai.implementation.mappers;

import com.azure.ai.openai.models.AzureCreateChatCompletionRequest;
import com.azure.ai.openai.models.ChatCompletionRequestMessage;
import com.azure.core.util.BinaryData;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.openai.core.ObjectMappers;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionMessageParam;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

public class AzureChatMapper {

    public static ChatCompletionCreateParams from(AzureCreateChatCompletionRequest request, String deploymentId) {
        if (request == null) {
            return null;
        }

        BinaryData requestJson = BinaryData.fromObject(request);
        String json = "";

        JsonMapper jsonMapper = ObjectMappers.jsonMapper();

        try {
            ChatCompletionCreateParams params = jsonMapper.readValue(requestJson.toString(), ChatCompletionCreateParams.class);
            return params.toBuilder().model(deploymentId).build();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static AzureCreateChatCompletionRequest from(ChatCompletionCreateParams request) {
        request.messages();
        if (request == null) {
            return null;
        }
        AzureCreateChatCompletionRequest azureCreateChatCompletionRequest = new AzureCreateChatCompletionRequest(from(request.messages()));
//        azureCreateChatCompletionRequest.setModel(createChatCompletionRequest.getModel());

        request.temperature().ifPresent(azureCreateChatCompletionRequest::setTemperature);
        request.topP().ifPresent(azureCreateChatCompletionRequest::setTopP);
//        request.n().ifPresent(azureCreateChatCompletionRequest::setN);
//        request.streamOptions().ifPresent(azureCreateChatCompletionRequest::setStream);
//        request.stop().ifPresent(azureCreateChatCompletionRequest::setStop);
        request.user().ifPresent(azureCreateChatCompletionRequest::setUser);
        request.presencePenalty().ifPresent(azureCreateChatCompletionRequest::setPresencePenalty);
        request.frequencyPenalty().ifPresent(azureCreateChatCompletionRequest::setFrequencyPenalty);
//        request.logitBias().ifPresent(azureCreateChatCompletionRequest::setLogitBias);
        request.logprobs().ifPresent(azureCreateChatCompletionRequest::setLogprobs);

        return azureCreateChatCompletionRequest;
    }

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
        ChatMessageVisitor chatMessageVisitor = new ChatMessageVisitor();
        return message.accept(chatMessageVisitor);
    }
}

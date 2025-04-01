package com.azure.ai.openai.implementation.mappers.visitors;

import com.azure.ai.openai.models.ChatCompletionRequestAssistantMessage;
import com.azure.ai.openai.models.ChatCompletionRequestDeveloperMessage;
import com.azure.ai.openai.models.ChatCompletionRequestFunctionMessage;
import com.azure.ai.openai.models.ChatCompletionRequestMessage;
import com.azure.ai.openai.models.ChatCompletionRequestSystemMessage;
import com.azure.ai.openai.models.ChatCompletionRequestToolMessage;
import com.azure.ai.openai.models.ChatCompletionRequestUserMessage;
import com.openai.core.JsonField;
import com.openai.core.JsonValue;
import com.openai.models.chat.completions.ChatCompletionAssistantMessageParam;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionDeveloperMessageParam;
import com.openai.models.chat.completions.ChatCompletionFunctionMessageParam;
import com.openai.models.chat.completions.ChatCompletionMessageParam;
import com.openai.models.chat.completions.ChatCompletionSystemMessageParam;
import com.openai.models.chat.completions.ChatCompletionToolMessageParam;
import com.openai.models.chat.completions.ChatCompletionUserMessageParam;

public class ChatCompletionRequestMessageVisitor {

    public static ChatCompletionMessageParam accept(ChatCompletionRequestMessage requestMessage) {
        if (requestMessage == null) {
            return null;
        }

        switch (requestMessage.getRole()) {
            case "developer" -> {
                return from((ChatCompletionRequestDeveloperMessage) requestMessage);
            }
            case "system" -> {
                return from((ChatCompletionRequestSystemMessage) requestMessage);
            }
            case "user" -> {
                return from((ChatCompletionRequestUserMessage) requestMessage);
            }
            case "assistant" -> {
                return from((ChatCompletionRequestAssistantMessage) requestMessage);
            }
            case "tool" -> {
                return from((ChatCompletionRequestToolMessage) requestMessage);
            }
            case "function" -> {
                return from((ChatCompletionRequestFunctionMessage) requestMessage);
            }
            default -> throw new IllegalStateException("Unexpected value: " + requestMessage.getRole());
        }
    }

    private static ChatCompletionMessageParam from(ChatCompletionRequestDeveloperMessage message) {
        ChatCompletionDeveloperMessageParam.Builder messageParamBuilder = new ChatCompletionDeveloperMessageParam.Builder();
        messageParamBuilder.name(message.getName());
        // TODO jpalvarezl: implement
        messageParamBuilder.content(message.getContent().toString());
        return ChatCompletionMessageParam.ofDeveloper(messageParamBuilder.build());
    }

    private static ChatCompletionMessageParam from(ChatCompletionRequestSystemMessage message) {
        ChatCompletionSystemMessageParam.Builder messageParamBuilder = new ChatCompletionSystemMessageParam.Builder();
        // TODO jpalvarezl: implement
        return ChatCompletionMessageParam.ofSystem(messageParamBuilder.build());
    }

    private static ChatCompletionMessageParam from(ChatCompletionRequestUserMessage message) {
        ChatCompletionUserMessageParam.Builder messageParamBuilder = new ChatCompletionUserMessageParam.Builder();
        if (message.getName() != null) {
            messageParamBuilder.name(message.getName());
        }
        // TODO jpalvarezl: implement support for other Content types
        messageParamBuilder.content(message.getContent().toString());
        return ChatCompletionMessageParam.ofUser(messageParamBuilder.build());
    }

    private static ChatCompletionMessageParam from(ChatCompletionRequestAssistantMessage message) {
        ChatCompletionAssistantMessageParam.Builder messageParamBuilder = new ChatCompletionAssistantMessageParam.Builder();
        // TODO jpalvarezl: implement
        return ChatCompletionMessageParam.ofAssistant(messageParamBuilder.build());
    }

    private static ChatCompletionMessageParam from(ChatCompletionRequestToolMessage message) {
        ChatCompletionToolMessageParam .Builder messageParamBuilder = new ChatCompletionToolMessageParam.Builder();
        // TODO jpalvarezl: implement
        return ChatCompletionMessageParam.ofTool(messageParamBuilder.build());
    }

    private static ChatCompletionMessageParam from(ChatCompletionRequestFunctionMessage message) {
        ChatCompletionFunctionMessageParam.Builder messageParamBuilder = new ChatCompletionFunctionMessageParam.Builder();
        // TODO jpalvarezl: implement
        return ChatCompletionMessageParam.ofFunction(messageParamBuilder.build());
    }
}

package com.azure.ai.openai.implementation.mappers.visitors;

import com.azure.ai.openai.models.ChatCompletionRequestAssistantMessage;
import com.azure.ai.openai.models.ChatCompletionRequestAssistantMessageAudio;
import com.azure.ai.openai.models.ChatCompletionRequestMessage;
import com.azure.ai.openai.models.ChatCompletionRequestUserMessage;
import com.openai.core.JsonValue;
import com.openai.models.chat.completions.ChatCompletionAssistantMessageParam;
import com.openai.models.chat.completions.ChatCompletionDeveloperMessageParam;
import com.openai.models.chat.completions.ChatCompletionFunctionMessageParam;
import com.openai.models.chat.completions.ChatCompletionMessageParam;
import com.openai.models.chat.completions.ChatCompletionSystemMessageParam;
import com.openai.models.chat.completions.ChatCompletionToolMessageParam;
import com.openai.models.chat.completions.ChatCompletionUserMessageParam;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ChatCompletionMessageParamVisitor implements ChatCompletionMessageParam.Visitor<ChatCompletionRequestMessage> {

    @Override
    public ChatCompletionRequestMessage unknown(@Nullable JsonValue json) {
        return ChatCompletionMessageParam.Visitor.super.unknown(json);
    }

    @Override
    public ChatCompletionRequestMessage visitAssistant(@NotNull ChatCompletionAssistantMessageParam chatCompletionAssistantMessageParam) {
        ChatCompletionRequestAssistantMessage message = new ChatCompletionRequestAssistantMessage();
        AssistantContentVisitor assistantContentVisitor = new AssistantContentVisitor();

        chatCompletionAssistantMessageParam.name().ifPresent(message::setName);
        chatCompletionAssistantMessageParam.audio().ifPresent(it -> message.setAudio(new ChatCompletionRequestAssistantMessageAudio(it.id())));
        chatCompletionAssistantMessageParam.content().ifPresent(it -> message.setContent(it.accept(assistantContentVisitor)));
        chatCompletionAssistantMessageParam.refusal().ifPresent(message::setRefusal);
        // TODO jpalvarezl: map the rest of the fields
//        chatCompletionAssistantMessageParam
        return message;
    }

    @Override
    public ChatCompletionRequestMessage visitDeveloper(@NotNull ChatCompletionDeveloperMessageParam chatCompletionDeveloperMessageParam) {
        throw new UnsupportedOperationException("Developer request messages not yet supported.");
    }

    @Override
    public ChatCompletionRequestMessage visitFunction(@NotNull ChatCompletionFunctionMessageParam chatCompletionFunctionMessageParam) {
        throw new UnsupportedOperationException("Function request messages not yet supported.");
    }

    @Override
    public ChatCompletionRequestMessage visitSystem(@NotNull ChatCompletionSystemMessageParam chatCompletionSystemMessageParam) {
        throw new UnsupportedOperationException("System request messages not yet supported.");
    }

    @Override
    public ChatCompletionRequestMessage visitTool(@NotNull ChatCompletionToolMessageParam chatCompletionToolMessageParam) {
        throw new UnsupportedOperationException("Tool request messages not yet supported.");
    }

    @Override
    public ChatCompletionRequestMessage visitUser(@NotNull ChatCompletionUserMessageParam chatCompletionUserMessageParam) {
        UserContentVisitor userContentVisitor = new UserContentVisitor();
        ChatCompletionRequestUserMessage message = new ChatCompletionRequestUserMessage(
                chatCompletionUserMessageParam.content().accept(userContentVisitor));
        chatCompletionUserMessageParam.name().ifPresent(message::setName);
        return message;
    }
}

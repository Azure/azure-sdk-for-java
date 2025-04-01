package com.azure.ai.openai.implementation.mappers.visitors;

import com.azure.ai.openai.models.ChatCompletionRequestMessageContentPartRefusal;
import com.azure.ai.openai.models.ChatCompletionRequestMessageContentPartText;
import com.azure.core.util.BinaryData;
import com.openai.models.chat.completions.ChatCompletionAssistantMessageParam;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class AssistantContentVisitor implements ChatCompletionAssistantMessageParam.Content.Visitor<BinaryData> {


    @Override
    public BinaryData visitArrayOfContentParts(@NotNull List<ChatCompletionAssistantMessageParam.Content.ChatCompletionRequestAssistantMessageContentPart> list) {
        List<Object> contentParts = list.stream()
            .map(contentPart -> contentPart.refusal()
                .map(it -> (Object) new ChatCompletionRequestMessageContentPartRefusal(it.refusal()))
                .orElseGet(() -> contentPart.text()
                    .map(it -> (Object) new ChatCompletionRequestMessageContentPartText(it.text()))
                    .orElse(null)))
            .filter(Objects::nonNull)
            .toList();
        return BinaryData.fromObject(contentParts);
    }

    @Override
    public BinaryData visitText(@NotNull String s) {
        return BinaryData.fromString(s);
    }
}

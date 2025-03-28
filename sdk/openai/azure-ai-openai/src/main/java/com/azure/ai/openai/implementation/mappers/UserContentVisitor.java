package com.azure.ai.openai.implementation.mappers;

import com.azure.ai.openai.models.ChatCompletionRequestMessageContentPartImage;
import com.azure.ai.openai.models.ChatCompletionRequestMessageContentPartImageImageUrl;
import com.azure.ai.openai.models.ChatCompletionRequestMessageContentPartImageImageUrlDetail;
import com.azure.ai.openai.models.ChatCompletionRequestMessageContentPartText;
import com.azure.core.util.BinaryData;
import com.openai.core.JsonNull;
import com.openai.core.JsonValue;
import com.openai.models.chat.completions.ChatCompletionContentPart;
import com.openai.models.chat.completions.ChatCompletionContentPartImage;
import com.openai.models.chat.completions.ChatCompletionContentPartInputAudio;
import com.openai.models.chat.completions.ChatCompletionContentPartText;
import com.openai.models.chat.completions.ChatCompletionUserMessageParam;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class UserContentVisitor implements ChatCompletionUserMessageParam.Content.Visitor<BinaryData> {


    @Override
    public BinaryData visitText(@NotNull String s) {
        // not sure if this should be fromObject instead, as the String is not JSON
        return BinaryData.fromString(s);
    }

    @Override
    public BinaryData visitArrayOfContentParts(@NotNull List<ChatCompletionContentPart> list) {
        ArrayPartVisitor arrayPartVisitor = new ArrayPartVisitor();
        List<BinaryData> contentParts = list.stream()
            .map(contentPart -> contentPart.accept(arrayPartVisitor))
            .toList();
        return BinaryData.fromObject(contentParts);
    }

    private static class ArrayPartVisitor implements ChatCompletionContentPart.Visitor<BinaryData> {

        @Override
        public BinaryData visitText(@NotNull ChatCompletionContentPartText chatCompletionContentPartText) {
            return BinaryData.fromObject(new ChatCompletionRequestMessageContentPartText(chatCompletionContentPartText.text()));
        }

        @Override
        public BinaryData visitImageUrl(@NotNull ChatCompletionContentPartImage chatCompletionContentPartImage) {
            ChatCompletionRequestMessageContentPartImageImageUrl imageUrl =
                    new ChatCompletionRequestMessageContentPartImageImageUrl(chatCompletionContentPartImage.imageUrl().url());
            chatCompletionContentPartImage._additionalProperties().getOrDefault("detail", new JsonNull())
                    .asString()
                    .ifPresent(detail ->
                            imageUrl.setDetail(
                                    ChatCompletionRequestMessageContentPartImageImageUrlDetail.fromString((String) detail)));
            ChatCompletionRequestMessageContentPartImage image = new ChatCompletionRequestMessageContentPartImage(imageUrl);
            return BinaryData.fromObject(image);
        }

        @Override
        public BinaryData visitInputAudio(@NotNull ChatCompletionContentPartInputAudio chatCompletionContentPartInputAudio) {
            throw new UnsupportedOperationException("Input audio request messages not yet supported.");
        }

        @Override
        public BinaryData visitFile(@NotNull ChatCompletionContentPart.File file) {
            throw new UnsupportedOperationException("File request messages not yet supported.");
        }
    }
}

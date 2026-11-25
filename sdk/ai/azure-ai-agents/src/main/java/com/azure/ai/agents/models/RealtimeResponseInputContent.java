// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;
import java.util.Objects;

/**
 * Text, audio, or image content in a user message supplied as response input.
 * Use the factory methods to combine different content variants in the same message.
 */
@Immutable
public final class RealtimeResponseInputContent implements JsonSerializable<RealtimeResponseInputContent> {
    private static final ClientLogger LOGGER = new ClientLogger(RealtimeResponseInputContent.class);

    private final JsonSerializable<?> value;

    private RealtimeResponseInputContent(JsonSerializable<?> value) {
        this.value = Objects.requireNonNull(value, "'value' cannot be null.");
    }

    /**
     * Creates content containing text.
     * @param text the text content.
     * @return the input content.
     * @throws NullPointerException if {@code text} is null.
     */
    public static RealtimeResponseInputContent ofText(RealtimeResponseInputText text) {
        return new RealtimeResponseInputContent(text);
    }

    /**
     * Creates content containing audio.
     * @param audio the audio content.
     * @return the input content.
     * @throws NullPointerException if {@code audio} is null.
     */
    public static RealtimeResponseInputContent ofAudio(RealtimeResponseInputAudio audio) {
        return new RealtimeResponseInputContent(audio);
    }

    /**
     * Creates content containing an image.
     * @param image the image content.
     * @return the input content.
     * @throws NullPointerException if {@code image} is null.
     */
    public static RealtimeResponseInputContent ofImage(RealtimeResponseInputImage image) {
        return new RealtimeResponseInputContent(image);
    }

    /**
     * Gets the text variant.
     * @return the text, or null if this content contains another variant.
     */
    public RealtimeResponseInputText getText() {
        return value instanceof RealtimeResponseInputText ? (RealtimeResponseInputText) value : null;
    }

    /**
     * Gets the audio variant.
     * @return the audio, or null if this content contains another variant.
     */
    public RealtimeResponseInputAudio getAudio() {
        return value instanceof RealtimeResponseInputAudio ? (RealtimeResponseInputAudio) value : null;
    }

    /**
     * Gets the image variant.
     * @return the image, or null if this content contains another variant.
     */
    public RealtimeResponseInputImage getImage() {
        return value instanceof RealtimeResponseInputImage ? (RealtimeResponseInputImage) value : null;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return value.toJson(jsonWriter);
    }

    /**
     * Reads input content, selecting its variant using the {@code type} discriminator.
     * @param jsonReader the JSON reader.
     * @return the input content, or null for JSON null.
     * @throws IOException if the JSON cannot be read.
     * @throws IllegalStateException if the content type is missing or unsupported.
     */
    public static RealtimeResponseInputContent fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            try (JsonReader bufferedReader = reader.bufferObject()) {
                String type = null;
                bufferedReader.nextToken();
                while (bufferedReader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = bufferedReader.getFieldName();
                    bufferedReader.nextToken();
                    if ("type".equals(fieldName)) {
                        type = bufferedReader.getString();
                        break;
                    }
                    bufferedReader.skipChildren();
                }
                if ("input_text".equals(type)) {
                    return ofText(RealtimeResponseInputText.fromJson(bufferedReader.reset()));
                } else if ("input_audio".equals(type)) {
                    return ofAudio(RealtimeResponseInputAudio.fromJson(bufferedReader.reset()));
                } else if ("input_image".equals(type)) {
                    return ofImage(RealtimeResponseInputImage.fromJson(bufferedReader.reset()));
                }
                throw LOGGER.logExceptionAsError(
                    new IllegalStateException("Unsupported realtime response input content type: " + type));
            }
        });
    }
}

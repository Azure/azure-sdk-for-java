// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/**
 * Base type for the different voice providers supported by the VoiceLive service.
 */
public abstract class VoiceProvider {
    /**
     * Creates a new instance of {@link VoiceProvider}.
     * <p>
     * Protected because {@code VoiceProvider} is intended to be subclassed by
     * specific voice provider implementations.
     */
    protected VoiceProvider() {
        // no-op
    }

    /**
     * Polymorphic deserializer that inspects the {@code "type"} discriminator and
     * dispatches to the appropriate concrete subtype.
     *
     * @param jsonReader The {@link JsonReader} positioned at the start of an object or at JSON {@code null}.
     * @return A concrete {@link VoiceProvider} instance, or {@code null} if the JSON was {@code null}
     *         or the discriminator is unknown.
     * @throws IOException If parsing fails.
     */
    public static VoiceProvider fromJson(JsonReader jsonReader) throws IOException {
        if (jsonReader.currentToken() == null) {
            jsonReader.nextToken();
        }
        if (jsonReader.currentToken() == JsonToken.NULL) {
            return null;
        }

        // Buffer the whole object once so we can replay it twice:
        // 1) to read the "type", 2) to delegate to the subtype parser.
        JsonReader snapshot = jsonReader.bufferObject();

        // Pass 1: find the discriminator
        String type = snapshot.readObject(reader -> {
            String t = null;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String name = reader.getFieldName();
                reader.nextToken();
                if ("type".equals(name)) {
                    t = reader.getString();
                    // Still need to finish the object to leave reader in a consistent state
                    reader.skipChildren();
                } else {
                    reader.skipChildren();
                }
            }
            return t;
        });

        if (type == null) {
            // No discriminator — choose policy; returning null keeps parsing lenient.
            return null;
        }

        // Pass 2: replay for the subtype
        JsonReader replay = snapshot.bufferObject();
        switch (type) {
            case "openai":
                return OpenAIVoice.fromJson(replay);

            case "azure-custom":
            case "azure-standard":
            case "azure-personal":
                return AzureVoice.fromJson(replay);

            default:
                // Unknown discriminator — return null (or throw if you prefer strictness).
                return null;
        }
    }

    /**
     * Serialize this voice provider to JSON.
     *
     * @param writer The {@link JsonWriter} to write to.
     * @return The same {@link JsonWriter} instance for chaining.
     * @throws IOException If serialization fails.
     */
    public abstract JsonWriter toJson(JsonWriter writer) throws IOException;
}

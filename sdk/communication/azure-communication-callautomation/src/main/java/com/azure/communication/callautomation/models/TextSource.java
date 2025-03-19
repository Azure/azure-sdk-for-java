// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/** The FileSource model. */
@Fluent
public final class TextSource extends PlaySource {
    /*
     * Text for the cognitive service to be played
     */
    private String text;

    /*
     * Source language locale to be played
     */
    private String sourceLocale;

    /*
     * Voice kind type
     */
    private VoiceKind voiceKind;

    /*
     * Voice name to be played
     */
    private String voiceName;

    /*
     * Endpoint where the custom voice was deployed.
     */
    private String customVoiceEndpointId;

    /**
     * Get the text property: Text for the cognitive service to be played.
     *
     * @return the text value.
     */
    public String getText() {
        return this.text;
    }

    /**
     * Set the text property: Text for the cognitive service to be played.
     *
     * @param text the text value to set.
     * @return the TextSource object itself.
     */
    public TextSource setText(String text) {
        this.text = text;
        return this;
    }

    /**
     * Get the sourceLocale property: Source language locale to be played.
     *
     * @return the sourceLocale value.
     */
    public String getSourceLocale() {
        return this.sourceLocale;
    }

    /**
     * Set the sourceLocale property: Source language locale to be played.
     *
     * @param sourceLocale the sourceLocale value to set.
     * @return the TextSource object itself.
     */
    public TextSource setSourceLocale(String sourceLocale) {
        this.sourceLocale = sourceLocale;
        return this;
    }

    /**
     * Get the voiceKind property: Voice kind type.
     *
     * @return the voiceKind value.
     */
    public VoiceKind getVoiceKind() {
        return this.voiceKind;
    }

    /**
     * Set the voiceKind property: Voice kind type.
     *
     * @param voiceKind the voiceKind value to set.
     * @return the TextSource object itself.
     */
    public TextSource setVoiceKind(VoiceKind voiceKind) {
        this.voiceKind = voiceKind;
        return this;
    }

    /**
     * Get the voiceName property: Voice name to be played.
     *
     * @return the voiceName value.
     */
    public String getVoiceName() {
        return this.voiceName;
    }

    /**
     * Set the voiceName property: Voice name to be played.
     *
     * @param voiceName the voiceName value to set.
     * @return the TextSourceInternal object itself.
     */
    public TextSource setVoiceName(String voiceName) {
        this.voiceName = voiceName;
        return this;
    }

    /**
     * Get the customVoiceEndpointId property: Endpoint where the custom voice was deployed.
     *
     * @return the customVoiceEndpointId value.
     */
    public String getCustomVoiceEndpointId() {
        return this.customVoiceEndpointId;
    }

    /**
     * Set the customVoiceEndpointId property: Endpoint where the custom voice was deployed.
     *
     * @param customVoiceEndpointId the customVoiceEndpointId value to set.
     * @return the TextSourceInternal object itself.
     */
    public TextSource setCustomVoiceEndpointId(String customVoiceEndpointId) {
        this.customVoiceEndpointId = customVoiceEndpointId;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("text", this.text);
        jsonWriter.writeStringField("sourceLocale", this.sourceLocale);
        jsonWriter.writeStringField("voiceKind", this.voiceKind.toString());
        jsonWriter.writeStringField("voiceName", this.voiceName);
        jsonWriter.writeStringField("customVoiceEndpointId", this.customVoiceEndpointId);
        jsonWriter.writeStringField("playSourceCacheId", this.getPlaySourceCacheId());
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of TextSource from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of TextSource if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the TextSource.
     */
    public static TextSource fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final TextSource source = new TextSource();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("text".equals(fieldName)) {
                    source.text = reader.getString();
                } else if ("sourceLocale".equals(fieldName)) {
                    source.sourceLocale = reader.getString();
                } else if ("voiceKind".equals(fieldName)) {
                    source.voiceKind = VoiceKind.fromString(reader.getString());
                } else if ("voiceName".equals(fieldName)) {
                    source.voiceName = reader.getString();
                } else if ("customVoiceEndpointId".equals(fieldName)) {
                    source.customVoiceEndpointId = reader.getString();
                } else if ("playSourceCacheId".equals(fieldName)) {
                    // Set the property of the base class 'PlaySource'.
                    source.setPlaySourceCacheId(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }
            return source;
        });
    }
}

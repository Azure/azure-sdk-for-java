// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;

/** The TranscriptionConfigurationInternal model. */
@Fluent
public final class TranscriptionOptions implements JsonSerializable<TranscriptionOptions> {
    /*
     * Transport URL for live transcription
     */
    @JsonProperty(value = "transportUrl", required = true)
    private String transportUrl;

    /*
     * The type of transport to be used for live transcription, eg. Websocket
     */
    @JsonProperty(value = "transportType", required = true)
    private TranscriptionTransportType transportType;

    /*
     * Defines the locale for the data e.g en-CA, en-AU
     */
    @JsonProperty(value = "locale", required = true)
    private String locale;

    /*
     * Determines if the transcription should be started immediately after call is answered or not.
     */
    @JsonProperty(value = "startTranscription", required = true)
    private boolean startTranscription;

    /**
     * Creates a new instance of MediaStreamingConfiguration
     * @param transportUrl - The Transport URL
     * @param transportType - Transport type
     * @param locale - Locale
     * @param startTranscription - Start Transcription
     */
    public TranscriptionOptions(String transportUrl, TranscriptionTransportType transportType, String locale, boolean startTranscription) {
        this.transportUrl = transportUrl;
        this.transportType = transportType;
        this.locale = locale;
        this.startTranscription = startTranscription;
    }

    /**
     * Get the transportUrl property: Transport URL for live transcription.
     *
     * @return the transportUrl value.
     */
    public String getTransportUrl() {
        return this.transportUrl;
    }

    /**
     * Get the transportType property: The type of transport to be used for live transcription, eg. Websocket.
     *
     * @return the transportType value.
     */
    public TranscriptionTransportType getTransportType() {
        return this.transportType;
    }

    /**
     * Get the locale property: locale for the data e.g en-CA, en-AU.
     *
     * @return the locale value.
     */
    public String getLocale() {
        return this.locale;
    }

    /**
     * Get the startTranscription property: Which determines if the transcription should be started immediately after call is answered or not.
     *
     * @return the startTranscription value.
     */
    public boolean getStartTranscription() {
        return this.startTranscription;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("transportUrl", this.transportUrl);
        jsonWriter.writeStringField("transportType", this.transportType.toString());
        jsonWriter.writeStringField("locale", this.locale);
        jsonWriter.writeBooleanField("startTranscription", this.startTranscription);
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of TranscriptionOptions from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of TranscriptionOptions if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the TranscriptionOptions.
     */
    public static TranscriptionOptions fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            TranscriptionOptions options = new TranscriptionOptions(null, null, null, false);
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("transportUrl".equals(fieldName)) {
                    options.transportUrl = reader.getString();
                } else if ("transportType".equals(fieldName)) {
                    options.transportType = TranscriptionTransportType.fromString(reader.getString());
                } else if ("locale".equals(fieldName)) {
                    options.locale = reader.getString();
                } else if ("startTranscription".equals(fieldName)) {
                    options.startTranscription = reader.getBoolean();
                } else {
                    reader.skipChildren();
                }
            }
            return options;
        });
    }
}

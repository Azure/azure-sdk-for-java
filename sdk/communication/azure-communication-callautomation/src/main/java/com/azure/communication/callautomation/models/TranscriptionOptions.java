// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.annotation.Fluent;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.io.IOException;

/** The TranscriptionConfigurationInternal model. */
@Fluent
public final class TranscriptionOptions implements JsonSerializable<TranscriptionOptions> {
    /*
     * Transport URL for live transcription
     */
    private final String transportUrl;

    /*
     * The type of transport to be used for live transcription, eg. Websocket
     */
    private final TranscriptionTransport transportType;

    /*
     * Defines the locale for the data e.g en-CA, en-AU
     */
    private final String locale;

    /*
     * Determines if the transcription should be started immediately after call is answered or not.
     */
    private final boolean startTranscription;

    /*
     * Endpoint where the custom model was deployed.
     */
    private String speechRecognitionModelEndpointId;

    /*
     * Enables intermediate results for the transcribed speech.
     */
    private Boolean enableIntermediateResults;

    /**
     * Creates a new instance of MediaStreamingConfiguration
     * @param transportUrl - The Transport URL
     * @param transportType - Transport type
     * @param locale - Locale
     * @param startTranscription - Start Transcription
     */
    public TranscriptionOptions(String transportUrl, TranscriptionTransport transportType, String locale, boolean startTranscription) {
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
    public TranscriptionTransport getTransportType() {
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

    /**
     * Get the speechRecognitionModelEndpointId property: Endpoint where the custom model was deployed.
     * 
     * @return the speechRecognitionModelEndpointId value.
     */
    public String getSpeechRecognitionModelEndpointId() {
        return this.speechRecognitionModelEndpointId;
    }

    /**
     * Set the speechRecognitionModelEndpointId property: Endpoint where the custom model was deployed.
     * 
     * @param speechRecognitionModelEndpointId the speechRecognitionModelEndpointId value to set.
     * @return the TranscriptionOptions object itself.
     */
    public TranscriptionOptions setSpeechRecognitionModelEndpointId(String speechRecognitionModelEndpointId) {
        this.speechRecognitionModelEndpointId = speechRecognitionModelEndpointId;
        return this;
    }

    /**
     * Get the enableIntermediateResults property: Enables intermediate results for the transcribed speech.
     * 
     * @return the enableIntermediateResults value.
     */
    public Boolean isIntermediateResultsEnabled() {
        return this.enableIntermediateResults;
    }

    /**
     * Set the enableIntermediateResults property: Enables intermediate results for the transcribed speech.
     * 
     * @param enableIntermediateResults the enableIntermediateResults value to set.
     * @return the TranscriptionOptions object itself.
     */
    public TranscriptionOptions setEnableIntermediateResults(Boolean enableIntermediateResults) {
        this.enableIntermediateResults = enableIntermediateResults;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("transportUrl", transportUrl);
        jsonWriter.writeStringField("transportType", transportType != null ? transportType.toString() : null);
        jsonWriter.writeStringField("locale", locale);
        jsonWriter.writeBooleanField("startTranscription", startTranscription);
        jsonWriter.writeStringField(speechRecognitionModelEndpointId, speechRecognitionModelEndpointId);
        jsonWriter.writeBooleanField("enableIntermediateResults", enableIntermediateResults);
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
            String transportUrl = null;
            TranscriptionTransport transportType = null;
            String locale = null;
            boolean startTranscription = false;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("transportUrl".equals(fieldName)) {
                    transportUrl = reader.getString();
                } else if ("transportType".equals(fieldName)) {
                    transportType = TranscriptionTransport.fromString(reader.getString());
                } else if ("locale".equals(fieldName)) {
                    locale = reader.getString();
                } else if ("startTranscription".equals(fieldName)) {
                    startTranscription = reader.getBoolean();
                } else {
                    reader.skipChildren();
                }
            }
            return new TranscriptionOptions(transportUrl, transportType, locale, startTranscription);
        });
    }
}

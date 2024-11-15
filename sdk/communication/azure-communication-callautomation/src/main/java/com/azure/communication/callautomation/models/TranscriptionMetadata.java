// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import java.io.IOException;

import com.azure.communication.callautomation.implementation.accesshelpers.TranscriptionMetadataContructorProxy;
import com.azure.communication.callautomation.implementation.converters.TranscriptionMetadataConverter;
import com.azure.json.JsonProviders;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.core.util.logging.ClientLogger;

/**
 * Metadata for Transcription Streaming.
 */
public final class TranscriptionMetadata extends StreamingData<TranscriptionMetadata> {

    private static final ClientLogger LOGGER = new ClientLogger(TranscriptionMetadata.class);

    /*
     * Transcription Subscription Id.
     */
    private final String transcriptionSubscriptionId;

    /*
     * The target locale in which the translated text needs to be
     */
    private final String locale;

    /*
     * Call connection id
     */
    private final String callConnectionId;

    /*
     * Correlation id
     */
    private final String correlationId;
   
    static {
        TranscriptionMetadataContructorProxy.setAccessor(
            new TranscriptionMetadataContructorProxy.TranscriptionMetadataContructorProxyAccessor() {
                @Override
                public TranscriptionMetadata create(TranscriptionMetadataConverter internalData) {
                    return new TranscriptionMetadata(internalData);
                }
            }
        );
    }

    /**
     * Creates an instance of TranscriptionMetadata class.
     * @param internalData Transcription meta data internal.
     */
    TranscriptionMetadata(TranscriptionMetadataConverter internalData) {
        this.transcriptionSubscriptionId = internalData.getTranscriptionSubscriptionId();
        this.locale = internalData.getLocale();
        this.callConnectionId = internalData.getCallConnectionId();
        this.correlationId = internalData.getCorrelationId();
    }

    /**
     * Creates an instance of TranscriptionMetadata class.
     */
    public TranscriptionMetadata() {
        this.transcriptionSubscriptionId = null;
        this.locale = null;
        this.callConnectionId = null;
        this.correlationId = null;
    }

    /**
     * Get the transcriptionSubscriptionId property.
     *
     * @return the transcriptionSubscriptionId value.
     */
    public String getTranscriptionSubscriptionId() {
        return transcriptionSubscriptionId;
    }

    /**
     * Get the locale property.
     *
     * @return the locale value.
     */
    public String getLocale() {
        return locale;
    }

    /**
     * Get the callConnectionId property.
     *
     * @return the callConnectionId value.
     */
    public String getCallConnectionId() {
        return callConnectionId;
    }

    /**
     * Get the correlationId property.
     *
     * @return the correlationId value.
     */
    public String getCorrelationId() {
        return correlationId;
    }

    @Override
    public TranscriptionMetadata parse(String data) {
        // Implementation for parsing audio data
        try (JsonReader jsonReader = JsonProviders.createReader(data)) {
            return jsonReader.readObject(reader -> {
                while (reader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    reader.nextToken();

                    if ("transcriptionMetadata".equals(fieldName)) {
                        // Possible return of AudioData
                        final TranscriptionMetadataConverter transcriptionMetadataInternal = TranscriptionMetadataConverter.fromJson(reader);
                        if (transcriptionMetadataInternal != null) {
                            return TranscriptionMetadataContructorProxy.create(transcriptionMetadataInternal);
                        } else {
                            return null;
                        }
                    } else {
                        reader.skipChildren();
                    }
                }

                return null; // cases triggered.
            });
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }
}

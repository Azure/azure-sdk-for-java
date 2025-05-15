// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import com.azure.communication.callautomation.implementation.accesshelpers.DtmfMetadataContructorProxy;
import com.azure.communication.callautomation.implementation.converters.DtmfMetadataConverter;
import com.azure.communication.common.CommunicationIdentifier;

/** The dtmf meta data model. */
public final class DtmfMetadata extends StreamingData {
    /*
     * The dtmf data.
     */
    private final String data;

    /*
     * The timestamp indicating when the media content was received by the bot, or if the bot is sending media, 
     * the timestamp of when the media was sourced. The format is ISO 8601 (yyyy-mm-ddThh:mm)
     */
    private final OffsetDateTime timestamp;

    /*
     * The raw ID of the participant.
     */
    private final CommunicationIdentifier participant;

    static {
        DtmfMetadataContructorProxy.setAccessor(new DtmfMetadataContructorProxy.DtmfMetadataContructorProxyAccessor() {
            @Override
            public DtmfMetadata create(DtmfMetadataConverter internalData) {
                return new DtmfMetadata(internalData);
            }

            @Override
            public DtmfMetadata create(String data) {
                return new DtmfMetadata(data);
            }
        });
    }

    /**
     * Package-private constructor of the class, used internally.
     *
     * @param internalData The DtmfMetadataconvertor
     */
    DtmfMetadata(DtmfMetadataConverter internalData) {
        this.data = internalData.getData();
        this.timestamp = OffsetDateTime.parse(internalData.getTimestamp(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        if (internalData.getParticipantRawID() != null && !internalData.getParticipantRawID().isEmpty()) {
            this.participant = CommunicationIdentifier.fromRawId(internalData.getParticipantRawID());
        } else {
            participant = null;
        }
    }

    /**
     * The constructor
     */
    public DtmfMetadata() {
        this.data = null;
        this.timestamp = null;
        this.participant = null;
    }

    /**
     * The constructor
     *
     * @param data The dtmf Metadata.
     */
    DtmfMetadata(String data) {
        this.data = data;
        this.timestamp = null;
        this.participant = null;
    }

    /**
     * Get the data property.
     *
     * @return the data value.
     */
    public String getData() {
        return data;
    }

    /**
     * Get the timestamp property.
     *
     * @return the timestamp value.
     */
    public OffsetDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Get the participantRawID property.
     *
     * @return the participantRawID value.
     */
    public CommunicationIdentifier getParticipant() {
        return participant;
    }
}

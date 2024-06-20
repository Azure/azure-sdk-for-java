// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import java.io.IOException;
import java.util.Map;

import com.azure.communication.callautomation.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callautomation.implementation.models.CommunicationIdentifierModel;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;


/** The CallTransferAccepted model. */
@Immutable
public final class CallTransferAccepted extends CallAutomationEventBase {
    /*
     * Contains the resulting SIP code, sub-code and message.
     */
    @JsonProperty(value = "resultInformation")
    private ResultInformation resultInformation;

    /**
     * The participant who is being transferred away.
     */
    @JsonIgnore
    private CommunicationIdentifier transferee;

    /**
     * Target to whom the call is transferred.
     */
    @JsonIgnore
    private CommunicationIdentifier transferTarget;

    @JsonCreator
    private CallTransferAccepted(
            @JsonProperty("transferee") Map<String, Object> transferee,
            @JsonProperty("transferTarget") Map<String, Object> transferTarget) {
        this.resultInformation = null;
        ObjectMapper mapper = new ObjectMapper();
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        this.transferee = CommunicationIdentifierConverter.convert(mapper.convertValue(transferee, CommunicationIdentifierModel.class));
        this.transferTarget = CommunicationIdentifierConverter.convert(mapper.convertValue(transferTarget, CommunicationIdentifierModel.class));

    }

    private CallTransferAccepted() {

    }

    /**
     * Get the resultInformation property: Contains the resulting SIP code, sub-code and message.
     *
     * @return the resultInformation value.
     */
    public ResultInformation getResultInformation() {
        return this.resultInformation;
    }

    /**
     * The participant who is being transferred away
     * @return the transferee value
     */
    public CommunicationIdentifier getTransferee() {
        return this.transferee;
    }

    /**
     * Target to whom the call is transferred.
     * @return the transferTarget value
     */
    public CommunicationIdentifier getTransferTarget() {
        return this.transferTarget;
    }

    static CallTransferAccepted fromJsonImpl(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            final CallTransferAccepted event = new CallTransferAccepted();
            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("resultInformation".equals(fieldName)) {
                    event.resultInformation = ResultInformation.fromJson(reader);
                } else if ("transferee".equals(fieldName)) {
                    // TODO (anu): final CommunicationIdentifierModel inner = CommunicationIdentifierModel.fromJson(reader);
                    final CommunicationIdentifierModel inner = null;
                    event.transferee = CommunicationIdentifierConverter.convert(inner);
                } else if ("transferTarget".equals(fieldName)) {
                    // TODO (anu): final CommunicationIdentifierModel inner = CommunicationIdentifierModel.fromJson(reader);
                    final CommunicationIdentifierModel inner = null;
                    event.transferTarget = CommunicationIdentifierConverter.convert(inner);
                } else {
                    reader.skipChildren();
                }
            }
            return event;
        });
    }
}

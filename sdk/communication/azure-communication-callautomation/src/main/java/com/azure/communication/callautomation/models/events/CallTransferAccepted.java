// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import java.util.Map;

import com.azure.communication.callautomation.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callautomation.implementation.models.CommunicationIdentifierModel;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.Immutable;
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
    private final ResultInformation resultInformation;
    
    /**
     * Participant being transferred away
     */
    @JsonIgnore
    private final CommunicationIdentifier transferee;
    
    /**
     * The target transferee is transferred to
     */
    @JsonIgnore
    private final CommunicationIdentifier transferTarget;
    
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

    /**
     * Get the resultInformation property: Contains the resulting SIP code, sub-code and message.
     *
     * @return the resultInformation value.
     */
    public ResultInformation getResultInformation() {
        return this.resultInformation;
    }
    
    /**
     * Participant being transferred away
     * @return the transferee value
     */
    public CommunicationIdentifier getTransferee() {
        return this.transferee;
    }
    
    /**
     * The target transferee is transferred to
     * @return the tansferTarget value
     */
    public CommunicationIdentifier getTransferTarget() {
        return this.transferTarget;
    }
}

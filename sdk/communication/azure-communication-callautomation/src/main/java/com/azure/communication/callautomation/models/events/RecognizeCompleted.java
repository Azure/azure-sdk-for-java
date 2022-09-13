// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models.events;

import com.azure.communication.callautomation.models.RecognitionType;
import com.azure.communication.callautomation.models.CollectTonesResult;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.azure.core.annotation.Immutable;

/** The RecognizeCompleted model. */
@Immutable
public final class RecognizeCompleted extends CallAutomationEventBase {
    /*
     * The operationContext property.
     */
    @JsonProperty(value = "operationContext", access = JsonProperty.Access.WRITE_ONLY)
    private String operationContext;

    /*
     * Result information defines the code, subcode and message
     */
    @JsonProperty(value = "resultInformation", access = JsonProperty.Access.WRITE_ONLY)
    private ResultInformation resultInformation;

    /*
     * Determines the sub-type of the recognize operation.
     * In case of cancel operation the this field is not set and is returned
     * empty
     */
    @JsonProperty(value = "recognitionType", access = JsonProperty.Access.WRITE_ONLY)
    private RecognitionType recognitionType;

    /*
     * Defines the result for RecognitionType = Dtmf
     */
    @JsonProperty(value = "collectTonesResult", access = JsonProperty.Access.WRITE_ONLY)
    private CollectTonesResult collectTonesResult;

    /*
     * Used to determine the version of the event.
     */
    @JsonProperty(value = "version", access = JsonProperty.Access.WRITE_ONLY)
    private String version;

    /*
     * Call connection ID.
     */
    @JsonProperty(value = "callConnectionId", access = JsonProperty.Access.WRITE_ONLY)
    private String callConnectionId;

    /*
     * Server call ID.
     */
    @JsonProperty(value = "serverCallId", access = JsonProperty.Access.WRITE_ONLY)
    private String serverCallId;

    /*
     * Correlation ID for event to call correlation. Also called ChainId for
     * skype chain ID.
     */
    @JsonProperty(value = "correlationId", access = JsonProperty.Access.WRITE_ONLY)
    private String correlationId;

    /*
     * The public event namespace used as the "type" property in the
     * CloudEvent.
     */
    @JsonProperty(value = "publicEventType", access = JsonProperty.Access.WRITE_ONLY)
    private String publicEventType;

    /**
     * Get the operationContext property: The operationContext property.
     *
     * @return the operationContext value.
     */
    public String getOperationContext() {
        return this.operationContext;
    }

    /**
     * Get the resultInformation property: Result information defines the code, subcode and message.
     *
     * @return the resultInformation value.
     */
    public ResultInformation getResultInformation() {
        return this.resultInformation;
    }

    /**
     * Get the recognitionType property: Determines the sub-type of the recognize operation. In case of cancel operation
     * the this field is not set and is returned empty.
     *
     * @return the recognitionType value.
     */
    public RecognitionType getRecognitionType() {
        return this.recognitionType;
    }

    /**
     * Get the collectTonesResult property: Defines the result for RecognitionType = Dtmf.
     *
     * @return the collectTonesResult value.
     */
    public CollectTonesResult getCollectTonesResult() {
        return this.collectTonesResult;
    }

    /**
     * Get the version property: Used to determine the version of the event.
     *
     * @return the version value.
     */
    public String getVersion() {
        return this.version;
    }

    /**
     * Get the callConnectionId property: Call connection ID.
     *
     * @return the callConnectionId value.
     */
    public String getCallConnectionId() {
        return this.callConnectionId;
    }

    /**
     * Get the serverCallId property: Server call ID.
     *
     * @return the serverCallId value.
     */
    public String getServerCallId() {
        return this.serverCallId;
    }

    /**
     * Get the correlationId property: Correlation ID for event to call correlation. Also called ChainId for skype chain
     * ID.
     *
     * @return the correlationId value.
     */
    public String getCorrelationId() {
        return this.correlationId;
    }

    /**
     * Get the publicEventType property: The public event namespace used as the "type" property in the CloudEvent.
     *
     * @return the publicEventType value.
     */
    public String getPublicEventType() {
        return this.publicEventType;
    }
}

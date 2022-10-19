// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.PhoneNumberIdentifier;
import com.azure.core.annotation.Fluent;

/**
 * The options for adding participants.
 */
@Fluent
public class TransferToParticipantCallOptions {
    /**
     * A {@link CommunicationIdentifier} representing the target participant of this transfer.
     */
    private final CommunicationIdentifier targetParticipant;

    /**
     * The operational context
     */
    private String operationContext;

    /**
     * A {@link PhoneNumberIdentifier} representing the caller ID of the transferee, if transferring to a pstn number.
     */
    private PhoneNumberIdentifier transfereeCallerId;

    /**
     * The user to user information.
     */
    private String userToUserInformation;

    /**
     * Repeatability Headers Configuration
     */
    private RepeatabilityHeaders repeatabilityHeaders;

    /**
     * Constructor
     *
     * @param targetParticipant A {@link CommunicationIdentifier} representing the target participant of this transfer.
     */
    public TransferToParticipantCallOptions(CommunicationIdentifier targetParticipant) {
        this.targetParticipant = targetParticipant;
    }

    /**
     * Get the target participant.
     *
     * @return the target participant.
     */
    public CommunicationIdentifier getTargetParticipant() {
        return targetParticipant;
    }

    /**
     * Get the operationContext.
     *
     * @return the operationContext
     */
    public String  getOperationContext() {
        return operationContext;
    }

    /**
     * Get the transfereeCallerId.
     *
     * @return the transfereeCallerId.
     */
    public PhoneNumberIdentifier getTransfereeCallerId() {
        return transfereeCallerId;
    }

    /**
     * Get the userToUserInformation.
     *
     * @return the userToUserInformation
     */
    public String  getUserToUserInformation() {
        return userToUserInformation;
    }

    /**
     * Get the Repeatability headers configuration.
     *
     * @return the repeatabilityHeaders
     */
    public RepeatabilityHeaders getRepeatabilityHeaders() {
        return repeatabilityHeaders;
    }

    /**
     * Set the operationContext.
     *
     * @param operationContext the operationContext to set
     * @return the TransferToParticipantCallOptions object itself.
     */
    public TransferToParticipantCallOptions setOperationContext(String operationContext) {
        this.operationContext = operationContext;
        return this;
    }

    /**
     * Set the transfereeCallerId.
     *
     * @param transfereeCallerId A {@link PhoneNumberIdentifier} representing the caller ID of the transferee
     *                           if transferring to a pstn number.
     * @return the TransferToParticipantCallOptions object itself.
     */
    public TransferToParticipantCallOptions setSourceCallerId(PhoneNumberIdentifier transfereeCallerId) {
        this.transfereeCallerId = transfereeCallerId;
        return this;
    }

    /**
     * Set the invitationTimeoutInSeconds.
     *
     * @param userToUserInformation The user to user information.
     * @return the TransferToParticipantCallOptions object itself.
     */
    public TransferToParticipantCallOptions setUserToUserInformation(String userToUserInformation) {
        this.userToUserInformation = userToUserInformation;
        return this;
    }

    /**
     * Set the repeatability headers
     *
     * @param repeatabilityHeaders The repeatability headers configuration.
     * @return the TransferToParticipantCallOptions object itself.
     */
    public TransferToParticipantCallOptions setRepeatabilityHeaders(RepeatabilityHeaders repeatabilityHeaders) {
        this.repeatabilityHeaders = repeatabilityHeaders;
        return this;
    }
}

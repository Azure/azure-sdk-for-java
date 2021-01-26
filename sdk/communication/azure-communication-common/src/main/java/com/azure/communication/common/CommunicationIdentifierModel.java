// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.common;

import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonProperty;

/** The CommunicationIdentifierModel model. */
@Fluent
public final class CommunicationIdentifierModel {
    /*
     * Kind of the communication identifier.
     */
    @JsonProperty(value = "kind", required = true)
    private CommunicationIdentifierKind kind;

    /*
     * Full Id of the identifier.
     */
    @JsonProperty(value = "id")
    private String id;

    /*
     * The phone number in E.164 format.
     */
    @JsonProperty(value = "phoneNumber")
    private String phoneNumber;

    /*
     * The AAD object Id of the Microsoft Teams user.
     */
    @JsonProperty(value = "microsoftTeamsUserId")
    private String microsoftTeamsUserId;

    /*
     * True if the identifier is anonymous.
     */
    @JsonProperty(value = "isAnonymous")
    private Boolean isAnonymous;

    private CommunicationCloudEnvironmentModel cloudEnvironmentModel;

    /**
     * Get the kind property: Kind of the communication identifier.
     *
     * @return the kind value.
     */
    public CommunicationIdentifierKind getKind() {
        return this.kind;
    }

    /**
     * Set the kind property: Kind of the communication identifier.
     *
     * @param kind the kind value to set.
     * @return the CommunicationIdentifierModel object itself.
     */
    public CommunicationIdentifierModel setKind(CommunicationIdentifierKind kind) {
        this.kind = kind;
        return this;
    }

    /**
     * Get the id property: Full Id of the identifier.
     *
     * @return the id value.
     */
    public String getId() {
        return this.id;
    }

    /**
     * Set the id property: Full Id of the identifier.
     *
     * @param id the id value to set.
     * @return the CommunicationIdentifierModel object itself.
     */
    public CommunicationIdentifierModel setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * Get the phoneNumber property: The phone number in E.164 format.
     *
     * @return the phoneNumber value.
     */
    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    /**
     * Set the phoneNumber property: The phone number in E.164 format.
     *
     * @param phoneNumber the phoneNumber value to set.
     * @return the CommunicationIdentifierModel object itself.
     */
    public CommunicationIdentifierModel setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    /**
     * Get the microsoftTeamsUserId property: The AAD object Id of the Microsoft Teams user.
     *
     * @return the microsoftTeamsUserId value.
     */
    public String getMicrosoftTeamsUserId() {
        return this.microsoftTeamsUserId;
    }

    /**
     * Set the microsoftTeamsUserId property: The AAD object Id of the Microsoft Teams user.
     *
     * @param microsoftTeamsUserId the microsoftTeamsUserId value to set.
     * @return the CommunicationIdentifierModel object itself.
     */
    public CommunicationIdentifierModel setMicrosoftTeamsUserId(String microsoftTeamsUserId) {
        this.microsoftTeamsUserId = microsoftTeamsUserId;
        return this;
    }

    /**
     * Get the isAnonymous property: True if the identifier is anonymous.
     *
     * @return the isAnonymous value.
     */
    public Boolean isAnonymous() {
        return this.isAnonymous;
    }

    /**
     * Set the isAnonymous property: True if the identifier is anonymous.
     *
     * @param isAnonymous the isAnonymous value to set.
     * @return the CommunicationIdentifierModel object itself.
     */
    public CommunicationIdentifierModel setIsAnonymous(Boolean isAnonymous) {
        this.isAnonymous = isAnonymous;
        return this;
    }

    /**
     * Get the cloud environment model in which this identifier model is valid
     * @return the cloud environment model
     */
    CommunicationCloudEnvironmentModel getCloudEnvironmentModel() {
        return this.cloudEnvironmentModel;
    }

    /**
     * Set the cloud environment model in which this identifier model is valid
     * @param cloudEnvironmentModel the cloud environment model in which this identifier model is valid
     * @return the CommunicationIdentifierModel object itself.
     */
    CommunicationIdentifierModel setCloudEnvironmentModel(CommunicationCloudEnvironmentModel cloudEnvironmentModel) {
        this.cloudEnvironmentModel = cloudEnvironmentModel;
        return this;
    }
}

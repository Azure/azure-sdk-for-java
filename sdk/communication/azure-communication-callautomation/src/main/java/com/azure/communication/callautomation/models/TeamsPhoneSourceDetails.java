// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import java.util.Map;
import com.azure.communication.common.CommunicationIdentifier;

/**
 * Container for details relating to the entity responsible for the creation of
 * these call details.
 */
public class TeamsPhoneSourceDetails {
    /*
     * ID of the source entity passing along the call details (ex. Application
     * Instance ID of - CQ/AA)
     */

    private CommunicationIdentifier source;

    /*
     * Language of the source entity passing along the call details, passed in the
     * ISO-639 standard
     */
    private String language;

    /*
     * Status of the source entity passing along the call details
     */
    private String status;

    /*
     * Intended targets of the source entity passing along the call details
     */
    private Map<String, CommunicationIdentifier> intendedTargets;

    /**
     * Creates an instance of TeamsPhoneSourceDetails class.
     */
    public TeamsPhoneSourceDetails() {
    }

    /**
     * Get the source property: ID of the source entity passing along the call
     * details (ex. Application Instance ID of -
     * CQ/AA).
     * 
     * @return the source value.
     */
    public CommunicationIdentifier getSource() {
        return this.source;
    }

    /**
     * Set the source property: ID of the source entity passing along the call
     * details (ex. Application Instance ID of -
     * CQ/AA).
     * 
     * @param source the source value to set.
     * @return the TeamsPhoneSourceDetails object itself.
     */
    public TeamsPhoneSourceDetails setSource(CommunicationIdentifier source) {
        this.source = source;
        return this;
    }

    /**
     * Get the language property: Language of the source entity passing along the
     * call details, passed in the ISO-639
     * standard.
     * 
     * @return the language value.
     */
    public String getLanguage() {
        return this.language;
    }

    /**
     * Set the language property: Language of the source entity passing along the
     * call details, passed in the ISO-639
     * standard.
     * 
     * @param language the language value to set.
     * @return the TeamsPhoneSourceDetails object itself.
     */
    public TeamsPhoneSourceDetails setLanguage(String language) {
        this.language = language;
        return this;
    }

    /**
     * Get the status property: Status of the source entity passing along the call
     * details.
     * 
     * @return the status value.
     */
    public String getStatus() {
        return this.status;
    }

    /**
     * Set the status property: Status of the source entity passing along the call
     * details.
     * 
     * @param status the status value to set.
     * @return the TeamsPhoneSourceDetails object itself.
     */
    public TeamsPhoneSourceDetails setStatus(String status) {
        this.status = status;
        return this;
    }

    /**
     * Get the intendedTargets property: Intended targets of the source entity
     * passing along the call details.
     * 
     * @return the intendedTargets value.
     */
    public Map<String, CommunicationIdentifier> getIntendedTargets() {
        return this.intendedTargets;
    }

    /**
     * Set the intendedTargets property: Intended targets of the source entity
     * passing along the call details.
     * 
     * @param intendedTargets the intendedTargets value to set.
     * @return the TeamsPhoneSourceDetails object itself.
     */
    public TeamsPhoneSourceDetails setIntendedTargets(Map<String, CommunicationIdentifier> intendedTargets) {
        this.intendedTargets = intendedTargets;
        return this;
    }
}

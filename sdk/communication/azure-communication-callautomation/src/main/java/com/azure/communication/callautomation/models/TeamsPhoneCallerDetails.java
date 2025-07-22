// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.communication.common.CommunicationIdentifier;

import java.util.Map;

/**
 * Container for details relating to the original caller of the call.
 */
public class TeamsPhoneCallerDetails {
    /*
     * Caller's ID
     */
    private CommunicationIdentifier caller;

    /*
     * Caller's name
     */
    private String name;

    /*
     * Caller's phone number
     */
    private String phoneNumber;

    /*
     * Caller's record ID (ex in CRM)
     */
    private String recordId;

    /*
     * Caller's screen pop URL
     */
    private String screenPopUrl;

    /*
     * Flag indicating whether the caller was authenticated
     */
    private Boolean isAuthenticated;

    /*
     * A set of key value pairs (max 10, any additional entries would be ignored)
     * which a bot author wants to pass to
     * the Teams Client for display to the agent
     */
    private Map<String, String> additionalCallerInformation;

    /**
     * Creates an instance of TeamsPhoneCallerDetails class.
     */
    public TeamsPhoneCallerDetails() {
    }

    /**
     * Get the caller property: Caller's ID.
     * 
     * @return the caller value.
     */
    public CommunicationIdentifier getCaller() {
        return this.caller;
    }

    /**
     * Set the caller property: Caller's ID.
     * 
     * @param caller the caller value to set.
     * @return the TeamsPhoneCallerDetails object itself.
     */
    public TeamsPhoneCallerDetails setCaller(CommunicationIdentifier caller) {
        this.caller = caller;
        return this;
    }

    /**
     * Get the name property: Caller's name.
     * 
     * @return the name value.
     */
    public String getName() {
        return this.name;
    }

    /**
     * Set the name property: Caller's name.
     * 
     * @param name the name value to set.
     * @return the TeamsPhoneCallerDetails object itself.
     */
    public TeamsPhoneCallerDetails setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Get the phoneNumber property: Caller's phone number.
     * 
     * @return the phoneNumber value.
     */
    public String getPhoneNumber() {
        return this.phoneNumber;
    }

    /**
     * Set the phoneNumber property: Caller's phone number.
     * 
     * @param phoneNumber the phoneNumber value to set.
     * @return the TeamsPhoneCallerDetails object itself.
     */
    public TeamsPhoneCallerDetails setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }

    /**
     * Get the recordId property: Caller's record ID (ex in CRM).
     * 
     * @return the recordId value.
     */
    public String getRecordId() {
        return this.recordId;
    }

    /**
     * Set the recordId property: Caller's record ID (ex in CRM).
     * 
     * @param recordId the recordId value to set.
     * @return the TeamsPhoneCallerDetails object itself.
     */
    public TeamsPhoneCallerDetails setRecordId(String recordId) {
        this.recordId = recordId;
        return this;
    }

    /**
     * Get the screenPopUrl property: Caller's screen pop URL.
     * 
     * @return the screenPopUrl value.
     */
    public String getScreenPopUrl() {
        return this.screenPopUrl;
    }

    /**
     * Set the screenPopUrl property: Caller's screen pop URL.
     * 
     * @param screenPopUrl the screenPopUrl value to set.
     * @return the TeamsPhoneCallerDetails object itself.
     */
    public TeamsPhoneCallerDetails setScreenPopUrl(String screenPopUrl) {
        this.screenPopUrl = screenPopUrl;
        return this;
    }

    /**
     * Get the isAuthenticated property: Flag indicating whether the caller was
     * authenticated.
     * 
     * @return the isAuthenticated value.
     */
    public Boolean isAuthenticated() {
        return this.isAuthenticated;
    }

    /**
     * Set the isAuthenticated property: Flag indicating whether the caller was
     * authenticated.
     * 
     * @param isAuthenticated the isAuthenticated value to set.
     * @return the TeamsPhoneCallerDetails object itself.
     */
    public TeamsPhoneCallerDetails setIsAuthenticated(Boolean isAuthenticated) {
        this.isAuthenticated = isAuthenticated;
        return this;
    }

    /**
     * Get the additionalCallerInformation property: A set of key value pairs (max
     * 10, any additional entries would be
     * ignored) which a bot author wants to pass to the Teams Client for display to
     * the agent.
     * 
     * @return the additionalCallerInformation value.
     */
    public Map<String, String> getAdditionalCallerInformation() {
        return this.additionalCallerInformation;
    }

    /**
     * Set the additionalCallerInformation property: A set of key value pairs (max
     * 10, any additional entries would be
     * ignored) which a bot author wants to pass to the Teams Client for display to
     * the agent.
     * 
     * @param additionalCallerInformation the additionalCallerInformation value to
     *                                    set.
     * @return the TeamsPhoneCallerDetails object itself.
     */
    public TeamsPhoneCallerDetails setAdditionalCallerInformation(Map<String, String> additionalCallerInformation) {
        this.additionalCallerInformation = additionalCallerInformation;
        return this;
    }
}

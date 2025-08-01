// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

/**
 * Container for details relating to the entity responsible for the creation of
 * these call details.
 */
public class TeamsPhoneCallDetails {
    /*
     * Container for details relating to the original caller of the call
     */
    private TeamsPhoneCallerDetails teamsPhoneCallerDetails;

    /*
     * Container for details relating to the entity responsible for the creation of
     * these call details
     */
    private TeamsPhoneSourceDetails teamsPhoneSourceDetails;

    /*
     * Id to exclusively identify this call session. IVR will use this for their
     * telemetry/reporting.
     */
    private String sessionId;

    /*
     * The intent of the call
     */
    private String intent;

    /*
     * A very short description (max 48 chars) of the reason for the call. To be
     * displayed in Teams CallNotification
     */
    private String callTopic;

    /*
     * A summary of the call thus far. It will be displayed on a side panel in the
     * Teams UI
     */
    private String callContext;

    /*
     * Url for fetching the transcript of the call
     */
    private String transcriptUrl;

    /*
     * Sentiment of the call thus far
     */
    private String callSentiment;

    /*
     * Recommendations for resolving the issue based on the customer's intent and
     * interaction history
     */
    private String suggestedActions;

    /**
     * Creates an instance of TeamsPhoneCallDetails class.
     */
    public TeamsPhoneCallDetails() {
    }

    /**
     * Get the teamsPhoneCallerDetails property: Container for details relating to
     * the original caller of the call.
     * 
     * @return the teamsPhoneCallerDetails value.
     */
    public TeamsPhoneCallerDetails getTeamsPhoneCallerDetails() {
        return this.teamsPhoneCallerDetails;
    }

    /**
     * Set the teamsPhoneCallerDetails property: Container for details relating to
     * the original caller of the call.
     * 
     * @param teamsPhoneCallerDetails the teamsPhoneCallerDetails value to set.
     * @return the TeamsPhoneCallDetails object itself.
     */
    public TeamsPhoneCallDetails setTeamsPhoneCallerDetails(TeamsPhoneCallerDetails teamsPhoneCallerDetails) {
        this.teamsPhoneCallerDetails = teamsPhoneCallerDetails;
        return this;
    }

    /**
     * Get the teamsPhoneSourceDetails property: Container for details relating to
     * the entity responsible for the
     * creation of these call details.
     * 
     * @return the teamsPhoneSourceDetails value.
     */

    public TeamsPhoneSourceDetails getTeamsPhoneSourceDetails() {
        return this.teamsPhoneSourceDetails;
    }

    /**
     * Set the teamsPhoneSourceDetails property: Container for details relating to
     * the entity responsible for the
     * creation of these call details.
     * 
     * @param teamsPhoneSourceDetails the teamsPhoneSourceDetails value to set.
     * @return the TeamsPhoneCallDetails object itself.
     */
    public TeamsPhoneCallDetails setTeamsPhoneSourceDetails(TeamsPhoneSourceDetails teamsPhoneSourceDetails) {
        this.teamsPhoneSourceDetails = teamsPhoneSourceDetails;
        return this;
    }

    /**
     * Get the sessionId property: Id to exclusively identify this call session. IVR
     * will use this for their
     * telemetry/reporting.
     * 
     * @return the sessionId value.
     */
    public String getSessionId() {
        return this.sessionId;
    }

    /**
     * Set the sessionId property: Id to exclusively identify this call session. IVR
     * will use this for their
     * telemetry/reporting.
     * 
     * @param sessionId the sessionId value to set.
     * @return the TeamsPhoneCallDetails object itself.
     */
    public TeamsPhoneCallDetails setSessionId(String sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    /**
     * Get the intent property: The intent of the call.
     * 
     * @return the intent value.
     */
    public String getIntent() {
        return this.intent;
    }

    /**
     * Set the intent property: The intent of the call.
     * 
     * @param intent the intent value to set.
     * @return the TeamsPhoneCallDetails object itself.
     */
    public TeamsPhoneCallDetails setIntent(String intent) {
        this.intent = intent;
        return this;
    }

    /**
     * Get the callTopic property: A very short description (max 48 chars) of the
     * reason for the call. To be displayed
     * in Teams CallNotification.
     * 
     * @return the callTopic value.
     */
    public String getCallTopic() {
        return this.callTopic;
    }

    /**
     * Set the callTopic property: A very short description (max 48 chars) of the
     * reason for the call. To be displayed
     * in Teams CallNotification.
     * 
     * @param callTopic the callTopic value to set.
     * @return the TeamsPhoneCallDetails object itself.
     */
    public TeamsPhoneCallDetails setCallTopic(String callTopic) {
        this.callTopic = callTopic;
        return this;
    }

    /**
     * Get the callContext property: A summary of the call thus far. It will be
     * displayed on a side panel in the Teams
     * UI.
     * 
     * @return the callContext value.
     */
    public String getCallContext() {
        return this.callContext;
    }

    /**
     * Set the callContext property: A summary of the call thus far. It will be
     * displayed on a side panel in the Teams
     * UI.
     * 
     * @param callContext the callContext value to set.
     * @return the TeamsPhoneCallDetails object itself.
     */
    public TeamsPhoneCallDetails setCallContext(String callContext) {
        this.callContext = callContext;
        return this;
    }

    /**
     * Get the transcriptUrl property: Url for fetching the transcript of the call.
     * 
     * @return the transcriptUrl value.
     */
    public String getTranscriptUrl() {
        return this.transcriptUrl;
    }

    /**
     * Set the transcriptUrl property: Url for fetching the transcript of the call.
     * 
     * @param transcriptUrl the transcriptUrl value to set.
     * @return the TeamsPhoneCallDetails object itself.
     */
    public TeamsPhoneCallDetails setTranscriptUrl(String transcriptUrl) {
        this.transcriptUrl = transcriptUrl;
        return this;
    }

    /**
     * Get the callSentiment property: Sentiment of the call thus far.
     * 
     * @return the callSentiment value.
     */
    public String getCallSentiment() {
        return this.callSentiment;
    }

    /**
     * Set the callSentiment property: Sentiment of the call thus far.
     * 
     * @param callSentiment the callSentiment value to set.
     * @return the TeamsPhoneCallDetails object itself.
     */
    public TeamsPhoneCallDetails setCallSentiment(String callSentiment) {
        this.callSentiment = callSentiment;
        return this;
    }

    /**
     * Get the suggestedActions property: Recommendations for resolving the issue
     * based on the customer's intent and
     * interaction history.
     * 
     * @return the suggestedActions value.
     */

    public String getSuggestedActions() {
        return this.suggestedActions;
    }

    /**
     * Set the suggestedActions property: Recommendations for resolving the issue
     * based on the customer's intent and
     * interaction history.
     * 
     * @param suggestedActions the suggestedActions value to set.
     * @return the TeamsPhoneCallDetails object itself.
     */
    public TeamsPhoneCallDetails setSuggestedActions(String suggestedActions) {
        this.suggestedActions = suggestedActions;
        return this;
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
// Code generated by Microsoft (R) AutoRest Code Generator.

package com.azure.communication.email.implementation.models;

import com.azure.communication.email.models.EmailAddress;
import com.azure.core.annotation.Fluent;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Map;

/** Message payload for sending an email. */
@Fluent
public final class EmailMessage {
    /*
     * Custom email headers to be passed.
     */
    @JsonProperty(value = "headers")
    private Map<String, String> headers;

    /*
     * Sender email address from a verified domain.
     */
    @JsonProperty(value = "senderAddress", required = true)
    private String senderAddress;

    /*
     * Email content to be sent.
     */
    @JsonProperty(value = "content", required = true)
    private EmailContent content;

    /*
     * Recipients for the email.
     */
    @JsonProperty(value = "recipients", required = true)
    private EmailRecipients recipients;

    /*
     * List of attachments. Please note that we limit the total size of an email request (which includes attachments)
     * to 10MB.
     */
    @JsonProperty(value = "attachments")
    private List<EmailAttachment> attachments;

    /*
     * Email addresses where recipients' replies will be sent to.
     */
    @JsonProperty(value = "replyTo")
    private List<EmailAddress> replyTo;

    /*
     * Indicates whether user engagement tracking should be disabled for this request if the resource-level user
     * engagement tracking setting was already enabled in the control plane.
     */
    @JsonProperty(value = "userEngagementTrackingDisabled")
    private Boolean userEngagementTrackingDisabled;

    /**
     * Creates an instance of EmailMessage class.
     *
     * @param senderAddress the senderAddress value to set.
     * @param content the content value to set.
     * @param recipients the recipients value to set.
     */
    @JsonCreator
    public EmailMessage(
            @JsonProperty(value = "senderAddress", required = true) String senderAddress,
            @JsonProperty(value = "content", required = true) EmailContent content,
            @JsonProperty(value = "recipients", required = true) EmailRecipients recipients) {
        this.senderAddress = senderAddress;
        this.content = content;
        this.recipients = recipients;
    }

    /**
     * Get the headers property: Custom email headers to be passed.
     *
     * @return the headers value.
     */
    public Map<String, String> getHeaders() {
        return this.headers;
    }

    /**
     * Set the headers property: Custom email headers to be passed.
     *
     * @param headers the headers value to set.
     * @return the EmailMessage object itself.
     */
    public EmailMessage setHeaders(Map<String, String> headers) {
        this.headers = headers;
        return this;
    }

    /**
     * Get the senderAddress property: Sender email address from a verified domain.
     *
     * @return the senderAddress value.
     */
    public String getSenderAddress() {
        return this.senderAddress;
    }

    /**
     * Get the content property: Email content to be sent.
     *
     * @return the content value.
     */
    public EmailContent getContent() {
        return this.content;
    }

    /**
     * Get the recipients property: Recipients for the email.
     *
     * @return the recipients value.
     */
    public EmailRecipients getRecipients() {
        return this.recipients;
    }

    /**
     * Get the attachments property: List of attachments. Please note that we limit the total size of an email request
     * (which includes attachments) to 10MB.
     *
     * @return the attachments value.
     */
    public List<EmailAttachment> getAttachments() {
        return this.attachments;
    }

    /**
     * Set the attachments property: List of attachments. Please note that we limit the total size of an email request
     * (which includes attachments) to 10MB.
     *
     * @param attachments the attachments value to set.
     * @return the EmailMessage object itself.
     */
    public EmailMessage setAttachments(List<EmailAttachment> attachments) {
        this.attachments = attachments;
        return this;
    }

    /**
     * Get the replyTo property: Email addresses where recipients' replies will be sent to.
     *
     * @return the replyTo value.
     */
    public List<EmailAddress> getReplyTo() {
        return this.replyTo;
    }

    /**
     * Set the replyTo property: Email addresses where recipients' replies will be sent to.
     *
     * @param replyTo the replyTo value to set.
     * @return the EmailMessage object itself.
     */
    public EmailMessage setReplyTo(List<EmailAddress> replyTo) {
        this.replyTo = replyTo;
        return this;
    }

    /**
     * Get the userEngagementTrackingDisabled property: Indicates whether user engagement tracking should be disabled
     * for this request if the resource-level user engagement tracking setting was already enabled in the control plane.
     *
     * @return the userEngagementTrackingDisabled value.
     */
    public Boolean isUserEngagementTrackingDisabled() {
        return this.userEngagementTrackingDisabled;
    }

    /**
     * Set the userEngagementTrackingDisabled property: Indicates whether user engagement tracking should be disabled
     * for this request if the resource-level user engagement tracking setting was already enabled in the control plane.
     *
     * @param userEngagementTrackingDisabled the userEngagementTrackingDisabled value to set.
     * @return the EmailMessage object itself.
     */
    public EmailMessage setUserEngagementTrackingDisabled(Boolean userEngagementTrackingDisabled) {
        this.userEngagementTrackingDisabled = userEngagementTrackingDisabled;
        return this;
    }
}

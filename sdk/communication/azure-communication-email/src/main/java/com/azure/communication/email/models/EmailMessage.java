// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.email.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/** Message payload for sending an email. */
public final class EmailMessage {
    /*
     * Custom email headers to be passed.
     */
    private Map<String, String> headers;

    /*
     * Sender email address from a verified domain.
     */
    private String senderAddress;

    /*
     * Subject of the email message
     */
    private String subject;

    /*
     * Plain text version of the email message.
     */
    private String bodyPlainText;

    /*
     * Html version of the email message.
     */
    private String bodyHtml;

    /*
     * Email To recipients
     */
    private List<EmailAddress> toRecipients;

    /*
     * Email CC recipients
     */
    private List<EmailAddress> ccRecipients;

    /*
     * Email BCC recipients
     */
    private List<EmailAddress> bccRecipients;

    /*
     * List of attachments. Please note that we limit the total size of an email request (which includes attachments)
     * to 10MB.
     */
    private List<EmailAttachment> attachments;

    /*
     * Email addresses where recipients' replies will be sent to.
     */
    private List<EmailAddress> replyTo;

    /*
     * Indicates whether user engagement tracking should be disabled for this request if the resource-level user
     * engagement tracking setting was already enabled in the control plane.
     */
    private Boolean userEngagementTrackingDisabled;

    /**
     * Get the headers property: Custom email headers to be passed.
     *
     * @return the headers value.
     */
    public Map<String, String> getHeaders() {
        return headers;
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
        return senderAddress;
    }

    /**
     * Set the senderAddress property: Sender email address from a verified domain.
     *
     * @param senderAddress the senderAddress value to set.
     * @return the EmailMessage object itself.
     */
    public EmailMessage setSenderAddress(String senderAddress) {
        this.senderAddress = senderAddress;
        return this;
    }

    /**
     * Get the subject property: Subject of the email message.
     *
     * @return the subject value.
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Set the subject property: Subject of the email message.
     *
     * @param subject the subject value to set.
     * @return the EmailMessage object itself.
     */
    public EmailMessage setSubject(String subject) {
        this.subject = subject;
        return this;
    }

    /**
     * Get the bodyPlainText property: Plain text version of the email message.
     *
     * @return the bodyPlainText value.
     */
    public String getBodyPlainText() {
        return bodyPlainText;
    }

    /**
     * Set the bodyPlainText property: Plain text version of the email message.
     *
     * @param bodyPlainText the bodyPlainText value to set.
     * @return the EmailMessage object itself.
     */
    public EmailMessage setBodyPlainText(String bodyPlainText) {
        this.bodyPlainText = bodyPlainText;
        return this;
    }

    /**
     * Get the bodyHtml property: Html version of the email message.
     *
     * @return the html value.
     */
    public String getBodyHtml() {
        return bodyHtml;
    }

    /**
     * Set the bodyHtml property: Html version of the email message.
     *
     * @param bodyHtml the html value to set.
     * @return the EmailContent object itself.
     */
    public EmailMessage setBodyHtml(String bodyHtml) {
        this.bodyHtml = bodyHtml;
        return this;
    }

    /**
     * Get the toRecipients property: Email To recipients.
     *
     * @return the toRecipients value.
     */
    public List<EmailAddress> getToRecipients() {
        return toRecipients;
    }

    /**
     * Set the toRecipients property: Email To recipients.
     *
     * @param toRecipients the toRecipients value to set.
     * @return the EmailContent object itself.
     */
    public EmailMessage setToRecipients(List<EmailAddress> toRecipients) {
        this.toRecipients = toRecipients;
        return this;
    }

    /**
     * Set the toRecipients property: Email To recipients.
     *
     * @param toRecipients the toRecipients value to set.
     * @return the EmailContent object itself.
     */
    public EmailMessage setToRecipients(EmailAddress... toRecipients) {
        this.toRecipients = Arrays.asList(toRecipients);
        return this;
    }

    /**
     * Set the toRecipients property: Email To recipients.
     *
     * @param toRecipientAddresses the addresses of the toRecipients to set.
     * @return the EmailContent object itself.
     */
    public EmailMessage setToRecipients(String... toRecipientAddresses) {
        List<EmailAddress> toRecipients = new ArrayList<>();
        for (String toRecipientAddress: toRecipientAddresses) {
            toRecipients.add(new EmailAddress(toRecipientAddress));
        }
        this.toRecipients = toRecipients;
        return this;
    }

    /**
     * Get the ccRecipients property: Email CC recipients.
     *
     * @return the cc value.
     */
    public List<EmailAddress> getCcRecipients() {
        return ccRecipients;
    }

    /**
     * Set the ccRecipients property: Email CC recipients.
     *
     * @param ccRecipients the cc value to set.
     * @return the EmailMessage object itself.
     */
    public EmailMessage setCcRecipients(List<EmailAddress> ccRecipients) {
        this.ccRecipients = ccRecipients;
        return this;
    }

    /**
     * Set the ccRecipients property: Email CC recipients.
     *
     * @param ccRecipients the cc value to set.
     * @return the EmailMessage object itself.
     */
    public EmailMessage setCcRecipients(EmailAddress... ccRecipients) {
        this.ccRecipients = Arrays.asList(ccRecipients);
        return this;
    }

    /**
     * Set the ccRecipients property: Email CC recipients.
     *
     * @param ccRecipientAddresses the addresses of the ccRecipients to set.
     * @return the EmailContent object itself.
     */
    public EmailMessage setCcRecipients(String... ccRecipientAddresses) {
        List<EmailAddress> ccRecipients = new ArrayList<>();
        for (String ccRecipientAddress: ccRecipientAddresses) {
            ccRecipients.add(new EmailAddress(ccRecipientAddress));
        }
        this.ccRecipients = ccRecipients;
        return this;
    }

    /**
     * Get the bccRecipients property: Email BCC recipients.
     *
     * @return the bCC value.
     */
    public List<EmailAddress> getBccRecipients() {
        return bccRecipients;
    }

    /**
     * Set the bccRecipients property: Email BCC recipients.
     *
     * @param bccRecipients the bccRecipients value to set.
     * @return the EmailRecipients object itself.
     */
    public EmailMessage setBccRecipients(List<EmailAddress> bccRecipients) {
        this.bccRecipients = bccRecipients;
        return this;
    }

    /**
     * Set the bccRecipients property: Email BCC recipients.
     *
     * @param bccRecipients the bccRecipients value to set.
     * @return the EmailRecipients object itself.
     */
    public EmailMessage setBccRecipients(EmailAddress... bccRecipients) {
        this.bccRecipients = Arrays.asList(bccRecipients);
        return this;
    }

    /**
     * Set the bccRecipients property: Email BCC recipients.
     *
     * @param bccRecipientAddresses the addresses of the bccRecipients to set.
     * @return the EmailContent object itself.
     */
    public EmailMessage setBccRecipients(String... bccRecipientAddresses) {
        List<EmailAddress> bccRecipients = new ArrayList<>();
        for (String bccRecipientAddress: bccRecipientAddresses) {
            bccRecipients.add(new EmailAddress(bccRecipientAddress));
        }
        this.bccRecipients = bccRecipients;
        return this;
    }

    /**
     * Get the attachments property: List of attachments. Please note that we limit the total size of an email request
     * (which includes attachments) to 10MB.
     *
     * @return the attachments value.
     */
    public List<EmailAttachment> getAttachments() {
        return attachments;
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
     * Set the attachments property: List of attachments. Please note that we limit the total size of an email request
     * (which includes attachments) to 10MB.
     *
     * @param attachments the attachments value to set.
     * @return the EmailMessage object itself.
     */
    public EmailMessage setAttachments(EmailAttachment... attachments) {
        this.attachments = Arrays.asList(attachments);
        return this;
    }

    /**
     * Get the replyTo property: Email addresses where recipients' replies will be sent to.
     *
     * @return the replyTo value.
     */
    public List<EmailAddress> getReplyTo() {
        return replyTo;
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
     * Set the replyTo property: Email addresses where recipients' replies will be sent to.
     *
     * @param replyTo the replyTo value to set.
     * @return the EmailMessage object itself.
     */
    public EmailMessage setReplyTo(EmailAddress... replyTo) {
        this.replyTo = Arrays.asList(replyTo);
        return this;
    }

    /**
     * Get the disableUserEngagementTracking property: Indicates whether user engagement tracking should be disabled for
     * this request if the resource-level user engagement tracking setting was already enabled in the control plane.
     *
     * @return the userEngagementTrackingDisabled value.
     */
    public Boolean isUserEngagementTrackingDisabled() {
        return userEngagementTrackingDisabled;
    }

    /**
     * Set the userEngagementTrackingDisabled property: Indicates whether user engagement tracking should be disabled for
     * this request if the resource-level user engagement tracking setting was already enabled in the control plane.
     *
     * @param userEngagementTrackingDisabled the userEngagementTrackingDisabled value to set.
     * @return the EmailMessage object itself.
     */
    public EmailMessage setUserEngagementTrackingDisabled(Boolean userEngagementTrackingDisabled) {
        this.userEngagementTrackingDisabled = userEngagementTrackingDisabled;
        return this;
    }
}

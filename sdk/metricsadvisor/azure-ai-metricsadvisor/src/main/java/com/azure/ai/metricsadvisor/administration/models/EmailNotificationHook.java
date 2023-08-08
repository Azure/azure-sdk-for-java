// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.administration.models;

import com.azure.core.annotation.Fluent;

import java.util.Collections;
import java.util.List;

/**
 * A hook that describes email based incident alerts notification.
 */
@Fluent
public final class EmailNotificationHook extends NotificationHook {
    private String name;
    private String description;
    private List<String> emailsToAlert;
    private String externalLink;

    /**
     * Create a new instance of EmailNotificationHook.
     *
     * @param name The email hook name.
     */
    public EmailNotificationHook(String name) {
        this.name = name;
    }

    /**
     * Create a new instance of EmailNotificationHook.
     *
     * @param name The email hook name.
     * @param emails The emails to send the alerts.
     */
    public EmailNotificationHook(String name, List<String> emails) {
        if (emails != null) {
            this.emailsToAlert = dedupe(emails);
        }
        this.name = name;
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getDescription() {
        return this.description;
    }

    /**
     * Gets the customized external link which is displayed in the title bar of the alert email.
     *
     * @return The external link.
     */
    public String getExternalLink() {
        return this.externalLink;
    }

    /**
     * Gets the emails to send the alerts.
     *
     * @return The emails.
     */
    public List<String> getEmailsToAlert() {
        if (this.emailsToAlert != null) {
            return Collections.unmodifiableList(this.emailsToAlert);
        } else {
            return Collections.emptyList();
        }
    }

    /**
     * Sets the emails to send the alert.
     *
     * @param emails The emails.
     * @return The EmailNotificationHook object itself.
     */
    public EmailNotificationHook setEmailsToAlert(List<String> emails) {
        this.emailsToAlert = emails != null ? dedupe(emails) : null;
        return this;
    }

    /**
     * Sets email hook name.
     *
     * @param name The email hook name.
     * @return The EmailNotificationHook object itself.
     */
    public EmailNotificationHook setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets email hook description.
     *
     * @param description The email hook description.
     * @return The EmailNotificationHook object itself.
     */
    public EmailNotificationHook setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Sets the customized external link which is displayed in the title bar of the alert email.
     *
     * @param externalLink The customized link.
     * @return The EmailNotificationHook object itself.
     */
    public EmailNotificationHook setExternalLink(String externalLink) {
        this.externalLink = externalLink;
        return this;
    }

    /**
     * Sets the user e-mails and clientIds with administrative rights to manage the hook.
     * <p>
     * The administrators have total control over the hook, being allowed to update or delete the hook.
     * Each element in this list represents a user with administrator access, but the value of each string element
     * is either user email address or clientId uniquely identifying the user service principal.
     *
     * @param admins A list containing email or clientId of admins
     * @return The EmailNotificationHook object itself.
     */
    public EmailNotificationHook setAdmins(List<String> admins) {
        super.setAdministrators(admins);
        return this;
    }

    List<String> getEmailsToAlertRaw() {
        // package private getter that won't translate null emails to empty-list.
        return this.emailsToAlert;
    }
}

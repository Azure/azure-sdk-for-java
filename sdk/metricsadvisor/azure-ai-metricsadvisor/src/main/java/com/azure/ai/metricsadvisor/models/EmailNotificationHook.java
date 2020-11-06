// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.metricsadvisor.models;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.TreeSet;
import java.util.stream.IntStream;

/**
 * A hook that describes email based incident alerts notification.
 */
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
        this.emailsToAlert = new ArrayList<>();
        this.name = name;
    }

    /**
     * Create a new instance of EmailNotificationHook.
     *
     * @param name The email hook name.
     * @param emails The emails to send the alerts.
     */
    public EmailNotificationHook(String name, List<String> emails) {
        if (emails == null) {
            this.emailsToAlert = new ArrayList<>();
        } else {
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
     * Gets the emails to send the alerts.
     *
     * @return The emails.
     */
    public List<String> getEmailsToAlert() {
        return Collections.unmodifiableList(this.emailsToAlert);
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
     * Sets the emails to send the alert.
     *
     * @param emails The emails.
     * @return The EmailNotificationHook object itself.
     */
    public EmailNotificationHook setEmailsToAlert(List<String> emails) {
        if (emails == null) {
            this.emailsToAlert = new ArrayList<>();
        } else {
            this.emailsToAlert = dedupe(emails);
        }
        return this;
    }

    /**
     * Add an email to the list of emails to send the alert.
     *
     * @param email The email to add.
     * @return The EmailNotificationHook object itself.
     */
    public EmailNotificationHook addEmailToAlert(String email) {
        if (this.emailsToAlert
            .stream()
            .anyMatch(email::equalsIgnoreCase)) {
            return this;
        }
        this.emailsToAlert.add(email);
        return this;
    }

    /**
     * Removes an email from the list of alert emails.
     *
     * @param email The email to remove.
     * @return The EmailNotificationHook object itself.
     */
    public EmailNotificationHook removeEmailToAlert(String email) {
        int idx = IntStream.range(0, this.emailsToAlert.size())
            .filter(i -> this.emailsToAlert.get(i).equalsIgnoreCase(email))
            .findFirst()
            .orElse(-1);
        if (idx != -1) {
            this.emailsToAlert.remove(idx);
        }
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
     * Removes duplicates in a list.
     *
     * @param list The list to dedupe
     * @return A new deduped list.
     */
    private List<String> dedupe(List<String> list) {
        TreeSet<String> seen = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        List<String> dedupedList = new ArrayList<>(list);
        dedupedList.removeIf(e -> !seen.add(e));
        return dedupedList;
    }
}

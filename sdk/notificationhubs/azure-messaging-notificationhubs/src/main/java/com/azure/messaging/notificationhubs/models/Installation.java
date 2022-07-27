// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.notificationhubs.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Represents an Azure Notification Hubs installation.
 */
public abstract class Installation {
    @JsonProperty(value = "installationId", required = true)
    private String installationId;

    @JsonProperty(value = "userId")
    private String userId;

    @JsonProperty(value = "platform", required = true)
    private NotificationPlatform platform;

    @JsonProperty(value = "expirationTime", access = JsonProperty.Access.WRITE_ONLY)
    private Date expirationTime;

    @JsonProperty(value = "lastActiveOn", access = JsonProperty.Access.WRITE_ONLY)
    private Date lastActiveOn;

    @JsonProperty(value = "tags")
    private Set<String> tags;

    @JsonProperty(value = "templates")
    private Map<String, InstallationTemplate> templates;

    /**
     * Gets the Installation ID.
     * @return The Installation ID.
     */
    public String getInstallationId() {
        return this.installationId;
    }

    /**
     * Sets the Installation ID.
     * @param value The Installation ID to set.
     */
    public void setInstallationId(String value) {
        this.installationId = value;
    }

    /**
     * Gets the installation User ID.
     * @return The installation User ID.
     */
    public String getUserId() {
        return this.userId;
    }

    /**
     * Sets the installation user ID.
     * @param value The installation user ID to send.
     */
    public void setUserId(String value) {
        this.userId = value;
    }

    /**
     * Gets the installation expiration time.
     * @return The installation expiration time.
     */
    public Date getExpirationTime() {
        return this.expirationTime;
    }

    /**
     * Gets the last active date for the installation device.
     * @return The last active date for the installation device.
     */
    public Date getLastActiveOn() {
        return this.lastActiveOn;
    }

    /**
     * Gets the installation tags list.
     * @return The list of tags for the installation.
     */
    public Set<String> getTags() {
        if (this.tags == null) {
            return null;
        }

        return new HashSet<>(this.tags);
    }

    /**
     * Adds a tag to the installation.
     * @param tag The tag to add to the installation.
     */
    public void addTag(String tag) {
        if (this.tags == null) {
            this.tags = new HashSet<>();
        }

        this.tags.add(tag);
    }

    /**
     * Adds the tags to the installation.
     * @param tags The tags to add to the installation.
     */
    public void addTags(Collection<? extends String> tags) {
        if (this.tags == null) {
            this.tags = new HashSet<>();
        }

        this.tags.addAll(tags);
    }

    /**
     * Removes a tag from the installation.
     * @param tag The tag to remove from the installation.
     * @return Returns true if removed, otherwise false.
     */
    public boolean removeTag(String tag) {
        if (this.tags == null) {
            return false;
        }

        return this.tags.remove(tag);
    }

    /**
     * Clears the tags from the installation.
     */
    public void clearTags() {
        if (this.tags == null) {
            return;
        }

        this.tags.clear();
    }

    /**
     * Gets the templates for the installation.
     * @return A dictionary of templates for the installation.
     */
    public Map<String, InstallationTemplate> getTemplates() {
        if (this.templates == null) {
            return null;
        }

        return new HashMap<>(this.templates);
    }

    /**
     * Adds a template to the installation.
     * @param templateName The name of the template.
     * @param template The installation template to add.
     */
    public void addTemplate(String templateName, InstallationTemplate template) {
        if (this.templates == null) {
            this.templates = new HashMap<>();
        }

        this.templates.put(templateName, template);
    }

    /**
     * Removes a template from the installation.
     * @param templateName The name of the template to remove.
     */
    public void removeTemplate(String templateName) {
        if (this.templates == null) {
            return;
        }

        this.templates.remove(templateName);
    }

    /**
     * Clears the templates from the installation.
     */
    public void clearTemplates() {
        if (this.templates == null) {
            return;
        }

        this.templates.clear();
    }
}

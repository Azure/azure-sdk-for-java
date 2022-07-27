// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.messaging.notificationhubs.models;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents an Azure Notification Hubs registration description.
 */
public abstract class RegistrationDescription {

    @JacksonXmlProperty(localName = "RegistrationId")
    private String registrationId;

    @JacksonXmlProperty(localName = "ETag")
    private String etag;

    @JacksonXmlProperty(localName = "Tags")
    private String tags;

    private final Set<String> tagsList = new HashSet<>();

    protected RegistrationDescription() {

    }

    protected RegistrationDescription(String registrationId) {
        this.registrationId = registrationId;
    }

    protected RegistrationDescription(Collection<? extends String> tags) {
        this.addTags(tags);
    }

    protected RegistrationDescription(String registrationId, Collection<? extends String> tags) {
        this.registrationId = registrationId;
        this.addTags(tags);
    }

    /**
     * Gets the registration description ID.
     * @return The registration description ID.
     */
    public String getRegistrationId() {
        return this.registrationId;
    }

    /**
     * Sets the registration description ID.
     * @param value The registration description ID to set.
     */
    public void setRegistrationId(String value) {
        this.registrationId = value;
    }

    /**
     * Gets the registration description tags.
     * @return The registration description tags.
     */
    public Set<String> getTags() {
        return new HashSet<>(this.tagsList);
    }

    /**
     * Adds a tag to the registration description tags.
     * @param tag The tag to add to the registration description tags.
     */
    public void addTag(String tag) {
        this.tagsList.add(tag);
        this.setTagsProperty();
    }

    /**
     * Adds tags to the registration description tags.
     * @param tags The tags to add to the registration description tags.
     */
    public void addTags(Collection<? extends String> tags) {
        this.tagsList.addAll(tags);
        this.setTagsProperty();
    }

    /**
     * Removes a tag from the registration description tags.
     * @param tag The tag to remove from the registration description tags.
     * @return Returns true if removed. else false.
     */
    public boolean removeTag(String tag) {
        boolean removed = this.tagsList.remove(tag);
        this.setTagsProperty();
        return removed;
    }

    /**
     * Clears the registration description tags.
     */
    public void clearTags() {
        this.tagsList.clear();
        this.setTagsProperty();
    }

    private void setTagsProperty() {
        this.tags = String.join(",", this.tagsList);
    }
}

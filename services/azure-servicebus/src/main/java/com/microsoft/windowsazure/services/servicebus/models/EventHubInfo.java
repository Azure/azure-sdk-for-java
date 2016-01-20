package com.microsoft.windowsazure.services.servicebus.models;

import com.microsoft.windowsazure.services.servicebus.implementation.Content;
import com.microsoft.windowsazure.services.servicebus.implementation.EntityAvailabilityStatus;
import com.microsoft.windowsazure.services.servicebus.implementation.EntityStatus;
import com.microsoft.windowsazure.services.servicebus.implementation.Entry;
import com.microsoft.windowsazure.services.servicebus.implementation.EntryModel;
import com.microsoft.windowsazure.services.servicebus.implementation.EventHubDescription;

import java.net.URI;
import java.util.Calendar;

import javax.ws.rs.core.MediaType;

public class EventHubInfo extends EntryModel<EventHubDescription> {

    /**
     * Creates an instance of the <code>EventHubInfo</code> class.
     */
    public EventHubInfo() {
        super(new Entry(), new EventHubDescription());
        getEntry().setContent(new Content());
        getEntry().getContent().setType(MediaType.APPLICATION_XML);
        getEntry().getContent().setEventHubDescription(getModel());
    }

    /**
     * Creates an instance of the <code>EventHubInfo</code> class using the
     * specified entry.
     *
     * @param entry
     *            An <code>Entry</code> object that represents the entry for the
     *            topic.
     */
    public EventHubInfo(Entry entry) {
        super(entry, entry.getContent().getEventHubDescription());
    }

    /**
     * Creates an instance of the <code>EventHubInfo</code> class using the
     * specified name.
     *
     * @param path
     *            A <code>String</code> object that represents the name for the
     *            event hub.
     */
    public EventHubInfo(String path) {
        this();
        setPath(path);
    }

    /**
     * Returns the name of the event hub.
     *
     * @return A <code>String</code> object that represents the name of the
     *         event hub.
     */
    public String getPath() {
        return getEntry().getTitle();
    }

    /**
     * Sets the name of the event hub.
     *
     * @param value
     *            A <code>String</code> that represents the name of the EventHub.
     *
     * @return A <code>EventHubInfo</code> object that represents the updated
     *         event hub.
     */
    public EventHubInfo setPath(String value) {
        getEntry().setTitle(value);
        return this;
    }

    /**
     * Returns the message retention in days.
     *
     * @return A <code>long</code> that represents the
     *         message retention in days.
     */
    public Long getDefaultMessageRetention() {
        return getModel().getDefaultMessageRetention();
    }

    /**
     * Sets the message retention in days.
     *
     * @param value
     *            A <code>long</code> that represents the
     *            message retention in days.
     *
     * @return A <code>EventHubInfo</code> object that represents the updated
     *         event hub.
     */
    public EventHubInfo setDefaultMessageRetention(Long value) {
        getModel().setDefaultMessageRetention(value);
        return this;
    }

    /**
     * Returns the partition count of the event hub.
     *
     * @return The partition count of the event hub.
     */
    public Integer getPartitionCount() {
        return getModel().getPartitionCount();
    }

    /**
     * Sets the partition count of the event hub.
     *
     * @param value
     *            The partition count of the event hub.
     *
     * @return A <code>EventHubInfo</code> object that represents the updated
     *         event hub.
     */
    public EventHubInfo setPartitionCount(Integer value) {
        getModel().setPartitionCount(value);
        return this;
    }

    /**
     * Sets the status.
     *
     * @param status
     *            the status
     * @return A <code>EventHubInfo</code> object that represents the updated
     *         event hub.
     */
    public EventHubInfo setStatus(EntityStatus status) {
        getModel().setStatus(status);
        return this;
    }

    /**
     * Gets the status.
     *
     * @return An <code>EntityStatus</code> object that represents the status of
     *         the object.
     */
    public EntityStatus getStatus() {
        return getModel().getStatus();
    }

    /**
     * Sets the created at.
     *
     * @param createdAt
     *            the created at
     * @return A <code>TopicInfo</code> object that represents the updated
     *         event hub.
     */
    public EventHubInfo setCreatedAt(Calendar createdAt) {
        getModel().setCreatedAt(createdAt);
        return this;
    }

    /**
     * Gets the created at.
     *
     * @return A <code>Calendar</code> object which represents when the event hub
     *         was created.
     */
    public Calendar getCreatedAt() {
        return getModel().getCreatedAt();
    }

    /**
     * Sets the updated at.
     *
     * @param updatedAt
     *            A <code>Calendar</code> object which represents when the event hub
     *            was updated.
     * @return A <code>TopicInfo</code> object that represents the updated
     *         event hub.
     */
    public EventHubInfo setUpdatedAt(Calendar updatedAt) {
        getModel().setUpdatedAt(updatedAt);
        return this;
    }

    /**
     * Gets the updated at.
     *
     * @return A <code>Calendar</code> object which represents when the event hub
     *         was updated.
     */
    public Calendar getUpdatedAt() {
        return getModel().getUpdatedAt();
    }

    /**
     * Sets the accessed at.
     *
     * @param accessedAt
     *            A <code>Calendar</code> instance representing when event hub was
     *            last accessed at.
     * @return A <code>EventHubInfo</code> object that represents the updated
     *         event hub.
     */
    public EventHubInfo setAccessedAt(Calendar accessedAt) {
        getModel().setAccessedAt(accessedAt);
        return this;
    }

    /**
     * Gets the accessed at.
     *
     * @return A <code>Calendar</code> instance representing when event hub was last
     *         accessed at.
     */
    public Calendar getAccessedAt() {
        return getModel().getAccessedAt();
    }

    /**
     * Sets the user metadata.
     *
     * @param userMetadata
     *            A <code>String</code> represents the user metadata.
     * @return A <code>EventHubInfo</code> object that represents the updated
     *         event hub.
     */
    public EventHubInfo setUserMetadata(String userMetadata) {
        getModel().setUserMetadata(userMetadata);
        return this;
    }

    /**
     * Gets the user metadata.
     *
     * @return A <code>String</code> represents the user metadata.
     */
    public String getUserMetadata() {
        return getModel().getUserMetadata();
    }

    /**
     * Sets the entity availability status.
     *
     * @param entityAvailabilityStatus
     *            An <code>EntityAvailabilityStatus</code> instance which
     *            represents the entity availability status.
     * @return A <code>TopicInfo</code> object that represents the updated
     *         event hub.
     */
    public EventHubInfo setEntityAvailabilityStatus(
            EntityAvailabilityStatus entityAvailabilityStatus) {
        getModel().setEntityAvailabilityStatus(entityAvailabilityStatus);
        return this;
    }

    /**
     * Gets the entity availability status.
     *
     * @return An <code>EntityAvailabilityStatus</code> instance which
     *         represents the entity availability status.
     */
    public EntityAvailabilityStatus getEntityAvailabilityStatus() {
        return getModel().getEntityAvailabilityStatus();
    }

    /**
     * Gets the uri.
     *
     * @return the uri
     */
    public URI getUri() {
        return URI.create(getEntry().getId());
    }
}

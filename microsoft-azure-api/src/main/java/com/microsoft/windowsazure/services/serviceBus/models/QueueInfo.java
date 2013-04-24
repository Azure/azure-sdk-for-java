/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.microsoft.windowsazure.services.serviceBus.models;

import java.util.Calendar;

import javax.ws.rs.core.MediaType;
import javax.xml.datatype.Duration;

import com.microsoft.windowsazure.services.serviceBus.implementation.AuthorizationRules;
import com.microsoft.windowsazure.services.serviceBus.implementation.Content;
import com.microsoft.windowsazure.services.serviceBus.implementation.EntityAvailabilityStatus;
import com.microsoft.windowsazure.services.serviceBus.implementation.EntityStatus;
import com.microsoft.windowsazure.services.serviceBus.implementation.Entry;
import com.microsoft.windowsazure.services.serviceBus.implementation.EntryModel;
import com.microsoft.windowsazure.services.serviceBus.implementation.MessageCountDetails;
import com.microsoft.windowsazure.services.serviceBus.implementation.PartitioningPolicy;
import com.microsoft.windowsazure.services.serviceBus.implementation.QueueDescription;

/**
 * Represents a queue.
 */
public class QueueInfo extends EntryModel<QueueDescription> {

    /**
     * Creates an instance of the <code>Queue</code> class.
     */
    public QueueInfo() {
        super(new Entry(), new QueueDescription());
        getEntry().setContent(new Content());
        getEntry().getContent().setType(MediaType.APPLICATION_XML);
        getEntry().getContent().setQueueDescription(getModel());
    }

    /**
     * Creates an instance of the <code>Queue</code> class using the specified entry.
     * 
     * @param entry
     *            An <code>Entry</code> object.
     */
    public QueueInfo(Entry entry) {
        super(entry, entry.getContent().getQueueDescription());
    }

    /**
     * Creates an instance of the <code>Queue</code> class using the specified name.
     * 
     * @param path
     *            A <code>String</code> object that represents the name of the queue.
     */
    public QueueInfo(String path) {
        this();
        setPath(path);
    }

    /**
     * Returns the name of the queue.
     * 
     * @return A <code>String</code> object that represents the name of the queue.
     */
    public String getPath() {
        return getEntry().getTitle();
    }

    /**
     * Sets the name of the queue.
     * 
     * @param value
     *            A <code>String</code> that represents the name of the queue.
     * 
     * @return A <code>QueueInfo</code> object that represents the updated queue.
     */
    public QueueInfo setPath(String value) {
        getEntry().setTitle(value);
        return this;
    }

    /**
     * Returns the duration of the lock.
     * 
     * @return A <code>Duration</code> object that represents the duration of the lock.
     */
    public Duration getLockDuration() {
        return getModel().getLockDuration();
    }

    /**
     * Sets the duration of the lock.
     * 
     * @param value
     *            The duration, in seconds, of the lock.
     * 
     * @return A <code>QueueInfo</code> object that represents the updated queue.
     */
    public QueueInfo setLockDuration(Duration value) {
        getModel().setLockDuration(value);
        return this;
    }

    /**
     * Returns the maximum size of the queue.
     * 
     * @return The maximum size, in megabytes, of the queue.
     */
    public Long getMaxSizeInMegabytes() {
        return getModel().getMaxSizeInMegabytes();
    }

    /**
     * Sets the maximum size of the queue.
     * 
     * @param value
     *            The maximum size, in megabytes, of the queue.
     * 
     * @return A <code>QueueInfo</code> object that represents the updated queue.
     */
    public QueueInfo setMaxSizeInMegabytes(Long value) {
        getModel().setMaxSizeInMegabytes(value);
        return this;
    }

    /**
     * Indicates whether duplicate message detection is required.
     * 
     * @return <code>true</code> if duplicate message detection is required; otherwise, <code>false</code>.
     */
    public Boolean isRequiresDuplicateDetection() {
        return getModel().isRequiresDuplicateDetection();
    }

    /**
     * Specifies whether duplicate message detection is required.
     * 
     * @param value
     *            <code>true</code> if duplicate message detection is required; otherwise, <code>false</code>.
     * 
     * @return A <code>QueueInfo</code> object that represents the updated queue.
     */
    public QueueInfo setRequiresDuplicateDetection(Boolean value) {
        getModel().setRequiresDuplicateDetection(value);
        return this;
    }

    /**
     * Indicates whether the queue is session-aware.
     * 
     * @return <code>true</code> if the queue is session aware; otherwise, <code>false</code>.
     */
    public Boolean isRequiresSession() {
        return getModel().isRequiresSession();
    }

    /**
     * Specifies whether the queue is session-aware.
     * 
     * @param value
     *            <code>true</code> if the queue is session aware; otherwise, <code>false</code>.
     * 
     * @return A <code>QueueInfo</code> object that represents the updated queue.
     */
    public QueueInfo setRequiresSession(Boolean value) {
        getModel().setRequiresSession(value);
        return this;
    }

    /**
     * Returns the default message time-to-live (TTL). This applies when dead
     * lettering is in effect.
     * 
     * @return A <code>Duration</code> object that represents the default message TTL.
     */
    public Duration getDefaultMessageTimeToLive() {
        return getModel().getDefaultMessageTimeToLive();
    }

    /**
     * Sets the default message time-to-live (TTL). This applies when dead lettering is in effect.
     * 
     * @param value
     *            A <code>Duration</code> object that represents the default message TTL.
     * 
     * @return A <code>QueueInfo</code> object that represents the updated queue.
     */
    public QueueInfo setDefaultMessageTimeToLive(Duration value) {
        getModel().setDefaultMessageTimeToLive(value);
        return this;
    }

    /**
     * Gets the time span before auto deletion starts.
     * 
     * @return A <code>Duration</code> object that represents the time span before auto deletion.
     */
    public Duration getAutoDeleteOnIdle() {
        return getModel().getAutoDeleteOnIdle();
    }

    /**
     * Sets the time span before auto deletion starts.
     * 
     * @param autoDeleteOnIdle
     *            A <code>Duration</code> object that represents the time span before auto deletion starts.
     * @return A <code>QueueInfo</code> object that represents the updated queue.
     */
    public QueueInfo setAutoDeleteOnIdle(Duration autoDeleteOnIdle) {
        getModel().setAutoDeleteOnIdle(autoDeleteOnIdle);
        return this;
    }

    /**
     * Indicates whether dead lettering is in effect upon message expiration.
     * 
     * @return <code>true</code> if dead lettering is in effect; otherwise, <code>false</code>.
     */
    public Boolean isDeadLetteringOnMessageExpiration() {
        return getModel().isDeadLetteringOnMessageExpiration();
    }

    /**
     * Specifies whether dead lettering is in effect upon message expiration.
     * 
     * @param value
     *            <code>true</code> if dead lettering is in effect; otherwise, <code>false</code>.
     * 
     * @return A <code>Queue</code> object that represents the updated queue.
     */
    public QueueInfo setDeadLetteringOnMessageExpiration(Boolean value) {
        getModel().setDeadLetteringOnMessageExpiration(value);
        return this;
    }

    /**
     * Returns the time span during which the service bus will detect message duplication. This applies when duplicate
     * message detection is in effect.
     * 
     * @return A <code>Duration</code> object that represents the time span for
     *         detecting message duplication.
     */
    public Duration getDuplicateDetectionHistoryTimeWindow() {
        return getModel().getDuplicateDetectionHistoryTimeWindow();
    }

    /**
     * Sets the time span during which the service bus will detect message duplication. This applies when duplicate
     * message detection is in effect.
     * 
     * @param value
     *            A <code>Duration</code> object that represents the time span for detecting message duplication.
     * 
     * @return A <code>Queue</code> object that represents the updated queue.
     */
    public QueueInfo setDuplicateDetectionHistoryTimeWindow(Duration value) {
        getModel().setDuplicateDetectionHistoryTimeWindow(value);
        return this;
    }

    /**
     * Returns the maximum delivery count for the queue.
     * 
     * @return The maximum delivery count.
     */
    public Integer getMaxDeliveryCount() {
        return getModel().getMaxDeliveryCount();
    }

    /**
     * Sets the maximum delivery count for the queue.
     * 
     * @param value
     *            The maximum delivery count for the queue.
     * 
     * @return A <code>Queue</code> object that represents the updated queue.
     */
    public QueueInfo setMaxDeliveryCount(Integer value) {
        getModel().setMaxDeliveryCount(value);
        return this;
    }

    /**
     * Indicates whether batch operations are enabled.
     * 
     * @return <code>true</code> if batch operations are enabled; otherwise, <code>false</code>.
     */
    public Boolean isEnableBatchedOperations() {
        return getModel().isEnableBatchedOperations();
    }

    /**
     * Specifies whether batch operations are enabled.
     * 
     * @param value
     *            <code>true</code> if batch operations are enabled; otherwise, <code>false</code>.
     * 
     * @return A <code>Queue</code> object that represents the updated queue.
     */
    public QueueInfo setEnableBatchedOperations(Boolean value) {
        getModel().setEnableBatchedOperations(value);
        return this;
    }

    /**
     * Returns the size of the queue.
     * 
     * @return The size, in bytes, of the queue.
     */
    public Long getSizeInBytes() {
        return getModel().getSizeInBytes();
    }

    /**
     * Sets the size in bytes.
     * 
     * @param sizeInBytes
     *            the size in bytes
     * @return the queue info
     */
    public QueueInfo setSizeInBytes(Long sizeInBytes) {
        getModel().setSizeInBytes(sizeInBytes);
        return this;
    }

    /**
     * Returns the number of messages in the queue.
     * 
     * @return The number of messages in the queue.
     */
    public Long getMessageCount() {
        return getModel().getMessageCount();
    }

    /**
     * Sets the message count.
     * 
     * @param messageCount
     *            the message count
     * @return the queue info
     */
    public QueueInfo setMessageCount(Long messageCount) {
        getModel().setMessageCount(messageCount);
        return this;
    }

    /**
     * Sets the count details.
     * 
     * @param countDetails
     *            the count details
     * @return the queue info
     */
    public QueueInfo setCountDetails(MessageCountDetails countDetails) {
        getModel().setCountDetails(countDetails);
        return this;
    }

    /**
     * Gets the count details.
     * 
     * @return the count details
     */
    public MessageCountDetails getCountDetails() {
        return getModel().getCountDetails();
    }

    /**
     * Gets the authorization.
     * 
     * @return the authorization
     */
    public AuthorizationRules getAuthorization() {
        return getModel().getAuthorizationRules();
    }

    /**
     * Sets the authorization.
     * 
     * @param authorizationRules
     *            the authorization rules
     * @return the queue info
     */
    public QueueInfo setAuthorization(AuthorizationRules authorizationRules) {
        getModel().setAuthorizationRules(authorizationRules);
        return this;
    }

    /**
     * Checks if is anonymous accessible.
     * 
     * @return the boolean
     */
    public Boolean isAnonymousAccessible() {
        return getModel().isIsAnonymousAccessible();
    }

    /**
     * Sets the is anonymous accessible.
     * 
     * @param isAnonymousAccessible
     *            the is anonymous accessible
     * @return the queue info
     */
    public QueueInfo setIsAnonymousAccessible(Boolean isAnonymousAccessible) {
        getModel().setIsAnonymousAccessible(isAnonymousAccessible);
        return this;
    }

    /**
     * Checks if is support ordering.
     * 
     * @return the boolean
     */
    public Boolean isSupportOrdering() {
        return getModel().isSupportOrdering();
    }

    /**
     * Sets the support ordering.
     * 
     * @param supportOrdering
     *            the support ordering
     * @return the queue info
     */
    public QueueInfo setSupportOrdering(Boolean supportOrdering) {
        getModel().setSupportOrdering(supportOrdering);
        return this;
    }

    /**
     * Gets the status.
     * 
     * @return the status
     */
    public EntityStatus getStatus() {
        return getModel().getStatus();
    }

    /**
     * Sets the status.
     * 
     * @param entityStatus
     *            the entity status
     * @return the queue info
     */
    public QueueInfo setStatus(EntityStatus entityStatus) {
        getModel().setStatus(entityStatus);
        return this;
    }

    /**
     * Gets the entity availability status.
     * 
     * @return the entity availability status
     */
    public EntityAvailabilityStatus getEntityAvailabilityStatus() {
        return getModel().getEntityAvailabilityStatus();
    }

    /**
     * Sets the entity availability status.
     * 
     * @param entityAvailabilityStatus
     *            the entity availability status
     * @return the queue info
     */
    public QueueInfo setEntityAvailabilityStatus(EntityAvailabilityStatus entityAvailabilityStatus) {
        getModel().setEntityAvailabilityStatus(entityAvailabilityStatus);
        return this;
    }

    /**
     * Gets the forward to.
     * 
     * @return the forward to
     */
    public String getForwardTo() {
        return getModel().getForwardTo();
    }

    /**
     * Sets the forward to.
     * 
     * @param forwardTo
     *            the forward to
     * @return the queue info
     */
    public QueueInfo setForwardTo(String forwardTo) {
        getModel().setForwardTo(forwardTo);
        return this;
    }

    /**
     * Gets the created at.
     * 
     * @return the created at
     */
    public Calendar getCreatedAt() {
        return getModel().getCreatedAt();
    }

    /**
     * Sets the created at.
     * 
     * @param createdAt
     *            the created at
     * @return the queue info
     */
    public QueueInfo setCreatedAt(Calendar createdAt) {
        getModel().setCreatedAt(createdAt);
        return this;
    }

    /**
     * Gets the updated at.
     * 
     * @return the updated at
     */
    public Calendar getUpdatedAt() {
        return getModel().getUpdatedAt();
    }

    /**
     * Sets the updated at.
     * 
     * @param updatedAt
     *            the updated at
     * @return the queue info
     */
    public QueueInfo setUpdatedAt(Calendar updatedAt) {
        getModel().setUpdatedAt(updatedAt);
        return this;
    }

    /**
     * Gets the accessed at.
     * 
     * @return the accessed at
     */
    public Calendar getAccessedAt() {
        return getModel().getAccessedAt();
    }

    /**
     * Sets the accessed at.
     * 
     * @param accessedAt
     *            the accessed at
     * @return the queue info
     */
    public QueueInfo setAccessedAt(Calendar accessedAt) {
        getModel().setAccessedAt(accessedAt);
        return this;
    }

    /**
     * Gets the partitioning policy.
     * 
     * @return the partitioning policy
     */
    public PartitioningPolicy getPartitioningPolicy() {
        return getModel().getPartitioningPolicy();
    }

    /**
     * Sets the partitioning policy.
     * 
     * @param partitioningPolicy
     *            the partitioning policy
     * @return the queue info
     */
    public QueueInfo setPartitioningPolicy(PartitioningPolicy partitioningPolicy) {
        getModel().setPartitioningPolicy(partitioningPolicy);
        return this;
    }

    /**
     * Sets the user metadata.
     * 
     * @return the string
     */
    public String setUserMetadata() {
        return getModel().getUserMetadata();
    }

    /**
     * Sets the user metadata.
     * 
     * @param userMetadata
     *            the user metadata
     * @return the queue info
     */
    public QueueInfo setUserMetadata(String userMetadata) {
        getModel().setUserMetadata(userMetadata);
        return this;
    }
}

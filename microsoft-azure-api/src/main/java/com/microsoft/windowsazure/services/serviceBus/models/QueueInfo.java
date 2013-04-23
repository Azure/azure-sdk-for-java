/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.microsoft.windowsazure.services.serviceBus.models;

import java.util.Date;

import javax.ws.rs.core.MediaType;
import javax.xml.datatype.Duration;

import com.microsoft.windowsazure.services.serviceBus.implementation.AuthorizationRule;
import com.microsoft.windowsazure.services.serviceBus.implementation.AuthorizationRules;
import com.microsoft.windowsazure.services.serviceBus.implementation.Content;
import com.microsoft.windowsazure.services.serviceBus.implementation.Entry;
import com.microsoft.windowsazure.services.serviceBus.implementation.EntryModel;
import com.microsoft.windowsazure.services.serviceBus.implementation.MessageCountDetails;
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
     * @return A <code>Queue</code> object that represents the updated queue.
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
     * @return A <code>Queue</code> object that represents the updated queue.
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
     * @return A <code>Queue</code> object that represents the updated queue.
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
     * @return A <code>Queue</code> object that represents the updated queue.
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
     * @return A <code>Queue</code> object that represents the updated queue.
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
     * @return A <code>Queue</code> object that represents the updated queue.
     */
    public QueueInfo setDefaultMessageTimeToLive(Duration value) {
        getModel().setDefaultMessageTimeToLive(value);
        return this;
    }

    public Duration getAutoDeleteOnIdle() 
    {
        return getModel().getAutoDeleteOnIdle();
    }
    
    public QueueInfo setAutoDeleteOnIdle(Duration duration) {
        getModel().setAutoDeleteOnIdle(duration);
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
     * Returns the number of messages in the queue.
     * 
     * @return The number of messages in the queue.
     */
    public Long getMessageCount() {
        return getModel().getMessageCount();
    }
    
    public MessageCountDetails getMessageCountDetails()
    {
        return getModel().getMessageCountDetails();
    }

    public AuthorizationRules getAuthorization()
    {
        return getModel().getAuthorization();
    }
    
    public QueueInfo setAuthorization(AuthorizationRules authorizationRules)
    {
        getModel().setAuthorization(authorizationRules);
        return this; 
    }
    
    public Boolean getIsAnonymousAccessible()
    {
        return getModel().getIsAnonymousAccessible();
    }
    
    public QueueInfo setIsAnonymouseAccessible(Boolean isAnonymousAccessible)
    {
        getModel().setIsAnonymousAccessible(isAnonymousAccessible);
        return this;
    }
    
    public Boolean getSupportOrdering()
    {
        return getModel().getSupportOrdering();
    }
    
    public QueueInfo setSupportOrdering(Boolean supportOrdering) 
    {
        getModel().setSupportOrdering(supportOrdering);
        return this; 
    }
    
    public EntityStatus getEntityStatus()
    {
        return getModel().getEntityStatus();
    }
    
    public QueueInfo setEntityStatus(EntityStatus entityStatus)
    {
        getModel().setEntityStatus(entityStatus);
        return this; 
    }
    
    public EntityAvailabilityStatus getEntityAvailabilityStatus() 
    {
        return getModel().getEntityAvailabilityStatus();
    }
    
    public QueueInfo setEntityAvailabilityStatus(EntityAvailabilityStatus entityAvailabilityStatus)
    {
        getModel().setEntityAvailabilityStatus(entityAvailabilityStatus);
        return this; 
    }
    
    public String getForwardTo()
    {
        return getModel().getForwardTo();
    }
    
    public QueueInfo setForwardTo(String forwardTo)
    {
        getModel().setForwardTo(forwardTo);
        return this;
    }
    
    public Date getCreatedAt()
    {
        return getModel().getCreatedAt();
    }
    
    public QueueInfo setCreatedAt(Date createdAt)
    {
        getModel().setCreatedAt(createdAt);
        return this;
    }
    
    public Date getUpdatedAt() 
    {
        return getModel().getUpdatedAt();
    }
    
    public QueueInfo setUpdatedAt(Date updatedAt)
    {
        getModel().setUpdatedAt(updatedAt);
        return this;
    }
    
    public Date getAccessedAt() 
    {
        return getModel().getAccessedAt();
    }
    
    public QueueInfo setAccessedAt(Date accessedAt) 
    {
        getModel().setAccessedAt(accessedAt);
        return this;
    }
    
    public Date PartitioningPolicy getPartitioningPolicy()
    {
        return getModel().getPartitioningPolicy();
    }
    
    public QueueInfo setPartitioningPolicy(PartitioningPolicy partitioningPolicy)
    {
        getModel().setPartitioningPolicy(partitioningPolicy);
        return this;
    }
    
    public String setUserMetadata()
    {
        return getModel().getUserMetadata();
    }
    
    public QueueInfo setUserMetadata(String userMetadata)
    {
        getModel().setUserMetadata(userMetadata);
        return this; 
    }
}

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

import javax.ws.rs.core.MediaType;
import javax.xml.datatype.Duration;

import com.microsoft.windowsazure.services.serviceBus.implementation.Content;
import com.microsoft.windowsazure.services.serviceBus.implementation.Entry;
import com.microsoft.windowsazure.services.serviceBus.implementation.EntryModel;
import com.microsoft.windowsazure.services.serviceBus.implementation.TopicDescription;

/**
 * Represents a topic.
 */
public class TopicInfo extends EntryModel<TopicDescription> {
    /**
     * Creates an instance of the <code>Topic</code> class.
     */
    public TopicInfo() {
        super(new Entry(), new TopicDescription());
        getEntry().setContent(new Content());
        getEntry().getContent().setType(MediaType.APPLICATION_XML);
        getEntry().getContent().setTopicDescription(getModel());
    }

    /**
     * Creates an instance of the <code>Topic</code> class using the specified entry.
     * 
     * @param entry
     *            An <code>Entry</code> object that represents the entry for the topic.
     */
    public TopicInfo(Entry entry) {
        super(entry, entry.getContent().getTopicDescription());
    }

    /**
     * Creates an instance of the <code>Topic</code> class using the specified name.
     * 
     * @param path
     *            A <code>String</code> object that represents the name for the topic.
     */
    public TopicInfo(String path) {
        this();
        setPath(path);
    }

    /**
     * Returns the name of the topic.
     * 
     * @return A <code>String</code> object that represents the name of the topic.
     */
    public String getPath() {
        return getEntry().getTitle();
    }

    /**
     * Sets the name of the topic.
     * 
     * @param value
     *            A <code>String</code> that represents the name of the topic.
     * 
     * @return A <code>Topic</code> object that represents the updated topic.
     */
    public TopicInfo setPath(String value) {
        getEntry().setTitle(value);
        return this;
    }

    /**
     * Returns the default message time-to-live (TTL).
     * 
     * @return A <code>Duration</code> object that represents the default message TTL.
     */
    public Duration getDefaultMessageTimeToLive() {
        return getModel().getDefaultMessageTimeToLive();
    }

    /**
     * Sets the default message time-to-live (TTL).
     * 
     * @param value
     *            A <code>Duration</code> object that represents the default message TTL.
     * 
     * @return A <code>Topic</code> object that represents the updated topic.
     */
    public TopicInfo setDefaultMessageTimeToLive(Duration value) {
        getModel().setDefaultMessageTimeToLive(value);
        return this;
    }

    /**
     * Returns the maximum size of the topic.
     * 
     * @return The maximum size, in megabytes, of the topic.
     */
    public Long getMaxSizeInMegabytes() {
        return getModel().getMaxSizeInMegabytes();
    }

    /**
     * Sets the maximum size of the topic.
     * 
     * @param value
     *            The maximum size, in megabytes, of the topic.
     * 
     * @return A <code>Topic</code> object that represents the updated topic.
     */
    public TopicInfo setMaxSizeInMegabytes(Long value) {
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
     * @return A <code>Topic</code> object that represents the updated topic.
     */
    public TopicInfo setRequiresDuplicateDetection(Boolean value) {
        getModel().setRequiresDuplicateDetection(value);
        return this;
    }

    /**
     * Returns the time span during which the service bus will detect message duplication.
     * 
     * @return A <code>Duration</code> object that represents the time span for
     *         detecting message duplication.
     */
    public Duration getDuplicateDetectionHistoryTimeWindow() {
        return getModel().getDuplicateDetectionHistoryTimeWindow();
    }

    /**
     * Sets the time span during which the service bus will detect message duplication.
     * 
     * @param value
     *            A <code>Duration</code> object that represents the time span for detecting message duplication.
     * 
     * @return A <code>Topic</code> object that represents the updated topic.
     */
    public TopicInfo setDuplicateDetectionHistoryTimeWindow(Duration value) {
        getModel().setDuplicateDetectionHistoryTimeWindow(value);
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
     * @return A <code>Topic</code> object that represents the updated topic.
     */
    public TopicInfo setEnableBatchedOperations(Boolean value) {
        getModel().setEnableBatchedOperations(value);
        return this;
    }

    /**
     * Returns the size of the topic.
     * 
     * @return The size, in bytes, of the topic.
     */
    public Long getSizeInBytes() {
        return getModel().getSizeInBytes();
    }

}

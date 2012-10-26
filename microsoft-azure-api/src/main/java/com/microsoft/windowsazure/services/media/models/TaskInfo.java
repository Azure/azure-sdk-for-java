/**
 * Copyright 2012 Microsoft Corporation
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

package com.microsoft.windowsazure.services.media.models;

import java.util.Date;

import com.microsoft.windowsazure.services.media.implementation.ODataEntity;
import com.microsoft.windowsazure.services.media.implementation.atom.EntryType;
import com.microsoft.windowsazure.services.media.implementation.content.TaskType;

/**
 * The Class TaskInfo.
 */
public class TaskInfo extends ODataEntity<TaskType> {

    /**
     * Instantiates a new task info.
     * 
     * @param entry
     *            the entry
     * @param content
     *            the content
     */
    public TaskInfo(EntryType entry, TaskType content) {
        super(entry, content);
    }

    /**
     * Instantiates a new task info.
     */
    public TaskInfo() {
        super(new TaskType());
    }

    /**
     * Gets the id.
     * 
     * @return the id
     */
    public String getId() {
        return getContent().getId();
    }

    /**
     * Sets the id.
     * 
     * @param id
     *            the id
     * @return the task info
     */
    public TaskInfo setId(String id) {
        getContent().setId(id);
        return this;
    }

    /**
     * Gets the configuration.
     * 
     * @return the configuration
     */
    public String getConfiguration() {
        return getContent().getConfiguration();
    }

    /**
     * Sets the configuration.
     * 
     * @param configuration
     *            the configuration
     * @return the task info
     */
    public TaskInfo setConfiguration(String configuration) {
        getContent().setConfiguration(configuration);
        return this;
    }

    /**
     * Gets the end time.
     * 
     * @return the end time
     */
    public Date getEndTime() {
        return getContent().getEndTime();
    }

    /**
     * Sets the end time.
     * 
     * @param endTime
     *            the end time
     * @return the task info
     */
    public TaskInfo setEndTime(Date endTime) {
        getContent().setEndTime(endTime);
        return this;
    }

    /**
     * Gets the error details.
     * 
     * @return the error details
     */
    public String getErrorDetails() {
        return getContent().getErrorDetails();
    }

    /**
     * Sets the error details.
     * 
     * @param errorDetails
     *            the error details
     * @return the task info
     */
    public TaskInfo setErrorDetails(String errorDetails) {
        getContent().setErrorDetails(errorDetails);
        return this;
    }

    /**
     * Gets the media processor id.
     * 
     * @return the media processor id
     */
    public String getMediaProcessorId() {
        return getContent().getMediaProcessorId();
    }

    /**
     * Sets the media processor id.
     * 
     * @param mediaProcessorId
     *            the media processor id
     * @return the task info
     */
    public TaskInfo setMediaProcessorId(String mediaProcessorId) {
        getContent().setMediaProcessorId(mediaProcessorId);
        return this;
    }

    /**
     * Gets the name.
     * 
     * @return the name
     */
    public String getName() {
        return getContent().getName();
    }

    /**
     * Sets the name.
     * 
     * @param name
     *            the name
     * @return the task info
     */
    public TaskInfo setName(String name) {
        getContent().setName(name);
        return this;
    }

    /**
     * Gets the perf message.
     * 
     * @return the perf message
     */
    public String getPerfMessage() {
        return getContent().getPerfMessage();
    }

    /**
     * Sets the perf message.
     * 
     * @param perfMessage
     *            the perf message
     * @return the task info
     */
    public TaskInfo setPerfMessage(String perfMessage) {
        getContent().setPerfMessage(perfMessage);
        return this;
    }

    /**
     * Gets the priority.
     * 
     * @return the priority
     */
    public Integer getPriority() {
        return getContent().getPriority();
    }

    /**
     * Sets the priority.
     * 
     * @param priority
     *            the priority
     * @return the task info
     */
    public TaskInfo setPriority(Integer priority) {
        getContent().setPriority(priority);
        return this;
    }

    /**
     * Gets the progress.
     * 
     * @return the progress
     */
    public Double getProgress() {
        return getContent().getProgress();
    }

    /**
     * 
     * @param progress
     *            the progress
     * @return the task info
     */
    public TaskInfo setProgress(Double progress) {
        getContent().setProgress(progress);
        return this;
    }

    /**
     * Gets the running duration.
     * 
     * @return the running duration
     */
    public double getRunningDuration() {
        return getContent().getRunningDuration();
    }

    /**
     * Sets the running duration.
     * 
     * @param runningDuration
     *            the running duration
     * @return the task info
     */
    public TaskInfo setRunningDuration(double runningDuration) {
        getContent().setRunningDuration(runningDuration);
        return this;
    }

    /**
     * Gets the start time.
     * 
     * @return the start time
     */
    public Date getStartTime() {
        return getContent().getStartTime();
    }

    /**
     * Sets the start time.
     * 
     * @param startTime
     *            the start time
     * @return the task info
     */
    public TaskInfo setStartTime(Date startTime) {
        getContent().setStartTime(startTime);
        return this;
    }

    /**
     * Gets the state.
     * 
     * @return the state
     */
    public Integer getState() {
        return getContent().getState();
    }

    /**
     * Sets the state.
     * 
     * @param state
     *            the state
     * @return the task info
     */
    public TaskInfo setState(Integer state) {
        getContent().setState(state);
        return this;
    }

    /**
     * Gets the task body.
     * 
     * @return the task body
     */
    public String getTaskBody() {
        return getContent().getTaskBody();
    }

    /**
     * Sets the task body.
     * 
     * @param taskBody
     *            the task body
     * @return the task info
     */
    public TaskInfo setTaskBody(String taskBody) {
        getContent().setTaskBody(taskBody);
        return this;
    }

    /**
     * Gets the options.
     * 
     * @return the options
     */
    public Integer getOptions() {
        return getContent().getOptions();
    }

    /**
     * Sets the options.
     * 
     * @param options
     *            the options
     * @return the task info
     */
    public TaskInfo setOptions(Integer options) {
        getContent().setOptions(options);
        return this;
    }

    /**
     * Gets the encryption key id.
     * 
     * @return the encryption key id
     */
    public String getEncryptionKeyId() {
        return getContent().getEncryptionKeyId();
    }

    /**
     * Sets the encryption key id.
     * 
     * @param encryptionKeyId
     *            the encryption key id
     * @return the task info
     */
    public TaskInfo setEncryptionKeyId(String encryptionKeyId) {
        getContent().setEncryptionKeyId(encryptionKeyId);
        return this;
    }

    /**
     * Gets the encryption scheme.
     * 
     * @return the encryption scheme
     */
    public String getEncryptionScheme() {
        return getContent().getEncryptionScheme();
    }

    /**
     * Sets the encryption scheme.
     * 
     * @param encryptionScheme
     *            the encryption scheme
     * @return the task info
     */
    public TaskInfo setEncryptionScheme(String encryptionScheme) {
        getContent().setEncryptionScheme(encryptionScheme);
        return this;
    }

    /**
     * Gets the encryption version.
     * 
     * @return the encryption version
     */
    public String getEncryptionVersion() {
        return getContent().getEncryptionVersion();
    }

    /**
     * Sets the encryption version.
     * 
     * @param encryptionVersion
     *            the encryption version
     * @return the task info
     */
    public TaskInfo setEncryptionVersion(String encryptionVersion) {
        getContent().setEncryptionVersion(encryptionVersion);
        return this;
    }

    /**
     * Gets the initialization vector.
     * 
     * @return the initialization vector
     */
    public String getInitializationVector() {
        return getContent().getInitializationVector();
    }

    /**
     * Sets the initialization vector.
     * 
     * @param initializationVector
     *            the initialization vector
     * @return the task info
     */
    public TaskInfo setInitializationVector(String initializationVector) {
        getContent().setInitializationVector(initializationVector);
        return this;
    }

}

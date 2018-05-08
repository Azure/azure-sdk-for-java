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

package com.microsoft.windowsazure.services.media.models;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.microsoft.windowsazure.services.media.implementation.ODataEntity;
import com.microsoft.windowsazure.services.media.implementation.atom.EntryType;
import com.microsoft.windowsazure.services.media.implementation.content.ErrorDetailType;
import com.microsoft.windowsazure.services.media.implementation.content.TaskHistoricalEventType;
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
     * Gets the id.
     * 
     * @return the id
     */
    public String getId() {
        return getContent().getId();
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
     * Gets the end time.
     * 
     * @return the end time
     */
    public Date getEndTime() {
        return getContent().getEndTime();
    }

    /**
     * Gets the error details.
     * 
     * @return the error details
     */
    public List<ErrorDetail> getErrorDetails() {
        List<ErrorDetail> result = new ArrayList<ErrorDetail>();
        List<ErrorDetailType> errorDetailTypes = getContent().getErrorDetails();
        if (errorDetailTypes != null) {
            for (ErrorDetailType errorDetailType : errorDetailTypes) {
                ErrorDetail errorDetail = new ErrorDetail(
                        errorDetailType.getCode(), errorDetailType.getMessage());
                result.add(errorDetail);
            }
            return result;
        }
        return null;
    }

    /**
     * Gets the task historical events.
     * 
     * @return the task historical events
     */
    public List<TaskHistoricalEvent> getHistoricalEvents() {
        List<TaskHistoricalEvent> result = new ArrayList<TaskHistoricalEvent>();
        List<TaskHistoricalEventType> historicalEventTypes = getContent()
                .getHistoricalEventTypes();

        if (historicalEventTypes != null) {
            for (TaskHistoricalEventType taskHistoricalEventType : historicalEventTypes) {
                String message = taskHistoricalEventType.getMessage();
                if ((message != null) && (message.isEmpty())) {
                    message = null;
                }
                TaskHistoricalEvent taskHistoricalEvent = new TaskHistoricalEvent(
                        taskHistoricalEventType.getCode(), message,
                        taskHistoricalEventType.getTimeStamp());
                result.add(taskHistoricalEvent);
            }
        }

        return result;
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
     * Gets the name.
     * 
     * @return the name
     */
    public String getName() {
        return getContent().getName();
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
     * Gets the priority.
     * 
     * @return the priority
     */
    public int getPriority() {
        return getContent().getPriority();
    }

    /**
     * Gets the progress.
     * 
     * @return the progress
     */
    public double getProgress() {
        return getContent().getProgress();
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
     * Gets the start time.
     * 
     * @return the start time
     */
    public Date getStartTime() {
        return getContent().getStartTime();
    }

    /**
     * Gets the state.
     * 
     * @return the state
     */
    public TaskState getState() {
        return TaskState.fromCode(getContent().getState());
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
     * Gets the options.
     * 
     * @return the options
     */
    public TaskOption getOptions() {
        return TaskOption.fromCode(getContent().getOptions());
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
     * Gets the encryption scheme.
     * 
     * @return the encryption scheme
     */
    public String getEncryptionScheme() {
        return getContent().getEncryptionScheme();
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
     * Gets the initialization vector.
     * 
     * @return the initialization vector
     */
    public String getInitializationVector() {
        return getContent().getInitializationVector();
    }

    /**
     * Gets link to the task's input assets.
     * 
     * @return the link
     */
    public LinkInfo<AssetInfo> getInputAssetsLink() {
        return this.<AssetInfo> getRelationLink("InputMediaAssets");
    }

    /**
     * Gets link to the task's output assets.
     * 
     * @return the link
     */
    public LinkInfo<AssetInfo> getOutputAssetsLink() {
        return this.<AssetInfo> getRelationLink("OutputMediaAssets");
    }
}

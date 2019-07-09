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

package com.microsoft.windowsazure.services.media.implementation.content;

import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

/**
 * This type maps the XML returned in the odata ATOM serialization for Task
 * entities.
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class TaskType implements MediaServiceDTO {

    @XmlElement(name = "Id", namespace = Constants.ODATA_DATA_NS)
    private String id;

    @XmlElement(name = "Configuration", namespace = Constants.ODATA_DATA_NS)
    private String configuration;

    @XmlElement(name = "EndTime", namespace = Constants.ODATA_DATA_NS)
    private Date endTime;

    @XmlElementWrapper(name = "ErrorDetails", namespace = Constants.ODATA_DATA_NS)
    @XmlElement(name = "element", namespace = Constants.ODATA_DATA_NS)
    private List<ErrorDetailType> errorDetails;

    @XmlElementWrapper(name = "HistoricalEvents", namespace = Constants.ODATA_DATA_NS)
    @XmlElement(name = "element", namespace = Constants.ODATA_DATA_NS)
    private List<TaskHistoricalEventType> historicalEventTypes;

    @XmlElement(name = "MediaProcessorId", namespace = Constants.ODATA_DATA_NS)
    private String mediaProcessorId;

    @XmlElement(name = "Name", namespace = Constants.ODATA_DATA_NS)
    private String name;

    @XmlElement(name = "PerfMessage", namespace = Constants.ODATA_DATA_NS)
    private String perfMessage;

    @XmlElement(name = "Priority", namespace = Constants.ODATA_DATA_NS)
    private Integer priority;

    @XmlElement(name = "Progress", namespace = Constants.ODATA_DATA_NS)
    private Double progress;

    @XmlElement(name = "RunningDuration", namespace = Constants.ODATA_DATA_NS)
    private Double runningDuration;

    @XmlElement(name = "StartTime", namespace = Constants.ODATA_DATA_NS)
    private Date startTime;

    @XmlElement(name = "State", namespace = Constants.ODATA_DATA_NS)
    private Integer state;

    @XmlElement(name = "TaskBody", namespace = Constants.ODATA_DATA_NS)
    private String taskBody;

    @XmlElement(name = "Options", namespace = Constants.ODATA_DATA_NS)
    private Integer options;

    @XmlElement(name = "EncryptionKeyId", namespace = Constants.ODATA_DATA_NS)
    private String encryptionKeyId;

    @XmlElement(name = "EncryptionScheme", namespace = Constants.ODATA_DATA_NS)
    private String encryptionScheme;

    @XmlElement(name = "EncryptionVersion", namespace = Constants.ODATA_DATA_NS)
    private String encryptionVersion;

    @XmlElement(name = "InitializationVector", namespace = Constants.ODATA_DATA_NS)
    private String initializationVector;

    private List<String> outputMediaAssets;

    private List<String> inputMediaAssets;

    public String getId() {
        return id;
    }

    public TaskType setId(String id) {
        this.id = id;
        return this;
    }

    public String getConfiguration() {
        return configuration;
    }

    public TaskType setConfiguration(String configuration) {
        this.configuration = configuration;
        return this;
    }

    public Date getEndTime() {
        return endTime;
    }

    public TaskType setEndTime(Date endTime) {
        this.endTime = endTime;
        return this;
    }

    public List<ErrorDetailType> getErrorDetails() {
        return errorDetails;
    }

    public TaskType setErrorDetails(List<ErrorDetailType> errorDetails) {
        this.errorDetails = errorDetails;
        return this;
    }

    public String getMediaProcessorId() {
        return mediaProcessorId;
    }

    public TaskType setMediaProcessorId(String mediaProcessorId) {
        this.mediaProcessorId = mediaProcessorId;
        return this;
    }

    public String getName() {
        return name;
    }

    public TaskType setName(String name) {
        this.name = name;
        return this;
    }

    public String getPerfMessage() {
        return perfMessage;
    }

    public TaskType setPerfMessage(String perfMessage) {
        this.perfMessage = perfMessage;
        return this;
    }

    public Integer getPriority() {
        return priority;
    }

    public TaskType setPriority(Integer priority) {
        this.priority = priority;
        return this;
    }

    public Double getProgress() {
        return progress;
    }

    public TaskType setProgress(Double progress) {
        this.progress = progress;
        return this;
    }

    public Double getRunningDuration() {
        return runningDuration;
    }

    public TaskType setRunningDuration(Double runningDuration) {
        this.runningDuration = runningDuration;
        return this;
    }

    public Date getStartTime() {
        return startTime;
    }

    public TaskType setStartTime(Date startTime) {
        this.startTime = startTime;
        return this;
    }

    public Integer getState() {
        return state;
    }

    public TaskType setState(Integer state) {
        this.state = state;
        return this;
    }

    public String getTaskBody() {
        return taskBody;
    }

    public TaskType setTaskBody(String taskBody) {
        this.taskBody = taskBody;
        return this;
    }

    public Integer getOptions() {
        return options;
    }

    public TaskType setOptions(Integer options) {
        this.options = options;
        return this;
    }

    public String getEncryptionKeyId() {
        return encryptionKeyId;
    }

    public TaskType setEncryptionKeyId(String encryptionKeyId) {
        this.encryptionKeyId = encryptionKeyId;
        return this;
    }

    public String getEncryptionScheme() {
        return encryptionScheme;
    }

    public TaskType setEncryptionScheme(String encryptionScheme) {
        this.encryptionScheme = encryptionScheme;
        return this;
    }

    public String getEncryptionVersion() {
        return encryptionVersion;
    }

    public TaskType setEncryptionVersion(String encryptionVersion) {
        this.encryptionVersion = encryptionVersion;
        return this;
    }

    public String getInitializationVector() {
        return initializationVector;
    }

    public TaskType setInitializationVector(String initializationVector) {
        this.initializationVector = initializationVector;
        return this;
    }

    public List<String> getOutputMediaAssets() {
        return outputMediaAssets;
    }

    public TaskType setOutputMediaAssets(List<String> outputMediaAssets) {
        this.outputMediaAssets = outputMediaAssets;
        return this;
    }

    public List<String> getInputMediaAssets() {
        return inputMediaAssets;
    }

    public TaskType setInputMediaAssets(List<String> inputMediaAssets) {
        this.inputMediaAssets = inputMediaAssets;
        return this;
    }

    public List<TaskHistoricalEventType> getHistoricalEventTypes() {
        return historicalEventTypes;
    }

    public TaskType setHistoricalEventTypes(
            List<TaskHistoricalEventType> historicalEventTypes) {
        this.historicalEventTypes = historicalEventTypes;
        return this;
    }
}

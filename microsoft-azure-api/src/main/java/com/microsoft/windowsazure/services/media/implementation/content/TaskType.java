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

package com.microsoft.windowsazure.services.media.implementation.content;

import java.net.URI;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * This type maps the XML returned in the odata ATOM serialization
 * for Task entities.
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class TaskType implements MediaServiceDTO {

    @XmlElement(name = "Id", namespace = Constants.ODATA_DATA_NS)
    protected String id;

    @XmlElement(name = "Configuration", namespace = Constants.ODATA_DATA_NS)
    protected String configuration;

    @XmlElement(name = "EndTime", namespace = Constants.ODATA_DATA_NS)
    protected Date endTime;

    @XmlElement(name = "ErrorDetails", namespace = Constants.ODATA_DATA_NS)
    protected String errorDetails;

    @XmlElement(name = "MediaProcessorId", namespace = Constants.ODATA_DATA_NS)
    protected String mediaProcessorId;

    @XmlElement(name = "Name", namespace = Constants.ODATA_DATA_NS)
    protected String name;

    @XmlElement(name = "PerfMessage", namespace = Constants.ODATA_DATA_NS)
    protected String perfMessage;

    @XmlElement(name = "Priority", namespace = Constants.ODATA_DATA_NS)
    protected Integer priority;

    @XmlElement(name = "Progress", namespace = Constants.ODATA_DATA_NS)
    protected Double progress;

    @XmlElement(name = "RunningDuration", namespace = Constants.ODATA_DATA_NS)
    protected Double runningDuration;

    @XmlElement(name = "StartTime", namespace = Constants.ODATA_DATA_NS)
    protected Date startTime;

    @XmlElement(name = "State", namespace = Constants.ODATA_DATA_NS)
    protected Integer state;

    @XmlElement(name = "TaskBody", namespace = Constants.ODATA_DATA_NS)
    protected String taskBody;

    @XmlElement(name = "Options", namespace = Constants.ODATA_DATA_NS)
    protected int options;

    @XmlElement(name = "EncryptionKeyId", namespace = Constants.ODATA_DATA_NS)
    protected String encryptionKeyId;

    @XmlElement(name = "EncryptionScheme", namespace = Constants.ODATA_DATA_NS)
    protected String encryptionScheme;

    @XmlElement(name = "EncryptionVersion", namespace = Constants.ODATA_DATA_NS)
    protected String encryptionVersion;

    @XmlElement(name = "InitializationVector", namespace = Constants.ODATA_DATA_NS)
    protected String initializationVector;

    @XmlElement(name = "OutputMediaAssets", namespace = Constants.ODATA_DATA_NS)
    protected List<URI> outputMediaAssets;

    @XmlElement(name = "InputMediaAssets", namespace = Constants.ODATA_DATA_NS)
    protected List<URI> inputMediaAssets;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getConfiguration() {
        return configuration;
    }

    public void setConfiguration(String configuration) {
        this.configuration = configuration;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public String getErrorDetails() {
        return errorDetails;
    }

    public void setErrorDetails(String errorDetails) {
        this.errorDetails = errorDetails;
    }

    public String getMediaProcessorId() {
        return mediaProcessorId;
    }

    public void setMediaProcessorId(String mediaProcessorId) {
        this.mediaProcessorId = mediaProcessorId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPerfMessage() {
        return perfMessage;
    }

    public void setPerfMessage(String perfMessage) {
        this.perfMessage = perfMessage;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public Double getProgress() {
        return progress;
    }

    public void setProgress(Double progress) {
        this.progress = progress;
    }

    public Double getRunningDuration() {
        return runningDuration;
    }

    public void setRunningDuration(Double runningDuration) {
        this.runningDuration = runningDuration;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Integer getState() {
        return state;
    }

    public void setState(Integer state) {
        this.state = state;
    }

    public String getTaskBody() {
        return taskBody;
    }

    public void setTaskBody(String taskBody) {
        this.taskBody = taskBody;
    }

    public Integer getOptions() {
        return options;
    }

    public void setOptions(Integer options) {
        this.options = options;
    }

    public String getEncryptionKeyId() {
        return encryptionKeyId;
    }

    public void setEncryptionKeyId(String encryptionKeyId) {
        this.encryptionKeyId = encryptionKeyId;
    }

    public String getEncryptionScheme() {
        return encryptionScheme;
    }

    public void setEncryptionScheme(String encryptionScheme) {
        this.encryptionScheme = encryptionScheme;
    }

    public String getEncryptionVersion() {
        return encryptionVersion;
    }

    public void setEncryptionVersion(String encryptionVersion) {
        this.encryptionVersion = encryptionVersion;
    }

    public String getInitializationVector() {
        return initializationVector;
    }

    public void setInitializationVector(String initializationVector) {
        this.initializationVector = initializationVector;
    }

    public List<URI> getOutputMediaAssets() {
        return outputMediaAssets;
    }

    public void setOutputMediaAssets(List<URI> outputMediaAssets) {
        this.outputMediaAssets = outputMediaAssets;
    }

    public List<URI> getInputMediaAssets() {
        return inputMediaAssets;
    }

    public void setInputMediaAssets(List<URI> inputMediaAssets) {
        this.inputMediaAssets = inputMediaAssets;
    }
}

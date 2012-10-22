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

import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

/**
 * This type maps the XML returned in the odata ATOM serialization
 * for Job entities.
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class JobType implements MediaServiceDTO {

    /** The id. */
    @XmlElement(name = "Id", namespace = Constants.ODATA_DATA_NS)
    protected String id;

    /** The name. */
    @XmlElement(name = "Name", namespace = Constants.ODATA_DATA_NS)
    protected String name;

    /** The created. */
    @XmlElement(name = "Created", namespace = Constants.ODATA_DATA_NS)
    protected Date created;

    /** The last modified. */
    @XmlElement(name = "LastModified", namespace = Constants.ODATA_DATA_NS)
    protected Date lastModified;

    /** The end time. */
    @XmlElement(name = "EndTime", namespace = Constants.ODATA_DATA_NS)
    protected Date endTime;

    /** The priority. */
    @XmlElement(name = "Priority", namespace = Constants.ODATA_DATA_NS)
    protected int priority;

    /** The running duration. */
    @XmlElement(name = "RunningDuration", namespace = Constants.ODATA_DATA_NS)
    protected double runningDuration;

    /** The start time. */
    @XmlElement(name = "StartTime", namespace = Constants.ODATA_DATA_NS)
    protected Date startTime;

    /** The state. */
    @XmlElement(name = "State", namespace = Constants.ODATA_DATA_NS)
    protected int state;

    /** The template id. */
    @XmlElement(name = "TemplateId", namespace = Constants.ODATA_DATA_NS)
    protected String templateId;

    /** The output media assets. */
    @XmlElement(name = "OutputMediaAssets", namespace = Constants.ODATA_DATA_NS)
    protected String outputMediaAssets;

    /** The input media assets. */
    @XmlElement(name = "InputMediaAssets", namespace = Constants.ODATA_DATA_NS)
    protected String inputMediaAssets;

    /** The tasks. */
    @XmlElement(name = "Tasks", namespace = Constants.ODATA_DATA_NS)
    protected String tasks;

    /**
     * Gets the id.
     * 
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the id.
     * 
     * @param id
     *            the new id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the name.
     * 
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name.
     * 
     * @param name
     *            the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the created.
     * 
     * @return the created
     */
    public Date getCreated() {
        return created;
    }

    /**
     * Sets the created.
     * 
     * @param created
     *            the new created
     */
    public void setCreated(Date created) {
        this.created = created;
    }

    /**
     * Gets the last modified.
     * 
     * @return the last modified
     */
    public Date getLastModified() {
        return lastModified;
    }

    /**
     * Sets the last modified.
     * 
     * @param lastModified
     *            the new last modified
     */
    public void setLastModified(Date lastModified) {
        this.lastModified = lastModified;
    }

    /**
     * Gets the end time.
     * 
     * @return the end time
     */
    public Date getEndTime() {
        return endTime;
    }

    /**
     * Sets the end time.
     * 
     * @param endTime
     *            the new end time
     */
    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    /**
     * Gets the priority.
     * 
     * @return the priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * Sets the priority.
     * 
     * @param priority
     *            the new priority
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * Gets the running duration.
     * 
     * @return the running duration
     */
    public double getRunningDuration() {
        return runningDuration;
    }

    /**
     * Sets the running duration.
     * 
     * @param runningDuration
     *            the new running duration
     */
    public void setRunningDuration(double runningDuration) {
        this.runningDuration = runningDuration;
    }

    /**
     * Gets the start time.
     * 
     * @return the start time
     */
    public Date getStartTime() {
        return startTime;
    }

    /**
     * Sets the start time.
     * 
     * @param startTime
     *            the new start time
     */
    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    /**
     * Gets the state.
     * 
     * @return the state
     */
    public int getState() {
        return state;
    }

    /**
     * Sets the state.
     * 
     * @param state
     *            the new state
     */
    public void setState(int state) {
        this.state = state;
    }

    /**
     * Gets the template id.
     * 
     * @return the template id
     */
    public String getTemplateId() {
        return templateId;
    }

    /**
     * Sets the template id.
     * 
     * @param templateId
     *            the new template id
     */
    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    /**
     * Gets the output media assets.
     * 
     * @return the output media assets
     */
    public String getOutputMediaAssets() {
        return outputMediaAssets;
    }

    /**
     * Sets the output media assets.
     * 
     * @param outputMediaAssets
     *            the new output media assets
     */
    public void setOutputMediaAssets(String outputMediaAssets) {
        this.outputMediaAssets = outputMediaAssets;
    }

    /**
     * Gets the input media assets.
     * 
     * @return the input media assets
     */
    public String getInputMediaAssets() {
        return inputMediaAssets;
    }

    /**
     * Sets the input media assets.
     * 
     * @param inputMediaAssets
     *            the new input media assets
     */
    public void setInputMediaAssets(String inputMediaAssets) {
        this.inputMediaAssets = inputMediaAssets;
    }

    /**
     * Gets the tasks.
     * 
     * @return the tasks
     */
    public String getTasks() {
        return tasks;
    }

    /**
     * Sets the tasks.
     * 
     * @param tasks
     *            the new tasks
     */
    public void setTasks(String tasks) {
        this.tasks = tasks;
    }

}

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
    protected Integer priority;

    /** The running duration. */
    @XmlElement(name = "RunningDuration", namespace = Constants.ODATA_DATA_NS)
    protected Double runningDuration;

    /** The start time. */
    @XmlElement(name = "StartTime", namespace = Constants.ODATA_DATA_NS)
    protected Date startTime;

    /** The state. */
    @XmlElement(name = "State", namespace = Constants.ODATA_DATA_NS)
    protected Integer state;

    /** The template id. */
    @XmlElement(name = "TemplateId", namespace = Constants.ODATA_DATA_NS)
    protected String templateId;

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
    public JobType setId(String id) {
        this.id = id;
        return this;
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
    public JobType setName(String name) {
        this.name = name;
        return this;
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
    public JobType setCreated(Date created) {
        this.created = created;
        return this;
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
    public JobType setLastModified(Date lastModified) {
        this.lastModified = lastModified;
        return this;
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
    public JobType setEndTime(Date endTime) {
        this.endTime = endTime;
        return this;
    }

    /**
     * Gets the priority.
     * 
     * @return the priority
     */
    public Integer getPriority() {
        return priority;
    }

    /**
     * Sets the priority.
     * 
     * @param priority
     *            the new priority
     */
    public JobType setPriority(Integer priority) {
        this.priority = priority;
        return this;
    }

    /**
     * Gets the running duration.
     * 
     * @return the running duration
     */
    public Double getRunningDuration() {
        return runningDuration;
    }

    /**
     * Sets the running duration.
     * 
     * @param runningDuration
     *            the new running duration
     */
    public JobType setRunningDuration(Double runningDuration) {
        this.runningDuration = runningDuration;
        return this;
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
    public JobType setStartTime(Date startTime) {
        this.startTime = startTime;
        return this;
    }

    /**
     * Gets the state.
     * 
     * @return the state
     */
    public Integer getState() {
        return state;
    }

    /**
     * Sets the state.
     * 
     * @param state
     *            the new state
     */
    public JobType setState(Integer state) {
        this.state = state;
        return this;
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
    public JobType setTemplateId(String templateId) {
        this.templateId = templateId;
        return this;
    }

}

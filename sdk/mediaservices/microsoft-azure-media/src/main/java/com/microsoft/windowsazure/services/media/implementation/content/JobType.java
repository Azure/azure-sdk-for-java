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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

/**
 * This type maps the XML returned in the odata ATOM serialization for Job
 * entities.
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class JobType implements MediaServiceDTO {

    /** The id. */
    @XmlElement(name = "Id", namespace = Constants.ODATA_DATA_NS)
    private String id;

    /** The job notification subscriptions. */
    @XmlElementWrapper(name = "JobNotificationSubscriptions", namespace = Constants.ODATA_DATA_NS)
    @XmlElement(name = "element", namespace = Constants.ODATA_DATA_NS)
    private List<JobNotificationSubscriptionType> jobNotificationSubscriptionTypes;

    /** The name. */
    @XmlElement(name = "Name", namespace = Constants.ODATA_DATA_NS)
    private String name;

    /** The created. */
    @XmlElement(name = "Created", namespace = Constants.ODATA_DATA_NS)
    private Date created;

    /** The last modified. */
    @XmlElement(name = "LastModified", namespace = Constants.ODATA_DATA_NS)
    private Date lastModified;

    /** The end time. */
    @XmlElement(name = "EndTime", namespace = Constants.ODATA_DATA_NS)
    private Date endTime;

    /** The priority. */
    @XmlElement(name = "Priority", namespace = Constants.ODATA_DATA_NS)
    private Integer priority;

    /** The running duration. */
    @XmlElement(name = "RunningDuration", namespace = Constants.ODATA_DATA_NS)
    private Double runningDuration;

    /** The start time. */
    @XmlElement(name = "StartTime", namespace = Constants.ODATA_DATA_NS)
    private Date startTime;

    /** The state. */
    @XmlElement(name = "State", namespace = Constants.ODATA_DATA_NS)
    private Integer state;

    /** The template id. */
    @XmlElement(name = "TemplateId", namespace = Constants.ODATA_DATA_NS)
    private String templateId;

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
     * @return the job type
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
     * @return the job type
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
     * @return the job type
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
     * @return the job type
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
     * @return the job type
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
     * @return the job type
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
     * @return the job type
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
     * @return the job type
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
     * @return the job type
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
     * @return the job type
     */
    public JobType setTemplateId(String templateId) {
        this.templateId = templateId;
        return this;
    }

    /**
     * Gets the job notification subscriptions.
     * 
     * @return the job notification subscriptions
     */
    public List<JobNotificationSubscriptionType> getJobNotificationSubscriptionTypes() {
        return this.jobNotificationSubscriptionTypes;
    }

    /**
     * Adds the job notification subscription type.
     * 
     * @param jobNotificationSubscription
     *            the job notification subscription
     * @return the job type
     */
    public JobType addJobNotificationSubscriptionType(
            JobNotificationSubscriptionType jobNotificationSubscription) {
        if (this.jobNotificationSubscriptionTypes == null) {
            this.jobNotificationSubscriptionTypes = new ArrayList<JobNotificationSubscriptionType>();
        }
        this.jobNotificationSubscriptionTypes.add(jobNotificationSubscription);
        return this;
    }

}

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
import com.microsoft.windowsazure.services.media.implementation.content.JobType;

/**
 * The Class JobInfo.
 */
public class JobInfo extends ODataEntity<JobType> {

    /**
     * Instantiates a new job info.
     * 
     * @param entry
     *            the entry
     * @param content
     *            the content
     */
    public JobInfo(EntryType entry, JobType content) {
        super(entry, content);
    }

    /**
     * Instantiates a new job info.
     */
    public JobInfo() {
        super(new JobType());
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
     * @return the job info
     */
    public JobInfo setId(String id) {
        getContent().setId(id);
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
     * @return the job info
     */
    public JobInfo setName(String name) {
        getContent().setName(name);
        return this;
    }

    /**
     * Gets the created.
     * 
     * @return the created
     */
    public Date getCreated() {
        return getContent().getCreated();
    }

    /**
     * Sets the created.
     * 
     * @param created
     *            the created
     * @return the job info
     */
    public JobInfo setCreated(Date created) {
        getContent().setCreated(created);
        return this;
    }

    /**
     * Gets the last modified.
     * 
     * @return the last modified
     */
    public Date getLastModified() {
        return getContent().getLastModified();
    }

    /**
     * Sets the last modified.
     * 
     * @param lastModified
     *            the last modified
     * @return the job info
     */
    public JobInfo setLastModified(Date lastModified) {
        getContent().setLastModified(lastModified);
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
     * @return the job info
     */
    public JobInfo setEndTime(Date endTime) {
        getContent().setEndTime(endTime);
        return this;
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
     * Sets the priority.
     * 
     * @param priority
     *            the priority
     * @return the job info
     */
    public JobInfo setPriority(int priority) {
        getContent().setPriority(priority);
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
     * @return the job info
     */
    public JobInfo setRunningDuration(double runningDuration) {
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
     * @return the job info
     */
    public JobInfo setStartTime(Date startTime) {
        getContent().setStartTime(startTime);
        return this;
    }

    /**
     * Gets the state.
     * 
     * @return the state
     */
    public int getState() {
        return getContent().getState();
    }

    /**
     * Sets the state.
     * 
     * @param state
     *            the state
     * @return the job info
     */
    public JobInfo setState(int state) {
        getContent().setState(state);
        return this;
    }

    /**
     * Gets the template id.
     * 
     * @return the template id
     */
    public String getTemplateId() {
        return getContent().getTemplateId();
    }

    /**
     * Sets the template id.
     * 
     * @param templateId
     *            the template id
     * @return the job info
     */
    public JobInfo setTemplateId(String templateId) {
        getContent().setTemplateId(templateId);
        return this;
    }

    /**
     * Gets the input media assets.
     * 
     * @return the input media assets
     */
    public String getInputMediaAssets() {
        return getContent().getInputMediaAssets();
    }

    /**
     * Sets the input media assets.
     * 
     * @param inputMediaAssets
     *            the input media assets
     * @return the job info
     */
    public JobInfo setInputMediaAssets(String inputMediaAssets) {
        getContent().setInputMediaAssets(inputMediaAssets);
        return this;
    }

    /**
     * Gets the output media assets.
     * 
     * @return the output media assets
     */
    public String getOutputMediaAssets() {
        return getContent().getOutputMediaAssets();
    }

    /**
     * Sets the output media assets.
     * 
     * @param outputMediaAssets
     *            the output media assets
     * @return the job info
     */
    public JobInfo setOutputMediaAssets(String outputMediaAssets) {
        getContent().setOutputMediaAssets(outputMediaAssets);
        return this;
    }

    /**
     * Gets the tasks.
     * 
     * @return the tasks
     */
    public String getTasks() {
        return getContent().getTaskBody();
    }

    /**
     * Sets the tasks.
     * 
     * @param tasks
     *            the tasks
     * @return the job info
     */
    public JobInfo setTasks(String tasks) {
        getContent().setTaskBody(tasks);
        return this;
    }
}

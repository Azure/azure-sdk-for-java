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

import java.util.Date;
import java.util.List;

import com.microsoft.windowsazure.services.media.implementation.ODataEntity;
import com.microsoft.windowsazure.services.media.implementation.atom.EntryType;
import com.microsoft.windowsazure.services.media.implementation.content.JobNotificationSubscriptionType;
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
     * Gets the id.
     * 
     * @return the id
     */
    public String getId() {
        return getContent().getId();
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
     * Gets the created.
     * 
     * @return the created
     */
    public Date getCreated() {
        return getContent().getCreated();
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
     * Gets the end time.
     * 
     * @return the end time
     */
    public Date getEndTime() {
        return getContent().getEndTime();
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
    public JobState getState() {
        return JobState.fromCode(getContent().getState());
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
     * Get a link which, when listed, will return the input assets for the job.
     * 
     * @return Link if found, null if not.
     */
    public LinkInfo<AssetInfo> getInputAssetsLink() {
        return this.<AssetInfo> getRelationLink("InputMediaAssets");
    }

    /**
     * Get a link which, when listed, will return the output assets for the job.
     * 
     * @return Link if found, null if not.
     */
    public LinkInfo<AssetInfo> getOutputAssetsLink() {
        return this.<AssetInfo> getRelationLink("OutputMediaAssets");
    }

    /**
     * Gets the tasks link.
     * 
     * @return the tasks link
     */
    public LinkInfo<TaskInfo> getTasksLink() {
        return this.<TaskInfo> getRelationLink("Tasks");
    }

    /**
     * Gets the job notification subscriptions.
     * 
     * @return the job notification subscriptions
     */
    public List<JobNotificationSubscription> getJobNotificationSubscriptions() {
        return JobNotificationSubscriptionListFactory.create(getContent()
                .getJobNotificationSubscriptionTypes());
    }

    /**
     * Adds the job notification subscription.
     * 
     * @param jobNotificationSubscription
     *            the job notification subscription
     * @return the job info
     */
    public JobInfo addJobNotificationSubscription(
            JobNotificationSubscription jobNotificationSubscription) {
        getContent().addJobNotificationSubscriptionType(
                new JobNotificationSubscriptionType()
                        .setNotificationEndPointId(
                                jobNotificationSubscription
                                        .getNotificationEndPointId())
                        .setTargetJobState(
                                jobNotificationSubscription.getTargetJobState()
                                        .getCode()));
        return this;
    }
}

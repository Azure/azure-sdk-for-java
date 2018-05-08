/*
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
import java.util.List;

import com.microsoft.windowsazure.services.media.implementation.content.JobNotificationSubscriptionType;

/**
 * The Class JobNotificationSubscription factory.
 */
public abstract class JobNotificationSubscriptionListFactory {

    public static List<JobNotificationSubscription> create(
            List<JobNotificationSubscriptionType> jobNotificationSubscriptionTypeList) {
        if (jobNotificationSubscriptionTypeList == null) {
            throw new IllegalArgumentException(
                    "The jobNotificationSubscriptionTypeList cannot be null.");
        }
        List<JobNotificationSubscription> jobNotificationSubscriptionList = new ArrayList<JobNotificationSubscription>();
        for (JobNotificationSubscriptionType jobNotificationSubscriptionType : jobNotificationSubscriptionTypeList) {
            jobNotificationSubscriptionList
                    .add(new JobNotificationSubscription(
                            jobNotificationSubscriptionType
                                    .getNotificationEndPointId(),
                            TargetJobState
                                    .fromCode(jobNotificationSubscriptionType
                                            .getTargetJobState())));
        }
        return jobNotificationSubscriptionList;

    }
}

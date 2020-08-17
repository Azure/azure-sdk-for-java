// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.redis.models;

import com.azure.resourcemanager.redis.fluent.inner.RedisPatchScheduleInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ExternalChildResource;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;
import java.util.List;

/** The Azure Redis Patch Schedule entries are of type ScheduleEntry. */
public interface RedisPatchSchedule
    extends ExternalChildResource<RedisPatchSchedule, RedisCache>, HasInner<RedisPatchScheduleInner> {
    /**
     * Get the scheduleEntries value.
     *
     * @return the scheduleEntries value
     */
    List<ScheduleEntry> scheduleEntries();
}

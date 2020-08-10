/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.redis;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.apigeneration.LangMethodDefinition;
import com.microsoft.azure.management.redis.implementation.RedisPatchScheduleInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ExternalChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;

import java.util.List;

/**
 * The Azure Redis Patch Schedule entries are of type ScheduleEntry.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.Redis.Fluent.Models")
@Beta(Beta.SinceVersion.V1_12_0)
public interface RedisPatchSchedule extends
        ExternalChildResource<RedisPatchSchedule, RedisCache>,
        HasInner<RedisPatchScheduleInner> {
    /**
     * Get the scheduleEntries value.
     *
     * @return the scheduleEntries value
     */
    @LangMethodDefinition(AsType = LangMethodDefinition.LangMethodType.Property)
    List<ScheduleEntry> scheduleEntries();
}

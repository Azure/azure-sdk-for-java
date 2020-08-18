// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.redis.implementation;

import com.azure.resourcemanager.redis.fluent.inner.RedisPatchScheduleInner;
import com.azure.resourcemanager.redis.models.DefaultName;
import com.azure.resourcemanager.redis.models.RedisCache;
import com.azure.resourcemanager.redis.models.RedisPatchSchedule;
import com.azure.resourcemanager.redis.models.ScheduleEntry;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import java.util.Collections;
import java.util.List;
import reactor.core.publisher.Mono;

/** The Azure {@link RedisPatchSchedule} wrapper class implementation. */
class RedisPatchScheduleImpl
    extends ExternalChildResourceImpl<RedisPatchSchedule, RedisPatchScheduleInner, RedisCacheImpl, RedisCache>
    implements RedisPatchSchedule {

    RedisPatchScheduleImpl(String name, RedisCacheImpl parent, RedisPatchScheduleInner innerObject) {
        super(getChildName(name, parent.name()), parent, innerObject);
    }

    @Override
    public String id() {
        return this.inner().id();
    }

    @Override
    public List<ScheduleEntry> scheduleEntries() {
        return Collections.unmodifiableList(this.inner().scheduleEntries());
    }

    @Override
    public Mono<RedisPatchSchedule> createResourceAsync() {
        final RedisPatchScheduleImpl self = this;
        return this
            .parent()
            .manager()
            .inner()
            .getPatchSchedules()
            .createOrUpdateAsync(
                this.parent().resourceGroupName(),
                this.parent().name(),
                DefaultName.DEFAULT,
                this.inner().scheduleEntries())
            .map(
                patchScheduleInner -> {
                    self.setInner(patchScheduleInner);
                    return self;
                });
    }

    @Override
    public Mono<RedisPatchSchedule> updateResourceAsync() {
        return this.createResourceAsync();
    }

    @Override
    public Mono<Void> deleteResourceAsync() {
        return this
            .parent()
            .manager()
            .inner()
            .getPatchSchedules()
            .deleteAsync(this.parent().resourceGroupName(), this.parent().name(), DefaultName.DEFAULT);
    }

    @Override
    protected Mono<RedisPatchScheduleInner> getInnerAsync() {
        return this
            .parent()
            .manager()
            .inner()
            .getPatchSchedules()
            .getAsync(this.parent().resourceGroupName(), this.parent().name(), DefaultName.DEFAULT);
    }

    private static String getChildName(String name, String parentName) {
        if (name != null && name.contains("/")) {
            // Patch Schedule name consist of "parent/child" name syntax but delete/update/get should be called only on
            // child name
            return name.substring(parentName.length() + 1);
        }
        return name;
    }
}

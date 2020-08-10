/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.redis.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.redis.RedisCache;
import com.microsoft.azure.management.redis.RedisPatchSchedule;
import com.microsoft.azure.management.redis.ScheduleEntry;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import rx.Observable;
import rx.functions.Func1;

import java.util.Collections;
import java.util.List;

/**
 * The Azure {@link RedisPatchSchedule} wrapper class implementation.
 */
@LangDefinition
class RedisPatchScheduleImpl extends
        ExternalChildResourceImpl<RedisPatchSchedule,
                RedisPatchScheduleInner,
                RedisCacheImpl,
                RedisCache>
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
    public Observable<RedisPatchSchedule> createResourceAsync() {
        final RedisPatchScheduleImpl self = this;
        return this.parent().manager().inner().patchSchedules().createOrUpdateAsync(
                this.parent().resourceGroupName(),
                this.parent().name(),
                this.inner().scheduleEntries())
                .map(new Func1<RedisPatchScheduleInner, RedisPatchSchedule>() {
                    @Override
                    public RedisPatchSchedule call(RedisPatchScheduleInner patchScheduleInner) {
                        self.setInner(patchScheduleInner);
                        return self;
                    }
                });
    }

    @Override
    public Observable<RedisPatchSchedule> updateResourceAsync() {
        return this.createResourceAsync();
    }

    @Override
    public Observable<Void> deleteResourceAsync() {
        return this.parent().manager().inner().patchSchedules().deleteAsync(this.parent().resourceGroupName(),
                this.parent().name());
    }

    @Override
    protected Observable<RedisPatchScheduleInner> getInnerAsync() {
        return this.parent().manager().inner().patchSchedules().getAsync(this.parent().resourceGroupName(),
                this.parent().name());
    }


    private static String getChildName(String name, String parentName) {
        if (name != null
                && name.indexOf("/") != -1) {
            // Patch Schedule name consist of "parent/child" name syntax but delete/update/get should be called only on child name
            return name.substring(parentName.length() + 1);
        }
        return name;
    }
}

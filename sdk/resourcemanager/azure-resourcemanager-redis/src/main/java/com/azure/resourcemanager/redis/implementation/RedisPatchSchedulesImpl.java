// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


package com.azure.resourcemanager.redis.implementation;

import com.azure.resourcemanager.redis.fluent.inner.RedisPatchScheduleInner;
import com.azure.resourcemanager.redis.models.RedisCache;
import com.azure.resourcemanager.redis.models.RedisPatchSchedule;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.ExternalChildResourcesCachedImpl;
import reactor.core.publisher.Flux;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a Redis patch schedule collection associated with a Redis cache instance.
 */
class RedisPatchSchedulesImpl extends
    ExternalChildResourcesCachedImpl<RedisPatchScheduleImpl,
        RedisPatchSchedule,
        RedisPatchScheduleInner,
                RedisCacheImpl,
        RedisCache> {
    // Currently Redis Cache has one PatchSchedule
    private final String patchScheduleName = "default";

    RedisPatchSchedulesImpl(RedisCacheImpl parent) {
        super(parent, parent.taskGroup(), "PatchSchedule");
        if (parent.id() != null) {
            this.cacheCollection();
        }
    }

    Map<String, RedisPatchSchedule> patchSchedulesAsMap() {
        Map<String, RedisPatchSchedule> result = new HashMap<>();
        for (Map.Entry<String, RedisPatchScheduleImpl> entry : this.collection().entrySet()) {
            RedisPatchScheduleImpl patchSchedule = entry.getValue();
            result.put(entry.getKey(), patchSchedule);
        }
        return Collections.unmodifiableMap(result);
    }

    public void addPatchSchedule(RedisPatchScheduleImpl patchSchedule) {
        this.addChildResource(patchSchedule);
    }

    public RedisPatchScheduleImpl getPatchSchedule() {
        return this.collection().get(this.patchScheduleName);
    }

    public void removePatchSchedule() {
        RedisPatchScheduleImpl psch = this.getPatchSchedule();
        if (psch != null) {
            psch.deleteResourceAsync().block();
        }
}

    public RedisPatchScheduleImpl defineInlinePatchSchedule() {
        return prepareInlineDefine(this.patchScheduleName);
    }

    public RedisPatchScheduleImpl updateInlinePatchSchedule() {
        return prepareInlineUpdate(this.patchScheduleName);
    }

    public void deleteInlinePatchSchedule() {
        prepareInlineRemove(this.patchScheduleName);
    }

    @Override
    protected Flux<RedisPatchScheduleImpl> listChildResourcesAsync() {
        return this.getParent().manager().inner().getPatchSchedules().listByRedisResourceAsync(
                this.getParent().resourceGroupName(),
                this.getParent().name())
            .map(patchScheduleInner -> new RedisPatchScheduleImpl(patchScheduleInner.name(), this.getParent(), patchScheduleInner));
    }

    @Override
    protected RedisPatchScheduleImpl newChildResource(String name) {
        return new RedisPatchScheduleImpl(name, this.getParent(), new RedisPatchScheduleInner());
    }
}

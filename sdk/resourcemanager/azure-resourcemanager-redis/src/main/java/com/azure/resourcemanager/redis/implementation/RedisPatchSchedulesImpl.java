// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.redis.implementation;

import com.azure.resourcemanager.redis.fluent.inner.RedisPatchScheduleInner;
import com.azure.resourcemanager.redis.models.RedisCache;
import com.azure.resourcemanager.redis.models.RedisPatchSchedule;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.ExternalChildResourcesCachedImpl;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Represents a Redis patch schedule collection associated with a Redis cache instance. */
class RedisPatchSchedulesImpl
    extends ExternalChildResourcesCachedImpl<
        RedisPatchScheduleImpl, RedisPatchSchedule, RedisPatchScheduleInner, RedisCacheImpl, RedisCache> {
    // Currently Redis Cache has one PatchSchedule
    private static final String PATCH_SCHEDULE_NAME = "default";
    private boolean load = false;

    RedisPatchSchedulesImpl(RedisCacheImpl parent) {
        super(parent, parent.taskGroup(), "PatchSchedule");
    }

    void ensureCollectionLoaded() {
        if (!load) {
            load = true;
            cacheCollection();
        }
    }

    Map<String, RedisPatchSchedule> patchSchedulesAsMap() {
        ensureCollectionLoaded();
        Map<String, RedisPatchSchedule> result = new HashMap<>();
        for (Map.Entry<String, RedisPatchScheduleImpl> entry : this.collection().entrySet()) {
            RedisPatchScheduleImpl patchSchedule = entry.getValue();
            result.put(entry.getKey(), patchSchedule);
        }
        return Collections.unmodifiableMap(result);
    }

    public void addPatchSchedule(RedisPatchScheduleImpl patchSchedule) {
        ensureCollectionLoaded();
        this.addChildResource(patchSchedule);
    }

    public RedisPatchScheduleImpl getPatchSchedule() {
        ensureCollectionLoaded();
        return this.collection().get(PATCH_SCHEDULE_NAME);
    }

    public void removePatchSchedule() {
        ensureCollectionLoaded();
        RedisPatchScheduleImpl psch = this.getPatchSchedule();
        if (psch != null) {
            psch.deleteResourceAsync().block();
        }
    }

    public RedisPatchScheduleImpl defineInlinePatchSchedule() {
        ensureCollectionLoaded();
        return prepareInlineDefine(PATCH_SCHEDULE_NAME);
    }

    public RedisPatchScheduleImpl updateInlinePatchSchedule() {
        ensureCollectionLoaded();
        return prepareInlineUpdate(PATCH_SCHEDULE_NAME);
    }

    public void deleteInlinePatchSchedule() {
        ensureCollectionLoaded();
        prepareInlineRemove(PATCH_SCHEDULE_NAME);
    }

    @Override
    protected Flux<RedisPatchScheduleImpl> listChildResourcesAsync() {
        return this
            .getParent()
            .manager()
            .inner()
            .getPatchSchedules()
            .listByRedisResourceAsync(this.getParent().resourceGroupName(), this.getParent().name())
            .map(
                patchScheduleInner ->
                    new RedisPatchScheduleImpl(patchScheduleInner.name(), this.getParent(), patchScheduleInner))
            .onErrorResume(e -> Mono.empty());
    }

    @Override
    protected List<RedisPatchScheduleImpl> listChildResources() {
        return listChildResourcesAsync().collectList().block();
    }

    @Override
    protected RedisPatchScheduleImpl newChildResource(String name) {
        return new RedisPatchScheduleImpl(name, this.getParent(), new RedisPatchScheduleInner());
    }
}

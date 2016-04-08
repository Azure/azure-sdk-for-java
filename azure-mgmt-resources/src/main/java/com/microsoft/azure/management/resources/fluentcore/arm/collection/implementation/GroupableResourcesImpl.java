package com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.Azure;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class GroupableResourcesImpl<WRAPPER,
        INNER extends com.microsoft.azure.Resource,
        WRAPPERIMPL extends WRAPPER> extends ArmEntitiesImpl {
    protected GroupableResourcesImpl(Azure azure) {
        super(azure);
    }

    protected abstract List<INNER> getNativeEntities(String group) throws Exception;
    protected abstract INNER getNativeEntity(String group, String name) throws Exception;
    protected abstract WRAPPERIMPL wrap(INNER nativeItem);

    public abstract void delete(String groupName, String name) throws Exception;

    public final Map<String, WRAPPER> list(String groupName) throws Exception {
        HashMap<String, WRAPPER> wrappers = new HashMap<>();
        for(INNER nativeItem : getNativeEntities(groupName)) {
            wrappers.put(nativeItem.id(), wrap(nativeItem));
        }
        return Collections.unmodifiableMap(wrappers);
    }

    public final Map<String, WRAPPER> list() throws Exception {
        return list(null);
    }

    public final WRAPPER get(String groupName, String name) throws Exception {
        return wrap(getNativeEntity(groupName, name));
    }

    public final WRAPPER get(String id) throws Exception {
        return get(
                ResourceUtils.groupFromResourceId(id),
                ResourceUtils.nameFromResourceId(id));
    }

    public final void delete(String id) throws Exception {
        this.delete(
                ResourceUtils.groupFromResourceId(id),
                ResourceUtils.nameFromResourceId(id));
    }
}
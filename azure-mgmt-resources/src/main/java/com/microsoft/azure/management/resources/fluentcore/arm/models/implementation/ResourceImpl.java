package com.microsoft.azure.management.resources.fluentcore.arm.models.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.IndexableRefreshableWrapperImpl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public abstract class ResourceImpl<
        WRAPPER,
        INNER extends com.microsoft.azure.Resource,
        WRAPPERIMPL extends ResourceImpl<WRAPPER, INNER, WRAPPERIMPL>>
        extends
        IndexableRefreshableWrapperImpl<WRAPPER, INNER>
        implements
        Resource {


    protected ResourceImpl(String id, INNER innerObject) {
        super(id, innerObject);
    }

    /*******************************************
     * Getters
     *******************************************/

    @Override
    public String region() {
        return this.inner().location();
    }

    @Override
    public Map<String, String> tags() {
        return Collections.unmodifiableMap(this.inner().getTags());
    }

    @Override
    public String id() {
        return this.inner().id();
    }

    @Override
    public String type() {
        return this.inner().type();
    }

    @Override
    public String name() {
        return this.inner().name();
    }

    /**************************************************
     * Tag setters
     **************************************************/

    @SuppressWarnings("unchecked")
    public final WRAPPERIMPL withTags(Map<String, String> tags) {
        this.inner().setTags(new HashMap<>(tags));
        return (WRAPPERIMPL) this;
    }

    @SuppressWarnings("unchecked")
    public final WRAPPERIMPL withTag(String name, String value) {
        this.inner().getTags().put(name, value);
        return (WRAPPERIMPL) this;
    }

    @SuppressWarnings("unchecked")
    public final WRAPPERIMPL withoutTag(String name) {
        this.inner().getTags().remove(name);
        return (WRAPPERIMPL) this;
    }

    /**********************************************
     * Region setters
     **********************************************/

    @SuppressWarnings("unchecked")
    public final WRAPPERIMPL withRegion(String regionName) {
        this.inner().setLocation(regionName);
        return (WRAPPERIMPL) this;
    }

    public final WRAPPERIMPL withRegion(Region region) {
        return this.withRegion(region.toString());
    }
}
/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.resources.implementation;

import com.azure.management.resources.ExportTemplateRequest;
import com.azure.management.resources.ResourceGroup;
import com.azure.management.resources.ResourceGroupExportResult;
import com.azure.management.resources.ResourceGroupExportTemplateOptions;
import com.azure.management.resources.fluentcore.arm.Region;
import com.azure.management.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import com.azure.management.resources.models.ResourceGroupInner;
import com.azure.management.resources.models.ResourceGroupsInner;
import com.azure.management.resources.models.ResourceManagementClientImpl;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * The implementation for {@link ResourceGroup} and its create and update interfaces.
 */
class ResourceGroupImpl extends
        CreatableUpdatableImpl<ResourceGroup, ResourceGroupInner, ResourceGroupImpl>
        implements
        ResourceGroup,
        ResourceGroup.Definition,
        ResourceGroup.Update {

    private final ResourceGroupsInner client;

    protected ResourceGroupImpl(final ResourceGroupInner innerModel, String name, final ResourceManagementClientImpl serviceClient) {
        super(name, innerModel);
        this.client = serviceClient.resourceGroups();
    }

    @Override
    public String provisioningState() {
        return this.inner().getProperties().getProvisioningState();
    }

    @Override
    public String regionName() {
        return this.inner().getLocation();
    }

    @Override
    public Region region() {
        return Region.fromName(this.regionName());
    }

    @Override
    public String id() {
        return this.inner().getId();
    }

    @Override
    public String type() {
        return null;
    }

    @Override
    public Map<String, String> tags() {
        Map<String, String> tags = this.inner().getTags();
        if (tags == null) {
            tags = new HashMap<>();
        }
        return Collections.unmodifiableMap(tags);
    }

    @Override
    public ResourceGroupExportResult exportTemplate(ResourceGroupExportTemplateOptions options) {
        return this.exportTemplateAsync(options).block();
    }

    @Override
    public Mono<ResourceGroupExportResult> exportTemplateAsync(ResourceGroupExportTemplateOptions options) {
        ExportTemplateRequest inner = new ExportTemplateRequest()
                .setResources(Arrays.asList("*"))
                .setOptions(options.toString());
        return client.exportTemplateAsync(name(), inner).map(resourceGroupExportResultInner -> new ResourceGroupExportResultImpl(resourceGroupExportResultInner));
    }

    @Override
    public ResourceGroupImpl withRegion(String regionName) {
        this.inner().setLocation(regionName);
        return this;
    }

    @Override
    public ResourceGroupImpl withRegion(Region region) {
        return this.withRegion(region.toString());
    }

    @Override
    public ResourceGroupImpl withTags(Map<String, String> tags) {
        this.inner().setTags(new HashMap<>(tags));
        return this;
    }

    @Override
    public ResourceGroupImpl withTag(String key, String value) {
        if (this.inner().getTags() == null) {
            this.inner().setTags(new HashMap<String, String>());
        }
        this.inner().getTags().put(key, value);
        return this;
    }

    @Override
    public ResourceGroupImpl withoutTag(String key) {
        this.inner().getTags().remove(key);
        return this;
    }

    @Override
    public Mono<ResourceGroup> createResourceAsync() {
        ResourceGroupInner params = new ResourceGroupInner();
        params.setLocation(this.inner().getLocation());
        params.setTags(this.inner().getTags());
        return client.createOrUpdateAsync(this.name(), params)
                .map(innerToFluentMap(this));
    }

    @Override
    public Mono<ResourceGroup> updateResourceAsync() {
        return createResourceAsync();
    }

    @Override
    public boolean isInCreateMode() {
        return this.inner().getId() == null;
    }

    @Override
    protected Mono<ResourceGroupInner> getInnerAsync() {
        return client.getAsync(this.key);
    }
}

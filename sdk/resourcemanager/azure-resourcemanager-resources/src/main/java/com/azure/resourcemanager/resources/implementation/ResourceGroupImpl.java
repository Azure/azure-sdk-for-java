// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.resources.implementation;

import com.azure.resourcemanager.resources.models.ExportTemplateRequest;
import com.azure.resourcemanager.resources.models.ResourceGroup;
import com.azure.resourcemanager.resources.models.ResourceGroupExportResult;
import com.azure.resourcemanager.resources.models.ResourceGroupExportTemplateOptions;
import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import com.azure.resourcemanager.resources.fluent.models.ResourceGroupInner;
import com.azure.resourcemanager.resources.fluent.ResourceGroupsClient;
import com.azure.resourcemanager.resources.fluent.ResourceManagementClient;
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

    private final ResourceGroupsClient client;

    protected ResourceGroupImpl(final ResourceGroupInner innerModel, String name,
            final ResourceManagementClient serviceClient) {
        super(name, innerModel);
        this.client = serviceClient.getResourceGroups();
    }

    @Override
    public String provisioningState() {
        return this.innerModel().properties().provisioningState();
    }

    @Override
    public String regionName() {
        return this.innerModel().location();
    }

    @Override
    public Region region() {
        return Region.fromName(this.regionName());
    }

    @Override
    public String id() {
        return this.innerModel().id();
    }

    @Override
    public String type() {
        return null;
    }

    @Override
    public Map<String, String> tags() {
        Map<String, String> tags = this.innerModel().tags();
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
                .withResources(Arrays.asList("*"))
                .withOptions(options.toString());
        return client.exportTemplateAsync(name(), inner).map(resourceGroupExportResultInner ->
            new ResourceGroupExportResultImpl(resourceGroupExportResultInner));
    }

    @Override
    public ResourceGroupImpl withRegion(String regionName) {
        this.innerModel().withLocation(regionName);
        return this;
    }

    @Override
    public ResourceGroupImpl withRegion(Region region) {
        return this.withRegion(region.toString());
    }

    @Override
    public ResourceGroupImpl withTags(Map<String, String> tags) {
        this.innerModel().withTags(new HashMap<>(tags));
        return this;
    }

    @Override
    public ResourceGroupImpl withTag(String key, String value) {
        if (this.innerModel().tags() == null) {
            this.innerModel().withTags(new HashMap<String, String>());
        }
        this.innerModel().tags().put(key, value);
        return this;
    }

    @Override
    public ResourceGroupImpl withoutTag(String key) {
        this.innerModel().tags().remove(key);
        return this;
    }

    @Override
    public Mono<ResourceGroup> createResourceAsync() {
        ResourceGroupInner params = new ResourceGroupInner();
        params.withLocation(this.innerModel().location());
        params.withTags(this.innerModel().tags());
        return client.createOrUpdateAsync(this.name(), params)
                .map(innerToFluentMap(this));
    }

    @Override
    public Mono<ResourceGroup> updateResourceAsync() {
        return createResourceAsync();
    }

    @Override
    public boolean isInCreateMode() {
        return this.innerModel().id() == null;
    }

    @Override
    protected Mono<ResourceGroupInner> getInnerAsync() {
        return client.getAsync(this.key);
    }
}

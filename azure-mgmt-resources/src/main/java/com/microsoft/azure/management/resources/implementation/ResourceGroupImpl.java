/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.management.resources.ResourceConnector;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.CreatableImpl;
import com.microsoft.azure.management.resources.implementation.api.ResourceGroupInner;
import com.microsoft.azure.management.resources.implementation.api.ResourceGroupsInner;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An instance of this class provides access to a resource group in Azure.
 */
public class ResourceGroupImpl extends
        CreatableImpl<ResourceGroup, ResourceGroupInner>
        implements
        ResourceGroup,
        ResourceGroup.DefinitionBlank,
        ResourceGroup.DefinitionCreatable,
        ResourceGroup.Update  {

    private final ResourceGroupsInner client;
    private final ResourceManagementClientImpl serviceClient;

    protected ResourceGroupImpl(final ResourceGroupInner innerModel, final ResourceManagementClientImpl serviceClient) {
        super(innerModel.name(), innerModel);
        this.client = serviceClient.resourceGroups();
        this.serviceClient = serviceClient;
    }

    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public String provisioningState() {
        return this.inner().properties().provisioningState();
    }

    @Override
    public String region() {
        return this.inner().location();
    }

    @Override
    public String id() {
        return this.inner().id();
    }

    @Override
    public String type() {
        return null;
    }

    @Override
    public Map<String, String> tags() {
        return Collections.unmodifiableMap(this.inner().tags());
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
        if (this.inner().tags() == null) {
            this.inner().setTags(new HashMap<String, String>());
        }
        this.inner().tags().put(key, value);
        return this;
    }

    @Override
    public ResourceGroupImpl withoutTag(String key) {
        this.inner().tags().remove(key);
        return this;
    }

    @Override
    public ResourceGroupImpl apply() throws Exception {
        ResourceGroupInner params = new ResourceGroupInner();
        ResourceGroupInner group;

        params.setTags(this.inner().tags());

        // Figure out the location, since the SDK requires on the params explicitly even though it cannot be changed
        if (this.inner().location() != null) {
            params.setLocation(this.inner().location());
        } else {
            group = client.get(this.key).getBody();
            if (null == group) {
                throw new Exception("Resource group not found");
            } else {
                params.setLocation(group.location());
            }
        }

        client.createOrUpdate(this.key, params);
        return this;
    }

    @Override
    public ResourceGroupImpl create() throws Exception {          //  FLUENT: implementation of ResourceGroup.DefinitionCreatable.Creatable<ResourceGroup>
        super.creatablesCreate();
        return this;
    }

    @Override
    public ResourceGroupImpl refresh() throws Exception {            //  FLUENT: implementation of ResourceGroup.Refreshable<ResourceGroup>
        this.setInner(client.get(this.key).getBody());
        return this;
    }

    @Override
    public <T extends ResourceConnector> T connectToResource(ResourceConnector.Builder<T> adapterBuilder) {
        return adapterBuilder.create(this.serviceClient.restClient(), this.serviceClient.subscriptionId(), this);
    }

    @Override
    protected void createResource() throws Exception {
        ResourceGroupInner params = new ResourceGroupInner();
        params.setLocation(this.inner().location());
        params.setTags(this.inner().tags());
        client.createOrUpdate(this.key, params);
    }
}

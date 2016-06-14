/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.network.NetworkSecurityGroup;
import com.microsoft.azure.management.network.NetworkSecurityGroups;
import com.microsoft.azure.management.network.implementation.api.NetworkSecurityGroupInner;
import com.microsoft.azure.management.network.implementation.api.NetworkSecurityGroupsInner;
import com.microsoft.azure.management.network.implementation.api.SecurityRuleInner;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.resources.implementation.ResourceManager;
import com.microsoft.rest.ServiceResponse;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Implementation of the NetworkSecurityGroups interface.
 * (Internal use only)
 */
class NetworkSecurityGroupsImpl implements NetworkSecurityGroups {
    private final NetworkSecurityGroupsInner client;
    private final ResourceManager resourceManager;
    private final PagedListConverter<NetworkSecurityGroupInner, NetworkSecurityGroup> converter;

    NetworkSecurityGroupsImpl(final NetworkSecurityGroupsInner client, final ResourceManager resourceManager) {
        this.client = client;
        this.resourceManager = resourceManager;
        this.converter = new PagedListConverter<NetworkSecurityGroupInner, NetworkSecurityGroup>() {
            @Override
            public NetworkSecurityGroup typeConvert(NetworkSecurityGroupInner inner) {
                return createFluentModel(inner);
            }
        };
    }

    @Override
    public PagedList<NetworkSecurityGroup> list() throws CloudException, IOException {
        ServiceResponse<PagedList<NetworkSecurityGroupInner>> response = client.listAll();
        return converter.convert(response.getBody());
    }

    @Override
    public PagedList<NetworkSecurityGroup> listByGroup(String groupName) throws CloudException, IOException {
        ServiceResponse<PagedList<NetworkSecurityGroupInner>> response = client.list(groupName);
        return converter.convert(response.getBody());
    }

    @Override
    public NetworkSecurityGroupImpl getByGroup(String groupName, String name) throws CloudException, IOException {
        ServiceResponse<NetworkSecurityGroupInner> serviceResponse = this.client.get(groupName, name);
        return createFluentModel(serviceResponse.getBody());
    }

    @Override
    public void delete(String id) throws Exception {
        this.delete(ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public void delete(String groupName, String name) throws Exception {
        this.client.delete(groupName, name);
    }

    @Override
    public NetworkSecurityGroupImpl define(String name) {
        return createFluentModel(name);
    }

    // Fluent model create helpers
    private NetworkSecurityGroupImpl createFluentModel(String name) {
        NetworkSecurityGroupInner inner = new NetworkSecurityGroupInner();

        // Initialize rules
        if (inner.securityRules() == null) {
            inner.withSecurityRules(new ArrayList<SecurityRuleInner>());
        }

        if (inner.defaultSecurityRules() == null) {
            inner.withDefaultSecurityRules(new ArrayList<SecurityRuleInner>());
        }

        return new NetworkSecurityGroupImpl(name, inner, this.client, this.resourceManager);
    }

    private NetworkSecurityGroupImpl createFluentModel(NetworkSecurityGroupInner inner) {
        return new NetworkSecurityGroupImpl(inner.name(), inner, this.client, this.resourceManager);
    }
}

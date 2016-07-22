/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.network.NetworkSecurityGroup;
import com.microsoft.azure.management.network.NetworkSecurityRule;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *  Implementation for {@link NetworkSecurityGroup} and its create and update interfaces.
 */
class NetworkSecurityGroupImpl
    extends GroupableResourceImpl<
        NetworkSecurityGroup,
        NetworkSecurityGroupInner,
        NetworkSecurityGroupImpl,
        NetworkManager>
    implements
        NetworkSecurityGroup,
        NetworkSecurityGroup.Definition,
        NetworkSecurityGroup.Update {

    private final NetworkSecurityGroupsInner innerCollection;
    private List<NetworkSecurityRule> rules;
    private List<NetworkSecurityRule> defaultRules;

    NetworkSecurityGroupImpl(
            final String name,
            final NetworkSecurityGroupInner innerModel,
            final NetworkSecurityGroupsInner innerCollection,
            final NetworkManager networkManager) {
        super(name, innerModel, networkManager);
        this.innerCollection = innerCollection;
        initializeRulesFromInner();
    }

    private void initializeRulesFromInner() {
        this.rules = new ArrayList<>();
        if (this.inner().securityRules() != null) {
            for (SecurityRuleInner ruleInner : this.inner().securityRules()) {
                this.rules.add(new NetworkSecurityRuleImpl(ruleInner.name(), ruleInner, this));
            }
        }

        this.defaultRules = new ArrayList<>();
        if (this.inner().defaultSecurityRules() != null) {
            for (SecurityRuleInner ruleInner : this.inner().defaultSecurityRules()) {
                this.defaultRules.add(new NetworkSecurityRuleImpl(ruleInner.name(), ruleInner, this));
            }
        }
    }

    // Verbs

    @Override
    public NetworkSecurityRuleImpl updateRule(String name) {
        for (NetworkSecurityRule r : this.rules) {
            if (r.name().equalsIgnoreCase(name)) {
                return (NetworkSecurityRuleImpl) r;
            }
        }
        throw new RuntimeException("Network security rule '" + name + "' not found");
    }

    @Override
    public NetworkSecurityRuleImpl defineRule(String name) {
        SecurityRuleInner inner = new SecurityRuleInner();
        inner.withName(name);
        inner.withPriority(100); // Must be at least 100
        return new NetworkSecurityRuleImpl(name, inner, this);
    }

    @Override
    public NetworkSecurityGroupImpl refresh() throws Exception {
        ServiceResponse<NetworkSecurityGroupInner> response =
            this.innerCollection.get(this.resourceGroupName(), this.name());
        this.setInner(response.getBody());
        initializeRulesFromInner();
        return this;
    }

    @Override
    public NetworkSecurityGroupImpl apply() throws Exception {
        return this.create();
    }

    @Override
    public ServiceCall applyAsync(ServiceCallback<NetworkSecurityGroup> callback) {
        return createAsync(callback);
    }

    // Setters (fluent)

    @Override
    public Update withoutRule(String name) {
        // Remove from cache
        List<NetworkSecurityRule> r = this.rules;
        for (int i = 0; i < r.size(); i++) {
            if (r.get(i).name().equalsIgnoreCase(name)) {
                r.remove(i);
                break;
            }
        }

        // Remove from inner
        List<SecurityRuleInner> innerRules = this.inner().securityRules();
        for (int i = 0; i < innerRules.size(); i++) {
            if (innerRules.get(i).name().equalsIgnoreCase(name)) {
                innerRules.remove(i);
                break;
            }
        }

        return this;
    }


    // Getters

    @Override
    public List<NetworkSecurityRule> securityRules() {
        return Collections.unmodifiableList(this.rules);
    }

    @Override
    public List<NetworkSecurityRule> defaultSecurityRules() {
        return Collections.unmodifiableList(this.defaultRules);
    }

    @Override
    public List<String> networkInterfaceIds() {
        List<String> ids = new ArrayList<>();
        if (this.inner().networkInterfaces() != null) {
            for (NetworkInterfaceInner inner : this.inner().networkInterfaces()) {
                ids.add(inner.id());
            }
        }
        return Collections.unmodifiableList(ids);
    }

    // CreatorTaskGroup.ResourceCreator implementation

    @Override
    public Resource createResource() throws Exception {
        ServiceResponse<NetworkSecurityGroupInner> response =
                this.innerCollection.createOrUpdate(this.resourceGroupName(), this.name(), this.inner());
        this.setInner(response.getBody());
        initializeRulesFromInner();
        return this;
    }

    @Override
    public ServiceCall createResourceAsync(final ServiceCallback<Resource> callback) {
        final NetworkSecurityGroupImpl self = this;
        return this.innerCollection.createOrUpdateAsync(this.resourceGroupName(), this.name(), this.inner(),
                new ServiceCallback<NetworkSecurityGroupInner>() {
                    @Override
                    public void failure(Throwable t) {
                        callback.failure(t);
                    }

                    @Override
                    public void success(ServiceResponse<NetworkSecurityGroupInner> response) {
                        self.setInner(response.getBody());
                        initializeRulesFromInner();
                        callback.success(new ServiceResponse<Resource>(self, response.getResponse()));
                    }
                });
    }
 }
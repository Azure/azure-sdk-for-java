/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.network.NetworkSecurityGroup;
import com.microsoft.azure.management.network.NetworkSecurityRule;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.rest.ServiceResponse;
import rx.Observable;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
    private final Map<String, NetworkSecurityRule> rules = new TreeMap<>();
    private final Map<String, NetworkSecurityRule> defaultRules = new TreeMap<>();

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
        this.rules.clear();
        List<SecurityRuleInner> inners = this.inner().securityRules();
        if (inners != null) {
            for (SecurityRuleInner inner : inners) {
                this.rules.put(inner.name(), new NetworkSecurityRuleImpl(inner, this));
            }
        }

        this.defaultRules.clear();
        inners = this.inner().defaultSecurityRules();
        if (inners != null) {
            for (SecurityRuleInner inner : inners) {
                this.defaultRules.put(inner.name(), new NetworkSecurityRuleImpl(inner, this));
            }
        }
    }

    // Verbs

    @Override
    public NetworkSecurityRuleImpl updateRule(String name) {
        return (NetworkSecurityRuleImpl) this.rules.get(name);
    }

    @Override
    public NetworkSecurityRuleImpl defineRule(String name) {
        SecurityRuleInner inner = new SecurityRuleInner();
        inner.withName(name);
        inner.withPriority(100); // Must be at least 100
        return new NetworkSecurityRuleImpl(inner, this);
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
    public Observable<NetworkSecurityGroup> applyAsync() {
        return createAsync();
    }

    // Setters (fluent)

    @Override
    public Update withoutRule(String name) {
        this.rules.remove(name);
        return this;
    }

    NetworkSecurityGroupImpl withRule(NetworkSecurityRuleImpl rule) {
        this.rules.put(rule.name(), rule);
        return this;
    }

    // Getters

    @Override
    public Map<String, NetworkSecurityRule> securityRules() {
        return Collections.unmodifiableMap(this.rules);
    }

    @Override
    public Map<String, NetworkSecurityRule> defaultSecurityRules() {
        return Collections.unmodifiableMap(this.defaultRules);
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

    private void beforeCreating() {
        // Reset and update subnets
        this.inner().withSecurityRules(innersFromWrappers(this.rules.values()));
    }

    // CreatorTaskGroup.ResourceCreator implementation
    @Override
    public Observable<NetworkSecurityGroup> createResourceAsync() {
        final NetworkSecurityGroupImpl self = this;
        beforeCreating();
        return this.innerCollection.createOrUpdateAsync(this.resourceGroupName(), this.name(), this.inner())
                .map(new Func1<ServiceResponse<NetworkSecurityGroupInner>, NetworkSecurityGroup>() {
                    @Override
                    public NetworkSecurityGroup call(ServiceResponse<NetworkSecurityGroupInner> networkSecurityGroupInner) {
                        self.setInner(networkSecurityGroupInner.getBody());
                        initializeRulesFromInner();
                        return self;
                    }
                });
    }
 }
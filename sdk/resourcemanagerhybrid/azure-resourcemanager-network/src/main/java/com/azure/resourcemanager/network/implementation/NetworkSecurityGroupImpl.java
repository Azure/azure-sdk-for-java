// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.models.NetworkInterfaceInner;
import com.azure.resourcemanager.network.fluent.models.NetworkSecurityGroupInner;
import com.azure.resourcemanager.network.fluent.models.SecurityRuleInner;
import com.azure.resourcemanager.network.models.NetworkSecurityGroup;
import com.azure.resourcemanager.network.models.NetworkSecurityRule;
import com.azure.resourcemanager.network.models.Subnet;
import com.azure.resourcemanager.network.models.TagsObject;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/** Implementation for NetworkSecurityGroup and its create and update interfaces. */
class NetworkSecurityGroupImpl
    extends GroupableParentResourceWithTagsImpl<
        NetworkSecurityGroup, NetworkSecurityGroupInner, NetworkSecurityGroupImpl, NetworkManager>
    implements NetworkSecurityGroup, NetworkSecurityGroup.Definition, NetworkSecurityGroup.Update {

    private Map<String, NetworkSecurityRule> rules;
    private Map<String, NetworkSecurityRule> defaultRules;

    NetworkSecurityGroupImpl(
        final String name, final NetworkSecurityGroupInner innerModel, final NetworkManager networkManager) {
        super(name, innerModel, networkManager);
    }

    @Override
    protected void initializeChildrenFromInner() {
        this.rules = new TreeMap<>();
        List<SecurityRuleInner> inners = this.innerModel().securityRules();
        if (inners != null) {
            for (SecurityRuleInner inner : inners) {
                this.rules.put(inner.name(), new NetworkSecurityRuleImpl(inner, this));
            }
        }

        this.defaultRules = new TreeMap<>();
        inners = this.innerModel().defaultSecurityRules();
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
    public Mono<NetworkSecurityGroup> refreshAsync() {
        return super
            .refreshAsync()
            .map(
                networkSecurityGroup -> {
                    NetworkSecurityGroupImpl impl = (NetworkSecurityGroupImpl) networkSecurityGroup;

                    impl.initializeChildrenFromInner();
                    return impl;
                });
    }

    @Override
    protected Mono<NetworkSecurityGroupInner> getInnerAsync() {
        return this
            .manager()
            .serviceClient()
            .getNetworkSecurityGroups()
            .getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    protected Mono<NetworkSecurityGroupInner> applyTagsToInnerAsync() {
        return this
            .manager()
            .serviceClient()
            .getNetworkSecurityGroups()
            .updateTagsAsync(resourceGroupName(), name(), new TagsObject().withTags(innerModel().tags()));
    }

    @Override
    public List<Subnet> listAssociatedSubnets() {
        return Utils.listAssociatedSubnets(this.myManager, this.innerModel().subnets());
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
    public Set<String> networkInterfaceIds() {
        Set<String> ids = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        if (this.innerModel().networkInterfaces() != null) {
            for (NetworkInterfaceInner inner : this.innerModel().networkInterfaces()) {
                ids.add(inner.id());
            }
        }
        return Collections.unmodifiableSet(ids);
    }

    @Override
    protected void beforeCreating() {
        // Reset and update subnets
        this.innerModel().withSecurityRules(innersFromWrappers(this.rules.values()));
    }

    @Override
    protected Mono<NetworkSecurityGroupInner> createInner() {
        return this
            .manager()
            .serviceClient()
            .getNetworkSecurityGroups()
            .createOrUpdateAsync(this.resourceGroupName(), this.name(), this.innerModel());
    }
}

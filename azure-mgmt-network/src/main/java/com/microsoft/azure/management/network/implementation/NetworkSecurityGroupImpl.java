/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.NetworkSecurityGroup;
import com.microsoft.azure.management.network.NetworkSecurityRule;
import com.microsoft.azure.management.network.Subnet;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableParentResourceImpl;
import rx.Observable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 *  Implementation for {@link NetworkSecurityGroup} and its create and update interfaces.
 */
@LangDefinition
class NetworkSecurityGroupImpl
    extends GroupableParentResourceImpl<
        NetworkSecurityGroup,
        NetworkSecurityGroupInner,
        NetworkSecurityGroupImpl,
        NetworkManager>
    implements
        NetworkSecurityGroup,
        NetworkSecurityGroup.Definition,
        NetworkSecurityGroup.Update {

    private final NetworkSecurityGroupsInner innerCollection;
    private Map<String, NetworkSecurityRule> rules;
    private Map<String, NetworkSecurityRule> defaultRules;

    NetworkSecurityGroupImpl(
            final String name,
            final NetworkSecurityGroupInner innerModel,
            final NetworkSecurityGroupsInner innerCollection,
            final NetworkManager networkManager) {
        super(name, innerModel, networkManager);
        this.innerCollection = innerCollection;
    }

    @Override
    protected void initializeChildrenFromInner() {
        this.rules = new TreeMap<>();
        List<SecurityRuleInner> inners = this.inner().securityRules();
        if (inners != null) {
            for (SecurityRuleInner inner : inners) {
                this.rules.put(inner.name(), new NetworkSecurityRuleImpl(inner, this));
            }
        }

        this.defaultRules = new TreeMap<>();
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
    public NetworkSecurityGroupImpl refresh() {
        NetworkSecurityGroupInner response = this.innerCollection.get(this.resourceGroupName(), this.name());
        this.setInner(response);
        initializeChildrenFromInner();
        return this;
    }

    @Override
    public List<Subnet> listAssociatedSubnets() {
        return this.myManager.listAssociatedSubnets(this.inner().subnets());
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

    @Override
    protected void beforeCreating() {
        // Reset and update subnets
        this.inner().withSecurityRules(innersFromWrappers(this.rules.values()));
    }

    @Override
    protected void afterCreating() {
    }

    @Override
    protected Observable<NetworkSecurityGroupInner> createInner() {
        return this.innerCollection.createOrUpdateAsync(this.resourceGroupName(), this.name(), this.inner());
    }
}

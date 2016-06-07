/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.microsoft.azure.management.network.NetworkSecurityGroup;
import com.microsoft.azure.management.network.NetworkSecurityRule;
import com.microsoft.azure.management.network.implementation.api.NetworkSecurityGroupInner;
import com.microsoft.azure.management.network.implementation.api.NetworkSecurityGroupsInner;
import com.microsoft.azure.management.network.implementation.api.SecurityRuleInner;
import com.microsoft.azure.management.resources.ResourceGroups;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.rest.ServiceResponse;

/**
 * Implementation of the NetworkSecurityGroup interface.
 * (Internal use only)
 */
class NetworkSecurityGroupImpl
    extends GroupableResourceImpl<NetworkSecurityGroup, NetworkSecurityGroupInner, NetworkSecurityGroupImpl>
    implements
        NetworkSecurityGroup,
        NetworkSecurityGroup.Definitions,
        NetworkSecurityGroup.Update {

    private final NetworkSecurityGroupsInner client;
    private List<NetworkSecurityRule> rules;
    private List<NetworkSecurityRule> defaultRules;

    NetworkSecurityGroupImpl(String name,
            NetworkSecurityGroupInner innerModel,
            final NetworkSecurityGroupsInner client,
            final ResourceGroups resourceGroups) {
        super(name, innerModel, resourceGroups);
        this.client = client;
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
    public NetworkSecurityGroupImpl refresh() throws Exception {
        ServiceResponse<NetworkSecurityGroupInner> response =
            this.client.get(this.resourceGroupName(), this.name());
        this.setInner(response.getBody());
        initializeRulesFromInner();
        return this;
    }

    @Override
    public NetworkSecurityGroupImpl create() throws Exception {
        super.creatablesCreate();
        return this;
    }

    @Override
    public NetworkSecurityGroupImpl update() throws Exception {
        return this;
    }

    @Override
    public NetworkSecurityGroupImpl apply() throws Exception {
        return this.create();
    }

    @Override
    protected void createResource() throws Exception {
        ServiceResponse<NetworkSecurityGroupInner> response =
                this.client.createOrUpdate(this.resourceGroupName(), this.name(), this.inner());
        this.setInner(response.getBody());
        initializeRulesFromInner();
    }


    // Setters (fluent)

    @Override
    public NetworkSecurityRuleImpl defineRule(String name) {
        SecurityRuleInner inner = new SecurityRuleInner();
        inner.withName(name);
        inner.withPriority(100); // Must be at least 100
        return new NetworkSecurityRuleImpl(name, inner, this);
    }

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
}
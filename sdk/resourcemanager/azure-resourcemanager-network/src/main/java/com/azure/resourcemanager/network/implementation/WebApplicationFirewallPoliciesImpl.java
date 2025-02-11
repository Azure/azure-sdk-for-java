// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.WebApplicationFirewallPoliciesClient;
import com.azure.resourcemanager.network.fluent.models.WebApplicationFirewallPolicyInner;
import com.azure.resourcemanager.network.models.WebApplicationFirewallPolicies;
import com.azure.resourcemanager.network.models.WebApplicationFirewallPolicy;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

/** Implementation for {@link WebApplicationFirewallPolicies}. */
public class WebApplicationFirewallPoliciesImpl extends
    TopLevelModifiableResourcesImpl<WebApplicationFirewallPolicy, WebApplicationFirewallPolicyImpl, WebApplicationFirewallPolicyInner, WebApplicationFirewallPoliciesClient, NetworkManager>
    implements WebApplicationFirewallPolicies {

    public WebApplicationFirewallPoliciesImpl(NetworkManager manager) {
        super(manager.serviceClient().getWebApplicationFirewallPolicies(), manager);
    }

    @Override
    public WebApplicationFirewallPolicyImpl define(String name) {
        return wrapModel(name).enablePolicy();
    }

    @Override
    protected WebApplicationFirewallPolicyImpl wrapModel(String name) {
        WebApplicationFirewallPolicyInner inner = new WebApplicationFirewallPolicyInner();
        return new WebApplicationFirewallPolicyImpl(name, inner, this.manager());
    }

    @Override
    protected WebApplicationFirewallPolicyImpl wrapModel(WebApplicationFirewallPolicyInner inner) {
        return new WebApplicationFirewallPolicyImpl(inner.name(), inner, this.manager());
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.models;

import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.models.WebApplicationFirewallPolicyInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;

public interface WebApplicationFirewallPolicy
    extends GroupableResource<NetworkManager, WebApplicationFirewallPolicyInner> {

    /** @return mode of the Web Application Firewall Policy */
    WebApplicationFirewallMode mode();

}

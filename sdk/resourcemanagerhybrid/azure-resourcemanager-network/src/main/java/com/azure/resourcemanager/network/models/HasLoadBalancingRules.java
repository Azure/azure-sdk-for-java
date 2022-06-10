// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;

import java.util.Map;

/** An interface representing a model's ability to reference load balancing rules. */
@Fluent()
public interface HasLoadBalancingRules {
    /** @return the associated load balancing rules from this load balancer, indexed by their names */
    Map<String, LoadBalancingRule> loadBalancingRules();
}

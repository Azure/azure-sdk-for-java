/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.model;

import java.util.Map;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.LoadBalancingRule;

/**
 * An interface representing a model's ability to reference load balancing rules.
 */
@Fluent()
public interface HasLoadBalancingRules  {
    /**
     * @return the associated load balancing rules from this load balancer, indexed by their names
     */
    Map<String, LoadBalancingRule> loadBalancingRules();
}

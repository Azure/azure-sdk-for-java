/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network.models;

import java.util.Map;

import com.azure.core.annotation.Fluent;
import com.azure.management.network.LoadBalancingRule;

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

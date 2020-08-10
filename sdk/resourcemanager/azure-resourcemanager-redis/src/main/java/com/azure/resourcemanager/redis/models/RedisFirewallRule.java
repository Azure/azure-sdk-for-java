/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.resourcemanager.redis.models;

import com.azure.resourcemanager.redis.fluent.inner.RedisFirewallRuleInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ExternalChildResource;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;

/**
 * The Azure Redis Firewall rule entries are of type RedisFirewallRule.
 */
public interface RedisFirewallRule extends
    ExternalChildResource<RedisFirewallRule, RedisCache>,
    HasInner<RedisFirewallRuleInner> {
    /**
     * Get the name value.
     *
     * @return the name value
     */
    String name();

    /**
     * Get the startIP value.
     *
     * @return the startIP value
     */
    String startIP();

    /**
     * Get the endIP value.
     *
     * @return the endIP value
     */
    String endIP();
}

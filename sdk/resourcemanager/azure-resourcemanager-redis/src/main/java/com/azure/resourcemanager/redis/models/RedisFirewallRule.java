// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.redis.models;

import com.azure.resourcemanager.redis.fluent.inner.RedisFirewallRuleInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ExternalChildResource;
import com.azure.resourcemanager.resources.fluentcore.model.HasInner;

/** The Azure Redis Firewall rule entries are of type RedisFirewallRule. */
public interface RedisFirewallRule
    extends ExternalChildResource<RedisFirewallRule, RedisCache>, HasInner<RedisFirewallRuleInner> {
    /**
     * Get the name value.
     *
     * @return the name value
     */
    String name();

    /**
     * Get the startIp value.
     *
     * @return the startIp value
     */
    String startIp();

    /**
     * Get the endIp value.
     *
     * @return the endIp value
     */
    String endIp();
}

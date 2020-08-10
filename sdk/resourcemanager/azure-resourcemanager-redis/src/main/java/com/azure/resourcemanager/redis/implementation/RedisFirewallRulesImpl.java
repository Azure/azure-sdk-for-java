/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.resourcemanager.redis.implementation;

import com.azure.resourcemanager.redis.fluent.inner.RedisFirewallRuleInner;
import com.azure.resourcemanager.redis.models.RedisCache;
import com.azure.resourcemanager.redis.models.RedisFirewallRule;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.ExternalChildResourcesCachedImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a Redis firewall rules collection associated with a Redis cache instance.
 */
class RedisFirewallRulesImpl extends
    ExternalChildResourcesCachedImpl<RedisFirewallRuleImpl,
        RedisFirewallRule,
        RedisFirewallRuleInner,
                RedisCacheImpl,
        RedisCache> {

    RedisFirewallRulesImpl(RedisCacheImpl parent) {
        super(parent, parent.taskGroup(), "FirewallRule");
        if (parent.id() != null) {
            this.cacheCollection();
        }
    }

    Map<String, RedisFirewallRule> rulesAsMap() {
        Map<String, RedisFirewallRule> result = new HashMap<>();
        for (Map.Entry<String, RedisFirewallRuleImpl> entry : this.collection().entrySet()) {
            RedisFirewallRuleImpl endpoint = entry.getValue();
            result.put(entry.getKey(), endpoint);
        }
        return Collections.unmodifiableMap(result);
    }

    public void addRule(RedisFirewallRuleImpl rule) {
        this.addChildResource(rule);
    }

    public void removeRule(String name) {
        this.prepareInlineRemove(name);
    }

    public RedisFirewallRuleImpl defineInlineFirewallRule(String name) {
        return prepareInlineDefine(name);
    }

    @Override
    protected List<RedisFirewallRuleImpl> listChildResources() {
        List<RedisFirewallRuleImpl> childResources = new ArrayList<>();
        for (RedisFirewallRuleInner firewallRule : this.getParent().manager().inner().getFirewallRules().listByRedisResource(
                this.getParent().resourceGroupName(),
                this.getParent().name())) {
            childResources.add(new RedisFirewallRuleImpl(firewallRule.name(), this.getParent(), firewallRule));
        }
        return Collections.unmodifiableList(childResources);
    }

    @Override
    protected RedisFirewallRuleImpl newChildResource(String name) {
        return new RedisFirewallRuleImpl(name, this.getParent(), new RedisFirewallRuleInner());
    }
}

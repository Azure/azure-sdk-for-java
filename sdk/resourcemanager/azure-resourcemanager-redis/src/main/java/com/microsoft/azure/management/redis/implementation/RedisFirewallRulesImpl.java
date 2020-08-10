/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.redis.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.redis.RedisCache;
import com.microsoft.azure.management.redis.RedisFirewallRule;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ExternalChildResourcesCachedImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a Redis firewall rules collection associated with a Redis cache instance.
 */
@LangDefinition
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
        for (RedisFirewallRuleInner firewallRule : this.parent().manager().inner().firewallRules().listByRedisResource(
                this.parent().resourceGroupName(),
                this.parent().name())) {
            childResources.add(new RedisFirewallRuleImpl(firewallRule.name(), this.parent(), firewallRule));
        }
        return Collections.unmodifiableList(childResources);
    }

    @Override
    protected RedisFirewallRuleImpl newChildResource(String name) {
        return new RedisFirewallRuleImpl(name, this.parent(), new RedisFirewallRuleInner());
    }
}

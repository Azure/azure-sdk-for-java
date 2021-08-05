// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.redis.implementation;

import com.azure.resourcemanager.redis.fluent.models.RedisFirewallRuleInner;
import com.azure.resourcemanager.redis.models.RedisCache;
import com.azure.resourcemanager.redis.models.RedisFirewallRule;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.ExternalChildResourcesCachedImpl;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Represents a Redis firewall rules collection associated with a Redis cache instance. */
class RedisFirewallRulesImpl
    extends ExternalChildResourcesCachedImpl<
        RedisFirewallRuleImpl, RedisFirewallRule, RedisFirewallRuleInner, RedisCacheImpl, RedisCache> {
    private boolean load = false;

    RedisFirewallRulesImpl(RedisCacheImpl parent) {
        super(parent, parent.taskGroup(), "FirewallRule");
    }

    void ensureCollectionLoaded() {
        if (!load) {
            load = true;
            cacheCollection();
        }
    }

    Map<String, RedisFirewallRule> rulesAsMap() {
        ensureCollectionLoaded();
        Map<String, RedisFirewallRule> result = new HashMap<>();
        for (Map.Entry<String, RedisFirewallRuleImpl> entry : this.collection().entrySet()) {
            RedisFirewallRuleImpl endpoint = entry.getValue();
            result.put(entry.getKey(), endpoint);
        }
        return Collections.unmodifiableMap(result);
    }

    public void addRule(RedisFirewallRuleImpl rule) {
        ensureCollectionLoaded();
        this.addChildResource(rule);
    }

    public void removeRule(String name) {
        ensureCollectionLoaded();
        this.prepareInlineRemove(name);
    }

    public RedisFirewallRuleImpl defineInlineFirewallRule(String name) {
        ensureCollectionLoaded();
        return prepareInlineDefine(name);
    }

    @Override
    protected Flux<RedisFirewallRuleImpl> listChildResourcesAsync() {
        return this
            .getParent()
            .manager()
            .serviceClient()
            .getFirewallRules()
            .listAsync(this.getParent().resourceGroupName(), this.getParent().name())
            .map(
                firewallRuleInner ->
                    new RedisFirewallRuleImpl(firewallRuleInner.name(), this.getParent(), firewallRuleInner))
            .onErrorResume(e -> Mono.empty());
    }

    @Override
    protected List<RedisFirewallRuleImpl> listChildResources() {
        return listChildResourcesAsync().collectList().block();
    }

    @Override
    protected RedisFirewallRuleImpl newChildResource(String name) {
        return new RedisFirewallRuleImpl(name, this.getParent(), new RedisFirewallRuleInner());
    }
}

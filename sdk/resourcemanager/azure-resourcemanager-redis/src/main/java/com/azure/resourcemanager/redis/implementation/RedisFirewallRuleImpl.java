// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.redis.implementation;

import com.azure.resourcemanager.redis.fluent.inner.RedisFirewallRuleInner;
import com.azure.resourcemanager.redis.models.RedisCache;
import com.azure.resourcemanager.redis.models.RedisFirewallRule;
import com.azure.resourcemanager.redis.models.RedisFirewallRuleCreateParameters;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import reactor.core.publisher.Mono;

/** The Azure {@link RedisFirewallRule} wrapper class implementation. */
class RedisFirewallRuleImpl
    extends ExternalChildResourceImpl<RedisFirewallRule, RedisFirewallRuleInner, RedisCacheImpl, RedisCache>
    implements RedisFirewallRule {

    RedisFirewallRuleImpl(String name, RedisCacheImpl parent, RedisFirewallRuleInner innerObject) {
        super(getChildName(name, parent.name()), parent, innerObject);
    }

    @Override
    public String id() {
        return this.inner().id();
    }

    @Override
    public String startIp() {
        return this.inner().startIp();
    }

    @Override
    public String endIp() {
        return this.inner().endIp();
    }

    @Override
    public Mono<RedisFirewallRule> createResourceAsync() {
        final RedisFirewallRuleImpl self = this;
        RedisFirewallRuleCreateParameters parameters =
            new RedisFirewallRuleCreateParameters().withStartIp(this.startIp()).withEndIp(this.endIp());
        return this
            .parent()
            .manager()
            .inner()
            .getFirewallRules()
            .createOrUpdateAsync(this.parent().resourceGroupName(), this.parent().name(), this.name(), parameters)
            .map(
                redisFirewallRuleInner -> {
                    self.setInner(redisFirewallRuleInner);
                    return self;
                });
    }

    @Override
    public Mono<RedisFirewallRule> updateResourceAsync() {
        return this.createResourceAsync();
    }

    @Override
    public Mono<Void> deleteResourceAsync() {
        return this
            .parent()
            .manager()
            .inner()
            .getFirewallRules()
            .deleteAsync(this.parent().resourceGroupName(), this.parent().name(), this.name());
    }

    @Override
    protected Mono<RedisFirewallRuleInner> getInnerAsync() {
        return this
            .parent()
            .manager()
            .inner()
            .getFirewallRules()
            .getAsync(this.parent().resourceGroupName(), this.parent().name(), this.name());
    }

    private static String getChildName(String name, String parentName) {
        if (name != null && name.contains("/")) {
            // rule name consist of "parent/child" name syntax but delete/update/get should be called only on child name
            return name.substring(parentName.length() + 1);
        }
        return name;
    }
}

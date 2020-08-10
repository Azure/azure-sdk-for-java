/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.redis.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.redis.RedisCache;
import com.microsoft.azure.management.redis.RedisFirewallRule;
import com.microsoft.azure.management.redis.RedisFirewallRuleCreateParameters;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import rx.Observable;
import rx.functions.Func1;

/**
 * The Azure {@link RedisFirewallRule} wrapper class implementation.
 */
@LangDefinition
class RedisFirewallRuleImpl extends
        ExternalChildResourceImpl<RedisFirewallRule,
                RedisFirewallRuleInner,
                RedisCacheImpl,
                RedisCache>
        implements RedisFirewallRule {

    RedisFirewallRuleImpl(String name, RedisCacheImpl parent, RedisFirewallRuleInner innerObject) {
        super(getChildName(name, parent.name()), parent, innerObject);
    }

    @Override
    public String id() {
        return this.inner().id();
    }

    @Override
    public String startIP() {
        return this.inner().startIP();
    }

    @Override
    public String endIP() {
        return this.inner().endIP();
    }

    @Override
    public Observable<RedisFirewallRule> createResourceAsync() {
        final RedisFirewallRuleImpl self = this;
        RedisFirewallRuleCreateParameters parameters = new RedisFirewallRuleCreateParameters()
                .withStartIP(this.startIP())
                .withEndIP(this.endIP());
        return this.parent().manager().inner().firewallRules().createOrUpdateAsync(
                this.parent().resourceGroupName(),
                this.parent().name(),
                this.name(),
                parameters)
                .map(new Func1<RedisFirewallRuleInner, RedisFirewallRule>() {
                    @Override
                    public RedisFirewallRule call(RedisFirewallRuleInner redisFirewallRuleInner) {
                        self.setInner(redisFirewallRuleInner);
                        return self;
                    }
                });
    }

    @Override
    public Observable<RedisFirewallRule> updateResourceAsync() {
        return this.createResourceAsync();
    }

    @Override
    public Observable<Void> deleteResourceAsync() {
        return this.parent().manager().inner().firewallRules().deleteAsync(this.parent().resourceGroupName(),
                this.parent().name(),
                this.name());
    }

    @Override
    protected Observable<RedisFirewallRuleInner> getInnerAsync() {
        return this.parent().manager().inner().firewallRules().getAsync(this.parent().resourceGroupName(),
                this.parent().name(),
                this.name());
    }

    private static String getChildName(String name, String parentName) {
        if (name != null
                && name.indexOf("/") != -1) {
            // rule name consist of "parent/child" name syntax but delete/update/get should be called only on child name
            return name.substring(parentName.length() + 1);
        }
        return name;
    }
}

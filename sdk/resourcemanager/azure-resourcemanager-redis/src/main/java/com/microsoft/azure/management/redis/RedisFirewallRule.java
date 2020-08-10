/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.redis;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.apigeneration.LangMethodDefinition;
import com.microsoft.azure.management.redis.implementation.RedisFirewallRuleInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ExternalChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;

/**
 * The Azure Redis Firewall rule entries are of type RedisFirewallRule.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.Redis.Fluent.Models")
@Beta(Beta.SinceVersion.V1_12_0)
public interface RedisFirewallRule extends
        ExternalChildResource<RedisFirewallRule, RedisCache>,
        HasInner<RedisFirewallRuleInner> {
    /**
     * Get the name value.
     *
     * @return the name value
     */
    @LangMethodDefinition(AsType = LangMethodDefinition.LangMethodType.Property)
    String name();

    /**
     * Get the startIP value.
     *
     * @return the startIP value
     */
    @LangMethodDefinition(AsType = LangMethodDefinition.LangMethodType.Property)
    String startIP();

    /**
     * Get the endIP value.
     *
     * @return the endIP value
     */
    String endIP();
}

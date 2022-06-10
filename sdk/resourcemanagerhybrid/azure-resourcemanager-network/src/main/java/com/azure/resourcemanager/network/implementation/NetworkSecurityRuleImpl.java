// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.network.models.NetworkSecurityGroup;
import com.azure.resourcemanager.network.models.NetworkSecurityRule;
import com.azure.resourcemanager.network.models.SecurityRuleAccess;
import com.azure.resourcemanager.network.models.SecurityRuleDirection;
import com.azure.resourcemanager.network.models.SecurityRuleProtocol;
import com.azure.resourcemanager.network.fluent.models.ApplicationSecurityGroupInner;
import com.azure.resourcemanager.network.fluent.models.SecurityRuleInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ChildResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Implementation for {@link NetworkSecurityRule} and its create and update interfaces. */
class NetworkSecurityRuleImpl
    extends ChildResourceImpl<SecurityRuleInner, NetworkSecurityGroupImpl, NetworkSecurityGroup>
    implements NetworkSecurityRule,
        NetworkSecurityRule.Definition<NetworkSecurityGroup.DefinitionStages.WithCreate>,
        NetworkSecurityRule.UpdateDefinition<NetworkSecurityGroup.Update>,
        NetworkSecurityRule.Update {
    private Map<String, ApplicationSecurityGroupInner> sourceAsgs = new HashMap<>();
    private Map<String, ApplicationSecurityGroupInner> destinationAsgs = new HashMap<>();
    private final ClientLogger logger = new ClientLogger(getClass());

    NetworkSecurityRuleImpl(SecurityRuleInner inner, NetworkSecurityGroupImpl parent) {
        super(inner, parent);
        if (inner.sourceApplicationSecurityGroups() != null) {
            for (ApplicationSecurityGroupInner asg : inner.sourceApplicationSecurityGroups()) {
                sourceAsgs.put(asg.id(), asg);
            }
        }
        if (inner.destinationApplicationSecurityGroups() != null) {
            for (ApplicationSecurityGroupInner asg : inner.destinationApplicationSecurityGroups()) {
                destinationAsgs.put(asg.id(), asg);
            }
        }
    }

    // Getters

    @Override
    public String name() {
        return this.innerModel().name();
    }

    @Override
    public SecurityRuleDirection direction() {
        return this.innerModel().direction();
    }

    @Override
    public SecurityRuleProtocol protocol() {
        return this.innerModel().protocol();
    }

    @Override
    public SecurityRuleAccess access() {
        return this.innerModel().access();
    }

    @Override
    public String sourceAddressPrefix() {
        return this.innerModel().sourceAddressPrefix();
    }

    @Override
    public List<String> sourceAddressPrefixes() {
        return Collections.unmodifiableList(this.innerModel().sourceAddressPrefixes());
    }

    @Override
    public String sourcePortRange() {
        return this.innerModel().sourcePortRange();
    }

    @Override
    public List<String> sourcePortRanges() {
        return Collections.unmodifiableList(innerModel().sourcePortRanges());
    }

    @Override
    public String destinationAddressPrefix() {
        return this.innerModel().destinationAddressPrefix();
    }

    @Override
    public List<String> destinationAddressPrefixes() {
        return Collections.unmodifiableList(this.innerModel().destinationAddressPrefixes());
    }

    @Override
    public String destinationPortRange() {
        return this.innerModel().destinationPortRange();
    }

    @Override
    public List<String> destinationPortRanges() {
        return Collections.unmodifiableList(innerModel().destinationPortRanges());
    }

    @Override
    public int priority() {
        return ResourceManagerUtils.toPrimitiveInt(this.innerModel().priority());
    }

    @Override
    public Set<String> sourceApplicationSecurityGroupIds() {
        return Collections.unmodifiableSet(sourceAsgs.keySet());
    }

    @Override
    public Set<String> destinationApplicationSecurityGroupIds() {
        return Collections.unmodifiableSet(destinationAsgs.keySet());
    }

    // Fluent setters

    @Override
    public NetworkSecurityRuleImpl allowInbound() {
        return this.withDirection(SecurityRuleDirection.INBOUND).withAccess(SecurityRuleAccess.ALLOW);
    }

    @Override
    public NetworkSecurityRuleImpl allowOutbound() {
        return this.withDirection(SecurityRuleDirection.OUTBOUND).withAccess(SecurityRuleAccess.ALLOW);
    }

    @Override
    public NetworkSecurityRuleImpl denyInbound() {
        return this.withDirection(SecurityRuleDirection.INBOUND).withAccess(SecurityRuleAccess.DENY);
    }

    @Override
    public NetworkSecurityRuleImpl denyOutbound() {
        return this.withDirection(SecurityRuleDirection.OUTBOUND).withAccess(SecurityRuleAccess.DENY);
    }

    @Override
    public NetworkSecurityRuleImpl withProtocol(SecurityRuleProtocol protocol) {
        this.innerModel().withProtocol(protocol);
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl withAnyProtocol() {
        return this.withProtocol(SecurityRuleProtocol.ASTERISK);
    }

    @Override
    public NetworkSecurityRuleImpl fromAddress(String cidr) {
        this.innerModel().withSourceAddressPrefix(cidr);
        this.innerModel().withSourceAddressPrefixes(null);
        this.innerModel().withSourceApplicationSecurityGroups(null);
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl fromAnyAddress() {
        this.innerModel().withSourceAddressPrefix("*");
        this.innerModel().withSourceAddressPrefixes(null);
        this.innerModel().withSourceApplicationSecurityGroups(null);
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl fromAddresses(String... addresses) {
        this.innerModel().withSourceAddressPrefixes(Arrays.asList(addresses));
        this.innerModel().withSourceAddressPrefix(null);
        this.innerModel().withSourceApplicationSecurityGroups(null);
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl fromPort(int port) {
        this.innerModel().withSourcePortRange(String.valueOf(port));
        this.innerModel().withSourcePortRanges(null);
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl fromAnyPort() {
        this.innerModel().withSourcePortRange("*");
        this.innerModel().withSourcePortRanges(null);
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl fromPortRange(int from, int to) {
        this.innerModel().withSourcePortRange(String.valueOf(from) + "-" + String.valueOf(to));
        this.innerModel().withSourcePortRanges(null);
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl fromPortRanges(String... ranges) {
        this.innerModel().withSourcePortRanges(Arrays.asList(ranges));
        this.innerModel().withSourcePortRange(null);
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl toAddress(String cidr) {
        this.innerModel().withDestinationAddressPrefix(cidr);
        this.innerModel().withDestinationAddressPrefixes(null);
        this.innerModel().withDestinationApplicationSecurityGroups(null);
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl toAddresses(String... addresses) {
        this.innerModel().withDestinationAddressPrefixes(Arrays.asList(addresses));
        this.innerModel().withDestinationAddressPrefix(null);
        this.innerModel().withDestinationApplicationSecurityGroups(null);
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl toAnyAddress() {
        this.innerModel().withDestinationAddressPrefix("*");
        this.innerModel().withDestinationAddressPrefixes(null);
        this.innerModel().withDestinationApplicationSecurityGroups(null);
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl toPort(int port) {
        this.innerModel().withDestinationPortRange(String.valueOf(port));
        this.innerModel().withDestinationPortRanges(null);
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl toAnyPort() {
        this.innerModel().withDestinationPortRange("*");
        this.innerModel().withDestinationPortRanges(null);
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl toPortRange(int from, int to) {
        this.innerModel().withDestinationPortRange(String.valueOf(from) + "-" + String.valueOf(to));
        this.innerModel().withDestinationPortRanges(null);
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl toPortRanges(String... ranges) {
        this.innerModel().withDestinationPortRanges(Arrays.asList(ranges));
        this.innerModel().withDestinationPortRange(null);
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl withPriority(int priority) {
        if (priority < 100 || priority > 4096) {
            throw logger
                .logExceptionAsError(
                    new IllegalArgumentException(
                        "The priority number of a network security rule must be between 100 and 4096."));
        }

        this.innerModel().withPriority(priority);
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl withDescription(String description) {
        this.innerModel().withDescription(description);
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl withSourceApplicationSecurityGroup(String id) {
        sourceAsgs.put(id, new ApplicationSecurityGroupInner().withId(id));
        innerModel().withSourceAddressPrefix(null);
        innerModel().withSourceAddressPrefixes(null);
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl withoutSourceApplicationSecurityGroup(String id) {
        sourceAsgs.remove(id);
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl withSourceApplicationSecurityGroup(String... ids) {
        sourceAsgs = Arrays.stream(ids)
            .collect(Collectors.toMap(Function.identity(), id -> new ApplicationSecurityGroupInner().withId(id)));
        innerModel().withSourceAddressPrefix(null);
        innerModel().withSourceAddressPrefixes(null);
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl withDestinationApplicationSecurityGroup(String id) {
        destinationAsgs.put(id, new ApplicationSecurityGroupInner().withId(id));
        innerModel().withDestinationAddressPrefix(null);
        innerModel().withDestinationAddressPrefixes(null);
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl withoutDestinationApplicationSecurityGroup(String id) {
        destinationAsgs.remove(id);
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl withDestinationApplicationSecurityGroup(String... ids) {
        destinationAsgs = Arrays.stream(ids)
            .collect(Collectors.toMap(Function.identity(), id -> new ApplicationSecurityGroupInner().withId(id)));
        innerModel().withDestinationAddressPrefix(null);
        innerModel().withDestinationAddressPrefixes(null);
        return this;
    }

    // Helpers

    private NetworkSecurityRuleImpl withDirection(SecurityRuleDirection direction) {
        this.innerModel().withDirection(direction);
        return this;
    }

    private NetworkSecurityRuleImpl withAccess(SecurityRuleAccess permission) {
        this.innerModel().withAccess(permission);
        return this;
    }

    // Verbs

    @Override
    public NetworkSecurityGroupImpl attach() {
        return this.parent().withRule(this);
    }

    @Override
    public NetworkSecurityGroupImpl parent() {
        innerModel().withSourceApplicationSecurityGroups(new ArrayList<>(sourceAsgs.values()));
        innerModel().withDestinationApplicationSecurityGroups(new ArrayList<>(destinationAsgs.values()));
        return super.parent();
    }

    @Override
    public String description() {
        return this.innerModel().description();
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.network.models.NetworkSecurityGroup;
import com.azure.resourcemanager.network.models.NetworkSecurityRule;
import com.azure.resourcemanager.network.models.SecurityRuleAccess;
import com.azure.resourcemanager.network.models.SecurityRuleDirection;
import com.azure.resourcemanager.network.models.SecurityRuleProtocol;
import com.azure.resourcemanager.network.fluent.inner.ApplicationSecurityGroupInner;
import com.azure.resourcemanager.network.fluent.inner.SecurityRuleInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ChildResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        return this.inner().name();
    }

    @Override
    public SecurityRuleDirection direction() {
        return this.inner().direction();
    }

    @Override
    public SecurityRuleProtocol protocol() {
        return this.inner().protocol();
    }

    @Override
    public SecurityRuleAccess access() {
        return this.inner().access();
    }

    @Override
    public String sourceAddressPrefix() {
        return this.inner().sourceAddressPrefix();
    }

    @Override
    public List<String> sourceAddressPrefixes() {
        return Collections.unmodifiableList(this.inner().sourceAddressPrefixes());
    }

    @Override
    public String sourcePortRange() {
        return this.inner().sourcePortRange();
    }

    @Override
    public List<String> sourcePortRanges() {
        return Collections.unmodifiableList(inner().sourcePortRanges());
    }

    @Override
    public String destinationAddressPrefix() {
        return this.inner().destinationAddressPrefix();
    }

    @Override
    public List<String> destinationAddressPrefixes() {
        return Collections.unmodifiableList(this.inner().destinationAddressPrefixes());
    }

    @Override
    public String destinationPortRange() {
        return this.inner().destinationPortRange();
    }

    @Override
    public List<String> destinationPortRanges() {
        return Collections.unmodifiableList(inner().destinationPortRanges());
    }

    @Override
    public int priority() {
        return Utils.toPrimitiveInt(this.inner().priority());
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
        this.inner().withProtocol(protocol);
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl withAnyProtocol() {
        return this.withProtocol(SecurityRuleProtocol.ASTERISK);
    }

    @Override
    public NetworkSecurityRuleImpl fromAddress(String cidr) {
        this.inner().withSourceAddressPrefix(cidr);
        this.inner().withSourceAddressPrefixes(null);
        this.inner().withSourceApplicationSecurityGroups(null);
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl fromAnyAddress() {
        this.inner().withSourceAddressPrefix("*");
        this.inner().withSourceAddressPrefixes(null);
        this.inner().withSourceApplicationSecurityGroups(null);
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl fromAddresses(String... addresses) {
        this.inner().withSourceAddressPrefixes(Arrays.asList(addresses));
        this.inner().withSourceAddressPrefix(null);
        this.inner().withSourceApplicationSecurityGroups(null);
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl fromPort(int port) {
        this.inner().withSourcePortRange(String.valueOf(port));
        this.inner().withSourcePortRanges(null);
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl fromAnyPort() {
        this.inner().withSourcePortRange("*");
        this.inner().withSourcePortRanges(null);
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl fromPortRange(int from, int to) {
        this.inner().withSourcePortRange(String.valueOf(from) + "-" + String.valueOf(to));
        this.inner().withSourcePortRanges(null);
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl fromPortRanges(String... ranges) {
        this.inner().withSourcePortRanges(Arrays.asList(ranges));
        this.inner().withSourcePortRange(null);
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl toAddress(String cidr) {
        this.inner().withDestinationAddressPrefix(cidr);
        this.inner().withDestinationAddressPrefixes(null);
        this.inner().withDestinationApplicationSecurityGroups(null);
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl toAddresses(String... addresses) {
        this.inner().withDestinationAddressPrefixes(Arrays.asList(addresses));
        this.inner().withDestinationAddressPrefix(null);
        this.inner().withDestinationApplicationSecurityGroups(null);
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl toAnyAddress() {
        this.inner().withDestinationAddressPrefix("*");
        this.inner().withDestinationAddressPrefixes(null);
        this.inner().withDestinationApplicationSecurityGroups(null);
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl toPort(int port) {
        this.inner().withDestinationPortRange(String.valueOf(port));
        this.inner().withDestinationPortRanges(null);
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl toAnyPort() {
        this.inner().withDestinationPortRange("*");
        this.inner().withDestinationPortRanges(null);
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl toPortRange(int from, int to) {
        this.inner().withDestinationPortRange(String.valueOf(from) + "-" + String.valueOf(to));
        this.inner().withDestinationPortRanges(null);
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl toPortRanges(String... ranges) {
        this.inner().withDestinationPortRanges(Arrays.asList(ranges));
        this.inner().withDestinationPortRange(null);
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl withPriority(int priority) {
        if (priority < 100 || priority > 4096) {
            throw logger.logExceptionAsError(new IllegalArgumentException(
                "The priority number of a network security rule must be between 100 and 4096."));
        }

        this.inner().withPriority(priority);
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl withDescription(String description) {
        this.inner().withDescription(description);
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl withSourceApplicationSecurityGroup(String id) {
        sourceAsgs.put(id, new ApplicationSecurityGroupInner().withId(id));
        inner().withSourceAddressPrefix(null);
        inner().withSourceAddressPrefixes(null);
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl withDestinationApplicationSecurityGroup(String id) {
        destinationAsgs.put(id, new ApplicationSecurityGroupInner().withId(id));
        inner().withDestinationAddressPrefix(null);
        inner().withDestinationAddressPrefixes(null);
        return this;
    }

    // Helpers

    private NetworkSecurityRuleImpl withDirection(SecurityRuleDirection direction) {
        this.inner().withDirection(direction);
        return this;
    }

    private NetworkSecurityRuleImpl withAccess(SecurityRuleAccess permission) {
        this.inner().withAccess(permission);
        return this;
    }

    // Verbs

    @Override
    public NetworkSecurityGroupImpl attach() {
        inner().withSourceApplicationSecurityGroups(new ArrayList<>(sourceAsgs.values()));
        inner().withDestinationApplicationSecurityGroups(new ArrayList<>(destinationAsgs.values()));
        return this.parent().withRule(this);
    }

    @Override
    public String description() {
        return this.inner().description();
    }
}

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.network.NetworkSecurityGroup;
import com.microsoft.azure.management.network.NetworkSecurityRule;
import com.microsoft.azure.management.network.implementation.api.SecurityRuleInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

/**
 * Implementation of the NetworkSecurityRule interface.
 */
class NetworkSecurityRuleImpl
    extends ChildResourceImpl<SecurityRuleInner, NetworkSecurityGroupImpl>
    implements
        NetworkSecurityRule,
        NetworkSecurityRule.Definitions<NetworkSecurityGroup.DefinitionCreatable> {

    protected NetworkSecurityRuleImpl(String name, SecurityRuleInner inner, NetworkSecurityGroupImpl parent) {
        super(name, inner, parent);
    }

    // Getters

    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public Direction direction() {
        return Direction.fromString(this.inner().direction());
    }

    @Override
    public Protocol protocol() {
        return Protocol.fromString(this.inner().protocol());
    }

    @Override
    public Access access() {
        return Access.fromString(this.inner().access());
    }

    @Override
    public String sourceAddressPrefix() {
        return this.inner().sourceAddressPrefix();
    }

    @Override
    public String sourcePortRange() {
        return this.inner().sourcePortRange();
    }

    @Override
    public String destinationAddressPrefix() {
        return this.inner().destinationAddressPrefix();
    }

    @Override
    public String destinationPortRange() {
        return this.inner().destinationPortRange();
    }

    @Override
    public int priority() {
        return this.inner().priority();
    }

    // Fluent setters

    @Override
    public NetworkSecurityRuleImpl allowInbound() {
        return this
                .withDirection(Direction.INBOUND)
                .withAccess(Access.ALLOW);
    }

    @Override
    public NetworkSecurityRuleImpl allowOutbound() {
        return this
                .withDirection(Direction.OUTBOUND)
                .withAccess(Access.ALLOW);
    }

    @Override
    public NetworkSecurityRuleImpl denyInbound() {
        return this
                .withDirection(Direction.INBOUND)
                .withAccess(Access.DENY);
    }

    @Override
    public NetworkSecurityRuleImpl denyOutbound() {
        return this
                .withDirection(Direction.OUTBOUND)
                .withAccess(Access.DENY);
    }

    @Override
    public NetworkSecurityRuleImpl withProtocol(Protocol protocol) {
        this.inner().setProtocol(protocol.toString());
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl withAnyProtocol() {
        return this.withProtocol(Protocol.ANY);
    }

    @Override
    public NetworkSecurityRuleImpl fromAddress(String cidr) {
        this.inner().setSourceAddressPrefix(cidr);
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl fromAnyAddress() {
        this.inner().setSourceAddressPrefix("*");
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl fromPort(int port) {
        this.inner().setSourcePortRange(String.valueOf(port));
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl fromAnyPort() {
        this.inner().setSourcePortRange("*");
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl fromPortRange(int from, int to) {
        this.inner().setSourcePortRange(String.valueOf(from) + "-" + String.valueOf(to));
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl toAddress(String cidr) {
        this.inner().setDestinationAddressPrefix(cidr);
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl toAnyAddress() {
        this.inner().setDestinationAddressPrefix("*");
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl toPort(int port) {
        this.inner().setDestinationPortRange(String.valueOf(port));
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl toAnyPort() {
        this.inner().setDestinationPortRange("*");
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl toPortRange(int from, int to) {
        this.inner().setDestinationPortRange(String.valueOf(from) + "-" + String.valueOf(to));
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl  withPriority(int priority) {
        if (priority < 100 || priority > 4096) {
            throw new IllegalArgumentException("The priority number of a network security rule must be between 100 and 4096.");
        }

        this.inner().setPriority(priority);
        return this;
    }


    // Helpers

    private NetworkSecurityRuleImpl withDirection(Direction direction) {
        this.inner().setDirection(direction.toString());
        return this;
    }

    private NetworkSecurityRuleImpl withAccess(Access permission) {
        this.inner().setAccess(permission.toString());
        return this;
    }


    // Verbs

    @Override
    public NetworkSecurityGroupImpl attach() {
        this.parent().inner().securityRules().add(this.inner());
        return this.parent();
    }
}

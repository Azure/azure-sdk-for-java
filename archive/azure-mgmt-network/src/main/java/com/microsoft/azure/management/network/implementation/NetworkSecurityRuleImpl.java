/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.NetworkSecurityGroup;
import com.microsoft.azure.management.network.NetworkSecurityRule;
import com.microsoft.azure.management.network.SecurityRuleAccess;
import com.microsoft.azure.management.network.SecurityRuleDirection;
import com.microsoft.azure.management.network.SecurityRuleProtocol;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;

/**
 *  Implementation for {@link NetworkSecurityRule} and its create and update interfaces.
 */
@LangDefinition
class NetworkSecurityRuleImpl
    extends ChildResourceImpl<SecurityRuleInner, NetworkSecurityGroupImpl, NetworkSecurityGroup>
    implements
        NetworkSecurityRule,
        NetworkSecurityRule.Definition<NetworkSecurityGroup.DefinitionStages.WithCreate>,
        NetworkSecurityRule.UpdateDefinition<NetworkSecurityGroup.Update>,
        NetworkSecurityRule.Update {

    NetworkSecurityRuleImpl(SecurityRuleInner inner, NetworkSecurityGroupImpl parent) {
        super(inner, parent);
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
        return Utils.toPrimitiveInt(this.inner().priority());
    }

    // Fluent setters

    @Override
    public NetworkSecurityRuleImpl allowInbound() {
        return this
                .withDirection(SecurityRuleDirection.INBOUND)
                .withAccess(SecurityRuleAccess.ALLOW);
    }

    @Override
    public NetworkSecurityRuleImpl allowOutbound() {
        return this
                .withDirection(SecurityRuleDirection.OUTBOUND)
                .withAccess(SecurityRuleAccess.ALLOW);
    }

    @Override
    public NetworkSecurityRuleImpl denyInbound() {
        return this
                .withDirection(SecurityRuleDirection.INBOUND)
                .withAccess(SecurityRuleAccess.DENY);
    }

    @Override
    public NetworkSecurityRuleImpl denyOutbound() {
        return this
                .withDirection(SecurityRuleDirection.OUTBOUND)
                .withAccess(SecurityRuleAccess.DENY);
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
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl fromAnyAddress() {
        this.inner().withSourceAddressPrefix("*");
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl fromPort(int port) {
        this.inner().withSourcePortRange(String.valueOf(port));
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl fromAnyPort() {
        this.inner().withSourcePortRange("*");
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl fromPortRange(int from, int to) {
        this.inner().withSourcePortRange(String.valueOf(from) + "-" + String.valueOf(to));
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl toAddress(String cidr) {
        this.inner().withDestinationAddressPrefix(cidr);
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl toAnyAddress() {
        this.inner().withDestinationAddressPrefix("*");
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl toPort(int port) {
        this.inner().withDestinationPortRange(String.valueOf(port));
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl toAnyPort() {
        this.inner().withDestinationPortRange("*");
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl toPortRange(int from, int to) {
        this.inner().withDestinationPortRange(String.valueOf(from) + "-" + String.valueOf(to));
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl  withPriority(int priority) {
        if (priority < 100 || priority > 4096) {
            throw new IllegalArgumentException("The priority number of a network security rule must be between 100 and 4096.");
        }

        this.inner().withPriority(priority);
        return this;
    }

    @Override
    public NetworkSecurityRuleImpl withDescription(String description) {
        this.inner().withDescription(description);
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
        return this.parent().withRule(this);
    }

    @Override
    public String description() {
        return this.inner().description();
    }
}

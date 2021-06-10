// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.network.fluent.models.SecurityRuleInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.ChildResource;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.HasInnerModel;
import com.azure.resourcemanager.resources.fluentcore.model.Settable;

import java.util.List;
import java.util.Set;

/** A network security rule in a network security group. */
@Fluent
public interface NetworkSecurityRule extends HasInnerModel<SecurityRuleInner>, ChildResource<NetworkSecurityGroup> {

    /** @return the direction of the network traffic that the network security rule applies to. */
    SecurityRuleDirection direction();

    /** @return the network protocol the rule applies to */
    SecurityRuleProtocol protocol();

    /** @return the user-defined description of the security rule */
    String description();

    /** @return the type of access the rule enforces */
    SecurityRuleAccess access();

    /**
     * @return the source address prefix the rule applies to, expressed using the CIDR notation in the format:
     *     "###.###.###.###/##", and "*" means "any"
     */
    String sourceAddressPrefix();

    /**
     * @return the list of source address prefixes the rule applies to, expressed using the CIDR notation in the format:
     *     "###.###.###.###/##", and "*" means "any", or IP addresses
     */
    List<String> sourceAddressPrefixes();

    /** @return the source port range that the rule applies to, in the format "##-##", where "*" means "any" */
    String sourcePortRange();

    /** @return the source port ranges that the rule applies to, in the format "##-##", where "*" means "any" */
    List<String> sourcePortRanges();

    /**
     * @return the destination address prefix the rule applies to, expressed using the CIDR notation in the format:
     *     "###.###.###.###/##", and "*" means "any"
     */
    String destinationAddressPrefix();

    /**
     * @return the list of destination address prefixes the rule applies to, expressed using the CIDR notation in the
     *     format: "###.###.###.###/##", and "*" means "any", or IP addresses
     */
    List<String> destinationAddressPrefixes();

    /** @return the destination port range that the rule applies to, in the format "##-##", where "*" means any */
    String destinationPortRange();

    /** @return the destination port ranges that the rule applies to, in the format "##-##", where "*" means any */
    List<String> destinationPortRanges();

    /**
     * @return the priority number of this rule based on which this rule will be applied relative to the priority
     *     numbers of any other rules specified for this network security group
     */
    int priority();

    /** @return list of application security group ids specified as source */
    Set<String> sourceApplicationSecurityGroupIds();

    /** @return list of application security group ids specified as destination */
    Set<String> destinationApplicationSecurityGroupIds();

    /**
     * The entirety of a network security rule definition.
     *
     * @param <ParentT> the return type of the final {@link Attachable#attach()}
     */
    interface Definition<ParentT>
        extends DefinitionStages.Blank<ParentT>,
            DefinitionStages.WithAttach<ParentT>,
            DefinitionStages.WithDirectionAccess<ParentT>,
            DefinitionStages.WithSourceAddressOrSecurityGroup<ParentT>,
            DefinitionStages.WithSourcePort<ParentT>,
            DefinitionStages.WithDestinationAddressOrSecurityGroup<ParentT>,
            DefinitionStages.WithDestinationPort<ParentT>,
            DefinitionStages.WithProtocol<ParentT> {
    }

    /** Grouping of security rule definition stages applicable as part of a network security group creation. */
    interface DefinitionStages {
        /**
         * The first stage of a security rule definition.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface Blank<ParentT> extends WithDirectionAccess<ParentT> {
        }

        /**
         * The stage of the security rule definition allowing the protocol that the rule applies to to be specified.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithProtocol<ParentT> {
            /**
             * Specifies the protocol that this rule applies to.
             *
             * @param protocol one of the supported protocols
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withProtocol(SecurityRuleProtocol protocol);

            /**
             * Makes this rule apply to any supported protocol.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withAnyProtocol();
        }

        /**
         * The stage of the network rule definition allowing the destination port(s) to be specified.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithDestinationPort<ParentT> {
            /**
             * Specifies the destination port to which this rule applies.
             *
             * @param port the destination port number
             * @return the next stage of the definition
             */
            WithProtocol<ParentT> toPort(int port);

            /**
             * Makes this rule apply to any destination port.
             *
             * @return the next stage of the definition
             */
            WithProtocol<ParentT> toAnyPort();

            /**
             * Specifies the destination port range to which this rule applies.
             *
             * @param from the starting port number
             * @param to the ending port number
             * @return the next stage of the definition
             */
            WithProtocol<ParentT> toPortRange(int from, int to);

            /**
             * Specifies the destination port ranges to which this rule applies.
             *
             * @param ranges the destination port ranges
             * @return the next stage of the definition
             */
            WithProtocol<ParentT> toPortRanges(String... ranges);
        }

        /**
         * The stage of the network rule definition allowing the destination address to be specified. Note: network
         * security rule must specify a non empty value for exactly one of: DestinationAddressPrefixes,
         * DestinationAddressPrefix, DestinationApplicationSecurityGroups.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithDestinationAddressOrSecurityGroup<ParentT> {
            /**
             * Specifies the traffic destination address range to which this rule applies.
             *
             * @param cidr an IP address range expressed in the CIDR notation
             * @return the next stage of the definition
             */
            WithDestinationPort<ParentT> toAddress(String cidr);

            /**
             * Specifies the traffic destination address prefixes to which this rule applies.
             *
             * @param addresses IP address prefixes in CIDR notation or IP addresses
             * @return the next stage of the definition
             */
            WithDestinationPort<ParentT> toAddresses(String... addresses);

            /**
             * Makes the rule apply to any traffic destination address.
             *
             * @return the next stage of the definition
             */
            WithDestinationPort<ParentT> toAnyAddress();

            /**
             * Sets the application security group specified as destination.
             *
             * @param id application security group id
             * @return the next stage of the definition
             */
            WithDestinationPort<ParentT> withDestinationApplicationSecurityGroup(String id);

            /**
             * Sets the application security group specified as destination.
             *
             * @param ids the collection of application security group ID
             * @return the next stage of the definition
             */
            WithDestinationPort<ParentT> withDestinationApplicationSecurityGroup(String... ids);
        }

        /**
         * The stage of the network rule definition allowing the source port(s) to be specified.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithSourcePort<ParentT> {
            /**
             * Specifies the source port to which this rule applies.
             *
             * @param port the source port number
             * @return the next stage of the definition
             */
            WithDestinationAddressOrSecurityGroup<ParentT> fromPort(int port);

            /**
             * Makes this rule apply to any source port.
             *
             * @return the next stage of the definition
             */
            WithDestinationAddressOrSecurityGroup<ParentT> fromAnyPort();

            /**
             * Specifies the source port range to which this rule applies.
             *
             * @param from the starting port number
             * @param to the ending port number
             * @return the next stage of the definition
             */
            WithDestinationAddressOrSecurityGroup<ParentT> fromPortRange(int from, int to);

            /**
             * Specifies the source port ranges to which this rule applies.
             *
             * @param ranges the starting port ranges
             * @return the next stage of the definition
             */
            WithDestinationAddressOrSecurityGroup<ParentT> fromPortRanges(String... ranges);
        }

        /**
         * The stage of the network rule definition allowing the source address to be specified. Note: network security
         * rule must specify a non empty value for exactly one of: SourceAddressPrefixes, SourceAddressPrefix,
         * SourceApplicationSecurityGroups.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithSourceAddressOrSecurityGroup<ParentT> {
            /**
             * Specifies the traffic source address prefix to which this rule applies.
             *
             * @param cidr an IP address prefix expressed in the CIDR notation
             * @return the next stage of the definition
             */
            WithSourcePort<ParentT> fromAddress(String cidr);

            /**
             * Specifies that the rule applies to any traffic source address.
             *
             * @return the next stage of the definition
             */
            WithSourcePort<ParentT> fromAnyAddress();

            /**
             * Specifies the traffic source address prefixes to which this rule applies.
             *
             * @param addresses IP address prefixes in CIDR notation or IP addresses
             * @return the next stage of the definition
             */
            WithSourcePort<ParentT> fromAddresses(String... addresses);

            /**
             * Sets the application security group specified as source.
             *
             * @param id application security group id
             * @return the next stage of the definition
             */
            WithSourcePort<ParentT> withSourceApplicationSecurityGroup(String id);

            /**
             * Sets the application security group specified as source.
             *
             * @param ids the collection of application security group ID
             * @return the next stage of the definition
             */
            WithSourcePort<ParentT> withSourceApplicationSecurityGroup(String... ids);
        }

        /**
         * The stage of the network rule definition allowing the direction and the access type to be specified.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithDirectionAccess<ParentT> {
            /**
             * Allows inbound traffic.
             *
             * @return the next stage of the definition
             */
            WithSourceAddressOrSecurityGroup<ParentT> allowInbound();

            /**
             * Allows outbound traffic.
             *
             * @return the next stage of the definition
             */
            WithSourceAddressOrSecurityGroup<ParentT> allowOutbound();

            /**
             * Blocks inbound traffic.
             *
             * @return the next stage of the definition
             */
            WithSourceAddressOrSecurityGroup<ParentT> denyInbound();

            /**
             * Blocks outbound traffic.
             *
             * @return the next stage of the definition
             */
            WithSourceAddressOrSecurityGroup<ParentT> denyOutbound();
        }

        /**
         * The stage of the network rule definition allowing the priority to be specified.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithPriority<ParentT> {
            /**
             * Specifies the priority to assign to this rule.
             *
             * <p>Security rules are applied in the order of their assigned priority.
             *
             * @param priority the priority number in the range 100 to 4096
             * @return the next stage
             */
            WithAttach<ParentT> withPriority(int priority);
        }

        /**
         * The stage of the network rule definition allowing the description to be specified.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithDescription<ParentT> {
            /**
             * Specifies a description for this security rule.
             *
             * @param description the text description to associate with this security rule
             * @return the next stage
             */
            WithAttach<ParentT> withDescription(String description);
        }

        /**
         * The final stage of the security rule definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the security rule definition can be
         * attached to the parent network security group definition using {@link WithAttach#attach()}.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT>
            extends Attachable.InDefinition<ParentT>, WithPriority<ParentT>, WithDescription<ParentT> {
        }
    }

    /**
     * The entirety of a network security rule definition as part of a network security group update.
     *
     * @param <ParentT> the return type of the final {@link UpdateDefinitionStages.WithAttach#attach()}
     */
    interface UpdateDefinition<ParentT>
        extends UpdateDefinitionStages.Blank<ParentT>,
            UpdateDefinitionStages.WithDirectionAccess<ParentT>,
            UpdateDefinitionStages.WithSourceAddressOrSecurityGroup<ParentT>,
            UpdateDefinitionStages.WithSourcePort<ParentT>,
            UpdateDefinitionStages.WithDestinationAddressOrSecurityGroup<ParentT>,
            UpdateDefinitionStages.WithDestinationPort<ParentT>,
            UpdateDefinitionStages.WithProtocol<ParentT>,
            UpdateDefinitionStages.WithAttach<ParentT> {
    }

    /** Grouping of security rule definition stages applicable as part of a network security group update. */
    interface UpdateDefinitionStages {
        /**
         * The first stage of a security rule description as part of an update of a networking security group.
         *
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface Blank<ParentT> extends WithDirectionAccess<ParentT> {
        }

        /**
         * The stage of the network rule description allowing the direction and the access type to be specified.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithDirectionAccess<ParentT> {
            /**
             * Allows inbound traffic.
             *
             * @return the next stage of the definition
             */
            WithSourceAddressOrSecurityGroup<ParentT> allowInbound();

            /**
             * Allows outbound traffic.
             *
             * @return the next stage of the definition
             */
            WithSourceAddressOrSecurityGroup<ParentT> allowOutbound();

            /**
             * Blocks inbound traffic.
             *
             * @return the next stage of the definition
             */
            WithSourceAddressOrSecurityGroup<ParentT> denyInbound();

            /**
             * Blocks outbound traffic.
             *
             * @return the next stage of the definition
             */
            WithSourceAddressOrSecurityGroup<ParentT> denyOutbound();
        }

        /**
         * The stage of the network rule definition allowing the source address to be specified. Note: network security
         * rule must specify a non empty value for exactly one of: SourceAddressPrefixes, SourceAddressPrefix,
         * SourceApplicationSecurityGroups.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithSourceAddressOrSecurityGroup<ParentT> {
            /**
             * Specifies the traffic source address prefix to which this rule applies.
             *
             * @param cidr an IP address prefix expressed in the CIDR notation
             * @return the next stage of the definition
             */
            WithSourcePort<ParentT> fromAddress(String cidr);

            /**
             * Specifies the traffic source address prefixes to which this rule applies.
             *
             * @param addresses IP address prefixes in CIDR notation or IP addresses
             * @return the next stage of the definition
             */
            WithSourcePort<ParentT> fromAddresses(String... addresses);

            /**
             * Specifies that the rule applies to any traffic source address.
             *
             * @return the next stage of the definition
             */
            WithSourcePort<ParentT> fromAnyAddress();

            /**
             * Sets the application security group specified as source.
             *
             * @param id application security group id
             * @return the next stage of the update
             */
            WithSourcePort<ParentT> withSourceApplicationSecurityGroup(String id);

            /**
             * Sets the application security group specified as source.
             *
             * @param ids the collection of application security group ID
             * @return the next stage of the definition
             */
            WithSourcePort<ParentT> withSourceApplicationSecurityGroup(String... ids);
        }

        /**
         * The stage of the network rule definition allowing the source port(s) to be specified.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithSourcePort<ParentT> {
            /**
             * Specifies the source port to which this rule applies.
             *
             * @param port the source port number
             * @return the next stage of the definition
             */
            WithDestinationAddressOrSecurityGroup<ParentT> fromPort(int port);

            /**
             * Makes this rule apply to any source port.
             *
             * @return the next stage of the definition
             */
            WithDestinationAddressOrSecurityGroup<ParentT> fromAnyPort();

            /**
             * Specifies the source port range to which this rule applies.
             *
             * @param from the starting port number
             * @param to the ending port number
             * @return the next stage of the definition
             */
            WithDestinationAddressOrSecurityGroup<ParentT> fromPortRange(int from, int to);

            /**
             * Specifies the source port ranges to which this rule applies.
             *
             * @param ranges the starting port ranges
             * @return the next stage of the definition
             */
            WithDestinationAddressOrSecurityGroup<ParentT> fromPortRanges(String... ranges);
        }

        /**
         * The stage of the network rule definition allowing the destination address to be specified. Note: network
         * security rule must specify a non empty value for exactly one of: DestinationAddressPrefixes,
         * DestinationAddressPrefix, DestinationApplicationSecurityGroups.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithDestinationAddressOrSecurityGroup<ParentT> {
            /**
             * Specifies the traffic destination address range to which this rule applies.
             *
             * @param cidr an IP address range expressed in the CIDR notation
             * @return the next stage of the definition
             */
            WithDestinationPort<ParentT> toAddress(String cidr);

            /**
             * Specifies the traffic destination address prefixes to which this rule applies.
             *
             * @param addresses IP address prefixes in CIDR notation or IP addresses
             * @return the next stage of the definition
             */
            WithDestinationPort<ParentT> toAddresses(String... addresses);

            /**
             * Makes the rule apply to any traffic destination address.
             *
             * @return the next stage of the definition
             */
            WithDestinationPort<ParentT> toAnyAddress();

            /**
             * Sets the application security group specified as destination.
             *
             * @param id application security group id
             * @return the next stage of the definition
             */
            WithDestinationPort<ParentT> withDestinationApplicationSecurityGroup(String id);

            /**
             * Sets the application security group specified as destination.
             *
             * @param ids the collection of application security group ID
             * @return the next stage of the definition
             */
            WithDestinationPort<ParentT> withDestinationApplicationSecurityGroup(String... ids);
        }

        /**
         * The stage of the network rule definition allowing the destination port(s) to be specified.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithDestinationPort<ParentT> {
            /**
             * Specifies the destination port to which this rule applies.
             *
             * @param port the destination port number
             * @return the next stage of the definition
             */
            WithProtocol<ParentT> toPort(int port);

            /**
             * Makes this rule apply to any destination port.
             *
             * @return the next stage of the definition
             */
            WithProtocol<ParentT> toAnyPort();

            /**
             * Specifies the destination port range to which this rule applies.
             *
             * @param from the starting port number
             * @param to the ending port number
             * @return the next stage of the definition
             */
            WithProtocol<ParentT> toPortRange(int from, int to);

            /**
             * Specifies the destination port ranges to which this rule applies.
             *
             * @param ranges the destination port ranges
             * @return the next stage of the definition
             */
            WithProtocol<ParentT> toPortRanges(String... ranges);
        }

        /**
         * The stage of the security rule definition allowing the protocol that the rule applies to to be specified.
         *
         * @param <ParentT> the stage of the parent definition to return to after attaching this definition
         */
        interface WithProtocol<ParentT> {
            /**
             * Specifies the protocol that this rule applies to.
             *
             * @param protocol one of the supported protocols
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withProtocol(SecurityRuleProtocol protocol);

            /**
             * Makes this rule apply to any supported protocol.
             *
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withAnyProtocol();
        }

        /**
         * The final stage of the security rule definition.
         *
         * <p>At this stage, any remaining optional settings can be specified, or the security rule definition can be
         * attached to the parent network security group definition using {@link WithAttach#attach()}.
         *
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends Attachable.InUpdate<ParentT> {

            /**
             * Specifies the priority to assign to this rule.
             *
             * <p>Security rules are applied in the order of their assigned priority.
             *
             * @param priority the priority number in the range 100 to 4096
             * @return the next stage of the update
             */
            WithAttach<ParentT> withPriority(int priority);

            /**
             * Specifies a description for this security rule.
             *
             * @param descrtiption a text description to associate with the security rule
             * @return the next stage
             */
            WithAttach<ParentT> withDescription(String descrtiption);
        }
    }

    /** The entirety of a security rule update as part of a network security group update. */
    interface Update
        extends UpdateStages.WithDirectionAccess,
            UpdateStages.WithSourceAddressOrSecurityGroup,
            UpdateStages.WithSourcePort,
            UpdateStages.WithDestinationAddressOrSecurityGroup,
            UpdateStages.WithDestinationPort,
            UpdateStages.WithProtocol,
            Settable<NetworkSecurityGroup.Update> {

        /**
         * Specifies the priority to assign to this security rule.
         *
         * <p>Security rules are applied in the order of their assigned priority.
         *
         * @param priority the priority number in the range 100 to 4096
         * @return the next stage of the update
         */
        Update withPriority(int priority);

        /**
         * Specifies a description for this security rule.
         *
         * @param description a text description to associate with this security rule
         * @return the next stage
         */
        Update withDescription(String description);
    }

    /** Grouping of security rule update stages. */
    interface UpdateStages {
        /** The stage of the network rule description allowing the direction and the access type to be specified. */
        interface WithDirectionAccess {
            /**
             * Allows inbound traffic.
             *
             * @return the next stage of the definition
             */
            Update allowInbound();

            /**
             * Allows outbound traffic.
             *
             * @return the next stage of the definition
             */
            Update allowOutbound();

            /**
             * Blocks inbound traffic.
             *
             * @return the next stage of the definition
             */
            Update denyInbound();

            /**
             * Blocks outbound traffic.
             *
             * @return the next stage of the definition
             */
            Update denyOutbound();
        }

        /**
         * The stage of the network rule description allowing the source address to be specified. Note: network security
         * rule must specify a non empty value for exactly one of: SourceAddressPrefixes, SourceAddressPrefix,
         * SourceApplicationSecurityGroups.
         */
        interface WithSourceAddressOrSecurityGroup {
            /**
             * Specifies the traffic source address prefix to which this rule applies.
             *
             * @param cidr an IP address prefix expressed in the CIDR notation
             * @return the next stage of the definition
             */
            Update fromAddress(String cidr);

            /**
             * Specifies the traffic source address prefixes to which this rule applies.
             *
             * @param addresses IP address prefixes in CIDR notation or IP addresses
             * @return the next stage of the definition
             */
            Update fromAddresses(String... addresses);

            /**
             * Specifies that the rule applies to any traffic source address.
             *
             * @return the next stage of the definition
             */
            Update fromAnyAddress();

            /**
             * Sets the application security group specified as source.
             *
             * @param id application security group id
             * @return the next stage of the update
             */
            Update withSourceApplicationSecurityGroup(String id);

            /**
             * Removes the application security group specified as source.
             *
             * @param id application security group id
             * @return the next stage of the update
             */
            Update withoutSourceApplicationSecurityGroup(String id);
        }

        /** The stage of the network rule description allowing the source port(s) to be specified. */
        interface WithSourcePort {
            /**
             * Specifies the source port to which this rule applies.
             *
             * @param port the source port number
             * @return the next stage of the definition
             */
            Update fromPort(int port);

            /**
             * Makes this rule apply to any source port.
             *
             * @return the next stage of the definition
             */
            Update fromAnyPort();

            /**
             * Specifies the source port range to which this rule applies.
             *
             * @param from the starting port number
             * @param to the ending port number
             * @return the next stage of the definition
             */
            Update fromPortRange(int from, int to);

            /**
             * Specifies the source port ranges to which this rule applies.
             *
             * @param ranges the starting port ranges
             * @return the next stage of the definition
             */
            Update fromPortRanges(String... ranges);
        }

        /**
         * The stage of the network rule description allowing the destination address to be specified. Note: network
         * security rule must specify a non empty value for exactly one of: DestinationAddressPrefixes,
         * DestinationAddressPrefix, DestinationApplicationSecurityGroups.
         */
        interface WithDestinationAddressOrSecurityGroup {
            /**
             * Specifies the traffic destination address range to which this rule applies.
             *
             * @param cidr an IP address range expressed in the CIDR notation
             * @return the next stage of the update
             */
            Update toAddress(String cidr);

            /**
             * Makes the rule apply to any traffic destination address.
             *
             * @return the next stage of the update
             */
            Update toAnyAddress();

            /**
             * Specifies the traffic destination address prefixes to which this rule applies.
             *
             * @param addresses IP address prefixes in CIDR notation or IP addresses
             * @return the next stage of the definition
             */
            Update toAddresses(String... addresses);

            /**
             * Sets the application security group specified as destination.
             *
             * @param id application security group id
             * @return the next stage of the update
             */
            Update withDestinationApplicationSecurityGroup(String id);

            /**
             * Removes the application security group specified as destination.
             *
             * @param id application security group id
             * @return the next stage of the definition
             */
            Update withoutDestinationApplicationSecurityGroup(String id);
        }

        /** The stage of the network rule description allowing the destination port(s) to be specified. */
        interface WithDestinationPort {
            /**
             * Specifies the destination port to which this rule applies.
             *
             * @param port the destination port number
             * @return the next stage of the definition
             */
            Update toPort(int port);

            /**
             * Makes this rule apply to any destination port.
             *
             * @return the next stage of the definition
             */
            Update toAnyPort();

            /**
             * Specifies the destination port range to which this rule applies.
             *
             * @param from the starting port number
             * @param to the ending port number
             * @return the next stage of the definition
             */
            Update toPortRange(int from, int to);

            /**
             * Specifies the destination port ranges to which this rule applies.
             *
             * @param ranges the destination port ranges
             * @return the next stage of the definition
             */
            Update toPortRanges(String... ranges);
        }

        /**
         * The stage of the security rule description allowing the protocol that the rule applies to to be specified.
         */
        interface WithProtocol {
            /**
             * Specifies the protocol that this rule applies to.
             *
             * @param protocol one of the supported protocols
             * @return the next stage of the definition
             */
            Update withProtocol(SecurityRuleProtocol protocol);

            /**
             * Makes this rule apply to any supported protocol.
             *
             * @return the next stage of the definition
             */
            Update withAnyProtocol();
        }
    }
}

/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.network.implementation.api.SecurityRuleAccess;
import com.microsoft.azure.management.network.implementation.api.SecurityRuleDirection;
import com.microsoft.azure.management.network.implementation.api.SecurityRuleInner;
import com.microsoft.azure.management.network.implementation.api.SecurityRuleProtocol;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.Settable;
import com.microsoft.azure.management.resources.fluentcore.model.Wrapper;

/**
 * A network security rule in a network security group.
 */
public interface NetworkSecurityRule extends
    Wrapper<SecurityRuleInner>,
    ChildResource {

    /**
     * The possible directions of the network traffic supported by a network security rule.
     */
    enum Direction {
        INBOUND(SecurityRuleDirection.INBOUND),
        OUTBOUND(SecurityRuleDirection.OUTBOUND);

        private final String name;
        Direction(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }

        /**
         * Converts the string used by Azure into the corresponding constant, if any.
         * @param s the string used by Azure to convert to a constant
         * @return the identified constant, or null if not supported
         */
        public static Direction fromString(String s) {
            for (Direction e : Direction.values()) {
                if (e.name.equalsIgnoreCase(s)) {
                    return e;
                }
            }
            return null;
        }
    }

    /**
     * The possible access types supported by a network security rule.
     */
    enum Access {
        ALLOW(SecurityRuleAccess.ALLOW),
        DENY(SecurityRuleAccess.DENY);

        private final String name;
        Access(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }

        /**
         * Converts the string used by Azure into the corresponding constant, if any.
         * @param s the string used by Azure to convert to a constant
         * @return the identified constant, or null if not supported
         */
        public static Access fromString(String s) {
            for (Access e : Access.values()) {
                if (e.name.equalsIgnoreCase(s)) {
                    return e;
                }
            }
            return null;
        }
    }

    /**
     * The possible Azure network protocols supported by a network security rule.
     */
    enum Protocol {
        TCP(SecurityRuleProtocol.TCP),
        UDP(SecurityRuleProtocol.UDP),
        ANY(SecurityRuleProtocol.ASTERISK);

        public final String name;
        Protocol(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return this.name;
        }

        /**
         * Converts the string used by Azure into the corresponding constant, if any.
         * @param s the string used by Azure to convert to a constant
         * @return the identified constant, or null if not supported
         */
        public static Protocol fromString(String s) {
            for (Protocol protocol : Protocol.values()) {
                if (protocol.name.equalsIgnoreCase(s)) {
                    return protocol;
                }
            }
            return null;
        }
    }

    /**
     * @return the network traffic direction the rule applies to
     */
    Direction direction();

    /**
     * @return the network protocol the rule applies to
     */
    Protocol protocol();

    /**
     * @return the type of access the rule enforces
     */
    Access access();

    /**
     * @return the source address prefix the rule applies to, expressed using the CIDR notation in the format: "###.###.###.###/##",
     * and "*" means "any"
     */
    String sourceAddressPrefix();

    /**
     * @return the source port range that the rule applies to, in the format "##-##", where "*" means "any"
     */
    String sourcePortRange();

    /**
     * @return the destination address prefix the rule applies to, expressed using the CIDR notation in the format: "###.###.###.###/##",
     * and "*" means "any"
     */
    String destinationAddressPrefix();

    /**
     * @return the destination port range that the rule applies to, in the format "##-##", where "*" means any
     */
    String destinationPortRange();

    /**
     * @return the priority number of this rule based on which this rule will be applied relative to the priority numbers of any other rules specified
     * for this network security group
     */
    int priority();

    /**
     * The entirety of a network security rule definition.
     * @param <ParentT> the return type of the final {@link Attachable#attach()}
     */
    interface Definition<ParentT> extends
        Definables.Blank<ParentT>,
        Definables.WithAttach<ParentT>,
        Definables.WithDirectionAccess<ParentT>,
        Definables.WithSourceAddress<ParentT>,
        Definables.WithSourcePort<ParentT>,
        Definables.WithDestinationAddress<ParentT>,
        Definables.WithDestinationPort<ParentT>,
        Definables.WithProtocol<ParentT> {
    }

    /**
     * The entirety of a network security rule definition as part of a network security group update.
     * @param <ParentT> the return type of the final {@link Attachable#attach()}
     */
    interface UpdateDefinition<ParentT> extends
        UpdateDefinables.Blank<ParentT>,
        UpdateDefinables.WithDirectionAccess<ParentT>,
        UpdateDefinables.WithSourceAddress<ParentT>,
        UpdateDefinables.WithSourcePort<ParentT>,
        UpdateDefinables.WithDestinationAddress<ParentT>,
        UpdateDefinables.WithDestinationPort<ParentT>,
        UpdateDefinables.WithProtocol<ParentT>,
        UpdateDefinables.WithAttach<ParentT> {
    }

    /**
     * The entirety of a security rule update as part of a network security group update.
     */
    interface Update extends
        Updatables.WithDirectionAccess,
        Updatables.WithSourceAddress,
        Updatables.WithSourcePort,
        Updatables.WithDestinationAddress,
        Updatables.WithDestinationPort,
        Updatables.WithProtocol,
        Settable<NetworkSecurityGroup.Update> {

        /**
         * Specifies the priority to assign to this rule.
         * <p>
         * Security rules are applied in the order of their assigned priority.
         * @param priority the priority number in the range 100 to 4096
         * @return the next stage of the update
         */
        Update withPriority(int priority);
    }

    /**
     * Grouping of security rule update stages.
     */
    interface Updatables {
        /**
         * The stage of the network rule description allowing the direction and the access type to be specified.
         */
        interface WithDirectionAccess {
            /**
             * Allows inbound traffic.
             * @return the next stage of the security rule definition
             */
            Update allowInbound();

            /**
             * Allows outbound traffic.
             * @return the next stage of the security rule definition
             */
            Update allowOutbound();

            /**
             * Blocks inbound traffic.
             * @return the next stage of the security rule definition
             */
            Update denyInbound();

            /**
             * Blocks outbound traffic.
             * @return the next stage of the security rule definition
             */
            Update denyOutbound();
        }

        /**
         * The stage of the network rule description allowing the source address to be specified.
         */
        interface WithSourceAddress {
            /**
             * Specifies the traffic source address prefix to which this rule applies.
             * @param cidr an IP address prefix expressed in the CIDR notation
             * @return the next stage of the security rule definition
             */
            Update fromAddress(String cidr);

            /**
             * Specifies that the rule applies to any traffic source address.
             * @return the next stage of the security rule definition
             */
            Update fromAnyAddress();
        }

        /**
         * The stage of the network rule description allowing the source port(s) to be specified.
         */
        interface WithSourcePort {
            /**
             * Specifies the source port to which this rule applies.
             * @param port the source port number
             * @return the next stage of the security rule definition
             */
            Update fromPort(int port);

            /**
             * Makes this rule apply to any source port.
             * @return the next stage of the security rule definition
             */
            Update fromAnyPort();

            /**
             * Specifies the source port range to which this rule applies.
             * @param from the starting port number
             * @param to the ending port number
             * @return the next stage of the security rule definition
             */
            Update fromPortRange(int from, int to);
        }

        /**
         * The stage of the network rule description allowing the destination address to be specified.
         */
        interface WithDestinationAddress {
            /**
             * Specifies the traffic destination address range to which this rule applies.
             * @param cidr an IP address range expressed in the CIDR notation
             * @return the next stage of the security rule definition
             */
            Update toAddress(String cidr);

            /**
             * Makes the rule apply to any traffic destination address.
             * @return the next stage of the security rule definition
             */
            Update toAnyAddress();
        }

        /**
         * The stage of the network rule description allowing the destination port(s) to be specified.
         */
        interface WithDestinationPort {
            /**
             * Specifies the destination port to which this rule applies.
             * @param port the destination port number
             * @return the next stage of the security rule definition
             */
            Update toPort(int port);

            /**
             * Makes this rule apply to any destination port.
             * @return the next stage of the security rule definition
             */
            Update toAnyPort();

            /**
             * Specifies the destination port range to which this rule applies.
             * @param from the starting port number
             * @param to the ending port number
             * @return the next stage of the security rule definition
             */
            Update toPortRange(int from, int to);
        }

        /**
         * The stage of the security rule description allowing the protocol that the rule applies to to be specified.
         */
        interface WithProtocol {
            /**
             * Specifies the protocol that this rule applies to.
             * @param protocol one of the supported protocols
             * @return the next stage of the security rule definition
             */
            Update withProtocol(Protocol protocol);

            /**
             * Makes this rule apply to any supported protocol.
             * @return the next stage of the security rule definition
             */
            Update withAnyProtocol();
        }
    }


    /**
     * Grouping of security rule definition stages applicable as part of a network security group update.
     */
    interface UpdateDefinables {
        /**
         * The first stage of a security rule description as part of an update of a networking security group.
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface Blank<ParentT> extends WithDirectionAccess<ParentT> {
        }

        /**
         * The stage of the network rule definition allowing the direction and the access type to be specified.
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface WithDirectionAccess<ParentT> {
            /**
             * Allows inbound traffic.
             * @return the next stage of the security rule definition
             */
            WithSourceAddress<ParentT> allowInbound();

            /**
             * Allows outbound traffic.
             * @return the next stage of the security rule definition
             */
            WithSourceAddress<ParentT> allowOutbound();

            /**
             * Blocks inbound traffic.
             * @return the next stage of the security rule definition
             */
            WithSourceAddress<ParentT> denyInbound();

            /**
             * Blocks outbound traffic.
             * @return the next stage of the security rule definition
             */
            WithSourceAddress<ParentT> denyOutbound();
        }

        /**
         * The stage of the network rule definition allowing the source address to be specified.
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface WithSourceAddress<ParentT> {
            /**
             * Specifies the traffic source address prefix to which this rule applies.
             * @param cidr an IP address prefix expressed in the CIDR notation
             * @return the next stage of the security rule definition
             */
            WithSourcePort<ParentT> fromAddress(String cidr);

            /**
             * Specifies that the rule applies to any traffic source address.
             * @return the next stage of the security rule definition
             */
            WithSourcePort<ParentT> fromAnyAddress();
        }

        /**
         * The stage of the network rule definition allowing the source port(s) to be specified.
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface WithSourcePort<ParentT> {
            /**
             * Specifies the source port to which this rule applies.
             * @param port the source port number
             * @return the next stage of the security rule definition
             */
            WithDestinationAddress<ParentT> fromPort(int port);

            /**
             * Makes this rule apply to any source port.
             * @return the next stage of the security rule definition
             */
            WithDestinationAddress<ParentT> fromAnyPort();

            /**
             * Specifies the source port range to which this rule applies.
             * @param from the starting port number
             * @param to the ending port number
             * @return the next stage of the security rule definition
             */
            WithDestinationAddress<ParentT> fromPortRange(int from, int to);
        }

        /**
         * The stage of the network rule definition allowing the destination address to be specified.
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface WithDestinationAddress<ParentT> {
            /**
             * Specifies the traffic destination address range to which this rule applies.
             * @param cidr an IP address range expressed in the CIDR notation
             * @return the next stage of the security rule definition
             */
            WithDestinationPort<ParentT> toAddress(String cidr);

            /**
             * Makes the rule apply to any traffic destination address.
             * @return the next stage of the security rule definition
             */
            WithDestinationPort<ParentT> toAnyAddress();
        }

        /**
         * The stage of the network rule definition allowing the destination port(s) to be specified.
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface WithDestinationPort<ParentT> {
            /**
             * Specifies the destination port to which this rule applies.
             * @param port the destination port number
             * @return the next stage of the security rule definition
             */
            WithProtocol<ParentT> toPort(int port);

            /**
             * Makes this rule apply to any destination port.
             * @return the next stage of the security rule definition
             */
            WithProtocol<ParentT> toAnyPort();

            /**
             * Specifies the destination port range to which this rule applies.
             * @param from the starting port number
             * @param to the ending port number
             * @return the next stage of the security rule definition
             */
            WithProtocol<ParentT> toPortRange(int from, int to);
        }

        /**
         * The stage of the security rule definition allowing the protocol that the rule applies to to be specified.
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface WithProtocol<ParentT> {
            /**
             * Specifies the protocol that this rule applies to.
             * @param protocol one of the supported protocols
             * @return the next stage of the security rule definition
             */
            WithAttach<ParentT> withProtocol(Protocol protocol);

            /**
             * Makes this rule apply to any supported protocol.
             * @return the next stage of the security rule definition
             */
            WithAttach<ParentT> withAnyProtocol();
        }

        /** The final stage of the security rule definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the security rule definition
         * can be attached to the parent network security group definition using {@link WithAttach#attach()}.
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends Attachable.InUpdate<ParentT> {

            /**
             * Specifies the priority to assign to this rule.
             * <p>
             * Security rules are applied in the order of their assigned priority.
             * @param priority the priority number in the range 100 to 4096
             * @return the next stage of the update
             */
            WithAttach<ParentT> withPriority(int priority);
        }
    }

    /**
     * Grouping of security rule definition stages applicable as part of a network security group creation.
     */
    interface Definables {
        /**
         * The first stage of a security rule definition.
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface Blank<ParentT> extends WithDirectionAccess<ParentT> {
        }

        /**
         * The stage of the security rule definition allowing the protocol that the rule applies to to be specified.
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface WithProtocol<ParentT> {
            /**
             * Specifies the protocol that this rule applies to.
             * @param protocol one of the supported protocols
             * @return the next stage of the security rule definition
             */
            WithAttach<ParentT> withProtocol(Protocol protocol);

            /**
             * Makes this rule apply to any supported protocol.
             * @return the next stage of the security rule definition
             */
            WithAttach<ParentT> withAnyProtocol();
        }

        /**
         * The stage of the network rule definition allowing the destination port(s) to be specified.
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface WithDestinationPort<ParentT> {
            /**
             * Specifies the destination port to which this rule applies.
             * @param port the destination port number
             * @return the next stage of the security rule definition
             */
            WithProtocol<ParentT> toPort(int port);

            /**
             * Makes this rule apply to any destination port.
             * @return the next stage of the security rule definition
             */
            WithProtocol<ParentT> toAnyPort();

            /**
             * Specifies the destination port range to which this rule applies.
             * @param from the starting port number
             * @param to the ending port number
             * @return the next stage of the security rule definition
             */
            WithProtocol<ParentT> toPortRange(int from, int to);
        }

        /**
         * The stage of the network rule definition allowing the destination address to be specified.
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface WithDestinationAddress<ParentT> {
            /**
             * Specifies the traffic destination address range to which this rule applies.
             * @param cidr an IP address range expressed in the CIDR notation
             * @return the next stage of the security rule definition
             */
            WithDestinationPort<ParentT> toAddress(String cidr);

            /**
             * Makes the rule apply to any traffic destination address.
             * @return the next stage of the security rule definition
             */
            WithDestinationPort<ParentT> toAnyAddress();
        }

        /**
         * The stage of the network rule definition allowing the source port(s) to be specified.
         *
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface WithSourcePort<ParentT> {
            /**
             * Specifies the source port to which this rule applies.
             * @param port the source port number
             * @return the next stage of the security rule definition
             */
            WithDestinationAddress<ParentT> fromPort(int port);

            /**
             * Makes this rule apply to any source port.
             * @return the next stage of the security rule definition
             */
            WithDestinationAddress<ParentT> fromAnyPort();

            /**
             * Specifies the source port range to which this rule applies.
             * @param from the starting port number
             * @param to the ending port number
             * @return the next stage of the security rule definition
             */
            WithDestinationAddress<ParentT> fromPortRange(int from, int to);
        }

        /**
         * The stage of the network rule definition allowing the source address to be specified.
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface WithSourceAddress<ParentT> {
            /**
             * Specifies the traffic source address prefix to which this rule applies.
             * @param cidr an IP address prefix expressed in the CIDR notation
             * @return the next stage of the security rule definition
             */
            WithSourcePort<ParentT> fromAddress(String cidr);

            /**
             * Specifies that the rule applies to any traffic source address.
             * @return the next stage of the security rule definition
             */
            WithSourcePort<ParentT> fromAnyAddress();
        }

        /**
         * The stage of the network rule definition allowing the direction and the access type to be specified.
         * @param <ParentT> the return type of the final {@link Attachable#attach()}
         */
        interface WithDirectionAccess<ParentT> {
            /**
             * Allows inbound traffic.
             * @return the next stage of the security rule definition
             */
            WithSourceAddress<ParentT> allowInbound();

            /**
             * Allows outbound traffic.
             * @return the next stage of the security rule definition
             */
            WithSourceAddress<ParentT> allowOutbound();

            /**
             * Blocks inbound traffic.
             * @return the next stage of the security rule definition
             */
            WithSourceAddress<ParentT> denyInbound();

            /**
             * Blocks outbound traffic.
             * @return the next stage of the security rule definition
             */
            WithSourceAddress<ParentT> denyOutbound();
        }

        /** The final stage of the security rule definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the security rule definition
         * can be attached to the parent network security group definition using {@link WithAttach#attach()}.
         * @param <ParentT> the return type of {@link WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends Attachable.InDefinition<ParentT> {

            /**
             * Specifies the priority to assign to this rule.
             * <p>
             * Security rules are applied in the order of their assigned priority.
             * @param priority the priority number in the range 100 to 4096
             * @return the next stage of the update
             */
            WithAttach<ParentT> withPriority(int priority);
        }
    }
 }

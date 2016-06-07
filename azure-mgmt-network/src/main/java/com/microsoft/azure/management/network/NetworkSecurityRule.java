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
     * @param <ParentT> the return type of the final {@link DefinitionAttachable#attach()}
     */
    interface Definitions<ParentT> extends
        DefinitionBlank<ParentT>,
        DefinitionAttachable<ParentT>,
        DefinitionWithDirectionAccess<ParentT>,
        DefinitionWithProtocol<ParentT>,
        DefinitionWithSourceAddress<ParentT>,
        DefinitionWithSourcePort<ParentT>,
        DefinitionWithDestinationAddress<ParentT>,
        DefinitionWithDestinationPort<ParentT> {
    }

    /**
     * The first stage of the subnet definition.
     * @param <ParentT> the return type of the final {@link DefinitionAttachable#attach()}
     */
    interface DefinitionBlank<ParentT> extends DefinitionWithDirectionAccess<ParentT> {
    }

    /**
     * The stage of the network rule definition allowing the direction and the access type to be specified.
     * @param <ParentT> the return type of the final {@link DefinitionAttachable#attach()}
     */
    interface DefinitionWithDirectionAccess<ParentT> {
        /**
         * Allows inbound traffic.
         * @return the next stage of the security rule definition
         */
        DefinitionWithSourceAddress<ParentT> allowInbound();

        /**
         * Allows outbound traffic.
         * @return the next stage of the security rule definition
         */
        DefinitionWithSourceAddress<ParentT> allowOutbound();

        /**
         * Blocks inbound traffic.
         * @return the next stage of the security rule definition
         */
        DefinitionWithSourceAddress<ParentT> denyInbound();

        /**
         * Blocks outbound traffic.
         * @return the next stage of the security rule definition
         */
        DefinitionWithSourceAddress<ParentT> denyOutbound();
    }

    /**
     * The stage of the network rule definition allowing the source address to be specified.
     * @param <ParentT> the return type of the final {@link DefinitionAttachable#attach()}
     */
    interface DefinitionWithSourceAddress<ParentT> {
        /**
         * Specifies the traffic source address prefix to which this rule applies.
         * @param cidr an IP address prefix expressed in the CIDR notation
         * @return the next stage of the security rule definition
         */
        DefinitionWithSourcePort<ParentT> fromAddress(String cidr);

        /**
         * Specifies that the rule applies to any traffic source address.
         * @return the next stage of the security rule definition
         */
        DefinitionWithSourcePort<ParentT> fromAnyAddress();
    }

    /**
     * The stage of the network rule definition allowing the source port(s) to be specified.
     * @param <ParentT> the return type of the final {@link DefinitionAttachable#attach()}
     */
    interface DefinitionWithSourcePort<ParentT> {
        /**
         * Specifies the source port to which this rule applies.
         * @param port the source port number
         * @return the next stage of the security rule definition
         */
        DefinitionWithDestinationAddress<ParentT> fromPort(int port);

        /**
         * Makes this rule apply to any source port.
         * @return the next stage of the security rule definition
         */
        DefinitionWithDestinationAddress<ParentT> fromAnyPort();

        /**
         * Specifies the source port range to which this rule applies.
         * @param from the starting port number
         * @param to the ending port number
         * @return the next stage of the security rule definition
         */
        DefinitionWithDestinationAddress<ParentT> fromPortRange(int from, int to);
    }

    /**
     * The stage of the network rule definition allowing the destination address to be specified.
     * @param <ParentT> the return type of the final {@link DefinitionAttachable#attach()}
     */
    interface DefinitionWithDestinationAddress<ParentT> {
        /**
         * Specifies the traffic destination address range to which this rule applies.
         * @param cidr an IP address range expressed in the CIDR notation
         * @return the next stage of the security rule definition
         */
        DefinitionWithDestinationPort<ParentT> toAddress(String cidr);

        /**
         * Makes the rule apply to any traffic destination address.
         * @return the next stage of the security rule definition
         */
        DefinitionWithDestinationPort<ParentT> toAnyAddress();
    }

    /**
     * The stage of the network rule definition allowing the destination port(s) to be specified.
     * @param <ParentT> the return type of the final {@link DefinitionAttachable#attach()}
     */
    interface DefinitionWithDestinationPort<ParentT> {
        /**
         * Specifies the destination port to which this rule applies.
         * @param port the destination port number
         * @return the next stage of the security rule definition
         */
        DefinitionWithProtocol<ParentT> toPort(int port);

        /**
         * Makes this rule apply to any destination port.
         * @return the next stage of the security rule definition
         */
        DefinitionWithProtocol<ParentT> toAnyPort();

        /**
         * Specifies the destination port range to which this rule applies.
         * @param from the starting port number
         * @param to the ending port number
         * @return the next stage of the security rule definition
         */
        DefinitionWithProtocol<ParentT> toPortRange(int from, int to);
    }

    /**
     * The stage of the security rule definition allowing the protocol that the rule applies to to be specified.
     * @param <ParentT> the return type of the final {@link DefinitionAttachable#attach()}
     */
    interface DefinitionWithProtocol<ParentT> {
        /**
         * Specifies the protocol that this rule applies to.
         * @param protocol one of the supported protocols
         * @return the next stage of the security rule definition
         */
        DefinitionAttachable<ParentT> withProtocol(Protocol protocol);

        /**
         * Makes this rule apply to any supported protocol.
         * @return the next stage of the security rule definition
         */
        DefinitionAttachable<ParentT> withAnyProtocol();
    }

    /** The final stage of the security rule definition.
     * <p>
     * At this stage, any remaining optional settings can be specified, or the security rule definition
     * can be attached to the parent network security group definition using {@link DefinitionAttachable#attach()}.
     * @param <ParentT> the return type of {@link DefinitionAttachable#attach()}
     */
    interface DefinitionAttachable<ParentT> extends
        Attachable<ParentT> {

        /**
         * Specifies the priority to assign to this rule.
         * <p>
         * Security rules are applied in the order of their assigned priority.
         * @param priority the priority number in the range 100 to 4096
         * @return the next stage of the update
         */
        DefinitionAttachable<ParentT> withPriority(int priority);
    }
}

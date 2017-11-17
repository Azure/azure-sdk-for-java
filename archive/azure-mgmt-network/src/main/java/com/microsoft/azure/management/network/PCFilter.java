/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.network.model.HasProtocol;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasParent;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.azure.management.resources.fluentcore.model.Indexable;

import java.util.List;

/**
 * Client-side representation of packet capture filter.
 */
@Fluent
@Beta
public interface PCFilter extends Indexable,
        HasParent<PacketCapture>,
        HasInner<PacketCaptureFilter> {
    /**
     * @return protocol to be filtered on.
     */
    PcProtocol protocol();

    /**
     * @return local IP Address to be filtered on. Notation: "127.0.0.1" for single address entry. "127.0.0.1-127.0.0.255" for range. "127.0.0.1;127.0.0.5"? for multiple entries. Multiple ranges not currently supported. Mixing ranges with multiple entries not currently supported. Default = null.
     */
    String localIPAddress();

    /**
     * @return remote IP Address to be filtered on. Notation: "127.0.0.1" for single address entry. "127.0.0.1-127.0.0.255" for range. "127.0.0.1;127.0.0.5;" for multiple entries. Multiple ranges not currently supported. Mixing ranges with multiple entries not currently supported. Default = null.
     */
    String remoteIPAddress();

    /**
     * @return local port to be filtered on. Notation: "80" for single port entry."80-85" for range. "80;443;" for multiple entries. Multiple ranges not currently supported. Mixing ranges with multiple entries not currently supported. Default = null.
     */
    String localPort();

    /**
     * @return remote port to be filtered on. Notation: "80" for single port entry."80-85" for range. "80;443;" for multiple entries. Multiple ranges not currently supported. Mixing ranges with multiple entries not currently supported. Default = null.
     */
    String remotePort();

    /**
     * Definition of packet capture filter.
     *
     * @param <ParentT> the stage of the parent definition to return to after attaching this definition
     */
    interface Definition<ParentT> extends
            PCFilter.DefinitionStages.WithAttach<ParentT> {
    }

    /**
     * Definition stages for packet capture filter.
     */
    interface DefinitionStages {

        interface WithAttach<ParentT> extends
                Attachable.InDefinition<ParentT>,
                Blank<ParentT> {
        }

        interface Blank<ParentT> extends
                HasProtocol.DefinitionStages.WithProtocol<WithAttach<PacketCapture.DefinitionStages.WithCreate>, PcProtocol>,
                WithLocalIP<ParentT>,
                WithRemoteIPAddress<ParentT>,
                WithLocalPort<ParentT>,
                WithRemotePort<ParentT> {
        }

        /**
         * Set local IP Address to be filtered on.
         * Notation: "127.0.0.1" for single address entry. "127.0.0.1-127.0.0.255" for range. "127.0.0.1;127.0.0.5" for multiple entries.
         * Multiple ranges not currently supported. Mixing ranges with multiple entries not currently supported. Default = null.
         */
        interface WithLocalIP<ParentT> {
            /**
             * Set local IP address to be filtered on.
             *
             * @param ipAddress local IP address
             * @return the next stage
             */
            Definition<ParentT> withLocalIPAddress(String ipAddress);

            /**
             * Set local IP addresses range to be filtered on.
             *
             * @param startIPAddress range start IP address
             * @param endIPAddress   range end IP address
             * @return the next stage
             */
            Definition<ParentT> withLocalIPAddressesRange(String startIPAddress, String endIPAddress);

            /**
             * Set list of local IP addresses to be filtered on.
             *
             * @param ipAddresses list of IP address
             * @return the next stage
             */
            Definition<ParentT> withLocalIPAddresses(List<String> ipAddresses);
        }

        /**
         * Set remote IP Address to be filtered on.
         * Notation: "127.0.0.1" for single address entry. "127.0.0.1-127.0.0.255" for range. "127.0.0.1;127.0.0.5" for multiple entries.
         * Multiple ranges not currently supported. Mixing ranges with multiple entries not currently supported. Default = null.
         */
        interface WithRemoteIPAddress<ParentT> {
            /**
             * Set remote IP address to be filtered on.
             *
             * @param ipAddress remote IP address
             * @return the next stage
             */
            Definition<ParentT> withRemoteIPAddress(String ipAddress);

            /**
             * Set remote IP addresses range to be filtered on.
             *
             * @param startIPAddress range start IP address
             * @param endIPAddress   range end IP address
             * @return the next stage
             */
            Definition<ParentT> withRemoteIPAddressesRange(String startIPAddress, String endIPAddress);

            /**
             * Set list of remote IP addresses to be filtered on.
             *
             * @param ipAddresses list of IP addresses
             * @return the next stage
             */
            Definition<ParentT> withRemoteIPAddresses(List<String> ipAddresses);
        }

        /**
         * Set local port to be filtered on. Notation: "80" for single port entry."80-85" for range. "80;443;" for multiple entries.
         * Multiple ranges not currently supported. Mixing ranges with multiple entries not currently supported. Default = null.
         */
        interface WithLocalPort<ParentT> {
            /**
             * Set the local port to be filtered on.
             *
             * @param port port number
             * @return the next stage
             */
            Definition<ParentT> withLocalPort(int port);

            /**
             * Set the local port range to be filtered on.
             *
             * @param startPort range start port number
             * @param endPort   range end port number
             * @return the next stage
             */
            Definition<ParentT> withLocalPortRange(int startPort, int endPort);

            /**
             * Set the list of local ports to be filtered on.
             *
             * @param ports list of local ports
             * @return the next stage
             */
            Definition<ParentT> withLocalPorts(List<Integer> ports);
        }

        /**
         * Set local port to be filtered on. Notation: "80" for single port entry."80-85" for range. "80;443;" for multiple entries.
         * Multiple ranges not currently supported. Mixing ranges with multiple entries not currently supported. Default = null.
         */
        interface WithRemotePort<ParentT> {
            /**
             * Set the remote port to be filtered on.
             *
             * @param port port number
             * @return the next stage
             */
            Definition<ParentT> withRemotePort(int port);

            /**
             * Set the remote port range to be filtered on.
             *
             * @param startPort range start port number
             * @param endPort   range end port number
             * @return the next stage
             */
            Definition<ParentT> withRemotePortRange(int startPort, int endPort);

            /**
             * Set the list of remote ports to be filtered on.
             *
             * @param ports list of remote ports
             * @return the next stage
             */
            Definition<ParentT> withRemotePorts(List<Integer> ports);
        }
    }
}

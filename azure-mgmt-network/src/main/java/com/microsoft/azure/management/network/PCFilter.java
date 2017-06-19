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
     * @param <ParentT>
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
                WithLocalIPAddress<ParentT>,
                WithRemoteIPAddress<ParentT>,
                WithLocalPort<ParentT>,
                WithRemotePort<ParentT> {
        }

        interface WithLocalIPAddress<ParentT> {
            /**
             * Set local IP Address to be filtered on.
             * Notation: "127.0.0.1" for single address entry. "127.0.0.1-127.0.0.255" for range. "127.0.0.1;127.0.0.5"? for multiple entries.
             * Multiple ranges not currently supported. Mixing ranges with multiple entries not currently supported. Default = null.
             *
             * @param ipAddress local ip address to set
             * @return the next stage
             */
            Definition<ParentT> withLocalIPAddress(String ipAddress);
        }

        interface WithRemoteIPAddress<ParentT> {
            /**
             * Set remote IP Address to be filtered on.
             * Notation: "127.0.0.1" for single address entry. "127.0.0.1-127.0.0.255" for range. "127.0.0.1;127.0.0.5"? for multiple entries.
             * Multiple ranges not currently supported. Mixing ranges with multiple entries not currently supported. Default = null.
             *
             * @param ipAddress remote ip addess to set
             * @return the next stage
             */
            Definition<ParentT> withRemoteIPAddress(String ipAddress);
        }

        interface WithLocalPort<ParentT> {
            /**
             * Set local port to be filtered on. Notation: "80" for single port entry."80-85" for range. "80;443;" for multiple entries.
             * Multiple ranges not currently supported. Mixing ranges with multiple entries not currently supported. Default = null.
             *
             * @param localPort the local port to set
             * @return the next stage
             */
            Definition<ParentT> withLocalPort(String localPort);
        }

        interface WithRemotePort<ParentT> {
            /**
             * Set the remote port to be filtered on. Notation: "80" for single port entry."80-85" for range. "80;443;" for multiple entries.
             * Multiple ranges not currently supported. Mixing ranges with multiple entries not currently supported. Default = null.
             *
             * @param remotePort the remote port to set
             * @return the next stage
             */
            Definition<ParentT> withRemotePort(String remotePort);
        }
    }
}

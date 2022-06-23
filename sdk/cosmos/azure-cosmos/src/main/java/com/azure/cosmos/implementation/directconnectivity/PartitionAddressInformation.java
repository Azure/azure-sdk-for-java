// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class PartitionAddressInformation {
    private final List<AddressInformation> allAddresses;
    private final Map<Protocol, PerProtocolPartitionAddressInformation> perProtocolPartitionAddressInformationMap;

    public PartitionAddressInformation(List<AddressInformation> replicaAddresses) {
        checkNotNull(replicaAddresses, "Argument 'replicaAddresses' can not be null");
        this.allAddresses = replicaAddresses;

        perProtocolPartitionAddressInformationMap = new ConcurrentHashMap<>();
        for (Protocol protocol : Protocol.values()) {
            this.perProtocolPartitionAddressInformationMap.putIfAbsent(protocol, new PerProtocolPartitionAddressInformation(replicaAddresses, protocol));
        }
    }

    @Override
    public int hashCode() {
        int hashCode = 17;
        for (AddressInformation addressInformation : this.allAddresses) {
            hashCode = (hashCode * 397) ^ addressInformation.hashCode();
        }

        return hashCode;
    }

    public PerProtocolPartitionAddressInformation getAddressesByProtocol(Protocol protocol) {
        return this.perProtocolPartitionAddressInformationMap.get(protocol);
    }

    public List<AddressInformation> getAllAddresses() {
        return allAddresses;
    }
}

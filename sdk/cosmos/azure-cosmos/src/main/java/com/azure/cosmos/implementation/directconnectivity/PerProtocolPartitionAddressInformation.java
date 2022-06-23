// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Strings;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkNotNull;

public class PerProtocolPartitionAddressInformation {
    private final List<AddressInformation> replicaAddresses;
    private final List<Uri> transportAddressUris;
    private final List<Uri> nonPrimaryReplicaTransportAddressUris;
    private final Uri primaryReplicaAddressUri;
    private final Protocol protocol;

    public PerProtocolPartitionAddressInformation(List<AddressInformation> replicaAddresses, Protocol protocol) {
        checkNotNull(replicaAddresses, "Argument 'replicaAddresses' should not be null");

        this.protocol = protocol;
        List<AddressInformation> nonEmptyReplicaAddresses = replicaAddresses
                .stream()
                .filter(addressInformation ->
                        !Strings.isNullOrEmpty(addressInformation.getPhysicalUri().getURIAsString())
                                && Strings.areEqualIgnoreCase(addressInformation.getProtocolScheme(), protocol.scheme()))
                .collect(Collectors.toList());

        List<AddressInformation> internalAddresses =
                nonEmptyReplicaAddresses
                        .stream()
                        .filter(addressInformation -> !addressInformation.isPublic())
                        .collect(Collectors.toList());

        this.replicaAddresses = internalAddresses.size() > 0
                ? internalAddresses
                : nonEmptyReplicaAddresses.stream().filter(addressInformation -> addressInformation.isPublic()).collect(Collectors.toList());

        this.transportAddressUris =
                this.replicaAddresses
                        .stream()
                        .map(addressInformation -> addressInformation.getPhysicalUri())
                        .collect(Collectors.toList());

        this.nonPrimaryReplicaTransportAddressUris =
                this.replicaAddresses
                        .stream()
                        .filter(addressInformation -> !addressInformation.isPrimary())
                        .map(addressInformation -> addressInformation.getPhysicalUri())
                        .collect(Collectors.toList());

        this.primaryReplicaAddressUri =
                this.replicaAddresses
                        .stream()
                        .filter(addressInformation -> addressInformation.isPrimary() && !addressInformation.getPhysicalUri().getURIAsString().contains("["))
                        .map(addressInformation -> addressInformation.getPhysicalUri())
                        .findAny().orElse(null);
    }

    public List<Uri> getTransportAddressUris() {
        return this.transportAddressUris;
    }

    public List<Uri> getNonPrimaryReplicaTransportAddressUris() {
        return this.nonPrimaryReplicaTransportAddressUris;
    }

    public Uri getPrimaryAddressUri(RxDocumentServiceRequest request) {
        checkNotNull(request, "Argument 'request' can not be null");

        Uri primaryAddressUri = null;
        if (request.getDefaultReplicaIndex() != null) {
            int defaultReplicaIndex = request.getDefaultReplicaIndex();
            if (defaultReplicaIndex >= 0 && defaultReplicaIndex < this.transportAddressUris.size()) {
                primaryAddressUri = this.transportAddressUris.get(defaultReplicaIndex);
            }
        } else {
            primaryAddressUri = this.primaryReplicaAddressUri;
        }

        if (primaryAddressUri == null) {
            // Primary endpoint (of the desired protocol) was not found.
            throw new GoneException(String.format("The requested resource is no longer available at the server. Returned addresses are {%s}",
                    String.join(",", this.transportAddressUris.stream()
                            .map(address -> address.getURIAsString()).collect(Collectors.toList()))), null);
        }

        return primaryAddressUri;
    }
}

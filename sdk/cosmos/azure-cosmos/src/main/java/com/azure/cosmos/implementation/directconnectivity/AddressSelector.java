// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.OpenConnectionResponse;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.stream.Collectors;

public class AddressSelector {
    private final IAddressResolver addressResolver;
    private final Protocol protocol;

    public AddressSelector(IAddressResolver addressResolver, Protocol protocol) {
        this.addressResolver = addressResolver;
        this.protocol = protocol;
    }

    public Mono<List<Uri>> resolveAllUriAsync(
        RxDocumentServiceRequest request,
        boolean includePrimary,
        boolean forceRefresh) {
        return this.resolveAddressesAsync(request, forceRefresh)
                .flatMap(partitionPerProtocolAddress -> {
                    if (includePrimary) {
                        return Mono.just(partitionPerProtocolAddress.getTransportAddressUris());
                    }

                    return Mono.just(partitionPerProtocolAddress.getNonPrimaryReplicaTransportAddressUris());
                });
    }

    public Mono<Uri> resolvePrimaryUriAsync(RxDocumentServiceRequest request, boolean forceAddressRefresh) {
        return this.resolveAddressesAsync(request, forceAddressRefresh)
                .flatMap(perProtocolPartitionAddressInformation -> {
                    Uri primaryAddressUri = perProtocolPartitionAddressInformation.getPrimaryAddressUri(request);
                    if (primaryAddressUri != null) {
                        return Mono.just(primaryAddressUri);
                    }

                    return Mono.error(
                            new GoneException(
                                    String.format("The requested resource is no longer available at the server. Returned addresses are {%s}",
                                        String.join(
                                                ",",
                                                perProtocolPartitionAddressInformation
                                                        .getTransportAddressUris()
                                                        .stream()
                                                        .map(address -> address.getURIAsString())
                                                        .collect(Collectors.toList()))),
                                    null));
                });
    }

    public Mono<PerProtocolPartitionAddressInformation> resolveAddressesAsync(RxDocumentServiceRequest request, boolean forceAddressRefresh) {
        return this.addressResolver.resolveAsync(request, forceAddressRefresh)
                .flatMap(partitionAddressInformation -> Mono.just(partitionAddressInformation.getAddressesByProtocol(this.protocol)));
    }

    public Flux<OpenConnectionResponse> openConnectionsAndInitCaches(String containerLink) {
        return this.addressResolver.openConnectionsAndInitCaches(containerLink);
    }
}

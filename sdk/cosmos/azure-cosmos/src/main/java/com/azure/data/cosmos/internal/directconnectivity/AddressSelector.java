// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity;

import com.azure.data.cosmos.GoneException;
import com.azure.data.cosmos.internal.RxDocumentServiceRequest;
import com.azure.data.cosmos.internal.Strings;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class AddressSelector {
    private final IAddressResolver addressResolver;
    private final Protocol protocol;

    public AddressSelector(IAddressResolver addressResolver, Protocol protocol) {
        this.addressResolver = addressResolver;
        this.protocol = protocol;
    }

    public Mono<List<URI>> resolveAllUriAsync(
        RxDocumentServiceRequest request,
        boolean includePrimary,
        boolean forceRefresh) {
        Mono<List<AddressInformation>> allReplicaAddressesObs = this.resolveAddressesAsync(request, forceRefresh);
        return allReplicaAddressesObs.map(allReplicaAddresses -> allReplicaAddresses.stream().filter(a -> includePrimary || !a.isPrimary())
            .map(a -> HttpUtils.toURI(a.getPhysicalUri())).collect(Collectors.toList()));
    }

    public Mono<URI> resolvePrimaryUriAsync(RxDocumentServiceRequest request, boolean forceAddressRefresh) {
        Mono<List<AddressInformation>> replicaAddressesObs = this.resolveAddressesAsync(request, forceAddressRefresh);
        return replicaAddressesObs.flatMap(replicaAddresses -> {
            try {
                return Mono.just(AddressSelector.getPrimaryUri(request, replicaAddresses));
            } catch (Exception e) {
                return Mono.error(e);
            }
        });
    }

    public static URI getPrimaryUri(RxDocumentServiceRequest request, List<AddressInformation> replicaAddresses) throws GoneException {
        AddressInformation primaryAddress = null;

        if (request.getDefaultReplicaIndex() != null) {
            int defaultReplicaIndex = request.getDefaultReplicaIndex();
            if (defaultReplicaIndex >= 0 && defaultReplicaIndex < replicaAddresses.size()) {
                primaryAddress = replicaAddresses.get(defaultReplicaIndex);
            }
        } else {
            primaryAddress = replicaAddresses.stream().filter(address -> address.isPrimary() && !address.getPhysicalUri().contains("["))
                .findAny().orElse(null);
        }

        if (primaryAddress == null) {
            // Primary endpoint (of the desired protocol) was not found.
            throw new GoneException(String.format("The requested resource is no longer available at the server. Returned addresses are {%s}",
                    replicaAddresses.stream().map(AddressInformation::getPhysicalUri).collect(Collectors.joining(","))), null);
        }

        return HttpUtils.toURI(primaryAddress.getPhysicalUri());
    }

    public Mono<List<AddressInformation>> resolveAddressesAsync(RxDocumentServiceRequest request, boolean forceAddressRefresh) {
        Mono<List<AddressInformation>> resolvedAddressesObs =
            (this.addressResolver.resolveAsync(request, forceAddressRefresh))
                .map(addresses -> Arrays.stream(addresses)
                    .filter(address -> !Strings.isNullOrEmpty(address.getPhysicalUri()) && Strings.areEqualIgnoreCase(address.getProtocolScheme(), this.protocol.scheme()))
                    .collect(Collectors.toList()));

        return resolvedAddressesObs.map(
            resolvedAddresses -> {
                List<AddressInformation> r = resolvedAddresses.stream().filter(address -> !address.isPublic()).collect(Collectors.toList());
                if (r.size() > 0) {
                    return r;
                } else {
                    return resolvedAddresses.stream().filter(AddressInformation::isPublic).collect(Collectors.toList());
                }
            }
        );
    }
}

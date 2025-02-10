// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.CosmosContainerProactiveInitConfig;
import com.azure.cosmos.implementation.GoneException;
import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.Strings;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
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
        Mono<List<AddressInformation>> allReplicaAddressesObs = this.resolveAddressesAsync(request, forceRefresh);
        return allReplicaAddressesObs.map(allReplicaAddresses -> allReplicaAddresses.stream().filter(a -> includePrimary || !a.isPrimary())
            .map(a -> a.getPhysicalUri()).collect(Collectors.toList()));
    }

    public Mono<List<Uri>> resolveAllUriAsync(
        RxDocumentServiceRequest request,
        boolean includePrimary,
        boolean forceRefresh,
        List<Uri> allReplicas) {
        Mono<List<AddressInformation>> allReplicaAddressesObs = this.resolveAddressesAsync(request, forceRefresh);
        return allReplicaAddressesObs.map(allReplicaAddresses -> allReplicaAddresses.stream().map(a -> {
            allReplicas.add(a.getPhysicalUri());
            return a;
            }).filter(a -> includePrimary || !a.isPrimary())
            .map(a -> a.getPhysicalUri()).collect(Collectors.toList()));
    }

    public Mono<Uri> resolvePrimaryUriAsync(RxDocumentServiceRequest request, boolean forceAddressRefresh) {
        Mono<List<AddressInformation>> replicaAddressesObs = this.resolveAddressesAsync(request, forceAddressRefresh);
        return replicaAddressesObs.flatMap(replicaAddresses -> {
            try {
                return Mono.just(AddressSelector.getPrimaryUri(request, replicaAddresses));
            } catch (Exception e) {
                return Mono.error(e);
            }
        });
    }

    public Mono<Uri> resolvePrimaryUriAsync(RxDocumentServiceRequest request, boolean forceAddressRefresh, Set<String> replicaStatuses) {
        Mono<List<AddressInformation>> replicaAddressesObs = this.resolveAddressesAsync(request, forceAddressRefresh);
        return replicaAddressesObs.flatMap(replicaAddresses -> {
            try {
                replicaAddresses.stream().filter(replica -> !replica.isPrimary()).forEach(replica ->
                    replicaStatuses.add(replica.getPhysicalUri().getHealthStatusDiagnosticString()));
                return Mono.just(AddressSelector.getPrimaryUri(request, replicaAddresses));
            } catch (Exception e) {
                return Mono.error(e);
            }
        });
    }

    public static Uri getPrimaryUri(RxDocumentServiceRequest request, List<AddressInformation> replicaAddresses) throws GoneException {
        AddressInformation primaryAddress = null;

        if (request.getDefaultReplicaIndex() != null) {
            int defaultReplicaIndex = request.getDefaultReplicaIndex();
            if (defaultReplicaIndex >= 0 && defaultReplicaIndex < replicaAddresses.size()) {
                primaryAddress = replicaAddresses.get(defaultReplicaIndex);
            }
        } else {
            primaryAddress = replicaAddresses.stream().filter(address -> address.isPrimary() && !address.getPhysicalUri().getURIAsString().contains("["))
                .findAny().orElse(null);
        }

        if (primaryAddress == null) {
            // Primary endpoint (of the desired protocol) was not found.
            throw new GoneException(String.format("The requested resource is no longer available at the server. Returned addresses are {%s}",
                                                  String.join(",", replicaAddresses.stream()
                                                      .map(address -> address.getPhysicalUri().getURIAsString()).collect(Collectors.toList()))));
        }

        return primaryAddress.getPhysicalUri();
    }

    public Mono<List<AddressInformation>> resolveAddressesAsync(RxDocumentServiceRequest request, boolean forceAddressRefresh) {
        Mono<List<AddressInformation>> resolvedAddressesObs =
            (this.addressResolver.resolveAsync(request, forceAddressRefresh))
                .map(addresses -> Arrays.stream(addresses)
                    .filter(address -> !Strings.isNullOrEmpty(address.getPhysicalUri().getURIAsString()) && Strings.areEqualIgnoreCase(address.getProtocolScheme(), this.protocol.scheme()))
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

    public Flux<Void> submitOpenConnectionTasksAndInitCaches(CosmosContainerProactiveInitConfig proactiveContainerInitConfig) {
        return this.addressResolver.submitOpenConnectionTasksAndInitCaches(proactiveContainerInitConfig);
    }
}

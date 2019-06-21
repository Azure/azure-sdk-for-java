/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.azure.data.cosmos.directconnectivity;

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

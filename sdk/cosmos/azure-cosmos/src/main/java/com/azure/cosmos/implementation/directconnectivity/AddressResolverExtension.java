package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdAddressCacheToken;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

public interface AddressResolverExtension extends IAddressResolver {
    URI getAddressResolverURI(RxDocumentServiceRequest request);
    Mono<Void> updateAsync(List<RntbdAddressCacheToken> tokens);
}

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdAddressCacheToken;
import reactor.core.publisher.Mono;

import java.util.List;

public interface AddressResolverExtension extends IAddressResolver {
    Mono<Void> updateAsync(List<RntbdAddressCacheToken> tokens);
}

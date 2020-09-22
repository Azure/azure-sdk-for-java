//------------------------------------------------------------
// Copyright (c) Microsoft Corporation.  All rights reserved.
//------------------------------------------------------------

package com.azure.cosmos.implementation.directconnectivity;

import com.azure.cosmos.implementation.RxDocumentServiceRequest;
import com.azure.cosmos.implementation.directconnectivity.rntbd.RntbdAddressCacheToken;
import reactor.core.publisher.Mono;

import java.net.URI;
import java.util.List;

/**
 * Extends {@link IAddressResolver} with the methods required to support connection endpoint rediscovery.
 */
public interface AddressResolverExtension extends IAddressResolver {
    URI getAddressResolverURI(RxDocumentServiceRequest request);
    void remove(RxDocumentServiceRequest request, List<RntbdAddressCacheToken> tokens);
}

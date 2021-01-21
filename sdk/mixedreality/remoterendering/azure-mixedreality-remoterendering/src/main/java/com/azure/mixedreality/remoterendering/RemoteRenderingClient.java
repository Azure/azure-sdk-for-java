// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.mixedreality.remoterendering;

import com.azure.core.annotation.ServiceClient;

@ServiceClient(builder = RemoteRenderingClientBuilder.class)
public class RemoteRenderingClient {
    private final RemoteRenderingAsyncClient client;

    // package-private constructors only - all instantiation is done with builders
    RemoteRenderingClient(RemoteRenderingAsyncClient client) {
        this.client = client;
    }

    // service methods...

    /*
    // A single response API
    public Response<<model>> <service_operation>(<parameters>) {
        // deferring to async client internally
        return client.<service_operation>(<parameters>).block();
    }

    // A non-paginated sync list API (refer to pagination section for more details)
    public IterableStream<<model>> list<service_operation>(<parameters>) {
        // ...
    }

    // A paginated sync list API (refer to pagination section for more details)
    public PagedIterable<<model>> list<service_operation>(<parameters>) {
        // ...
    }
    */
}

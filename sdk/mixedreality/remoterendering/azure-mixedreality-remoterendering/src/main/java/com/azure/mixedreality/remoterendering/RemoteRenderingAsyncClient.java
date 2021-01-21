// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.mixedreality.remoterendering;

import com.azure.core.annotation.ServiceClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.mixedreality.remoterendering.implementation.MixedRealityRemoteRenderingImpl;

@ServiceClient(builder = RemoteRenderingClientBuilder.class, isAsync = true)
public class RemoteRenderingAsyncClient {
    private MixedRealityRemoteRenderingImpl impl;

    RemoteRenderingAsyncClient(MixedRealityRemoteRenderingImpl impl) {
        this.impl = impl;
    }
}

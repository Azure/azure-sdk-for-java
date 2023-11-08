// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.core.http.vertx.VertxAsyncHttpClientProvider;

module com.azure.core.http.vertx {
    requires transitive com.azure.core;

    requires io.netty.buffer;
    requires io.vertx.core;

    exports com.azure.core.http.vertx;

    provides com.azure.core.http.HttpClientProvider
        with VertxAsyncHttpClientProvider;

    uses com.azure.core.http.HttpClientProvider;
    uses com.azure.core.http.vertx.VertxProvider;
}

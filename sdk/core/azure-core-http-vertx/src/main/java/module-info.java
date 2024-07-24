// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.core.http.vertx.VertxHttpClientProvider;

module com.azure.core.http.vertx {
    requires transitive com.azure.core;

    requires reactor.core;
    requires io.netty.buffer;
    requires io.vertx.core;
    requires org.reactivestreams;

    exports com.azure.core.http.vertx;

    provides com.azure.core.http.HttpClientProvider with VertxHttpClientProvider;

    uses com.azure.core.http.HttpClientProvider;
    uses com.azure.core.http.vertx.VertxProvider;
}

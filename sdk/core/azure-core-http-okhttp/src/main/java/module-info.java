// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module azure.core.http.okhttp {

    requires transitive com.azure.core;
    requires okhttp3;
    requires okio;

    requires reactor.core;
    requires org.reactivestreams;
    requires com.azure.test;

    exports com.azure.core.http.okhttp;
    exports com.azure.core.http.okhttp.implementation;

    provides com.azure.core.http.HttpClientProvider
        with com.azure.core.http.okhttp.implementation.OkHttpClientProvider;

    uses com.azure.core.http.HttpClientProvider;
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.core.http.okhttp {

    requires transitive com.azure.core;
    requires okhttp3;
    requires okio;

    exports com.azure.core.http.okhttp;
    exports com.azure.core.http.okhttp.implementation;      // FIXME this should not be a long-term solution

    provides com.azure.core.http.HttpClientProvider
        with com.azure.core.http.okhttp.implementation.OkHttpClientProvider;

    uses com.azure.core.http.HttpClientProvider;
}

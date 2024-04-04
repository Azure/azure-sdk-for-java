// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module io.clientcore.core.http.okhttp {
    requires transitive io.clientcore.core;

    requires okhttp3;
    requires okio;
    requires kotlin.stdlib;

    exports io.clientcore.core.http.okhttp;

    provides io.clientcore.core.http.client.HttpClientProvider
        with io.clientcore.core.http.okhttp.OkHttpHttpClientProvider;

    uses io.clientcore.core.http.client.HttpClientProvider;
}

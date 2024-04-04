// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import io.clientcore.http.okhttp3.OkHttpHttpClientProvider;

module io.clientcore.http.okhttp {
    requires transitive io.clientcore.core;

    requires okhttp3;
    requires okio;
    requires kotlin.stdlib;

    exports io.clientcore.http.okhttp3;

    provides io.clientcore.core.http.client.HttpClientProvider
        with OkHttpHttpClientProvider;

    uses io.clientcore.core.http.client.HttpClientProvider;
}

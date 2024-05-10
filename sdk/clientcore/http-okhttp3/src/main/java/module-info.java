// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module io.clientcore.http.okhttp3 {
    requires transitive io.clientcore.core;

    requires okhttp3;
    requires okio;
    requires kotlin.stdlib;

    exports io.clientcore.http.okhttp3;

    provides io.clientcore.core.http.client.HttpClientProvider
        with io.clientcore.http.okhttp3.OkHttpHttpClientProvider;

    uses io.clientcore.core.http.client.HttpClientProvider;
}

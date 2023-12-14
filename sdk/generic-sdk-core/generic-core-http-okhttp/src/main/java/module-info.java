// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.generic.core.http.okhttp {
    requires transitive com.generic.core;

    requires okhttp3;
    requires okio;
    requires kotlin.stdlib;

    exports com.generic.core.http.okhttp;

    provides com.generic.core.http.client.HttpClientProvider
        with com.generic.core.http.okhttp.OkHttpHttpClientProvider;

    uses com.generic.core.http.client.HttpClientProvider;
}

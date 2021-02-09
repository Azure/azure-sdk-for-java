// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.core.http.okhttp {
    requires transitive com.azure.core;

    requires okhttp3;
    requires okio;

    exports com.azure.core.http.okhttp;

    provides com.azure.core.http.HttpClientProvider
        with com.azure.core.http.okhttp.OkHttpAsyncClientProvider;

    uses com.azure.core.http.HttpClientProvider;
}

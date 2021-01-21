// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.core.http.okhttp.OkHttpClientProvider;

module com.azure.core.http.okhttp {
    requires transitive com.azure.core;

    requires okhttp3;
    requires okio;

    exports com.azure.core.http.okhttp;

    provides com.azure.core.http.HttpClientProvider
        with OkHttpClientProvider;

    uses com.azure.core.http.HttpClientProvider;
}

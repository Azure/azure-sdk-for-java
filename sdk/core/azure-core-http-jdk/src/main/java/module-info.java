// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.core.http.jdk.implementation.JdkHttpClientProvider;

module com.azure.core.http.jdk {
    requires transitive com.azure.core;
    requires java.net.http;

    exports com.azure.core.http.jdk;

    provides com.azure.core.http.HttpClientProvider
        with JdkHttpClientProvider;

    uses com.azure.core.http.HttpClientProvider;
}

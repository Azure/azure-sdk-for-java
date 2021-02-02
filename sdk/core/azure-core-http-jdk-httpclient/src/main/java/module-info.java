// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module com.azure.core.http.jdk.httpclient {
    requires transitive com.azure.core;
    requires java.net.http;

    exports com.azure.core.http.jdk.httpclient;

    provides com.azure.core.http.HttpClientProvider
        with com.azure.core.http.jdk.httpclient.JdkHttpClientProvider;

    uses com.azure.core.http.HttpClientProvider;
}

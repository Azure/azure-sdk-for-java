// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

module io.clientcore.http.jdk.httpclient {
    requires transitive io.clientcore.core;
    requires java.net.http;

    exports io.clientcore.http.jdk.httpclient;

    provides io.clientcore.core.http.client.HttpClientProvider
        with io.clientcore.http.jdk.httpclient.JdkHttpClientProvider;

    uses io.clientcore.core.http.client.HttpClientProvider;
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

import com.azure.core.http.jdk11.httpclient.implementation.Jdk11HttpClientProvider;

module com.azure.core.http.jdk11.httpclient {
    requires transitive com.azure.core;
    requires java.net.http;

    exports com.azure.core.http.jdk11.httpclient;

    provides com.azure.core.http.HttpClientProvider
        with Jdk11HttpClientProvider;

    uses com.azure.core.http.HttpClientProvider;
}

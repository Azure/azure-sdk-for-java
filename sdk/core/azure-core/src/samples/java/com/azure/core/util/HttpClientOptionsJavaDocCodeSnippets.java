// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpProtocolVersion;

/**
 * Code snippets for {@link HttpClientOptions}.
 */
public class HttpClientOptionsJavaDocCodeSnippets {
    /**
     * Creates an HTTP client with HTTP/2 support without selecting a transport implementation.
     */
    public void configureHttpVersion() {
        // BEGIN: readme-sample-configureHttpVersionWithOptions
        HttpClientOptions options = new HttpClientOptions()
            .setMaximumHttpVersion(HttpProtocolVersion.HTTP_2);
        HttpClient client = HttpClient.createDefault(options);
        // END: readme-sample-configureHttpVersionWithOptions
    }
}

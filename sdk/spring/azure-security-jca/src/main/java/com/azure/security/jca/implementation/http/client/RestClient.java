// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.jca.implementation.http.client;

import java.util.Map;

public interface RestClient {

    String get(String url, Map<String, String> headers);

    String post(String url, String body, String contentType);
}

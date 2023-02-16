// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.personalizer.implementation.util;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.util.ClientOptions;

import java.time.Duration;

public class Constants {

    public static final Duration DEFAULT_POLL_INTERVAL = Duration.ofSeconds(5);
    static final String PERSONALIZER_PROPERTIES = "azure-ai-personalizer.properties";
    static final String NAME = "name";
    static final String OCP_APIM_SUBSCRIPTION_KEY = "Ocp-Apim-Subscription-Key";
    static final String VERSION = "version";
    static final ClientOptions DEFAULT_CLIENT_OPTIONS = new ClientOptions();
    static final HttpHeaders DEFAULT_HTTP_HEADERS = new HttpHeaders();
    static final HttpLogOptions DEFAULT_LOG_OPTIONS = new HttpLogOptions();
}

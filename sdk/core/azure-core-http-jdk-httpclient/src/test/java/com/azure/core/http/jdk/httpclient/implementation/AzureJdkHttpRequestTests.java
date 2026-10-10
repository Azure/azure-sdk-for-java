// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.jdk.httpclient.implementation;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpRequest;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullSource;

import java.net.http.HttpClient;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

/**
 * Tests protocol preferences on native JDK requests.
 */
public class AzureJdkHttpRequestTests {
    private static final ClientLogger LOGGER = new ClientLogger(AzureJdkHttpRequestTests.class);

    @ParameterizedTest
    @EnumSource(HttpClient.Version.class)
    @NullSource
    public void preservesRequestVersionPreference(HttpClient.Version version) {
        AzureJdkHttpRequest request = new AzureJdkHttpRequest(new HttpRequest(HttpMethod.GET, "https://localhost"),
            Context.NONE, Collections.emptySet(), LOGGER, null, null, version);
        assertEquals(Optional.ofNullable(version), request.version());
    }

    @Test
    public void existingConstructorUsesNativeClientPreference() {
        AzureJdkHttpRequest request = new AzureJdkHttpRequest(new HttpRequest(HttpMethod.GET, "https://localhost"),
            Context.NONE, Collections.emptySet(), LOGGER, null, null);
        assertFalse(request.version().isPresent());
    }
}

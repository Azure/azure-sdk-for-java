// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys;

import com.azure.core.http.HttpClient;
import org.junit.jupiter.api.Test;

public interface KeyClientManagedHsmTestBase {
    @Test
    void createOctKeyWithDefaultSize(HttpClient httpClient, KeyServiceVersion serviceVersion);

    @Test
    void createOctKeyWithValidSize(HttpClient httpClient, KeyServiceVersion serviceVersion);

    @Test
    void createOctKeyWithInvalidSize(HttpClient httpClient, KeyServiceVersion serviceVersion);

    @Test
    void getRandomBytes(HttpClient httpClient, KeyServiceVersion serviceVersion);
}

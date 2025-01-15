// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.implementation.keyvault.environment;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.security.keyvault.secrets.models.SecretProperties;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.Mockito.when;

public class KeyVaultSecretClientMockUtils {

    public static void mockSecretClientListPropertiesOfSecrets(SecretClient secretClient, SecretProperties... secretPropertiesList) {
        OnePageResponse<SecretProperties> secretResponse = new OnePageResponse<>(List.of(secretPropertiesList));
        when(secretClient.listPropertiesOfSecrets())
            .thenReturn(new PagedIterable<>(new PagedFlux<>(() -> Mono.just(secretResponse))));
    }

    public static KeyVaultSecret mockSecretClientGetSecretMethod(SecretClient secretClient, String secretName, String secretValue) {
        return mockSecretClientGetSecretMethod(secretClient, secretName, secretValue, true);
    }

    public static KeyVaultSecret mockSecretClientGetSecretMethod(SecretClient secretClient,
                                                                 String secretName,
                                                                 String secretValue,
                                                                 boolean enabled) {
        KeyVaultSecret keyVaultSecret = new KeyVaultSecret(secretName, secretValue);
        keyVaultSecret.getProperties().setEnabled(enabled);
        when(secretClient.getSecret(secretName, null)).thenReturn(keyVaultSecret);
        when(secretClient.getSecret(secretName)).thenReturn(keyVaultSecret);
        return keyVaultSecret;
    }
}

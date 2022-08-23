// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.secrets;

import com.azure.core.util.Context;
import com.azure.security.keyvault.secrets.implementation.SecretClientImpl;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.spy;

public class SecretAsyncClientInterruptedTests {
    private final SecretClientImpl implClient = new SecretClientImpl("http://localhost:8080", null, null);
    private final SecretAsyncClient secretAsyncClient = new SecretAsyncClient(implClient);

    @Test
    public void setSecretInterrupted() {
        SecretAsyncClient secretAsyncClientSpy = spy(secretAsyncClient);
        doThrow(new RuntimeException()).when(secretAsyncClientSpy).setSecretWithResponse(nullable(KeyVaultSecret.class));
        KeyVaultSecret secretToSet = new KeyVaultSecret("abc", "def");
        StepVerifier.create(secretAsyncClientSpy.setSecret(secretToSet))
            .expectError(RuntimeException.class)
            .verify();
    }

    @Test
    public void setSecretStringInterrupted() {
        SecretClientImpl implClientSpy = spy(implClient);
        SecretAsyncClient secretAsyncClientSpy = new SecretAsyncClient(implClientSpy);
        doThrow(new RuntimeException()).when(implClientSpy).setSecretWithResponseAsync(nullable(String.class), nullable(String.class), nullable(Context.class));
        StepVerifier.create(secretAsyncClientSpy.setSecret("abc", "def"))
            .expectError(RuntimeException.class)
            .verify();
    }

    @Test
    public void setSecretWithResponseInterrupted() {
        SecretClientImpl implClientSpy = spy(implClient);
        SecretAsyncClient secretAsyncClientSpy = new SecretAsyncClient(implClientSpy);
        doThrow(new RuntimeException()).when(implClientSpy).setSecretWithResponseAsync(nullable(KeyVaultSecret.class), nullable(Context.class));
        KeyVaultSecret secretToSet = new KeyVaultSecret("abc", "def");
        StepVerifier.create(secretAsyncClientSpy.setSecretWithResponse(secretToSet))
            .expectError(RuntimeException.class)
            .verify();
    }

    @Test
    public void getSecretInterrupted() {
        SecretAsyncClient secretAsyncClientSpy = spy(secretAsyncClient);
        doThrow(new RuntimeException()).when(secretAsyncClientSpy).getSecretWithResponse(nullable(String.class), nullable(String.class));
        StepVerifier.create(secretAsyncClientSpy.getSecret("secret"))
            .expectError(RuntimeException.class)
            .verify();
    }

    @Test
    public void getSecretVersionInterrupted() {
        SecretAsyncClient secretAsyncClientSpy = spy(secretAsyncClient);
        doThrow(new RuntimeException()).when(secretAsyncClientSpy).getSecretWithResponse(nullable(String.class), nullable(String.class));
        StepVerifier.create(secretAsyncClientSpy.getSecret("secret", "version"))
            .expectError(RuntimeException.class)
            .verify();
    }

    @Test
    public void getDeletedSecretInterrupted() {
        SecretAsyncClient secretAsyncClientSpy = spy(secretAsyncClient);
        doThrow(new RuntimeException()).when(secretAsyncClientSpy).getDeletedSecretWithResponse(nullable(String.class));
        StepVerifier.create(secretAsyncClientSpy.getDeletedSecret("secret"))
            .expectError(RuntimeException.class)
            .verify();
    }

    @Test
    public void backupSecretInterrupted() {
        SecretAsyncClient secretAsyncClientSpy = spy(secretAsyncClient);
        doThrow(new RuntimeException()).when(secretAsyncClientSpy).backupSecretWithResponse(nullable(String.class));
        StepVerifier.create(secretAsyncClientSpy.backupSecret("secret"))
            .expectError(RuntimeException.class)
            .verify();
    }

    @Test
    public void purgeDeletedSecretInterrupted() {
        SecretAsyncClient secretAsyncClientSpy = spy(secretAsyncClient);
        doThrow(new RuntimeException()).when(secretAsyncClientSpy).purgeDeletedSecretWithResponse(nullable(String.class));
        StepVerifier.create(secretAsyncClientSpy.purgeDeletedSecret("secret"))
            .expectError(RuntimeException.class)
            .verify();
    }

}

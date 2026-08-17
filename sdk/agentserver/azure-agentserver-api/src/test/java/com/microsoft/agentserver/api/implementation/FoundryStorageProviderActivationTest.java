// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api.implementation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit test for the activation contract of {@link FoundryStorageProvider}.
 * <p>
 * The provider is auto-activated by {@code ResponsesApi.Builder.resolveProvider()}
 * when {@code FOUNDRY_HOSTING_ENVIRONMENT} is set. Its {@link FoundryStorageProvider#fromEnvironment()}
 * factory requires {@code FOUNDRY_PROJECT_ENDPOINT} and must fail fast (rather than
 * silently produce a misconfigured provider) when it is absent — that signal is what
 * the builder relies on to log + fall back to in-memory.
 */
class FoundryStorageProviderActivationTest {

    @Test
    @DisplayName("fromEnvironment throws when FOUNDRY_PROJECT_ENDPOINT is not set")
    @DisabledIfEnvironmentVariable(named = "FOUNDRY_PROJECT_ENDPOINT", matches = ".+",
        disabledReason = "Only meaningful when the env var is absent")
    void fromEnvironmentFailsWithoutEndpoint() {
        IllegalStateException ex = assertThrows(IllegalStateException.class,
            FoundryStorageProvider::fromEnvironment);
        assertTrue(ex.getMessage().contains("FOUNDRY_PROJECT_ENDPOINT"),
            "message should explain the missing env var, was: " + ex.getMessage());
    }
}


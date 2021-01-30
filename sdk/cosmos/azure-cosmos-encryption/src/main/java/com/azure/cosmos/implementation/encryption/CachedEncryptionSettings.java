// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import java.time.Instant;

public final class CachedEncryptionSettings {
    private final EncryptionSettings encryptionSettings;
    private final Instant encryptionSettingsExpiryUtc;

    public CachedEncryptionSettings(EncryptionSettings encryptionSettings,
                                    Instant encryptionSettingsExpiryUtc) {
        if (encryptionSettings == null) {
            throw new IllegalArgumentException("encryptionSettings is null");
        }
        this.encryptionSettings = encryptionSettings;
        this.encryptionSettingsExpiryUtc = encryptionSettingsExpiryUtc;
    }

    public EncryptionSettings getEncryptionSettings() {
        return encryptionSettings;
    }

    public Instant getEncryptionSettingsExpiryUtc() {
        return encryptionSettingsExpiryUtc;
    }
}

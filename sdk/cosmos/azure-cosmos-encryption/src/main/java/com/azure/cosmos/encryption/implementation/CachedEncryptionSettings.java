// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption.implementation;

import java.time.Instant;

final class CachedEncryptionSettings {
    private final EncryptionSettings encryptionSettings;
    private final Instant encryptionSettingsExpiryUtc;

    CachedEncryptionSettings(EncryptionSettings encryptionSettings,
                             Instant encryptionSettingsExpiryUtc) {
        if (encryptionSettings == null) {
            throw new IllegalArgumentException("encryptionSettings is null");
        }
        this.encryptionSettings = encryptionSettings;
        this.encryptionSettingsExpiryUtc = encryptionSettingsExpiryUtc;
    }

    EncryptionSettings getEncryptionSettings() {
        return encryptionSettings;
    }

    Instant getEncryptionSettingsExpiryUtc() {
        return encryptionSettingsExpiryUtc;
    }
}

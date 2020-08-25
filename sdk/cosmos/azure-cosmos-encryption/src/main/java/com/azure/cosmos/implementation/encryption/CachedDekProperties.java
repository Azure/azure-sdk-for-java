// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import java.time.Instant;

class CachedDekProperties {
    private final DataEncryptionKeyProperties serverProperties;
    private final Instant serverPropertiesExpiryUtc;

    public CachedDekProperties(
        DataEncryptionKeyProperties serverProperties,
        Instant serverPropertiesExpiryUtc) {
        assert(serverProperties != null);

        this.serverProperties = serverProperties;
        this.serverPropertiesExpiryUtc = serverPropertiesExpiryUtc;
    }

    public DataEncryptionKeyProperties getServerProperties() {
        return serverProperties;
    }
    public Instant getServerPropertiesExpiryUtc() {
        return serverPropertiesExpiryUtc;
    }
}

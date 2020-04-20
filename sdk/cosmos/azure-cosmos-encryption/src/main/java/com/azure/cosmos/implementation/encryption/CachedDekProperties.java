// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import java.time.Instant;

class CachedDekProperties {
    private final DataEncryptionKeyProperties ServerProperties ;
    private final Instant ServerPropertiesExpiryUtc;

    public CachedDekProperties(
        DataEncryptionKeyProperties serverProperties,
        Instant serverPropertiesExpiryUtc) {
        assert(serverProperties != null);

        this.ServerProperties = serverProperties;
        this.ServerPropertiesExpiryUtc = serverPropertiesExpiryUtc;
    }

    public DataEncryptionKeyProperties getServerProperties() {
        return ServerProperties;
    }
    public Instant getServerPropertiesExpiryUtc() {
        return ServerPropertiesExpiryUtc;
    }
}

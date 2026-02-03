// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.kafka.connect.implementation;

import com.azure.cosmos.implementation.apachecommons.lang.StringUtils;

import java.util.Objects;

import static com.azure.cosmos.implementation.guava25.base.Preconditions.checkArgument;

public class CosmosMasterKeyAuthConfig implements CosmosAuthConfig {
    private final String masterKey;

    public CosmosMasterKeyAuthConfig(String masterKey) {
        checkArgument(StringUtils.isNotEmpty(masterKey), "Argument 'masterKey' should not be null");

        this.masterKey = masterKey;
    }

    public String getMasterKey() {
        return masterKey;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        CosmosMasterKeyAuthConfig that = (CosmosMasterKeyAuthConfig) o;
        return Objects.equals(masterKey, that.masterKey);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(masterKey);
    }
}

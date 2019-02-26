// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.azconfig.models;

public class KeyValueListFilter extends KeyValueGenericFilter<KeyValueListFilter> {
    private String key;
    private String range;
    /**
     * Sets the key to filter results.
     * @param key
     * @return KeyValueListFilter object itself
     */
    public KeyValueListFilter withKey(String key) {
        this.key = key;
        return this;
    }

    public String key() {
        return this.key;
    }

    public KeyValueListFilter withRange(String range) {
        this.range = range;
        return this;
    }

    public String range() {
        return this.range;
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file.datalake.options;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Encryption scope options to be used when creating a file system.
 */
public class FileSystemEncryptionScope {

    /*
     * Optional.  Version 2021-06-08 and later. Specifies the default
     * encryption scope to set on the container and use for all future writes.
     */
    @JsonProperty(value = "DefaultEncryptionScope")
    private String defaultEncryptionScope;

    /*
     * Optional.  Version 2021-06-08 and newer. If true, prevents any request
     * from specifying a different encryption scope than the scope set on the
     * container.
     */
    @JsonProperty(value = "EncryptionScopeOverridePrevented")
    private Boolean encryptionScopeOverridePrevented;

    /**
     * Get the defaultEncryptionScope property: Optional. Version 2021-06-08 and later. Specifies the default encryption
     * scope to set on the file system and use for all future writes.
     *
     * @return the defaultEncryptionScope value.
     */
    public String getDefaultEncryptionScope() {
        return defaultEncryptionScope;
    }

    /**
     * Set the defaultEncryptionScope property: Optional. Version 2021-06-08 and later. Specifies the default encryption
     * scope to set on the file system and use for all future writes.
     *
     * @param encryptionScope the defaultEncryptionScope value to set.
     * @return the updated {@link FileSystemEncryptionScope}.
     */
    public FileSystemEncryptionScope setDefaultEncryptionScope(String encryptionScope) {
        this.defaultEncryptionScope = encryptionScope;
        return this;
    }

    /**
     * Get the encryptionScopeOverridePrevented property: Optional. Version 2021-06-08 and newer. If true, prevents any
     * request from specifying a different encryption scope than the scope set on the container.
     *
     * @return the encryptionScopeOverridePrevented value.
     */
    public Boolean isEncryptionScopeOverridePrevented() {
        return encryptionScopeOverridePrevented;
    }

    /**
     * Set the encryptionScopeOverridePrevented property: Optional. Version 2021-06-08 and newer. If true, prevents any
     * request from specifying a different encryption scope than the scope set on the container.
     *
     * @param encryptionScopeOverridePrevented the encryptionScopeOverridePrevented value to set.
     * @return the updated {@link FileSystemEncryptionScope}.
     */
    public FileSystemEncryptionScope setEncryptionScopeOverridePrevented(Boolean encryptionScopeOverridePrevented) {
        this.encryptionScopeOverridePrevented = encryptionScopeOverridePrevented;
        return this;
    }
}

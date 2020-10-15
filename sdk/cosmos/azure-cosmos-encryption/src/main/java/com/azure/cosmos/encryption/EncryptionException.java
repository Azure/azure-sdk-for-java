// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.core.exception.AzureException;
import com.azure.cosmos.implementation.guava25.base.Preconditions;

/**
 * Encryption exception
 */
public class EncryptionException extends AzureException {

    private String dataEncryptionKeyId;
    private String encryptedContent;

    /**
     * Gets the Data Encryption Key Id used.
     *
     * @return data encryption key id.
     */
    public String getDataEncryptionKeyId() {
        return this.dataEncryptionKeyId;
    }

    /**
     * Gets the raw encrypted content as string.
     *
     * @return raw content.
     */
    public String getEncryptedContent() {
        return this.encryptedContent;
    }

    /**
     * Initializes a new instance of the {@link EncryptionException}
     *
     * @param dataEncryptionKeyId DataEncryptionKey
     * @param encryptedContent Encrypted content
     * @param innerException The inner exceptio
     */
    EncryptionException(String dataEncryptionKeyId,
                        String encryptedContent,
                        Exception innerException) {
        super(innerException.getMessage(), innerException);

        Preconditions.checkNotNull(encryptedContent, "encryptedContent");

        this.dataEncryptionKeyId = dataEncryptionKeyId;
        this.encryptedContent = encryptedContent;
    }
}


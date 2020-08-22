// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import java.net.URL;

/**
 * Metadata used by Azure Key Vault to wrap (encrypt) and unwrap (decrypt) keys.
 */
public final class AzureKeyVaultKeyWrapMetadata extends EncryptionKeyWrapMetadata {
    static final String TypeConstant = "akv";

    // TODO: moderakh use URL vs URI?
    /**
     * Creates a new instance of metadata that the Azure Key Vault can use to wrap and unwrap keys.
     *
     * @param masterKeyUri Key Vault URL of the master key to be used for wrapping and unwrapping keys.
     */
    public AzureKeyVaultKeyWrapMetadata(URL masterKeyUri) {
        // masterKeyUri.AbsoluteUri
        super(AzureKeyVaultKeyWrapMetadata.TypeConstant, masterKeyUri.toString());
    }
}

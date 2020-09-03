// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.cosmos.implementation.encryption.ImplementationBridgeHelpers;

import java.net.URL;

import static com.azure.cosmos.implementation.encryption.ImplementationBridgeHelpers.AzureKeyVaultKeyWrapMetadataHelper;

/**
 * Metadata used by Azure Key Vault to wrap (encrypt) and unwrap (decrypt) keys.
 */
public final class AzureKeyVaultKeyWrapMetadata extends EncryptionKeyWrapMetadata {
    private static final String TYPE_CONSTANT = "akv";

    // TODO: moderakh use URL vs URI?
    /**
     * Creates a new instance of metadata that the Azure Key Vault can use to wrap and unwrap keys.
     *
     * @param masterKeyUri Key Vault URL of the master key to be used for wrapping and unwrapping keys.
     */
    public AzureKeyVaultKeyWrapMetadata(URL masterKeyUri) {
        // masterKeyUri.AbsoluteUri
        super(AzureKeyVaultKeyWrapMetadata.TYPE_CONSTANT, masterKeyUri.toString());
    }

    ///////////////////////////////////////////////////////////////////////////////////////////
    // the following helper/accessor only helps to access this class outside of this package.//
    ///////////////////////////////////////////////////////////////////////////////////////////

    static {
        AzureKeyVaultKeyWrapMetadataHelper.setAzureKeyVaultKeyWrapMetadataAccessor(
            new ImplementationBridgeHelpers.AzureKeyVaultKeyWrapMetadataHelper.AzureKeyVaultKeyWrapMetadataAccessor() {
                @Override
                public String getTypeConstant() {
                    return TYPE_CONSTANT;
                }
            }
        );
    }
}

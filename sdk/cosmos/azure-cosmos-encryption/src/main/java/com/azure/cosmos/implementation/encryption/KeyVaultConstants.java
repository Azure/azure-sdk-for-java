// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.encryption;

import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;

public class KeyVaultConstants {
    // TODO: "keys/"? check with dotnet
    public static final String KEYS_SEGMENT = "keys";
    public static final String AUTHENTICATION_CHALLENGE_PREFIX = "Bearer ";
    public static final String AUTHENTICATION_RESPONSE_HEADER_NAME = "WWW-Authenticate";
    public static final String AUTHENTICATION_PARAMETER = "authorization";
    public static final KeyWrapAlgorithm RsaOaep256 = KeyWrapAlgorithm.RSA_OAEP_256;

    public static class DeletionRecoveryLevel {
        public static final String PURGEABLE = "Purgeable";
        public static final String RECOVERABLE = "Recoverable";
        public static final String RECOVERABLE_PROTECTED_SUBSCRIPTION = "Recoverable+ProtectedSubscription";
        public static final String RECOVERABLE_PURGEABLE = "Recoverable+Purgeable";
        public static final String CUSTOMIZED_RECOVERABLE = "CustomizedRecoverable";
        public static final String CUSTOMIZED_RECOVERABLE_PROTECTED_SUBSCRIPTION = "CustomizedRecoverable"
            + "+ProtectedSubscription";
        public static final String CUSTOMIZED_RECOVERABLE_PURGEABLE = "CustomizedRecoverable+Purgeable";
    }

    public static class AzureKeyVaultKeyWrapMetadata {
        public static final String TYPE_CONSTANT = ImplementationBridgeHelpers.AzureKeyVaultKeyWrapMetadataHelper.getTypeConstant();
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;

// TODO: can this class move to implementation?
public class KeyVaultConstants {
    // TODO: "keys/"? check with dotnet
    static final String KeysSegment = "keys";
    static final String AuthenticationChallengePrefix = "Bearer ";
    static final String AuthenticationResponseHeaderName = "WWW-Authenticate";
    static final String AuthenticationParameter = "authorization";
    // TODO: this doesn't need to be public FiXME
    public static final KeyWrapAlgorithm RsaOaep256 = KeyWrapAlgorithm.RSA_OAEP_256;

    static class DeletionRecoveryLevel {
        public static final String Purgeable = "Purgeable";
        public static final String Recoverable = "Recoverable";
        public static final String RecoverableProtectedSubscription = "Recoverable+ProtectedSubscription";
        public static final String RecoverablePurgeable = "Recoverable+Purgeable";
        public static final String CustomizedRecoverable = "CustomizedRecoverable";
        public static final String CustomizedRecoverableProtectedSubscription = "CustomizedRecoverable"
            + "+ProtectedSubscription";
        public static final String CustomizedRecoverablePurgeable = "CustomizedRecoverable+Purgeable";
    }
}

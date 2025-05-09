// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.keys.cryptography.implementation;

import com.azure.v2.security.keyvault.keys.cryptography.models.DecryptParameters;
import com.azure.v2.security.keyvault.keys.cryptography.models.DecryptResult;
import com.azure.v2.security.keyvault.keys.cryptography.models.EncryptParameters;
import com.azure.v2.security.keyvault.keys.cryptography.models.EncryptResult;
import com.azure.v2.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.azure.v2.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.azure.v2.security.keyvault.keys.cryptography.models.SignResult;
import com.azure.v2.security.keyvault.keys.cryptography.models.SignatureAlgorithm;
import com.azure.v2.security.keyvault.keys.cryptography.models.UnwrapResult;
import com.azure.v2.security.keyvault.keys.cryptography.models.VerifyResult;
import com.azure.v2.security.keyvault.keys.cryptography.models.WrapResult;
import com.azure.v2.security.keyvault.keys.models.JsonWebKey;
import io.clientcore.core.http.models.RequestContext;

public abstract class LocalKeyCryptographyClient {
    final CryptographyClientImpl implClient;
    final JsonWebKey jsonWebKey;

    LocalKeyCryptographyClient(JsonWebKey jsonWebKey, CryptographyClientImpl implClient) {
        this.jsonWebKey = jsonWebKey;
        this.implClient = implClient;
    }

    public abstract EncryptResult encrypt(EncryptionAlgorithm algorithm, byte[] plaintext,
        RequestContext requestContext);

    public abstract EncryptResult encrypt(EncryptParameters encryptParameters, RequestContext requestContext);

    public abstract DecryptResult decrypt(EncryptionAlgorithm algorithm, byte[] plaintext,
        RequestContext requestContext);

    public abstract DecryptResult decrypt(DecryptParameters decryptParameters, RequestContext requestContext);

    public abstract SignResult sign(SignatureAlgorithm algorithm, byte[] digest, RequestContext requestContext);

    public abstract VerifyResult verify(SignatureAlgorithm algorithm, byte[] digest, byte[] signature,
        RequestContext requestContext);

    public abstract WrapResult wrapKey(KeyWrapAlgorithm algorithm, byte[] keyToWrap, RequestContext requestContext);

    public abstract UnwrapResult unwrapKey(KeyWrapAlgorithm algorithm, byte[] encryptedKey,
        RequestContext requestContext);

    public abstract SignResult signData(SignatureAlgorithm algorithm, byte[] data, RequestContext requestContext);

    public abstract VerifyResult verifyData(SignatureAlgorithm algorithm, byte[] data, byte[] signature,
        RequestContext requestContext);

    public JsonWebKey getJsonWebKey() {
        return jsonWebKey;
    }
}

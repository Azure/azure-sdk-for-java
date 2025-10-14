// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.keys.cryptography.implementation;

import com.azure.v2.security.keyvault.keys.KeyServiceVersion;
import com.azure.v2.security.keyvault.keys.cryptography.CryptographyServiceVersion;
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
import com.azure.v2.security.keyvault.keys.implementation.KeyClientImpl;
import com.azure.v2.security.keyvault.keys.implementation.SecretMinClientImpl;
import com.azure.v2.security.keyvault.keys.implementation.models.KeyBundle;
import com.azure.v2.security.keyvault.keys.implementation.models.KeyOperationResult;
import com.azure.v2.security.keyvault.keys.implementation.models.KeyOperationsParameters;
import com.azure.v2.security.keyvault.keys.implementation.models.KeySignParameters;
import com.azure.v2.security.keyvault.keys.implementation.models.KeyVerifyParameters;
import com.azure.v2.security.keyvault.keys.implementation.models.KeyVerifyResult;
import com.azure.v2.security.keyvault.keys.implementation.models.SecretBundle;
import com.azure.v2.security.keyvault.keys.implementation.models.SecretSetParameters;
import com.azure.v2.security.keyvault.keys.models.JsonWebKey;
import com.azure.v2.security.keyvault.keys.models.KeyVaultKey;
import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.http.models.Response;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.models.CoreException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;

import static com.azure.v2.security.keyvault.keys.cryptography.implementation.CryptographyUtils.mapKeyEncryptionAlgorithm;
import static com.azure.v2.security.keyvault.keys.cryptography.implementation.CryptographyUtils.mapKeySignatureAlgorithm;
import static com.azure.v2.security.keyvault.keys.cryptography.implementation.CryptographyUtils.mapWrapAlgorithm;
import static com.azure.v2.security.keyvault.keys.cryptography.implementation.CryptographyUtils.transformSecretBundle;
import static com.azure.v2.security.keyvault.keys.cryptography.implementation.CryptographyUtils.unpackAndValidateId;
import static com.azure.v2.security.keyvault.keys.implementation.models.KeyVaultKeysModelsUtils.createKeyVaultKey;

public final class CryptographyClientImpl {
    private static final ClientLogger LOGGER = new ClientLogger(CryptographyClientImpl.class);

    private final KeyClientImpl keyClient;
    private final SecretMinClientImpl secretClient;
    private final String keyId;
    private final String endpoint;
    private final String keyCollection;
    private final String keyName;
    private final String keyVersion;

    public CryptographyClientImpl(String keyId, HttpPipeline pipeline, CryptographyServiceVersion serviceVersion) {
        Objects.requireNonNull(keyId);

        List<String> data = unpackAndValidateId(keyId, LOGGER);

        this.endpoint = data.get(0);
        this.keyCollection = data.get(1);
        this.keyName = data.get(2);
        this.keyVersion = data.get(3);
        this.keyId = keyId;
        this.keyClient = new KeyClientImpl(pipeline, endpoint, KeyServiceVersion.valueOf(serviceVersion.getVersion()));
        this.secretClient = new SecretMinClientImpl(pipeline, endpoint, serviceVersion.getVersion());
    }

    public String getKeyId() {
        return keyId;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public String getKeyCollection() {
        return keyCollection;
    }

    public Response<KeyVaultKey> getKeyWithResponse(RequestContext requestContext) {
        Response<KeyBundle> response = keyClient.getKeyWithResponse(keyName, keyVersion, requestContext);

        return new Response<>(response.getRequest(), response.getStatusCode(), response.getHeaders(),
            createKeyVaultKey(response.getValue()));
    }

    public JsonWebKey getSecretKey() {
        return transformSecretBundle(
            secretClient.getSecretWithResponse(keyName, keyVersion, RequestContext.none()).getValue());
    }

    public Response<SecretBundle> setSecretKey(SecretBundle secret, RequestContext requestContext) {
        Objects.requireNonNull(secret, "'secret' parameter cannot be null.");

        SecretSetParameters secretSetParameters
            = new SecretSetParameters(secret.getValue()).setSecretAttributes(secret.getAttributes())
                .setContentType(secret.getContentType())
                .setTags(secret.getTags());

        return secretClient.setSecretWithResponse("", secretSetParameters, requestContext);
    }

    public EncryptResult encrypt(EncryptionAlgorithm algorithm, byte[] plaintext) {
        Objects.requireNonNull(algorithm, "Encryption algorithm cannot be null.");
        Objects.requireNonNull(plaintext, "Plaintext cannot be null.");

        return encrypt(algorithm, plaintext, null, null, RequestContext.none());
    }

    public EncryptResult encrypt(EncryptParameters encryptParameters, RequestContext requestContext) {

        Objects.requireNonNull(encryptParameters, "Encrypt parameters cannot be null.");

        return encrypt(encryptParameters.getAlgorithm(), encryptParameters.getPlainText(), encryptParameters.getIv(),
            encryptParameters.getAdditionalAuthenticatedData(), requestContext);
    }

    EncryptResult encrypt(EncryptionAlgorithm algorithm, byte[] plainText, byte[] iv,
        byte[] additionalAuthenticatedData, RequestContext requestContext) {

        KeyOperationsParameters keyOperationsParameters
            = new KeyOperationsParameters(mapKeyEncryptionAlgorithm(algorithm), plainText).setIv(iv)
                .setAad(additionalAuthenticatedData);

        try (Response<KeyOperationResult> response
            = keyClient.encryptWithResponse(keyName, keyOperationsParameters, keyVersion, requestContext)) {

            KeyOperationResult result = response.getValue();

            return new EncryptResult(result.getResult(), algorithm, keyId, result.getIv(),
                result.getAuthenticationTag(), result.getAdditionalAuthenticatedData());
        }
    }

    public DecryptResult decrypt(EncryptionAlgorithm algorithm, byte[] ciphertext, RequestContext requestContext) {

        Objects.requireNonNull(algorithm, "Encryption algorithm cannot be null.");
        Objects.requireNonNull(ciphertext, "Ciphertext cannot be null.");

        return decrypt(algorithm, ciphertext, null, null, null, requestContext);
    }

    public DecryptResult decrypt(DecryptParameters decryptParameters, RequestContext requestContext) {

        Objects.requireNonNull(decryptParameters, "Decrypt parameters cannot be null.");

        return decrypt(decryptParameters.getAlgorithm(), decryptParameters.getCipherText(), decryptParameters.getIv(),
            decryptParameters.getAdditionalAuthenticatedData(), decryptParameters.getAuthenticationTag(),
            requestContext);
    }

    DecryptResult decrypt(EncryptionAlgorithm algorithm, byte[] ciphertext, byte[] iv,
        byte[] additionalAuthenticatedData, byte[] authenticationTag, RequestContext requestContext) {

        KeyOperationsParameters keyOperationsParameters
            = new KeyOperationsParameters(mapKeyEncryptionAlgorithm(algorithm), ciphertext).setIv(iv)
                .setAad(additionalAuthenticatedData)
                .setTag(authenticationTag);

        try (Response<KeyOperationResult> response
            = keyClient.decryptWithResponse(keyName, keyOperationsParameters, keyVersion, requestContext)) {

            return new DecryptResult(response.getValue().getResult(), algorithm, keyId);
        }
    }

    public SignResult sign(SignatureAlgorithm algorithm, byte[] digest, RequestContext requestContext) {

        Objects.requireNonNull(algorithm, "Signature algorithm cannot be null.");
        Objects.requireNonNull(digest, "Digest content cannot be null.");

        KeySignParameters keySignParameters = new KeySignParameters(mapKeySignatureAlgorithm(algorithm), digest);

        try (Response<KeyOperationResult> response
            = keyClient.signWithResponse(keyName, keySignParameters, keyVersion, requestContext)) {

            return new SignResult(response.getValue().getResult(), algorithm, keyId);
        }
    }

    public VerifyResult verify(SignatureAlgorithm algorithm, byte[] digest, byte[] signature,
        RequestContext requestContext) {

        Objects.requireNonNull(algorithm, "Signature algorithm cannot be null.");
        Objects.requireNonNull(digest, "Digest content cannot be null.");
        Objects.requireNonNull(signature, "Signature to be verified cannot be null.");

        KeyVerifyParameters keyVerifyParameters
            = new KeyVerifyParameters(mapKeySignatureAlgorithm(algorithm), digest, signature);

        try (Response<KeyVerifyResult> response
            = keyClient.verifyWithResponse(keyName, keyVerifyParameters, keyVersion, requestContext)) {

            return new VerifyResult(response.getValue().isValue(), algorithm, keyId);
        }
    }

    public WrapResult wrapKey(KeyWrapAlgorithm algorithm, byte[] key, RequestContext requestContext) {

        Objects.requireNonNull(algorithm, "Key wrap algorithm cannot be null.");
        Objects.requireNonNull(key, "Key content to be wrapped cannot be null.");

        KeyOperationsParameters keyOperationsParameters = new KeyOperationsParameters(mapWrapAlgorithm(algorithm), key);

        try (Response<KeyOperationResult> response
            = keyClient.wrapKeyWithResponse(keyName, keyOperationsParameters, keyVersion, requestContext)) {

            return new WrapResult(response.getValue().getResult(), algorithm, keyId);
        }
    }

    public UnwrapResult unwrapKey(KeyWrapAlgorithm algorithm, byte[] encryptedKey, RequestContext requestContext) {
        Objects.requireNonNull(algorithm, "Key wrap algorithm cannot be null.");
        Objects.requireNonNull(encryptedKey, "Encrypted key content to be unwrapped cannot be null.");

        KeyOperationsParameters keyOperationsParameters
            = new KeyOperationsParameters(mapWrapAlgorithm(algorithm), encryptedKey);

        try (Response<KeyOperationResult> response
            = keyClient.unwrapKeyWithResponse(keyName, keyOperationsParameters, keyVersion, requestContext)) {

            return new UnwrapResult(response.getValue().getResult(), algorithm, keyId);
        }
    }

    public SignResult signData(SignatureAlgorithm algorithm, byte[] data, RequestContext requestContext) {
        Objects.requireNonNull(algorithm, "Signature algorithm cannot be null.");
        Objects.requireNonNull(data, "Data to be signed cannot be null.");

        HashAlgorithm hashAlgorithm = SignatureHashResolver.DEFAULT.get(algorithm);
        MessageDigest md = getMessageDigest(hashAlgorithm);

        md.update(data);

        byte[] digest = md.digest();

        return sign(algorithm, digest, requestContext);
    }

    public VerifyResult verifyData(SignatureAlgorithm algorithm, byte[] data, byte[] signature,
        RequestContext requestContext) {
        Objects.requireNonNull(algorithm, "Signature algorithm cannot be null.");
        Objects.requireNonNull(data, "Data to verify cannot be null.");
        Objects.requireNonNull(signature, "Signature to be verified cannot be null.");

        HashAlgorithm hashAlgorithm = SignatureHashResolver.DEFAULT.get(algorithm);

        MessageDigest md = getMessageDigest(hashAlgorithm);
        md.update(data);

        byte[] digest = md.digest();

        return verify(algorithm, digest, signature, requestContext);
    }

    private MessageDigest getMessageDigest(HashAlgorithm hashAlgorithm) {
        try {
            return MessageDigest.getInstance(hashAlgorithm.toString());
        } catch (NoSuchAlgorithmException e) {
            throw LOGGER.throwableAtError()
                .addKeyValue("algorithm", hashAlgorithm.toString())
                .log(e, CoreException::from);
        }
    }
}

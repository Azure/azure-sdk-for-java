// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography.implementation;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.security.keyvault.keys.cryptography.CryptographyServiceVersion;
import com.azure.security.keyvault.keys.cryptography.models.DecryptParameters;
import com.azure.security.keyvault.keys.cryptography.models.DecryptResult;
import com.azure.security.keyvault.keys.cryptography.models.EncryptParameters;
import com.azure.security.keyvault.keys.cryptography.models.EncryptResult;
import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.SignResult;
import com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.UnwrapResult;
import com.azure.security.keyvault.keys.cryptography.models.VerifyResult;
import com.azure.security.keyvault.keys.cryptography.models.WrapResult;
import com.azure.security.keyvault.keys.implementation.KeyClientImpl;
import com.azure.security.keyvault.keys.implementation.KeyVaultKeysUtils;
import com.azure.security.keyvault.keys.implementation.SecretMinClientImpl;
import com.azure.security.keyvault.keys.implementation.models.KeyBundle;
import com.azure.security.keyvault.keys.implementation.models.KeyOperationResult;
import com.azure.security.keyvault.keys.implementation.models.KeyVaultErrorException;
import com.azure.security.keyvault.keys.implementation.models.KeyVerifyResult;
import com.azure.security.keyvault.keys.implementation.models.SecretKey;
import com.azure.security.keyvault.keys.implementation.models.SecretRequestAttributes;
import com.azure.security.keyvault.keys.models.JsonWebKey;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import reactor.core.publisher.Mono;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;

import static com.azure.core.util.FluxUtil.withContext;
import static com.azure.security.keyvault.keys.cryptography.implementation.CryptographyUtils.mapKeyEncryptionAlgorithm;
import static com.azure.security.keyvault.keys.cryptography.implementation.CryptographyUtils.mapKeySignatureAlgorithm;
import static com.azure.security.keyvault.keys.cryptography.implementation.CryptographyUtils.mapWrapAlgorithm;
import static com.azure.security.keyvault.keys.cryptography.implementation.CryptographyUtils.transformSecretKey;
import static com.azure.security.keyvault.keys.cryptography.implementation.CryptographyUtils.unpackAndValidateId;
import static com.azure.security.keyvault.keys.implementation.KeyVaultKeysUtils.callWithMappedException;
import static com.azure.security.keyvault.keys.implementation.models.KeyVaultKeysModelsUtils.createKeyVaultKey;

public final class CryptographyClientImpl {
    private static final ClientLogger LOGGER = new ClientLogger(CryptographyClientImpl.class);

    private final KeyClientImpl keyClient;
    private final SecretMinClientImpl secretClient;

    private final String keyId;

    private final String vaultUrl;
    private final String keyCollection;
    private final String keyName;
    private final String keyVersion;

    public CryptographyClientImpl(String keyId, HttpPipeline pipeline, CryptographyServiceVersion serviceVersion) {
        Objects.requireNonNull(keyId);

        List<String> data = unpackAndValidateId(keyId, LOGGER);

        this.vaultUrl = data.get(0);
        this.keyCollection = data.get(1);
        this.keyName = data.get(2);
        this.keyVersion = data.get(3);

        this.keyId = keyId;

        this.keyClient = new KeyClientImpl(pipeline, serviceVersion.getVersion());
        this.secretClient = new SecretMinClientImpl(pipeline, serviceVersion.getVersion());
    }

    public String getVaultUrl() {
        return vaultUrl;
    }

    public String getKeyCollection() {
        return keyCollection;
    }

    public Mono<Response<KeyVaultKey>> getKeyAsync() {
        return keyClient.getKeyWithResponseAsync(vaultUrl, keyName, keyVersion)
            .doOnRequest(ignored -> LOGGER.verbose("Retrieving key - {}", keyName))
            .doOnSuccess(response -> LOGGER.verbose("Retrieved key - {}", keyName))
            .doOnError(error -> LOGGER.warning("Failed to get key - {}", keyName, error))
            .onErrorMap(KeyVaultErrorException.class, KeyVaultKeysUtils::mapGetKeyException)
            .map(response -> new SimpleResponse<>(response, createKeyVaultKey(response.getValue())));
    }

    public Response<KeyVaultKey> getKey(Context context) {
        Response<KeyBundle> response
            = callWithMappedException(() -> keyClient.getKeyWithResponse(vaultUrl, keyName, keyVersion, context),
                KeyVaultKeysUtils::mapGetKeyException);

        return new SimpleResponse<>(response, createKeyVaultKey(response.getValue()));
    }

    public Mono<JsonWebKey> getSecretKeyAsync() {
        return withContext(context -> secretClient.getSecretWithResponseAsync(vaultUrl, keyName, keyVersion, context))
            .doOnRequest(ignored -> LOGGER.verbose("Retrieving key - {}", keyName))
            .doOnSuccess(response -> LOGGER.verbose("Retrieved key - {}", response.getValue().getName()))
            .doOnError(error -> LOGGER.warning("Failed to get key - {}", keyName, error))
            .map(response -> transformSecretKey(response.getValue()));
    }

    public JsonWebKey getSecretKey() {
        return transformSecretKey(
            secretClient.getSecretWithResponse(vaultUrl, keyName, keyVersion, Context.NONE).getValue());
    }

    public Mono<Response<SecretKey>> setSecretKeyAsync(SecretKey secret, Context context) {
        Objects.requireNonNull(secret, "The secret key cannot be null.");

        return secretClient
            .setSecretWithResponseAsync(vaultUrl, secret.getName(), secret.getValue(), secret.getProperties().getTags(),
                secret.getProperties().getContentType(), new SecretRequestAttributes(secret.getProperties()), context)
            .doOnRequest(ignored -> LOGGER.verbose("Setting secret - {}", secret.getName()))
            .doOnSuccess(response -> LOGGER.verbose("Set secret - {}", response.getValue().getName()))
            .doOnError(error -> LOGGER.warning("Failed to set secret - {}", secret.getName(), error));
    }

    public Response<SecretKey> setSecretKey(SecretKey secret, Context context) {
        Objects.requireNonNull(secret, "The Secret input parameter cannot be null.");

        return secretClient.setSecretWithResponse(vaultUrl, secret.getName(), secret.getValue(),
            secret.getProperties().getTags(), secret.getProperties().getContentType(),
            new SecretRequestAttributes(secret.getProperties()), context);
    }

    public Mono<EncryptResult> encryptAsync(EncryptionAlgorithm algorithm, byte[] plaintext, Context context) {
        Objects.requireNonNull(algorithm, "Encryption algorithm cannot be null.");
        Objects.requireNonNull(plaintext, "Plaintext cannot be null.");

        return encryptAsync(algorithm, plaintext, null, null, context);
    }

    public Mono<EncryptResult> encryptAsync(EncryptParameters encryptParameters, Context context) {
        Objects.requireNonNull(encryptParameters, "Encrypt parameters cannot be null.");

        return encryptAsync(encryptParameters.getAlgorithm(), encryptParameters.getPlainText(),
            encryptParameters.getIv(), encryptParameters.getAdditionalAuthenticatedData(), context);
    }

    private Mono<EncryptResult> encryptAsync(EncryptionAlgorithm algorithm, byte[] plainText, byte[] iv,
        byte[] additionalAuthenticatedData, Context context) {
        return keyClient
            .encryptAsync(vaultUrl, keyName, keyVersion, mapKeyEncryptionAlgorithm(algorithm), plainText, iv,
                additionalAuthenticatedData, null, context)
            .doOnRequest(ignored -> LOGGER.verbose("Encrypting content with algorithm - {}", algorithm))
            .doOnSuccess(response -> LOGGER.verbose("Retrieved encrypted content with algorithm - {}", algorithm))
            .doOnError(error -> LOGGER.warning("Failed to encrypt content with algorithm - {}", algorithm, error))
            .map(result -> new EncryptResult(result.getResult(), algorithm, keyId, result.getIv(),
                result.getAuthenticationTag(), result.getAdditionalAuthenticatedData()));
    }

    public EncryptResult encrypt(EncryptionAlgorithm algorithm, byte[] plaintext, Context context) {
        Objects.requireNonNull(algorithm, "Encryption algorithm cannot be null.");
        Objects.requireNonNull(plaintext, "Plaintext cannot be null.");

        return encrypt(algorithm, plaintext, null, null, context);
    }

    public EncryptResult encrypt(EncryptParameters encryptParameters, Context context) {
        Objects.requireNonNull(encryptParameters, "Encrypt parameters cannot be null.");

        return encrypt(encryptParameters.getAlgorithm(), encryptParameters.getPlainText(), encryptParameters.getIv(),
            encryptParameters.getAdditionalAuthenticatedData(), context);
    }

    private EncryptResult encrypt(EncryptionAlgorithm algorithm, byte[] plainText, byte[] iv,
        byte[] additionalAuthenticatedData, Context context) {
        KeyOperationResult result
            = keyClient
                .encryptWithResponse(vaultUrl, keyName, keyVersion, mapKeyEncryptionAlgorithm(algorithm), plainText, iv,
                    additionalAuthenticatedData, null, context)
                .getValue();

        return new EncryptResult(result.getResult(), algorithm, keyId, result.getIv(), result.getAuthenticationTag(),
            result.getAdditionalAuthenticatedData());
    }

    public Mono<DecryptResult> decryptAsync(EncryptionAlgorithm algorithm, byte[] ciphertext, Context context) {
        Objects.requireNonNull(algorithm, "Encryption algorithm cannot be null.");
        Objects.requireNonNull(ciphertext, "Ciphertext cannot be null.");

        return decryptAsync(algorithm, ciphertext, null, null, null, context);
    }

    public Mono<DecryptResult> decryptAsync(DecryptParameters decryptParameters, Context context) {
        Objects.requireNonNull(decryptParameters, "Decrypt parameters cannot be null.");

        return decryptAsync(decryptParameters.getAlgorithm(), decryptParameters.getCipherText(),
            decryptParameters.getIv(), decryptParameters.getAdditionalAuthenticatedData(),
            decryptParameters.getAuthenticationTag(), context);
    }

    private Mono<DecryptResult> decryptAsync(EncryptionAlgorithm algorithm, byte[] ciphertext, byte[] iv,
        byte[] additionalAuthenticatedData, byte[] authenticationTag, Context context) {
        return keyClient
            .decryptAsync(vaultUrl, keyName, keyVersion, mapKeyEncryptionAlgorithm(algorithm), ciphertext, iv,
                additionalAuthenticatedData, authenticationTag, context)
            .map(result -> new DecryptResult(result.getResult(), algorithm, keyId))
            .doOnRequest(ignored -> LOGGER.verbose("Decrypting content with algorithm - {}", algorithm))
            .doOnSuccess(response -> LOGGER.verbose("Retrieved decrypted content with algorithm - {}", algorithm))
            .doOnError(error -> LOGGER.warning("Failed to decrypt content with algorithm - {}", algorithm, error));
    }

    public DecryptResult decrypt(EncryptionAlgorithm algorithm, byte[] ciphertext, Context context) {
        Objects.requireNonNull(algorithm, "Encryption algorithm cannot be null.");
        Objects.requireNonNull(ciphertext, "Ciphertext cannot be null.");

        return decrypt(algorithm, ciphertext, null, null, null, context);
    }

    public DecryptResult decrypt(DecryptParameters decryptParameters, Context context) {
        Objects.requireNonNull(decryptParameters, "Decrypt parameters cannot be null.");

        return decrypt(decryptParameters.getAlgorithm(), decryptParameters.getCipherText(), decryptParameters.getIv(),
            decryptParameters.getAdditionalAuthenticatedData(), decryptParameters.getAuthenticationTag(), context);
    }

    private DecryptResult decrypt(EncryptionAlgorithm algorithm, byte[] ciphertext, byte[] iv,
        byte[] additionalAuthenticatedData, byte[] authenticationTag, Context context) {
        KeyOperationResult result
            = keyClient
                .decryptWithResponse(vaultUrl, keyName, keyVersion, mapKeyEncryptionAlgorithm(algorithm), ciphertext,
                    iv, additionalAuthenticatedData, authenticationTag, context)
                .getValue();

        return new DecryptResult(result.getResult(), algorithm, keyId);
    }

    public Mono<SignResult> signAsync(SignatureAlgorithm algorithm, byte[] digest, Context context) {
        Objects.requireNonNull(algorithm, "Signature algorithm cannot be null.");
        Objects.requireNonNull(digest, "Digest content cannot be null.");

        return keyClient.signAsync(vaultUrl, keyName, keyVersion, mapKeySignatureAlgorithm(algorithm), digest, context)
            .map(result -> new SignResult(result.getResult(), algorithm, keyId))
            .doOnRequest(ignored -> LOGGER.verbose("Signing content with algorithm - {}", algorithm))
            .doOnSuccess(response -> LOGGER.verbose("Retrieved signed content with algorithm - {}", algorithm))
            .doOnError(error -> LOGGER.warning("Failed to sign content with algorithm - {}", algorithm, error));
    }

    public SignResult sign(SignatureAlgorithm algorithm, byte[] digest, Context context) {
        Objects.requireNonNull(algorithm, "Signature algorithm cannot be null.");
        Objects.requireNonNull(digest, "Digest content cannot be null.");

        KeyOperationResult result = keyClient
            .signWithResponse(vaultUrl, keyName, keyVersion, mapKeySignatureAlgorithm(algorithm), digest, context)
            .getValue();

        return new SignResult(result.getResult(), algorithm, keyId);
    }

    public Mono<VerifyResult> verifyAsync(SignatureAlgorithm algorithm, byte[] digest, byte[] signature,
        Context context) {
        Objects.requireNonNull(algorithm, "Signature algorithm cannot be null.");
        Objects.requireNonNull(digest, "Digest content cannot be null.");
        Objects.requireNonNull(signature, "Signature to be verified cannot be null.");

        return keyClient
            .verifyAsync(vaultUrl, keyName, keyVersion, mapKeySignatureAlgorithm(algorithm), digest, signature, context)
            .map(result -> new VerifyResult(result.isValue(), algorithm, keyId))
            .doOnRequest(ignored -> LOGGER.verbose("Verifying content with algorithm - {}", algorithm))
            .doOnSuccess(response -> LOGGER.verbose("Retrieved verified content with algorithm - {}", algorithm))
            .doOnError(error -> LOGGER.warning("Failed to verify content with algorithm - {}", algorithm, error));
    }

    public VerifyResult verify(SignatureAlgorithm algorithm, byte[] digest, byte[] signature, Context context) {
        Objects.requireNonNull(algorithm, "Signature algorithm cannot be null.");
        Objects.requireNonNull(digest, "Digest content cannot be null.");
        Objects.requireNonNull(signature, "Signature to be verified cannot be null.");

        KeyVerifyResult result
            = keyClient
                .verifyWithResponse(vaultUrl, keyName, keyVersion, mapKeySignatureAlgorithm(algorithm), digest,
                    signature, context)
                .getValue();

        return new VerifyResult(result.isValue(), algorithm, keyId);
    }

    public Mono<WrapResult> wrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] key, Context context) {
        Objects.requireNonNull(algorithm, "Key wrap algorithm cannot be null.");
        Objects.requireNonNull(key, "Key content to be wrapped cannot be null.");

        return keyClient
            .wrapKeyAsync(vaultUrl, keyName, keyVersion, mapWrapAlgorithm(algorithm), key, null, null, null, context)
            .map(result -> new WrapResult(result.getResult(), algorithm, keyId))
            .doOnRequest(ignored -> LOGGER.verbose("Wrapping key content with algorithm - {}", algorithm))
            .doOnSuccess(response -> LOGGER.verbose("Retrieved wrapped key content with algorithm - {}", algorithm))
            .doOnError(error -> LOGGER.warning("Failed to verify content with algorithm - {}", algorithm, error));
    }

    public WrapResult wrapKey(KeyWrapAlgorithm algorithm, byte[] key, Context context) {
        Objects.requireNonNull(algorithm, "Key wrap algorithm cannot be null.");
        Objects.requireNonNull(key, "Key content to be wrapped cannot be null.");

        KeyOperationResult result
            = keyClient
                .wrapKeyWithResponse(vaultUrl, keyName, keyVersion, mapWrapAlgorithm(algorithm), key, null, null, null,
                    context)
                .getValue();

        return new WrapResult(result.getResult(), algorithm, keyId);
    }

    public Mono<UnwrapResult> unwrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] encryptedKey, Context context) {
        Objects.requireNonNull(algorithm, "Key wrap algorithm cannot be null.");
        Objects.requireNonNull(encryptedKey, "Encrypted key content to be unwrapped cannot be null.");

        return keyClient
            .unwrapKeyAsync(vaultUrl, keyName, keyVersion, mapWrapAlgorithm(algorithm), encryptedKey, null, null, null,
                context)
            .map(result -> new UnwrapResult(result.getResult(), algorithm, keyId))
            .doOnRequest(ignored -> LOGGER.verbose("Unwrapping key content with algorithm - {}", algorithm))
            .doOnSuccess(response -> LOGGER.verbose("Retrieved unwrapped key content with algorithm - {}", algorithm))
            .doOnError(error -> LOGGER.warning("Failed to unwrap key content with algorithm - {}", algorithm, error));
    }

    public UnwrapResult unwrapKey(KeyWrapAlgorithm algorithm, byte[] encryptedKey, Context context) {
        Objects.requireNonNull(algorithm, "Key wrap algorithm cannot be null.");
        Objects.requireNonNull(encryptedKey, "Encrypted key content to be unwrapped cannot be null.");

        KeyOperationResult result = keyClient
            .unwrapKeyWithResponse(vaultUrl, keyName, keyVersion, mapWrapAlgorithm(algorithm), encryptedKey, null, null,
                null, context)
            .getValue();

        return new UnwrapResult(result.getResult(), algorithm, keyId);
    }

    public Mono<SignResult> signDataAsync(SignatureAlgorithm algorithm, byte[] data, Context context) {
        Objects.requireNonNull(algorithm, "Signature algorithm cannot be null.");
        Objects.requireNonNull(data, "Data to be signed cannot be null.");

        try {
            HashAlgorithm hashAlgorithm = SignatureHashResolver.DEFAULT.get(algorithm);
            MessageDigest md = MessageDigest.getInstance(hashAlgorithm.toString());
            md.update(data);
            byte[] digest = md.digest();

            return signAsync(algorithm, digest, context);
        } catch (NoSuchAlgorithmException e) {
            return Mono.error(e);
        }
    }

    public SignResult signData(SignatureAlgorithm algorithm, byte[] data, Context context) {
        Objects.requireNonNull(algorithm, "Signature algorithm cannot be null.");
        Objects.requireNonNull(data, "Data to be signed cannot be null.");

        try {
            HashAlgorithm hashAlgorithm = SignatureHashResolver.DEFAULT.get(algorithm);
            MessageDigest md = MessageDigest.getInstance(hashAlgorithm.toString());
            md.update(data);
            byte[] digest = md.digest();

            return sign(algorithm, digest, context);
        } catch (NoSuchAlgorithmException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    public Mono<VerifyResult> verifyDataAsync(SignatureAlgorithm algorithm, byte[] data, byte[] signature,
        Context context) {
        Objects.requireNonNull(algorithm, "Signature algorithm cannot be null.");
        Objects.requireNonNull(data, "Data to verify cannot be null.");
        Objects.requireNonNull(signature, "Signature to be verified cannot be null.");

        try {
            HashAlgorithm hashAlgorithm = SignatureHashResolver.DEFAULT.get(algorithm);
            MessageDigest md = MessageDigest.getInstance(hashAlgorithm.toString());
            md.update(data);
            byte[] digest = md.digest();

            return verifyAsync(algorithm, digest, signature, context);
        } catch (NoSuchAlgorithmException e) {
            return Mono.error(e);
        }
    }

    public VerifyResult verifyData(SignatureAlgorithm algorithm, byte[] data, byte[] signature, Context context) {
        Objects.requireNonNull(algorithm, "Signature algorithm cannot be null.");
        Objects.requireNonNull(data, "Data to verify cannot be null.");
        Objects.requireNonNull(signature, "Signature to be verified cannot be null.");

        try {
            HashAlgorithm hashAlgorithm = SignatureHashResolver.DEFAULT.get(algorithm);
            MessageDigest md = MessageDigest.getInstance(hashAlgorithm.toString());
            md.update(data);
            byte[] digest = md.digest();

            return verify(algorithm, digest, signature, context);
        } catch (NoSuchAlgorithmException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }
}

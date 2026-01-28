// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography.implementation;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.exception.ResourceModifiedException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.security.keyvault.keys.KeyServiceVersion;
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
import com.azure.security.keyvault.keys.implementation.SecretMinClientImpl;
import com.azure.security.keyvault.keys.implementation.models.KeyBundle;
import com.azure.security.keyvault.keys.implementation.models.KeyOperationResult;
import com.azure.security.keyvault.keys.implementation.models.KeyOperationsParameters;
import com.azure.security.keyvault.keys.implementation.models.KeySignParameters;
import com.azure.security.keyvault.keys.implementation.models.KeyVerifyParameters;
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
import static com.azure.security.keyvault.keys.implementation.KeyVaultKeysUtils.EMPTY_OPTIONS;
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

        this.keyClient = new KeyClientImpl(pipeline, vaultUrl, KeyServiceVersion.valueOf(serviceVersion.toString()));
        this.secretClient = new SecretMinClientImpl(pipeline, serviceVersion.getVersion());
    }

    public String getVaultUrl() {
        return vaultUrl;
    }

    public String getKeyCollection() {
        return keyCollection;
    }

    public Mono<Response<KeyVaultKey>> getKeyAsync() {
        return keyClient.getKeyWithResponseAsync(keyName, keyVersion, EMPTY_OPTIONS)
            .onErrorMap(HttpResponseException.class,
                e -> e.getResponse().getStatusCode() == 403
                    ? new ResourceModifiedException(e.getMessage(), e.getResponse(), e.getValue())
                    : e)
            .map(response -> new SimpleResponse<>(response,
                createKeyVaultKey(response.getValue().toObject(KeyBundle.class))));
    }

    public Response<KeyVaultKey> getKey(Context context) {
        Response<BinaryData> response
            = keyClient.getKeyWithResponse(keyName, keyVersion, new RequestOptions().setContext(context));

        return new SimpleResponse<>(response, createKeyVaultKey(response.getValue().toObject(KeyBundle.class)));
    }

    public Mono<JsonWebKey> getSecretKeyAsync() {
        return withContext(context -> secretClient.getSecretWithResponseAsync(vaultUrl, keyName, keyVersion, context))
            .map(response -> transformSecretKey(response.getValue()));
    }

    public JsonWebKey getSecretKey() {
        return transformSecretKey(
            secretClient.getSecretWithResponse(vaultUrl, keyName, keyVersion, Context.NONE).getValue());
    }

    public Mono<Response<SecretKey>> setSecretKeyAsync(SecretKey secret, Context context) {
        Objects.requireNonNull(secret, "The secret key cannot be null.");

        return secretClient.setSecretWithResponseAsync(vaultUrl, secret.getName(), secret.getValue(),
            secret.getProperties().getTags(), secret.getProperties().getContentType(),
            new SecretRequestAttributes(secret.getProperties()), context);
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
        KeyOperationsParameters keyOperationsParameters
            = new KeyOperationsParameters(mapKeyEncryptionAlgorithm(algorithm), plainText).setIv(iv)
                .setAad(additionalAuthenticatedData);

        return keyClient
            .encryptWithResponseAsync(keyName, keyVersion, BinaryData.fromObject(keyOperationsParameters),
                new RequestOptions().setContext(context))
            .map(response -> {
                KeyOperationResult result = response.getValue().toObject(KeyOperationResult.class);

                return new EncryptResult(result.getResult(), algorithm, keyId, result.getIv(),
                    result.getAuthenticationTag(), result.getAdditionalAuthenticatedData());
            });
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
        KeyOperationsParameters keyOperationsParameters
            = new KeyOperationsParameters(mapKeyEncryptionAlgorithm(algorithm), plainText).setIv(iv)
                .setAad(additionalAuthenticatedData);

        KeyOperationResult result
            = keyClient
                .encryptWithResponse(keyName, keyVersion, BinaryData.fromObject(keyOperationsParameters),
                    new RequestOptions().setContext(context))
                .getValue()
                .toObject(KeyOperationResult.class);

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

        KeyOperationsParameters keyOperationsParameters
            = new KeyOperationsParameters(mapKeyEncryptionAlgorithm(algorithm), ciphertext).setIv(iv)
                .setAad(additionalAuthenticatedData)
                .setTag(authenticationTag);

        return keyClient
            .decryptWithResponseAsync(keyName, keyVersion, BinaryData.fromObject(keyOperationsParameters),
                new RequestOptions().setContext(context))
            .map(response -> {
                KeyOperationResult result = response.getValue().toObject(KeyOperationResult.class);

                return new DecryptResult(result.getResult(), algorithm, keyId);
            });
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

        KeyOperationsParameters keyOperationsParameters
            = new KeyOperationsParameters(mapKeyEncryptionAlgorithm(algorithm), ciphertext).setIv(iv)
                .setAad(additionalAuthenticatedData)
                .setTag(authenticationTag);

        KeyOperationResult result
            = keyClient
                .decryptWithResponse(keyName, keyVersion, BinaryData.fromObject(keyOperationsParameters),
                    new RequestOptions().setContext(context))
                .getValue()
                .toObject(KeyOperationResult.class);

        return new DecryptResult(result.getResult(), algorithm, keyId);
    }

    public Mono<SignResult> signAsync(SignatureAlgorithm algorithm, byte[] digest, Context context) {
        Objects.requireNonNull(algorithm, "Signature algorithm cannot be null.");
        Objects.requireNonNull(digest, "Digest content cannot be null.");

        KeySignParameters keyOperationsParameters = new KeySignParameters(mapKeySignatureAlgorithm(algorithm), digest);

        return keyClient
            .signWithResponseAsync(keyName, keyVersion, BinaryData.fromObject(keyOperationsParameters),
                new RequestOptions().setContext(context))
            .map(response -> {
                KeyOperationResult result = response.getValue().toObject(KeyOperationResult.class);

                return new SignResult(result.getResult(), algorithm, keyId);
            });
    }

    public SignResult sign(SignatureAlgorithm algorithm, byte[] digest, Context context) {
        Objects.requireNonNull(algorithm, "Signature algorithm cannot be null.");
        Objects.requireNonNull(digest, "Digest content cannot be null.");

        KeySignParameters keyOperationsParameters = new KeySignParameters(mapKeySignatureAlgorithm(algorithm), digest);

        KeyOperationResult result
            = keyClient
                .signWithResponse(keyName, keyVersion, BinaryData.fromObject(keyOperationsParameters),
                    new RequestOptions().setContext(context))
                .getValue()
                .toObject(KeyOperationResult.class);

        return new SignResult(result.getResult(), algorithm, keyId);
    }

    public Mono<VerifyResult> verifyAsync(SignatureAlgorithm algorithm, byte[] digest, byte[] signature,
        Context context) {
        Objects.requireNonNull(algorithm, "Signature algorithm cannot be null.");
        Objects.requireNonNull(digest, "Digest content cannot be null.");
        Objects.requireNonNull(signature, "Signature to be verified cannot be null.");

        KeyVerifyParameters keyVerifyParameters
            = new KeyVerifyParameters(mapKeySignatureAlgorithm(algorithm), digest, signature);

        return keyClient
            .verifyWithResponseAsync(keyName, keyVersion, BinaryData.fromObject(keyVerifyParameters),
                new RequestOptions().setContext(context))
            .map(response -> {
                KeyVerifyResult result = response.getValue().toObject(KeyVerifyResult.class);

                return new VerifyResult(result.isValue(), algorithm, keyId);
            });
    }

    public VerifyResult verify(SignatureAlgorithm algorithm, byte[] digest, byte[] signature, Context context) {
        Objects.requireNonNull(algorithm, "Signature algorithm cannot be null.");
        Objects.requireNonNull(digest, "Digest content cannot be null.");
        Objects.requireNonNull(signature, "Signature to be verified cannot be null.");

        KeyVerifyParameters keyVerifyParameters
            = new KeyVerifyParameters(mapKeySignatureAlgorithm(algorithm), digest, signature);

        KeyVerifyResult result
            = keyClient
                .verifyWithResponse(keyName, keyVersion, BinaryData.fromObject(keyVerifyParameters),
                    new RequestOptions().setContext(context))
                .getValue()
                .toObject(KeyVerifyResult.class);

        return new VerifyResult(result.isValue(), algorithm, keyId);
    }

    public Mono<WrapResult> wrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] key, Context context) {
        Objects.requireNonNull(algorithm, "Key wrap algorithm cannot be null.");
        Objects.requireNonNull(key, "Key content to be wrapped cannot be null.");

        KeyOperationsParameters keyOperationsParameters = new KeyOperationsParameters(mapWrapAlgorithm(algorithm), key);

        return keyClient
            .wrapKeyWithResponseAsync(keyName, keyVersion, BinaryData.fromObject(keyOperationsParameters),
                new RequestOptions().setContext(context))
            .map(response -> {
                KeyOperationResult result = response.getValue().toObject(KeyOperationResult.class);

                return new WrapResult(result.getResult(), algorithm, keyId);
            });
    }

    public WrapResult wrapKey(KeyWrapAlgorithm algorithm, byte[] key, Context context) {
        Objects.requireNonNull(algorithm, "Key wrap algorithm cannot be null.");
        Objects.requireNonNull(key, "Key content to be wrapped cannot be null.");

        KeyOperationsParameters keyOperationsParameters = new KeyOperationsParameters(mapWrapAlgorithm(algorithm), key);

        KeyOperationResult result
            = keyClient
                .wrapKeyWithResponse(keyName, keyVersion, BinaryData.fromObject(keyOperationsParameters),
                    new RequestOptions().setContext(context))
                .getValue()
                .toObject(KeyOperationResult.class);

        return new WrapResult(result.getResult(), algorithm, keyId);
    }

    public Mono<UnwrapResult> unwrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] encryptedKey, Context context) {
        Objects.requireNonNull(algorithm, "Key wrap algorithm cannot be null.");
        Objects.requireNonNull(encryptedKey, "Encrypted key content to be unwrapped cannot be null.");

        KeyOperationsParameters keyOperationsParameters
            = new KeyOperationsParameters(mapWrapAlgorithm(algorithm), encryptedKey);

        return keyClient
            .unwrapKeyWithResponseAsync(keyName, keyVersion, BinaryData.fromObject(keyOperationsParameters),
                new RequestOptions().setContext(context))
            .map(response -> {
                KeyOperationResult result = response.getValue().toObject(KeyOperationResult.class);

                return new UnwrapResult(result.getResult(), algorithm, keyId);
            });
    }

    public UnwrapResult unwrapKey(KeyWrapAlgorithm algorithm, byte[] encryptedKey, Context context) {
        Objects.requireNonNull(algorithm, "Key wrap algorithm cannot be null.");
        Objects.requireNonNull(encryptedKey, "Encrypted key content to be unwrapped cannot be null.");

        KeyOperationsParameters keyOperationsParameters
            = new KeyOperationsParameters(mapWrapAlgorithm(algorithm), encryptedKey);

        KeyOperationResult result
            = keyClient
                .unwrapKeyWithResponse(keyName, keyVersion, BinaryData.fromObject(keyOperationsParameters),
                    new RequestOptions().setContext(context))
                .getValue()
                .toObject(KeyOperationResult.class);

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

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography.implementation;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
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
import com.azure.security.keyvault.keys.implementation.SecretMinClientImpl;
import com.azure.security.keyvault.keys.implementation.models.JsonWebKeyEncryptionAlgorithm;
import com.azure.security.keyvault.keys.implementation.models.KeyOperationResult;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Objects;

import static com.azure.security.keyvault.keys.cryptography.implementation.CryptographyUtils.unpackAndValidateId;

public final class CryptographyClientImpl {
    private static final ClientLogger LOGGER = new ClientLogger(CryptographyClientImpl.class);

    private final KeyClientImpl keyClient;
    private final SecretMinClientImpl secretClient;
    private final String serviceVersion;
    private final String keyId;

    private final String vaultUrl;
    private final String keyCollection;
    private final String keyName;
    private final String keyVersion;


    CryptographyClientImpl(String keyId, HttpPipeline pipeline, CryptographyServiceVersion serviceVersion) {
        Objects.requireNonNull(keyId);

        //Arrays.asList(vaultUrl, keyCollection, keyName, keyVersion);
        List<String> data = unpackAndValidateId(keyId, LOGGER);

        this.vaultUrl = data.get(0);
        this.keyCollection = data.get(1);
        this.keyName = data.get(2);
        this.keyVersion = data.get(3);

        this.keyId = keyId;
        this.keyClient = new KeyClientImpl(pipeline, serviceVersion.getVersion());
        this.secretClient = new SecretMinClientImpl(pipeline, serviceVersion.getVersion());
        this.serviceVersion = serviceVersion.getVersion();
    }

    Mono<Response<KeyVaultKey>> getKeyAsync(Context context) {
        return getKeyAsync(keyName, version, context);
    }

    Response<KeyVaultKey> getKey(Context context) {
        if (version == null) {
            version = "";
        }

        return getKey(keyName, version, context);
    }

    private Mono<Response<KeyVaultKey>> getKeyAsync(String name, String version, Context context) {
        return keyClient.getKeyWithResponseAsync(vaultUrl, name, version, context);
    }

    private Response<KeyVaultKey> getKey(String name, String version, Context context) {
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return keyClient.getKeyWithResponse(vaultUrl, name, version, context);
    }

    Mono<EncryptResult> encryptAsync(EncryptionAlgorithm algorithm, byte[] plaintext, Context context) {
        Objects.requireNonNull(algorithm, "Encryption algorithm cannot be null.");
        Objects.requireNonNull(plaintext, "Plaintext cannot be null.");

        KeyOperationParameters parameters = new KeyOperationParameters()
            .setAlgorithm(algorithm)
            .setValue(plaintext);

        return encryptAsync(parameters, context);
    }

    Mono<EncryptResult> encryptAsync(EncryptParameters encryptParameters, Context context) {
        Objects.requireNonNull(encryptParameters, "Encrypt parameters cannot be null.");

        KeyOperationParameters parameters = new KeyOperationParameters()
            .setAlgorithm(encryptParameters.getAlgorithm())
            .setValue(encryptParameters.getPlainText())
            .setIv(encryptParameters.getIv())
            .setAdditionalAuthenticatedData(encryptParameters.getAdditionalAuthenticatedData());

        return encryptAsync(parameters, context);
    }

    private Mono<EncryptResult> encryptAsync(KeyOperationParameters keyOperationParameters, Context context) {
        JsonWebKeyEncryptionAlgorithm algorithm = mapKeyEncryptionAlgorithm(keyOperationParameters.getAlgorithm());

        return keyClient.encryptWithResponseAsync(vaultUrl, keyName, version, algorithm,
            keyOperationParameters.getValue(), keyOperationParameters.getIv(),
            keyOperationParameters.getAdditionalAuthenticatedData(), keyOperationParameters.getAuthenticationTag(),
            context)
            .doOnRequest(ignored -> LOGGER.verbose("Encrypting content with algorithm - {}", algorithm))
            .doOnSuccess(response -> LOGGER.verbose("Retrieved encrypted content with algorithm - {}", algorithm))
            .doOnError(error -> LOGGER.warning("Failed to encrypt content with algorithm - {}", algorithm, error))
            .map(keyOperationResultResponse -> {
                KeyOperationResult keyOperationResult = keyOperationResultResponse.getValue();

                return new EncryptResult(keyOperationResult.getResult(), keyOperationParameters.getAlgorithm(), keyId,
                    keyOperationResult.getIv(), keyOperationResult.getAuthenticationTag(),
                    keyOperationResult.getAdditionalAuthenticatedData());
            });
    }

    private static JsonWebKeyEncryptionAlgorithm mapKeyEncryptionAlgorithm(EncryptionAlgorithm algorithm) {
        return JsonWebKeyEncryptionAlgorithm.fromString(Objects.toString(algorithm, null));
    }

    EncryptResult encrypt(EncryptionAlgorithm algorithm, byte[] plaintext, Context context) {
        Objects.requireNonNull(algorithm, "Encryption algorithm cannot be null.");
        Objects.requireNonNull(plaintext, "Plaintext cannot be null.");

        KeyOperationParameters parameters = new KeyOperationParameters()
            .setAlgorithm(algorithm)
            .setValue(plaintext);

        return encrypt(parameters, context);
    }

    EncryptResult encrypt(EncryptParameters encryptParameters, Context context) {
        Objects.requireNonNull(encryptParameters, "Encrypt parameters cannot be null.");

        KeyOperationParameters parameters = new KeyOperationParameters()
            .setAlgorithm(encryptParameters.getAlgorithm())
            .setValue(encryptParameters.getPlainText())
            .setIv(encryptParameters.getIv())
            .setAdditionalAuthenticatedData(encryptParameters.getAdditionalAuthenticatedData());

        return encrypt(parameters, context);
    }

    EncryptResult encrypt(KeyOperationParameters keyOperationParameters, Context context) {
        JsonWebKeyEncryptionAlgorithm algorithm = mapKeyEncryptionAlgorithm(keyOperationParameters.getAlgorithm());
        context = enableSyncRestProxy(context == null ? Context.NONE : context);

        Response<KeyOperationResult> encryptResult = keyClient.encryptWithResponse(vaultUrl, keyName, version,
            algorithm, keyOperationParameters.getValue(), keyOperationParameters.getIv(),
            keyOperationParameters.getAdditionalAuthenticatedData(), keyOperationParameters.getAuthenticationTag(),
            context);
        KeyOperationResult keyOperationResult = encryptResult.getValue();

        return new EncryptResult(keyOperationResult.getResult(), keyOperationParameters.getAlgorithm(), keyId,
            keyOperationResult.getIv(), keyOperationResult.getAuthenticationTag(),
            keyOperationResult.getAdditionalAuthenticatedData());
    }

    Mono<DecryptResult> decryptAsync(EncryptionAlgorithm algorithm, byte[] ciphertext, Context context) {
        Objects.requireNonNull(algorithm, "Encryption algorithm cannot be null.");
        Objects.requireNonNull(ciphertext, "Ciphertext cannot be null.");

        KeyOperationParameters parameters = new KeyOperationParameters()
            .setAlgorithm(algorithm)
            .setValue(ciphertext);

        return decryptAsync(parameters, context);
    }

    Mono<DecryptResult> decryptAsync(DecryptParameters decryptParameters, Context context) {
        Objects.requireNonNull(decryptParameters, "Decrypt parameters cannot be null.");

        KeyOperationParameters parameters = new KeyOperationParameters()
            .setAlgorithm(decryptParameters.getAlgorithm())
            .setValue(decryptParameters.getCipherText())
            .setIv(decryptParameters.getIv())
            .setAdditionalAuthenticatedData(decryptParameters.getAdditionalAuthenticatedData())
            .setAuthenticationTag(decryptParameters.getAuthenticationTag());

        return decryptAsync(parameters, context);
    }

    private Mono<DecryptResult> decryptAsync(KeyOperationParameters keyOperationParameters, Context context) {
        EncryptionAlgorithm algorithm = keyOperationParameters.getAlgorithm();

        return service.decryptAsync(vaultUrl, keyName, version, serviceVersion, ACCEPT_LANGUAGE, keyOperationParameters,
                CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> LOGGER.verbose("Decrypting content with algorithm - {}", algorithm))
            .doOnSuccess(response -> LOGGER.verbose("Retrieved decrypted content with algorithm - {}", algorithm))
            .doOnError(error -> LOGGER.warning("Failed to decrypt content with algorithm - {}", algorithm, error))
            .flatMap(keyOperationResultResponse -> Mono.just(
                new DecryptResult(keyOperationResultResponse.getValue().getResult(), algorithm, keyId)));
    }

    DecryptResult decrypt(EncryptionAlgorithm algorithm, byte[] ciphertext, Context context) {
        Objects.requireNonNull(algorithm, "Encryption algorithm cannot be null.");
        Objects.requireNonNull(ciphertext, "Ciphertext cannot be null.");

        KeyOperationParameters parameters = new KeyOperationParameters()
            .setAlgorithm(algorithm)
            .setValue(ciphertext);

        return decrypt(parameters, context);
    }

    DecryptResult decrypt(DecryptParameters decryptParameters, Context context) {
        Objects.requireNonNull(decryptParameters, "Decrypt parameters cannot be null.");

        KeyOperationParameters parameters = new KeyOperationParameters()
            .setAlgorithm(decryptParameters.getAlgorithm())
            .setValue(decryptParameters.getCipherText())
            .setIv(decryptParameters.getIv())
            .setAdditionalAuthenticatedData(decryptParameters.getAdditionalAuthenticatedData())
            .setAuthenticationTag(decryptParameters.getAuthenticationTag());

        return decrypt(parameters, context);
    }

    private DecryptResult decrypt(KeyOperationParameters keyOperationParameters, Context context) {
        EncryptionAlgorithm algorithm = keyOperationParameters.getAlgorithm();
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        Response<KeyOperationResult> decryptResult =
            service.decrypt(vaultUrl, keyName, version, serviceVersion, ACCEPT_LANGUAGE, keyOperationParameters,
                CONTENT_TYPE_HEADER_VALUE, context);

        return new DecryptResult(decryptResult.getValue().getResult(), algorithm, keyId);
    }

    Mono<SignResult> signAsync(SignatureAlgorithm algorithm, byte[] digest, Context context) {
        Objects.requireNonNull(algorithm, "Signature algorithm cannot be null.");
        Objects.requireNonNull(digest, "Digest content cannot be null.");

        KeySignRequest parameters = new KeySignRequest()
            .setAlgorithm(algorithm)
            .setValue(digest);

        return service.signAsync(vaultUrl, keyName, version, serviceVersion, ACCEPT_LANGUAGE, parameters,
                CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> LOGGER.verbose("Signing content with algorithm - {}", algorithm))
            .doOnSuccess(response -> LOGGER.verbose("Retrieved signed content with algorithm - {}", algorithm))
            .doOnError(error -> LOGGER.warning("Failed to sign content with algorithm - {}", algorithm, error))
            .flatMap(keyOperationResultResponse ->
                Mono.just(new SignResult(keyOperationResultResponse.getValue().getResult(), algorithm, keyId)));
    }

    SignResult sign(SignatureAlgorithm algorithm, byte[] digest, Context context) {
        Objects.requireNonNull(algorithm, "Signature algorithm cannot be null.");
        Objects.requireNonNull(digest, "Digest content cannot be null.");

        KeySignRequest parameters = new KeySignRequest()
            .setAlgorithm(algorithm)
            .setValue(digest);
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        Response<KeyOperationResult> signResponse =
            service.sign(vaultUrl, keyName, version, serviceVersion, ACCEPT_LANGUAGE, parameters,
                CONTENT_TYPE_HEADER_VALUE, context);

        return new SignResult(signResponse.getValue().getResult(), algorithm, keyId);
    }

    Mono<VerifyResult> verifyAsync(SignatureAlgorithm algorithm, byte[] digest, byte[] signature, Context context) {
        Objects.requireNonNull(algorithm, "Signature algorithm cannot be null.");
        Objects.requireNonNull(digest, "Digest content cannot be null.");
        Objects.requireNonNull(signature, "Signature to be verified cannot be null.");

        KeyVerifyRequest parameters = new KeyVerifyRequest()
            .setAlgorithm(algorithm)
            .setDigest(digest)
            .setSignature(signature);

        return service.verifyAsync(vaultUrl, keyName, version, serviceVersion, ACCEPT_LANGUAGE, parameters,
                CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> LOGGER.verbose("Verifying content with algorithm - {}", algorithm))
            .doOnSuccess(response -> LOGGER.verbose("Retrieved verified content with algorithm - {}", algorithm))
            .doOnError(error -> LOGGER.warning("Failed to verify content with algorithm - {}", algorithm, error))
            .flatMap(response -> Mono.just(new VerifyResult(response.getValue().getValue(), algorithm, keyId)));
    }

    VerifyResult verify(SignatureAlgorithm algorithm, byte[] digest, byte[] signature, Context context) {
        Objects.requireNonNull(algorithm, "Signature algorithm cannot be null.");
        Objects.requireNonNull(digest, "Digest content cannot be null.");
        Objects.requireNonNull(signature, "Signature to be verified cannot be null.");

        KeyVerifyRequest parameters = new KeyVerifyRequest()
            .setAlgorithm(algorithm)
            .setDigest(digest)
            .setSignature(signature);
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        Response<KeyVerifyResponse> verifyResult =
            service.verify(vaultUrl, keyName, version, serviceVersion, ACCEPT_LANGUAGE, parameters,
                CONTENT_TYPE_HEADER_VALUE, context);

        return new VerifyResult(verifyResult.getValue().getValue(), algorithm, keyId);
    }

    Mono<WrapResult> wrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] key, Context context) {
        Objects.requireNonNull(algorithm, "Key wrap algorithm cannot be null.");
        Objects.requireNonNull(key, "Key content to be wrapped cannot be null.");

        KeyWrapUnwrapRequest parameters = new KeyWrapUnwrapRequest()
            .setAlgorithm(algorithm)
            .setValue(key);

        return service.wrapKeyAsync(vaultUrl, keyName, version, serviceVersion, ACCEPT_LANGUAGE, parameters,
                CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> LOGGER.verbose("Wrapping key content with algorithm - {}", algorithm))
            .doOnSuccess(response -> LOGGER.verbose("Retrieved wrapped key content with algorithm - {}", algorithm))
            .doOnError(error -> LOGGER.warning("Failed to verify content with algorithm - {}", algorithm, error))
            .flatMap(keyOperationResultResponse ->
                Mono.just(new WrapResult(keyOperationResultResponse.getValue().getResult(), algorithm, keyId)));
    }

    WrapResult wrapKey(KeyWrapAlgorithm algorithm, byte[] key, Context context) {
        Objects.requireNonNull(algorithm, "Key wrap algorithm cannot be null.");
        Objects.requireNonNull(key, "Key content to be wrapped cannot be null.");

        KeyWrapUnwrapRequest parameters = new KeyWrapUnwrapRequest()
            .setAlgorithm(algorithm)
            .setValue(key);
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        Response<KeyOperationResult> wrapResponse =
            service.wrapKey(vaultUrl, keyName, version, serviceVersion, ACCEPT_LANGUAGE, parameters,
                CONTENT_TYPE_HEADER_VALUE, context);

        return new WrapResult(wrapResponse.getValue().getResult(), algorithm, keyId);
    }

    Mono<UnwrapResult> unwrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] encryptedKey, Context context) {
        Objects.requireNonNull(algorithm, "Key wrap algorithm cannot be null.");
        Objects.requireNonNull(encryptedKey, "Encrypted key content to be unwrapped cannot be null.");

        KeyWrapUnwrapRequest parameters = new KeyWrapUnwrapRequest()
            .setAlgorithm(algorithm)
            .setValue(encryptedKey);

        return service.unwrapKeyAsync(vaultUrl, keyName, version, serviceVersion, ACCEPT_LANGUAGE, parameters,
                CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> LOGGER.verbose("Unwrapping key content with algorithm - {}", algorithm))
            .doOnSuccess(response -> LOGGER.verbose("Retrieved unwrapped key content with algorithm - {}", algorithm))
            .doOnError(error -> LOGGER.warning("Failed to unwrap key content with algorithm - {}", algorithm, error))
            .flatMap(response -> Mono.just(new UnwrapResult(response.getValue().getResult(), algorithm, keyId)));
    }

    UnwrapResult unwrapKey(KeyWrapAlgorithm algorithm, byte[] encryptedKey, Context context) {
        Objects.requireNonNull(algorithm, "Key wrap algorithm cannot be null.");
        Objects.requireNonNull(encryptedKey, "Encrypted key content to be unwrapped cannot be null.");

        KeyWrapUnwrapRequest parameters = new KeyWrapUnwrapRequest()
            .setAlgorithm(algorithm)
            .setValue(encryptedKey);
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        Response<KeyOperationResult> unwrapResponse =
            service.unwrapKey(vaultUrl, keyName, version, serviceVersion, ACCEPT_LANGUAGE, parameters,
                CONTENT_TYPE_HEADER_VALUE, context);

        return new UnwrapResult(unwrapResponse.getValue().getResult(), algorithm, keyId);
    }


    Mono<SignResult> signDataAsync(SignatureAlgorithm algorithm, byte[] data, Context context) {
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

    SignResult signData(SignatureAlgorithm algorithm, byte[] data, Context context) {
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

    Mono<VerifyResult> verifyDataAsync(SignatureAlgorithm algorithm, byte[] data, byte[] signature, Context context) {
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

    VerifyResult verifyData(SignatureAlgorithm algorithm, byte[] data, byte[] signature, Context context) {
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

    private void unpackId(String keyId) {
        if (keyId != null && keyId.length() > 0) {
            try {
                URL url = new URL(keyId);
                String[] tokens = url.getPath().split("/");
                this.vaultUrl = url.getProtocol() + "://" + url.getHost();

                if (url.getPort() != -1) {
                    this.vaultUrl += ":" + url.getPort();
                }

                this.keyName = (tokens.length >= 3 ? tokens[2] : null);
                this.version = (tokens.length >= 4 ? tokens[3] : "");
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }
}

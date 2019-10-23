// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.security.keyvault.keys.cryptography.models.DecryptResult;
import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.EncryptResult;
import com.azure.security.keyvault.keys.cryptography.models.UnwrapResult;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.SignResult;
import com.azure.security.keyvault.keys.cryptography.models.VerifyResult;
import com.azure.security.keyvault.keys.cryptography.models.WrapResult;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

class CryptographyServiceClient {
    static final String API_VERSION = "7.0";
    static final String ACCEPT_LANGUAGE = "en-US";
    static final String CONTENT_TYPE_HEADER_VALUE = "application/json";

    private final ClientLogger logger = new ClientLogger(CryptographyServiceClient.class);
    private final CryptographyService service;
    private String endpoint;
    private String version;
    private String keyName;
    private final String keyId;

    CryptographyServiceClient(String keyId, CryptographyService service) {
        Objects.requireNonNull(keyId);
        unpackId(keyId);
        this.keyId = keyId;
        this.service = service;
    }

    Mono<Response<KeyVaultKey>> getKey(Context context) {
        if (version == null) {
            version = "";
        }
        return getKey(keyName, version, context);
    }

    private Mono<Response<KeyVaultKey>> getKey(String name, String version, Context context) {
        return service.getKey(endpoint, name, version, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Retrieving key - {}", name))
            .doOnSuccess(response -> logger.info("Retrieved key - {}", response.getValue().getName()))
            .doOnError(error -> logger.warning("Failed to get key - {}", name, error));
    }

    Mono<EncryptResult> encrypt(EncryptionAlgorithm algorithm, byte[] plaintext, Context context) {

        KeyOperationParameters parameters = new KeyOperationParameters().setAlgorithm(algorithm).setValue(plaintext);
        return service.encrypt(endpoint, keyName, version, API_VERSION, ACCEPT_LANGUAGE, parameters,
            CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Encrypting content with algorithm - {}", algorithm.toString()))
            .doOnSuccess(response -> logger.info("Retrieved encrypted content with algorithm- {}",
                algorithm.toString()))
            .doOnError(error -> logger.warning("Failed to encrypt content with algorithm - {}", algorithm.toString(),
                error))
            .flatMap(keyOperationResultResponse ->
                Mono.just(new EncryptResult(keyOperationResultResponse.getValue().getResult(), algorithm, keyId)));
    }

    Mono<DecryptResult> decrypt(EncryptionAlgorithm algorithm, byte[] cipherText, Context context) {
        KeyOperationParameters parameters = new KeyOperationParameters().setAlgorithm(algorithm).setValue(cipherText);
        return service.decrypt(endpoint, keyName, version, API_VERSION, ACCEPT_LANGUAGE, parameters,
            CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Decrypting content with algorithm - {}", algorithm.toString()))
            .doOnSuccess(response -> logger.info("Retrieved decrypted content with algorithm- {}",
                algorithm.toString()))
            .doOnError(error -> logger.warning("Failed to decrypt content with algorithm - {}", algorithm.toString(),
                error))
            .flatMap(keyOperationResultResponse -> Mono.just(
                new DecryptResult(keyOperationResultResponse.getValue().getResult(), algorithm, keyId)));
    }

    Mono<SignResult> sign(SignatureAlgorithm algorithm, byte[] digest, Context context) {
        KeySignRequest parameters = new KeySignRequest().setAlgorithm(algorithm).setValue(digest);
        return service.sign(endpoint, keyName, version, API_VERSION, ACCEPT_LANGUAGE, parameters,
            CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Signing content with algorithm - {}", algorithm.toString()))
            .doOnSuccess(response -> logger.info("Retrieved signed content with algorithm- {}", algorithm.toString()))
            .doOnError(error -> logger.warning("Failed to sign content with algorithm - {}", algorithm.toString(),
                error))
            .flatMap(keyOperationResultResponse ->
                Mono.just(new SignResult(keyOperationResultResponse.getValue().getResult(), algorithm, keyId)));
    }

    Mono<VerifyResult> verify(SignatureAlgorithm algorithm, byte[] digest, byte[] signature, Context context) {

        KeyVerifyRequest parameters = new KeyVerifyRequest().setAlgorithm(algorithm).setDigest(digest).setSignature(signature);
        return service.verify(endpoint, keyName, version, API_VERSION, ACCEPT_LANGUAGE, parameters,
            CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Verifying content with algorithm - {}", algorithm.toString()))
            .doOnSuccess(response -> logger.info("Retrieved verified content with algorithm- {}", algorithm.toString()))
            .doOnError(error -> logger.warning("Failed to verify content with algorithm - {}", algorithm.toString(),
                error))
            .flatMap(response ->
                Mono.just(new VerifyResult(response.getValue().getValue(), algorithm, keyId)));
    }

    Mono<WrapResult> wrapKey(KeyWrapAlgorithm algorithm, byte[] key, Context context) {

        KeyWrapUnwrapRequest parameters = new KeyWrapUnwrapRequest().setAlgorithm(algorithm).setValue(key);
        return service.wrapKey(endpoint, keyName, version, API_VERSION, ACCEPT_LANGUAGE, parameters,
            CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Wrapping key content with algorithm - {}", algorithm.toString()))
            .doOnSuccess(response -> logger.info("Retrieved wrapped key content with algorithm- {}",
                algorithm.toString()))
            .doOnError(error -> logger.warning("Failed to verify content with algorithm - {}", algorithm.toString(),
                error))
            .flatMap(keyOperationResultResponse ->
                Mono.just(new WrapResult(keyOperationResultResponse.getValue().getResult(), algorithm, keyId)));
    }

    Mono<UnwrapResult> unwrapKey(KeyWrapAlgorithm algorithm, byte[] encryptedKey, Context context) {

        KeyWrapUnwrapRequest parameters = new KeyWrapUnwrapRequest().setAlgorithm(algorithm).setValue(encryptedKey);
        return service.unwrapKey(endpoint, keyName, version, API_VERSION, ACCEPT_LANGUAGE, parameters,
            CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> logger.info("Unwrapping key content with algorithm - {}", algorithm.toString()))
            .doOnSuccess(response -> logger.info("Retrieved unwrapped key content with algorithm- {}",
                algorithm.toString()))
            .doOnError(error -> logger.warning("Failed to unwrap key content with algorithm - {}",
                algorithm.toString(), error))
            .flatMap(response ->
                Mono.just(new UnwrapResult(response.getValue().getResult(), algorithm, keyId)));
    }


    Mono<SignResult> signData(SignatureAlgorithm algorithm, byte[] data, Context context) {
        try {
            HashAlgorithm hashAlgorithm = SignatureHashResolver.DEFAULT.get(algorithm);
            MessageDigest md = MessageDigest.getInstance(hashAlgorithm.toString());
            md.update(data);
            byte[] digest = md.digest();
            return sign(algorithm, digest, context);
        } catch (NoSuchAlgorithmException e) {
            return Mono.error(e);
        }
    }


    Mono<VerifyResult> verifyData(SignatureAlgorithm algorithm, byte[] data, byte[] signature, Context context) {
        try {
            HashAlgorithm hashAlgorithm = SignatureHashResolver.DEFAULT.get(algorithm);
            MessageDigest md = MessageDigest.getInstance(hashAlgorithm.toString());
            md.update(data);
            byte[] digest = md.digest();
            return verify(algorithm, digest, signature, context);
        } catch (NoSuchAlgorithmException e) {
            return Mono.error(e);
        }
    }

    private void unpackId(String keyId) {
        if (keyId != null && keyId.length() > 0) {
            try {
                URL url = new URL(keyId);
                String[] tokens = url.getPath().split("/");
                this.endpoint = url.getProtocol() + "://" + url.getHost();
                this.keyName = (tokens.length >= 3 ? tokens[2] : null);
                this.version = (tokens.length >= 4 ? tokens[3] : null);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
        }
    }


}

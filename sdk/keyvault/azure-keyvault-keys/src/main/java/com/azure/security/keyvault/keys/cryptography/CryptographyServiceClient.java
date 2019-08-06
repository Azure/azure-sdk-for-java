// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.security.keyvault.keys.cryptography.models.DecryptResult;
import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.EncryptResult;
import com.azure.security.keyvault.keys.cryptography.models.KeyUnwrapResult;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapResult;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.SignResult;
import com.azure.security.keyvault.keys.cryptography.models.VerifyResult;
import com.azure.security.keyvault.keys.models.Key;
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

    /*
     * Creates a CryptographyServiceClient that uses {@code service} to service requests
     *
     * @param service the service to use for cryptography operations.
     */
    CryptographyServiceClient(CryptographyService service) {
        this.service = service;
    }

    CryptographyServiceClient(String keyId, CryptographyService service) {
        Objects.requireNonNull(keyId);
        unpackId(keyId);
        this.service = service;
    }

    Mono<Response<Key>> getKey(Context context) {
        if (version == null) {
            version = "";
        }
        return getKey(keyName, version, context);
    }

    private Mono<Response<Key>> getKey(String name, String version, Context context) {
        return service.getKey(endpoint, name, version, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context)
                .doOnRequest(ignored -> logger.info("Retrieving key - {}",  name))
                .doOnSuccess(response -> logger.info("Retrieved key - {}", response.value().name()))
                .doOnError(error -> logger.warning("Failed to get key - {}", name, error));
    }

    Mono<EncryptResult> encrypt(EncryptionAlgorithm algorithm, byte[] plaintext, Context context) {

        KeyOperationParameters parameters = new KeyOperationParameters().algorithm(algorithm).value(plaintext);
        return service.encrypt(endpoint, keyName, version, API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE, context)
                .doOnRequest(ignored -> logger.info("Encrypting content with algorithm - {}",  algorithm.toString()))
                .doOnSuccess(response -> logger.info("Retrieved encrypted content with algorithm- {}", algorithm.toString()))
                .doOnError(error -> logger.warning("Failed to encrypt content with algorithm - {}", algorithm.toString(), error))
                .flatMap(keyOperationResultResponse ->
                      Mono.just(new EncryptResult(keyOperationResultResponse.value().result(), null, algorithm)));
    }

    Mono<DecryptResult> decrypt(EncryptionAlgorithm algorithm, byte[] cipherText, Context context) {
        KeyOperationParameters parameters = new KeyOperationParameters().algorithm(algorithm).value(cipherText);
        return service.decrypt(endpoint, keyName, version, API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE, context)
                .doOnRequest(ignored -> logger.info("Decrypting content with algorithm - {}",  algorithm.toString()))
                .doOnSuccess(response -> logger.info("Retrieved decrypted content with algorithm- {}", algorithm.toString()))
                .doOnError(error -> logger.warning("Failed to decrypt content with algorithm - {}", algorithm.toString(), error))
                .flatMap(keyOperationResultResponse -> Mono.just(new DecryptResult(keyOperationResultResponse.value().result())));
    }

    Mono<SignResult> sign(SignatureAlgorithm algorithm, byte[] digest, Context context) {
        KeySignRequest parameters = new KeySignRequest().algorithm(algorithm).value(digest);
        return service.sign(endpoint, keyName, version, API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE, context)
                .doOnRequest(ignored -> logger.info("Signing content with algorithm - {}",  algorithm.toString()))
                .doOnSuccess(response -> logger.info("Retrieved signed content with algorithm- {}", algorithm.toString()))
                .doOnError(error -> logger.warning("Failed to sign content with algorithm - {}", algorithm.toString(), error))
                .flatMap(keyOperationResultResponse ->
                        Mono.just(new SignResult(keyOperationResultResponse.value().result(), algorithm)));
    }

    Mono<VerifyResult> verify(SignatureAlgorithm algorithm, byte[] digest, byte[] signature, Context context) {

        KeyVerifyRequest parameters = new KeyVerifyRequest().algorithm(algorithm).digest(digest).signature(signature);
        return service.verify(endpoint, keyName, version, API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE, context)
                .doOnRequest(ignored -> logger.info("Verifying content with algorithm - {}",  algorithm.toString()))
                .doOnSuccess(response -> logger.info("Retrieved verified content with algorithm- {}", algorithm.toString()))
                .doOnError(error -> logger.warning("Failed to verify content with algorithm - {}", algorithm.toString(), error))
                .flatMap(response ->
                        Mono.just(new VerifyResult(response.value().value())));
    }

    Mono<KeyWrapResult> wrapKey(KeyWrapAlgorithm algorithm, byte[] key, Context context) {

        KeyWrapUnwrapRequest parameters = new KeyWrapUnwrapRequest().algorithm(algorithm).value(key);
        return service.wrapKey(endpoint, keyName, version, API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE, context)
                .doOnRequest(ignored -> logger.info("Wrapping key content with algorithm - {}",  algorithm.toString()))
                .doOnSuccess(response -> logger.info("Retrieved wrapped key content with algorithm- {}", algorithm.toString()))
                .doOnError(error -> logger.warning("Failed to verify content with algorithm - {}", algorithm.toString(), error))
                .flatMap(keyOperationResultResponse ->
                        Mono.just(new KeyWrapResult(keyOperationResultResponse.value().result(), algorithm)));
    }

    Mono<KeyUnwrapResult> unwrapKey(KeyWrapAlgorithm algorithm, byte[] encryptedKey, Context context) {

        KeyWrapUnwrapRequest parameters = new KeyWrapUnwrapRequest().algorithm(algorithm).value(encryptedKey);
        return service.unwrapKey(endpoint, keyName, version, API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE, context)
                .doOnRequest(ignored -> logger.info("Unwrapping key content with algorithm - {}",  algorithm.toString()))
                .doOnSuccess(response -> logger.info("Retrieved unwrapped key content with algorithm- {}", algorithm.toString()))
                .doOnError(error -> logger.warning("Failed to unwrap key content with algorithm - {}", algorithm.toString(), error))
                .flatMap(response ->
                        Mono.just(new KeyUnwrapResult(response.value().result())));
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

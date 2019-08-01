package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.security.keyvault.keys.models.Key;
import com.azure.security.keyvault.keys.models.webkey.JsonWebKey;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import static com.azure.core.implementation.util.FluxUtil.withContext;

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

    CryptographyServiceClient(String kid, CryptographyService service) {
        Objects.requireNonNull(kid);
        unpackId(kid);
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
                .doOnRequest(ignored -> logger.info("Retrieving keyPair - {}",  name))
                .doOnSuccess(response -> logger.info("Retrieved keyPair - {}", response.value().name()))
                .doOnError(error -> logger.warning("Failed to get keyPair - {}", name, error));
    }

    Mono<EncryptResult> encryptAsync(EncryptionAlgorithm algorithm, byte[] plaintext, Context context) {

        KeyOperationParameters parameters = new KeyOperationParameters().algorithm(algorithm).value(plaintext);
        return service.encrypt(endpoint,keyName,version,API_VERSION,ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE, context)
                .flatMap(keyOperationResultResponse ->
                      Mono.just(new EncryptResult(keyOperationResultResponse.value().result(), null, algorithm)));

        // plaintext is required
        // iv is required for some implementation, for instance AESCBC
        // authenticationData is never required but it only makes sense in the case of authenticated encryption implementation, for instance AES-GCM AESCBC-HMAC
        // algorithm is not required in the case that we have the keyPair locally and know the keytype (in which case we can use the best available algorithm for the keytype), otherwise it would be required

        // this method **could** be performed locally in the case that the public portion of the keyPair is available locally for asymmetric keys, or all the keyPair data in the case of symmetric
    }

    Mono<byte[]> decryptAsync(EncryptionAlgorithm algorithm, byte[] cipherText, Context context) {

        KeyOperationParameters parameters = new KeyOperationParameters().algorithm(algorithm).value(cipherText);
        return service.decrypt(endpoint,keyName,version,API_VERSION,ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE, context)
                .flatMap(keyOperationResultResponse -> Mono.just(keyOperationResultResponse.value().result()));

        // plaintext is required
        // iv is required for some implementation, for instance AESCBC
        // authenticationData is never required but it only makes sense in the case of authenticated encryption implementation, for instance AES-GCM AESCBC-HMAC
        // algorithm is not required in the case that we have the keyPair locally and know the keytype (in which case we can use the best available algorithm for the keytype), otherwise it would be required

        // this method **could** be performed locally in the case that the public portion of the keyPair is available locally for asymmetric keys, or all the keyPair data in the case of symmetric
    }

    Mono<SignResult> signAsync(SignatureAlgorithm algorithm, byte[] digest, Context context) {

        KeySignRequest parameters = new KeySignRequest().algorithm(algorithm).value(digest);
        return service.sign(endpoint,keyName,version,API_VERSION,ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE, context)
                .flatMap(keyOperationResultResponse ->
                        Mono.just(new SignResult(keyOperationResultResponse.value().result(), algorithm)));
        // plaintext is required
        // iv is required for some implementation, for instance AESCBC
        // authenticationData is never required but it only makes sense in the case of authenticated encryption implementation, for instance AES-GCM AESCBC-HMAC
        // algorithm is not required in the case that we have the keyPair locally and know the keytype (in which case we can use the best available algorithm for the keytype), otherwise it would be required

        // this method **could** be performed locally in the case that the public portion of the keyPair is available locally for asymmetric keys, or all the keyPair data in the case of symmetric
    }

    Mono<Boolean> verifyAsync(SignatureAlgorithm algorithm, byte[] digest, byte[] signature, Context context) {

        KeyVerifyRequest parameters = new KeyVerifyRequest().algorithm(algorithm).digest(digest).signature(signature);
        return service.verify(endpoint,keyName,version,API_VERSION,ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE, context)
                .flatMap(response ->
                        Mono.just(response.value().value()));
        // plaintext is required
        // iv is required for some implementation, for instance AESCBC
        // authenticationData is never required but it only makes sense in the case of authenticated encryption implementation, for instance AES-GCM AESCBC-HMAC
        // algorithm is not required in the case that we have the keyPair locally and know the keytype (in which case we can use the best available algorithm for the keytype), otherwise it would be required

        // this method **could** be performed locally in the case that the public portion of the keyPair is available locally for asymmetric keys, or all the keyPair data in the case of symmetric
    }

    Mono<KeyWrapResult> wrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] key, Context context) {

        KeyWrapUnwrapRequest parameters = new KeyWrapUnwrapRequest().algorithm(algorithm).value(key);
        return service.wrapKey(endpoint,keyName,version,API_VERSION,ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE, context)
                .flatMap(keyOperationResultResponse ->
                        Mono.just(new KeyWrapResult(keyOperationResultResponse.value().result(),algorithm)));
        // plaintext is required
        // iv is required for some implementation, for instance AESCBC
        // authenticationData is never required but it only makes sense in the case of authenticated encryption implementation, for instance AES-GCM AESCBC-HMAC
        // algorithm is not required in the case that we have the keyPair locally and know the keytype (in which case we can use the best available algorithm for the keytype), otherwise it would be required

        // this method **could** be performed locally in the case that the public portion of the keyPair is available locally for asymmetric keys, or all the keyPair data in the case of symmetric
    }

    Mono<byte[]> unwrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] encryptedKey, Context context) {

        KeyWrapUnwrapRequest parameters = new KeyWrapUnwrapRequest().algorithm(algorithm).value(encryptedKey);
        return service.unwrapKey(endpoint,keyName,version,API_VERSION,ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE, context)
                .flatMap(response ->
                        Mono.just(response.value().result()));
        // plaintext is required
        // iv is required for some implementation, for instance AESCBC
        // authenticationData is never required but it only makes sense in the case of authenticated encryption implementation, for instance AES-GCM AESCBC-HMAC
        // algorithm is not required in the case that we have the keyPair locally and know the keytype (in which case we can use the best available algorithm for the keytype), otherwise it would be required

        // this method **could** be performed locally in the case that the public portion of the keyPair is available locally for asymmetric keys, or all the keyPair data in the case of symmetric
    }


    Mono<SignResult> signDataAsync(SignatureAlgorithm algorithm, byte[] data, Context context) throws NoSuchAlgorithmException {

        HashAlgorithm hashAlgorithm = SignatureHashResolver.Default.get(algorithm);
        MessageDigest md = MessageDigest.getInstance(hashAlgorithm.toString());
        md.update(data);
        byte[] digest = md.digest();

        KeySignRequest parameters = new KeySignRequest().algorithm(algorithm).value(digest);
        return service.sign(endpoint, keyName, version, API_VERSION, ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE, context)
                .flatMap(keyOperationResultResponse ->
                        Mono.just(new SignResult(keyOperationResultResponse.value().result(),algorithm)));

        // plaintext is required
        // iv is required for some implementation, for instance AESCBC
        // authenticationData is never required but it only makes sense in the case of authenticated encryption implementation, for instance AES-GCM AESCBC-HMAC
        // algorithm is not required in the case that we have the keyPair locally and know the keytype (in which case we can use the best available algorithm for the keytype), otherwise it would be required

        // this method **could** be performed locally in the case that the public portion of the keyPair is available locally for asymmetric keys, or all the keyPair data in the case of symmetric
    }


    Mono<Boolean> verifyDataAsync(SignatureAlgorithm algorithm, byte[] data, byte[] signature, Context context) throws NoSuchAlgorithmException {
        HashAlgorithm hashAlgorithm = SignatureHashResolver.Default.get(algorithm);
        MessageDigest md = MessageDigest.getInstance(hashAlgorithm.toString());
        md.update(data);
        byte[] digest = md.digest();

        KeyVerifyRequest parameters = new KeyVerifyRequest().algorithm(algorithm).digest(digest).signature(signature);
        return service.verify(endpoint,keyName,version,API_VERSION,ACCEPT_LANGUAGE, parameters, CONTENT_TYPE_HEADER_VALUE, context)
                .flatMap(response ->
                        Mono.just(response.value().value()));
        // plaintext is required
        // iv is required for some implementation, for instance AESCBC
        // authenticationData is never required but it only makes sense in the case of authenticated encryption implementation, for instance AES-GCM AESCBC-HMAC
        // algorithm is not required in the case that we have the keyPair locally and know the keytype (in which case we can use the best available algorithm for the keytype), otherwise it would be required

        // this method **could** be performed locally in the case that the public portion of the keyPair is available locally for asymmetric keys, or all the keyPair data in the case of symmetric
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

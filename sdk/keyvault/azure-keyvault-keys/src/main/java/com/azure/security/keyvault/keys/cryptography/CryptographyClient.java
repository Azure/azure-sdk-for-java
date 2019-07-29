package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.implementation.RestProxy;
import com.azure.core.implementation.annotation.ReturnType;
import com.azure.core.implementation.annotation.ServiceClient;
import com.azure.core.implementation.annotation.ServiceMethod;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.security.keyvault.keys.KeyClientBuilder;
import com.azure.security.keyvault.keys.models.Key;
import com.azure.security.keyvault.keys.models.webkey.JsonWebKey;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Objects;

import static com.azure.core.implementation.util.FluxUtil.withContext;

@ServiceClient(builder = KeyClientBuilder.class, isAsync = true, serviceInterfaces = CryptographyService.class)
public final class CryptographyClient {
    static final String API_VERSION = "7.0";
    static final String ACCEPT_LANGUAGE = "en-US";
    static final String CONTENT_TYPE_HEADER_VALUE = "application/json";
    static final String KEY_VAULT_SCOPE = "https://vault.azure.net/.default";

    private JsonWebKey key;
    private final CryptographyService service;
    private final ClientLogger logger = new ClientLogger(com.azure.security.keyvault.keys.cryptography.CryptographyClient.class);
    private String endpoint;
    private String version;
    private String keyName;
    private EcKeyCryptographyClient ecKeyCryptographyClient;
    private RsaKeyCryptographyClient rsaKeyCryptographyClient;


    /**
     * Creates a CryptographyClient that uses {@code pipeline} to service requests
     *
     * @param key the JsonWebKey to use for cryptography operations.
     * @param pipeline HttpPipeline that the HTTP requests and responses flow through.
     */
    CryptographyClient(JsonWebKey key, HttpPipeline pipeline) {
        Objects.requireNonNull(key);
        this.key = key;
        service = RestProxy.create(CryptographyService.class, pipeline);
        ecKeyCryptographyClient = new EcKeyCryptographyClient(key, service);
        rsaKeyCryptographyClient = new RsaKeyCryptographyClient(key, service);
    }

    CryptographyClient(String kid, HttpPipeline pipeline) {
        Objects.requireNonNull(kid);
        service = RestProxy.create(CryptographyService.class, pipeline);
        ecKeyCryptographyClient = new EcKeyCryptographyClient(kid, service);
        rsaKeyCryptographyClient = new RsaKeyCryptographyClient(kid, service);
        unpackId(kid);
    }

    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Key>> getKey() {
        if (version == null) {
            version = "";
        }
        return withContext(context -> getKey(keyName, version, context));
    }

    private Mono<Response<Key>> getKey(String name, String version, Context context) {
        return service.getKey(endpoint, name, version, API_VERSION, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context)
                .doOnRequest(ignored -> logger.info("Retrieving key - {}",  name))
                .doOnSuccess(response -> logger.info("Retrieved key - {}", response.value().name()))
                .doOnError(error -> logger.warning("Failed to get key - {}", name, error));
    }


    public Mono<EncryptResult> encryptAsync(EncryptionAlgorithm algorithm, byte[] plaintext) {
        return withContext(context -> encryptAsync(algorithm, plaintext, context));
    }

    public EncryptResult encrypt(EncryptionAlgorithm algorithm, byte[] plaintext) {
        return encryptAsync(algorithm, plaintext).block();
    }


    Mono<EncryptResult> encryptAsync(EncryptionAlgorithm algorithm, byte[] plaintext, Context context) {

        if(key == null){
            this.key = getKey().block().value().keyMaterial();
        }

        switch(key.kty()){
            case RSA:
            case RSA_HSM:
                return rsaKeyCryptographyClient.encryptAsync(algorithm, plaintext, context);
            case EC:
            case EC_HSM:
                return ecKeyCryptographyClient.encryptAsync(algorithm, plaintext, context);
            default:
                throw new UnsupportedOperationException(String.format("Encrypt Async is not supported for Key Type: %s", key.kty().toString()));
        }

    }

    public Mono<byte[]> decryptAsync(EncryptionAlgorithm algorithm, byte[] cipherText) {
        return withContext(context -> decryptAsync(algorithm, cipherText, context));
    }

    public byte[] decrypt(EncryptionAlgorithm algorithm, byte[] cipherText) {
        return decryptAsync(algorithm, cipherText).block();
    }

    Mono<byte[]> decryptAsync(EncryptionAlgorithm algorithm, byte[] cipherText, Context context) {

        if(key == null){
            this.key = getKey().block().value().keyMaterial();
        }

        switch(key.kty()){
            case RSA:
            case RSA_HSM:
                return rsaKeyCryptographyClient.decryptAsync(algorithm, cipherText, context);
            case EC:
            case EC_HSM:
                return ecKeyCryptographyClient.decryptAsync(algorithm, cipherText, context);
            default:
                throw new UnsupportedOperationException(String.format("Encrypt Async is not supported for Key Type: %s", key.kty().toString()));
        }

     }

    public Mono<SignResult> signAsync(SignatureAlgorithm algorithm, byte[] digest) {
        return withContext(context -> signAsync(algorithm, digest, context));
    }

    public SignResult sign(SignatureAlgorithm algorithm, byte[] digest) {
        return signAsync(algorithm, digest).block();
    }

    Mono<SignResult> signAsync(SignatureAlgorithm algorithm, byte[] digest, Context context) {

        if(key == null){
            this.key = getKey().block().value().keyMaterial();
        }

        switch(this.key.kty()){
            case RSA:
            case RSA_HSM:
                return rsaKeyCryptographyClient.signAsync(algorithm, digest, context);
            case EC:
            case EC_HSM:
                return ecKeyCryptographyClient.signAsync(algorithm, digest, context);
            default:
                throw new UnsupportedOperationException(String.format("Encrypt Async is not supported for Key Type: %s", key.kty().toString()));
        }

    }

    public Mono<Boolean> verifyAsync(SignatureAlgorithm algorithm, byte[] digest, byte[] signature) {
        return withContext(context -> verifyAsync(algorithm, digest, signature, context));
    }

    public Boolean verify(SignatureAlgorithm algorithm, byte[] digest, byte[] signature) {
        return verifyAsync(algorithm, digest, signature).block();
    }

    Mono<Boolean> verifyAsync(SignatureAlgorithm algorithm, byte[] digest, byte[] signature, Context context) {

        if(key == null){
            this.key = getKey().block().value().keyMaterial();
        }

        switch(this.key.kty()){
            case RSA:
            case RSA_HSM:
                return rsaKeyCryptographyClient.verifyAsync(algorithm, digest, signature, context);
            case EC:
            case EC_HSM:
                return ecKeyCryptographyClient.verifyAsync(algorithm, digest, signature, context);
            default:
                throw new UnsupportedOperationException(String.format("Encrypt Async is not supported for Key Type: %s", key.kty().toString()));
        }

    }


    public Mono<KeyWrapResult> wrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] key) {
        return withContext(context -> wrapKeyAsync(algorithm, key, context));
    }

    public KeyWrapResult wrapKey(KeyWrapAlgorithm algorithm, byte[] key) {
        return wrapKeyAsync(algorithm, key).block();
    }

    Mono<KeyWrapResult> wrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] key, Context context) {

        if(this.key == null){
            this.key = getKey().block().value().keyMaterial();
        }

        switch(this.key.kty()){
            case RSA:
            case RSA_HSM:
                return rsaKeyCryptographyClient.wrapKeyAsync(algorithm, key, context);
            case EC:
            case EC_HSM:
                return ecKeyCryptographyClient.wrapKeyAsync(algorithm, key, context);
            default:
                throw new UnsupportedOperationException(String.format("Encrypt Async is not supported for Key Type: %s", this.key.kty().toString()));
        }
    }


    public Mono<byte[]> unwrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] encryptedKey) {
        return withContext(context -> unwrapKeyAsync(algorithm, encryptedKey, context));
    }

    Mono<byte[]> unwrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] encryptedKey, Context context) {

        if(key == null){
            this.key = getKey().block().value().keyMaterial();
        }

        switch(this.key.kty()){
            case RSA:
            case RSA_HSM:
                return rsaKeyCryptographyClient.unwrapKeyAsync(algorithm, encryptedKey, context);
            case EC:
            case EC_HSM:
                return ecKeyCryptographyClient.unwrapKeyAsync(algorithm, encryptedKey, context);
            default:
                throw new UnsupportedOperationException(String.format("Encrypt Async is not supported for Key Type: %s", key.kty().toString()));
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

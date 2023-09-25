// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.RestProxy;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.security.keyvault.keys.cryptography.implementation.CryptographyService;
import com.azure.security.keyvault.keys.cryptography.implementation.KeyOperationParameters;
import com.azure.security.keyvault.keys.cryptography.implementation.KeyOperationResult;
import com.azure.security.keyvault.keys.cryptography.implementation.KeySignRequest;
import com.azure.security.keyvault.keys.cryptography.implementation.KeyVerifyRequest;
import com.azure.security.keyvault.keys.cryptography.implementation.KeyVerifyResponse;
import com.azure.security.keyvault.keys.cryptography.implementation.KeyWrapUnwrapRequest;
import com.azure.security.keyvault.keys.cryptography.implementation.SecretKey;
import com.azure.security.keyvault.keys.cryptography.implementation.SecretRequestAttributes;
import com.azure.security.keyvault.keys.cryptography.implementation.SecretRequestParameters;
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
import com.azure.security.keyvault.keys.models.JsonWebKey;
import com.azure.security.keyvault.keys.models.KeyOperation;
import com.azure.security.keyvault.keys.models.KeyType;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

import static com.azure.security.keyvault.keys.models.KeyType.EC;
import static com.azure.security.keyvault.keys.models.KeyType.EC_HSM;
import static com.azure.security.keyvault.keys.models.KeyType.OCT;
import static com.azure.security.keyvault.keys.models.KeyType.OCT_HSM;
import static com.azure.security.keyvault.keys.models.KeyType.RSA;
import static com.azure.security.keyvault.keys.models.KeyType.RSA_HSM;

class CryptographyClientImpl {
    private static final ClientLogger LOGGER = new ClientLogger(CryptographyClientImpl.class);
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final String HTTP_REST_PROXY_SYNC_PROXY_ENABLE = "com.azure.core.http.restproxy.syncproxy.enable";

    static final String SECRETS_COLLECTION = "secrets";

    private final CryptographyService service;
    private final String serviceVersion;
    private final String keyId;

    private String vaultUrl;
    private String keyName;
    private String version;

    static final String ACCEPT_LANGUAGE = "en-US";
    static final String CONTENT_TYPE_HEADER_VALUE = "application/json";


    CryptographyClientImpl(String keyId, HttpPipeline pipeline, CryptographyServiceVersion serviceVersion) {
        Objects.requireNonNull(keyId);

        unpackId(keyId);

        this.keyId = keyId;
        this.service = RestProxy.create(CryptographyService.class, pipeline);
        this.serviceVersion = serviceVersion.getVersion();
    }

    String getVaultUrl() {
        return vaultUrl;
    }

    Mono<String> getKeyIdAsync() {
        return Mono.defer(() -> Mono.just(this.keyId));
    }

    String getKeyId() {
        return this.keyId;
    }

    Mono<Response<KeyVaultKey>> getKeyAsync(Context context) {
        if (version == null) {
            version = "";
        }

        return getKeyAsync(keyName, version, context);
    }

    Response<KeyVaultKey> getKey(Context context) {
        if (version == null) {
            version = "";
        }

        return getKey(keyName, version, context);
    }

    private Mono<Response<KeyVaultKey>> getKeyAsync(String name, String version, Context context) {
        return service.getKeyAsync(vaultUrl, name, version, serviceVersion, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> LOGGER.verbose("Retrieving key - {}", name))
            .doOnSuccess(response -> LOGGER.verbose("Retrieved key - {}", response.getValue().getName()))
            .doOnError(error -> LOGGER.warning("Failed to get key - {}", name, error));
    }

    private Response<KeyVaultKey> getKey(String name, String version, Context context) {
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.getKey(vaultUrl, name, version, serviceVersion, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE, context);
    }

    Mono<Response<JsonWebKey>> getSecretKeyAsync(Context context) {
        return service.getSecretAsync(vaultUrl, keyName, version, serviceVersion, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
                context)
            .doOnRequest(ignored -> LOGGER.verbose("Retrieving key - {}", keyName))
            .doOnSuccess(response -> LOGGER.verbose("Retrieved key - {}", response.getValue().getName()))
            .doOnError(error -> LOGGER.warning("Failed to get key - {}", keyName, error))
            .flatMap((stringResponse -> {
                try {
                    return Mono.just(new SimpleResponse<>(stringResponse.getRequest(),
                        stringResponse.getStatusCode(),
                        stringResponse.getHeaders(), transformSecretKey(stringResponse.getValue())));
                } catch (JsonProcessingException e) {
                    return Mono.error(e);
                }
            }));
    }

    Response<JsonWebKey> getSecretKey(Context context) {
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        Response<SecretKey> secretKeyResponse =
            service.getSecret(vaultUrl, keyName, version, serviceVersion, ACCEPT_LANGUAGE, CONTENT_TYPE_HEADER_VALUE,
                context);

        try {
            return new SimpleResponse<>(secretKeyResponse.getRequest(), secretKeyResponse.getStatusCode(),
                secretKeyResponse.getHeaders(), transformSecretKey(secretKeyResponse.getValue()));
        } catch (JsonProcessingException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    Mono<Response<SecretKey>> setSecretKeyAsync(SecretKey secret, Context context) {
        Objects.requireNonNull(secret, "The secret key cannot be null.");

        SecretRequestParameters parameters = new SecretRequestParameters()
            .setValue(secret.getValue())
            .setTags(secret.getProperties().getTags())
            .setContentType(secret.getProperties().getContentType())
            .setSecretAttributes(new SecretRequestAttributes(secret.getProperties()));

        return service.setSecretAsync(vaultUrl, secret.getName(), serviceVersion, ACCEPT_LANGUAGE, parameters,
                CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> LOGGER.verbose("Setting secret - {}", secret.getName()))
            .doOnSuccess(response -> LOGGER.verbose("Set secret - {}", response.getValue().getName()))
            .doOnError(error -> LOGGER.warning("Failed to set secret - {}", secret.getName(), error));
    }

    Response<SecretKey> setSecretKey(SecretKey secret, Context context) {
        Objects.requireNonNull(secret, "The Secret input parameter cannot be null.");

        SecretRequestParameters parameters = new SecretRequestParameters()
            .setValue(secret.getValue())
            .setTags(secret.getProperties().getTags())
            .setContentType(secret.getProperties().getContentType())
            .setSecretAttributes(new SecretRequestAttributes(secret.getProperties()));
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);

        return service.setSecret(vaultUrl, secret.getName(), serviceVersion, ACCEPT_LANGUAGE, parameters,
            CONTENT_TYPE_HEADER_VALUE, context);
    }

    JsonWebKey transformSecretKey(SecretKey secretKey) throws JsonProcessingException {
        ObjectNode rootNode = MAPPER.createObjectNode();
        ArrayNode a = MAPPER.createArrayNode();

        a.add(KeyOperation.WRAP_KEY.toString());
        a.add(KeyOperation.UNWRAP_KEY.toString());
        a.add(KeyOperation.ENCRYPT.toString());
        a.add(KeyOperation.DECRYPT.toString());

        rootNode.put("k", Base64.getUrlDecoder().decode(secretKey.getValue()));
        rootNode.put("kid", this.keyId);
        rootNode.put("kty", KeyType.OCT.toString());
        rootNode.set("key_ops", a);

        return MAPPER.treeToValue(rootNode, JsonWebKey.class);
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
        EncryptionAlgorithm algorithm = keyOperationParameters.getAlgorithm();

        return service.encryptAsync(vaultUrl, keyName, version, serviceVersion, ACCEPT_LANGUAGE, keyOperationParameters,
                CONTENT_TYPE_HEADER_VALUE, context)
            .doOnRequest(ignored -> LOGGER.verbose("Encrypting content with algorithm - {}", algorithm))
            .doOnSuccess(response -> LOGGER.verbose("Retrieved encrypted content with algorithm - {}", algorithm))
            .doOnError(error -> LOGGER.warning("Failed to encrypt content with algorithm - {}", algorithm, error))
            .map(keyOperationResultResponse -> {
                KeyOperationResult keyOperationResult = keyOperationResultResponse.getValue();

                return new EncryptResult(keyOperationResult.getResult(), algorithm, keyId,
                    keyOperationResult.getIv(), keyOperationResult.getAuthenticationTag(),
                    keyOperationResult.getAdditionalAuthenticatedData());
            });
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
        EncryptionAlgorithm algorithm = keyOperationParameters.getAlgorithm();
        context = context == null ? Context.NONE : context;
        context = enableSyncRestProxy(context);
        Response<KeyOperationResult> encryptResult =
            service.encrypt(vaultUrl, keyName, version, serviceVersion, ACCEPT_LANGUAGE, keyOperationParameters,
                CONTENT_TYPE_HEADER_VALUE, context);
        KeyOperationResult keyOperationResult = encryptResult.getValue();

        return new EncryptResult(keyOperationResult.getResult(), algorithm, keyId, keyOperationResult.getIv(),
            keyOperationResult.getAuthenticationTag(), keyOperationResult.getAdditionalAuthenticatedData());
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

    private Context enableSyncRestProxy(Context context) {
        return context.addData(HTTP_REST_PROXY_SYNC_PROXY_ENABLE, true);
    }

    static String unpackAndValidateId(String keyId) {
        if (CoreUtils.isNullOrEmpty(keyId)) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("'keyId' cannot be null or empty."));
        }

        try {
            URL url = new URL(keyId);
            String[] tokens = url.getPath().split("/");
            String endpoint = url.getProtocol() + "://" + url.getHost();

            if (url.getPort() != -1) {
                endpoint += ":" + url.getPort();
            }

            String keyName = (tokens.length >= 3 ? tokens[2] : null);
            String keyCollection = (tokens.length >= 2 ? tokens[1] : null);

            if (Strings.isNullOrEmpty(endpoint)) {
                throw LOGGER.logExceptionAsError(
                    new IllegalArgumentException("Key endpoint in key identifier is invalid."));
            } else if (Strings.isNullOrEmpty(keyName)) {
                throw LOGGER.logExceptionAsError(
                    new IllegalArgumentException("Key name in key identifier is invalid."));
            }

            return keyCollection;
        } catch (MalformedURLException e) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("The key identifier is malformed.", e));
        }
    }

    static LocalKeyCryptographyClient initializeCryptoClient(JsonWebKey jsonWebKey,
                                                             CryptographyClientImpl implClient) {
        if (!KeyType.values().contains(jsonWebKey.getKeyType())) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(String.format(
                "The JSON Web Key type: %s is not supported.", jsonWebKey.getKeyType().toString())));
        } else {
            try {
                if (jsonWebKey.getKeyType().equals(RSA) || jsonWebKey.getKeyType().equals(RSA_HSM)) {
                    return new RsaKeyCryptographyClient(jsonWebKey, implClient);
                } else if (jsonWebKey.getKeyType().equals(EC) || jsonWebKey.getKeyType().equals(EC_HSM)) {
                    return new EcKeyCryptographyClient(jsonWebKey, implClient);
                } else if (jsonWebKey.getKeyType().equals(OCT) || jsonWebKey.getKeyType().equals(OCT_HSM)) {
                    return new AesKeyCryptographyClient(jsonWebKey, implClient);
                }
            } catch (RuntimeException e) {
                throw LOGGER.logExceptionAsError(
                    new RuntimeException("Could not initialize local cryptography client.", e));
            }

            // Should not reach here.
            return null;
        }
    }

    static boolean checkKeyPermissions(List<KeyOperation> operations, KeyOperation keyOperation) {
        return operations.contains(keyOperation);
    }
}

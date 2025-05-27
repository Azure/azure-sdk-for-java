// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.keys.cryptography.implementation;

import com.azure.v2.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.azure.v2.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.azure.v2.security.keyvault.keys.cryptography.models.SignatureAlgorithm;
import com.azure.v2.security.keyvault.keys.implementation.models.JsonWebKeyEncryptionAlgorithm;
import com.azure.v2.security.keyvault.keys.implementation.models.JsonWebKeySignatureAlgorithm;
import com.azure.v2.security.keyvault.keys.implementation.models.SecretBundle;
import com.azure.v2.security.keyvault.keys.models.JsonWebKey;
import com.azure.v2.security.keyvault.keys.models.KeyOperation;
import com.azure.v2.security.keyvault.keys.models.KeyType;
import io.clientcore.core.http.models.HttpResponseException;
import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.instrumentation.logging.ClientLogger;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import static com.azure.v2.security.keyvault.keys.models.KeyType.EC;
import static com.azure.v2.security.keyvault.keys.models.KeyType.EC_HSM;
import static com.azure.v2.security.keyvault.keys.models.KeyType.OCT;
import static com.azure.v2.security.keyvault.keys.models.KeyType.OCT_HSM;
import static com.azure.v2.security.keyvault.keys.models.KeyType.RSA;
import static com.azure.v2.security.keyvault.keys.models.KeyType.RSA_HSM;
import static io.clientcore.core.utils.CoreUtils.isNullOrEmpty;

/**
 * Utility methods for the Cryptography portion of Key Vault Keys.
 */
public final class CryptographyUtils {
    private CryptographyUtils() {
    }

    /**
     * The name of the collection for secrets.
     */
    public static final String SECRETS_COLLECTION = "secrets";

    /**
     * Extracts the components of a key identifier and validates them.
     *
     * @param keyId The key identifier to unpack.
     * @param logger The logger to use for logging errors.
     * @return A list containing the vault URL, key collection, key name, and key version.
     */
    public static List<String> unpackAndValidateId(String keyId, ClientLogger logger) {
        if (isNullOrEmpty(keyId)) {
            throw logger.throwableAtError().log("'keyId' cannot be null or empty.", IllegalArgumentException::new);
        }

        URI uri;
        try {
            uri = new URI(keyId);
        } catch (URISyntaxException e) {
            throw logger.throwableAtError().log("The key identifier is malformed.", e, IllegalArgumentException::new);
        }

        String[] tokens = uri.getPath().split("/");
        String vaultUrl = uri.getScheme() + "://" + uri.getHost();

        if (uri.getPort() != -1) {
            vaultUrl += ":" + uri.getPort();
        }

        String keyCollection = (tokens.length >= 2 ? tokens[1] : null);
        String keyName = (tokens.length >= 3 ? tokens[2] : null);
        String keyVersion = (tokens.length >= 4 ? tokens[3] : null);

        if (isNullOrEmpty(vaultUrl)) {
            throw logger.throwableAtError()
                .log("Key endpoint in key identifier is invalid.", IllegalArgumentException::new);
        } else if (isNullOrEmpty(keyName)) {
            throw logger.throwableAtError()
                .log("Key name in key identifier is invalid.", IllegalArgumentException::new);
        }

        return Arrays.asList(vaultUrl, keyCollection, keyName, keyVersion);
    }

    /**
     * Creates a local cryptography client using the provided key identifier and cryptography client implementation.
     *
     * @param implClient The cryptography client implementation.
     * @param logger The logger to use for logging errors.
     * @return A local cryptography client.
     *
     * @throws IllegalStateException If either of the key identifier or JSON Web Key is invalid or if the latter cannot
     * be retrieved.
     */
    public static LocalKeyCryptographyClient retrieveJwkAndCreateLocalClient(CryptographyClientImpl implClient,
        ClientLogger logger) {
        // Technically the collection portion of a key identifier should never be null/empty, but we still check for it.
        if (!isNullOrEmpty(implClient.getKeyCollection())) {
            // Get the JWK from the service and validate it. Then attempt to create a local cryptography client or
            // default to using service-side cryptography.
            JsonWebKey jsonWebKey = CryptographyUtils.SECRETS_COLLECTION.equals(implClient.getKeyCollection())
                ? implClient.getSecretKey()
                : implClient.getKeyWithResponse(RequestContext.none()).getValue().getKey();

            if (jsonWebKey == null) {
                throw logger.throwableAtError()
                    .log("Could not retrieve JSON Web Key to perform local cryptographic operations.",
                        IllegalStateException::new);
            } else if (!jsonWebKey.isValid()) {
                throw logger.throwableAtError()
                    .log("The retrieved JSON Web Key is not valid.", IllegalStateException::new);
            } else {
                return createLocalClient(jsonWebKey, implClient, logger);
            }
        } else {
            // Couldn't/didn't create a local cryptography client.
            throw logger.throwableAtError()
                .log("Could not create a local cryptography client, key collection is invalid",
                    IllegalStateException::new);
        }
    }

    /**
     * Creates a local cryptography client using the provided JSON Web Key and cryptography client implementation.
     *
     * @param jsonWebKey The JSON Web Key to use for local cryptographic operations.
     * @param implClient The cryptography client implementation.
     * @param logger The logger to use for logging errors.
     * @return A local cryptography client.
     *
     * @throws IllegalArgumentException If the JSON Web Key type is not supported.
     * @throws IllegalStateException If the local cryptography client cannot be created.
     */
    public static LocalKeyCryptographyClient createLocalClient(JsonWebKey jsonWebKey, CryptographyClientImpl implClient,
        ClientLogger logger) {

        if (jsonWebKey.getKeyType().equals(RSA) || jsonWebKey.getKeyType().equals(RSA_HSM)) {
            return new RsaKeyCryptographyClient(jsonWebKey, implClient);
        } else if (jsonWebKey.getKeyType().equals(EC) || jsonWebKey.getKeyType().equals(EC_HSM)) {
            return new EcKeyCryptographyClient(jsonWebKey, implClient);
        } else if (jsonWebKey.getKeyType().equals(OCT) || jsonWebKey.getKeyType().equals(OCT_HSM)) {
            return new AesKeyCryptographyClient(jsonWebKey, implClient);
        }

        // Should never reach this point.
        throw logger.throwableAtError()
            .addKeyValue("keyType", jsonWebKey.getKeyType().getValue())
            .log("The JSON Web Key type is not supported.", IllegalArgumentException::new);
    }

    /**
     * Verifies that the key operations are supported by the key.
     *
     * @param jsonWebKey The JSON Web Key to verify.
     * @param keyOperation The key operation to verify.
     * @param logger The logger to use for logging errors.
     *
     * @throws UnsupportedOperationException If the key operation is not supported by the key.
     */
    public static void verifyKeyPermissions(JsonWebKey jsonWebKey, KeyOperation keyOperation, ClientLogger logger) {
        if (!jsonWebKey.getKeyOps().contains(keyOperation)) {
            String keyOperationName = keyOperation == null ? null : keyOperation.toString().toLowerCase(Locale.ROOT);

            throw logger.throwableAtError()
                .addKeyValue("keyOperationName", keyOperationName)
                .addKeyValue("keyId", jsonWebKey.getId())
                .log("The operation is not allowed for key", UnsupportedOperationException::new);
        }
    }

    /**
     * Determines whether the exception is a retryable error.
     *
     * @param e The exception to check.
     * @return True if the exception is a retryable error, false otherwise.
     */
    public static boolean isThrowableRetryable(Throwable e) {
        if (e instanceof HttpResponseException) {
            int statusCode = ((HttpResponseException) e).getResponse().getStatusCode();

            // Not a retryable error code.
            return statusCode != 501
                && statusCode != 505
                && (statusCode >= 500 || statusCode == 408 || statusCode == 429);
        } else {
            // Not a service-related transient error.
            return false;
        }
    }

    /**
     * Determines whether the key is valid and of required size.
     *
     * @param key The key to be checked.
     * @param keySizeInBytes The minimum size required for the key.
     */
    static void validate(byte[] key, int keySizeInBytes, ClientLogger logger) {
        Objects.requireNonNull(key, "'key' cannot be null.");

        if (key.length < keySizeInBytes) {
            throw logger.throwableAtError()
                .addKeyValue("expectedLengthInBytes", keySizeInBytes)
                .addKeyValue("keyLength", key.length)
                .log("Key is too short", IllegalArgumentException::new);
        }
    }

    static JsonWebKey transformSecretBundle(SecretBundle secretKey) {
        return new JsonWebKey().setId(secretKey.getId())
            .setK(Base64.getUrlDecoder().decode(secretKey.getValue()))
            .setKeyType(KeyType.OCT)
            .setKeyOps(Arrays.asList(KeyOperation.WRAP_KEY, KeyOperation.UNWRAP_KEY, KeyOperation.ENCRYPT,
                KeyOperation.DECRYPT));
    }

    static JsonWebKeyEncryptionAlgorithm mapKeyEncryptionAlgorithm(EncryptionAlgorithm algorithm) {
        return JsonWebKeyEncryptionAlgorithm.fromValue(Objects.toString(algorithm, null));
    }

    static JsonWebKeySignatureAlgorithm mapKeySignatureAlgorithm(SignatureAlgorithm algorithm) {
        return JsonWebKeySignatureAlgorithm.fromValue(Objects.toString(algorithm, null));
    }

    static JsonWebKeyEncryptionAlgorithm mapWrapAlgorithm(KeyWrapAlgorithm algorithm) {
        return JsonWebKeyEncryptionAlgorithm.fromValue(Objects.toString(algorithm, null));
    }
}

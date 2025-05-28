// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.keys.implementation;

import com.azure.v2.security.keyvault.keys.KeyServiceVersion;
import com.azure.v2.security.keyvault.keys.cryptography.CryptographyClientBuilder;
import com.azure.v2.security.keyvault.keys.cryptography.CryptographyServiceVersion;
import io.clientcore.core.http.models.HttpResponseException;
import io.clientcore.core.http.pipeline.HttpPipeline;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.serialization.json.JsonReader;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import static io.clientcore.core.utils.CoreUtils.isNullOrEmpty;

/**
 * Utility class for Key Vault Keys.
 */
public final class KeyVaultKeysUtils {
    private static final ClientLogger LOGGER = new ClientLogger(KeyVaultKeysUtils.class);

    /**
     * Creates a {@link CryptographyClientBuilder} based on the values passed from a Keys service client.
     *
     * @param keyName The name of the key.
     * @param keyVersion The version of the key.
     * @param endpoint The endpoint of the key vault or managed HSM.
     * @param httpPipeline The HTTP pipeline to use.
     * @param serviceVersion The version of the service.
     * @return A new {@link CryptographyClientBuilder} with the values passed from a Keys service client.
     *
     * @throws IllegalArgumentException If {@code keyName} is {@code null} or an empty string.
     */
    public static CryptographyClientBuilder getCryptographyClientBuilder(String keyName, String keyVersion,
        String endpoint, HttpPipeline httpPipeline, KeyServiceVersion serviceVersion) {

        CryptographyClientBuilder builder
            = new CryptographyClientBuilder().keyIdentifier(generateKeyId(keyName, keyVersion, endpoint))
                .serviceVersion(CryptographyServiceVersion.valueOf(serviceVersion.getVersion()));

        httpPipeline.getPolicies().forEach(builder::addHttpPipelinePolicy);

        return builder;
    }

    /**
     * Generates a KeyVault Key ID from the name and version of the key and the KeyVault URL.
     *
     * @param keyName The name of the key.
     * @param keyVersion The version of the key.
     * @param vaultUrl The URL of the KeyVault.
     * @return The KeyVault Key ID.
     */
    private static String generateKeyId(String keyName, String keyVersion, String vaultUrl) {
        StringBuilder stringBuilder = new StringBuilder(vaultUrl);

        if (!vaultUrl.endsWith("/")) {
            stringBuilder.append("/");
        }

        stringBuilder.append("keys/").append(keyName);

        if (!isNullOrEmpty(keyVersion)) {
            stringBuilder.append("/").append(keyVersion);
        }

        return stringBuilder.toString();
    }

    /**
     * Calls a supplier and maps any {@link HttpResponseException} to an {@link HttpResponseException}.
     *
     * @param <T> The type of the result of the supplier.
     * @param call The supplier to call.
     * @param exceptionMapper The function to map a {@link HttpResponseException} to an {@link HttpResponseException}.
     * @return The result of the supplier.
     */
    public static <T> T callWithMappedException(Supplier<T> call,
        Function<HttpResponseException, HttpResponseException> exceptionMapper) {
        try {
            return call.get();
        } catch (HttpResponseException e) {
            throw exceptionMapper.apply(e);
        }
    }

    /**
     * Unpacks a Key Vault key ID into a name and version.
     *
     * @param id The Key Vault key ID to unpack.
     * @param nameConsumer The consumer to accept the name.
     * @param versionConsumer The consumer to accept the version.
     */
    public static void unpackId(String id, Consumer<String> nameConsumer, Consumer<String> versionConsumer) {
        if (isNullOrEmpty(id)) {
            return;
        }

        try {
            URI uri = new URI(id);
            String[] tokens = uri.getPath().split("/");

            if (tokens.length >= 3) {
                nameConsumer.accept(tokens[2]);
            }

            if (tokens.length >= 4) {
                versionConsumer.accept(tokens[3]);
            }
        } catch (URISyntaxException e) {
            // Should never come here.
            LOGGER.atError().setThrowable(e).log("Received malformed key id from service");
        }
    }

    /**
     * Converts epoch time to OffsetDateTime.
     *
     * @param epochReader The JsonReader containing the epoch time.
     * @return The OffsetDateTime.
     * @throws IOException If an error occurs while reading the epoch time.
     */
    public static OffsetDateTime epochToOffsetDateTime(JsonReader epochReader) throws IOException {
        Instant instant = Instant.ofEpochMilli(epochReader.getLong() * 1000L);
        return OffsetDateTime.ofInstant(instant, ZoneOffset.UTC);
    }

    /**
     * Base64 URL encodes the binary value.
     *
     * <p>Returns null if the {@code value} is null, returns an empty string if the {@code value} is empty.</p>
     *
     * @param value The binary value to base64 URL encode.
     * @return The base64 URL encoded value.
     */
    public static String base64UrlJsonSerialization(byte[] value) {
        if (value == null) {
            return null;
        } else if (value.length == 0) {
            return "";
        } else {
            return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
        }
    }

    /**
     * Base64 URL decodes the string value.
     *
     * @param value The string value to base64 URL decode.
     * @return The base64 URL decoded value.
     */
    public static byte[] base64UrlJsonDeserialization(String value) {
        return value == null ? null : Base64.getUrlDecoder().decode(value);
    }
}

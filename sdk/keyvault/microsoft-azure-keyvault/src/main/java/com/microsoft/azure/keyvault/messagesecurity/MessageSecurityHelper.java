// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault.messagesecurity;

import com.microsoft.azure.keyvault.webkey.JsonWebKey;
import com.microsoft.azure.keyvault.webkey.JsonWebKeyOperation;

import org.apache.commons.codec.binary.Base64;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

import java.security.KeyPair;
import java.security.KeyPairGenerator;

/**
 * Implements helper methods for message security.
 */
class MessageSecurityHelper {
    /**
     * Encoding for JWS and JWE header and contents specified in:
     * https://tools.ietf.org/html/rfc7515
     * https://tools.ietf.org/html/rfc7516
     */
    private static final Charset MESSAGE_ENCODING = StandardCharsets.UTF_8;

    /**
     * Convert base64Url string to bytes array.
     * 
     * @param base64url
     *            Base64Url string.
     * 
     * @returns Decoded bytes array.
     */
    public static byte[] base64UrltoByteArray(String base64url) {
        return Base64.decodeBase64(base64url.replace('-', '+').replace('_', '/'));
    }

    /**
     * Convert base64Url string to String.
     * 
     * @param base64url
     *            Base64Url string.
     * 
     * @returns Decoded string.
     */
    public static String base64UrltoString(String base64url) {
        return new String(base64UrltoByteArray(base64url), MESSAGE_ENCODING);
    }

    /**
     * Convert bytes array to Base64Url string.
     * 
     * @param bytes
     *            bytes array
     * 
     * @returns Encoded string.
     */
    public static String bytesToBase64Url(byte[] bytes) {
        String result = (new String(Base64.encodeBase64(bytes), MESSAGE_ENCODING)).replace("=", "").replace("\\", "").replace('+', '-')
                .replace('/', '_');
        return result;
    }

    /**
     * Convert bytes array to Base64Url string.
     * 
     * @param str
     *            string.
     * 
     * @returns Encoded string.
     */
    public static String stringToBase64Url(String str) {
        return bytesToBase64Url(str.getBytes(MESSAGE_ENCODING));
    }

    /**
     * Convert serialized JsonWebKey string to JsonWebKey object.
     * 
     * @param jwkString
     *            serialized JsonWebKey.
     * 
     * @returns JsonWebKey object.
     */
    public static JsonWebKey jsonWebKeyFromString(String jwkString) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(jwkString, JsonWebKey.class);
    }

    /**
     * Generates new JsonWebKey with random KeyID.
     * 
     * @returns JsonWebKey object.
     */
    public static JsonWebKey generateJsonWebKey() {
        try {
            final KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            KeyPair clientRsaKeyPair = generator.generateKeyPair();
            JsonWebKey result = JsonWebKey.fromRSA(clientRsaKeyPair);
            result.withKid(UUID.randomUUID().toString());
            return result;
        } catch (NoSuchAlgorithmException e) {
            // Unexpected. Should never be thrown.
            throw new RuntimeException(e);
        }
    }

    /**
     * Converts JsonWebKey with private key to JsonWebKey with public key only.
     * 
     * @param jwk
     *            JsonWebKey with private key.
     * @returns JsonWebKey object with public key only.
     */
    public static JsonWebKey getJwkWithPublicKeyOnly(JsonWebKey jwk) {
        KeyPair publicOnly = jwk.toRSA(false);
        JsonWebKey jsonkeyPublic = JsonWebKey.fromRSA(publicOnly);
        jsonkeyPublic.withKid(jwk.kid());
        jsonkeyPublic.withKeyOps(
                Arrays.asList(JsonWebKeyOperation.ENCRYPT, JsonWebKeyOperation.WRAP_KEY, JsonWebKeyOperation.VERIFY));
        return jsonkeyPublic;
    }

}

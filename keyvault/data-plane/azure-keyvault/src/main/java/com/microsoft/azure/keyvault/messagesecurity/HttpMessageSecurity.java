// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault.messagesecurity;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import com.microsoft.azure.keyvault.cryptography.RsaKey;
import com.microsoft.azure.keyvault.cryptography.SymmetricKey;
import com.microsoft.azure.keyvault.webkey.JsonWebKey;

import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.internal.http.HttpHeaders;
import okio.Buffer;

/**
 * Implements message security protocol. Encrypts requests &amp; decrypts
 * responses.
 */
public class HttpMessageSecurity {
    private static final String AUTHENTICATE = "Authorization";
    private static final String BEARER_TOKEP_REFIX = "Bearer ";
    /**
     * Encoding for JWS and JWE header and contents specified in:
     * https://tools.ietf.org/html/rfc7515
     * https://tools.ietf.org/html/rfc7516
     */
    private static final Charset MESSAGE_ENCODING = StandardCharsets.UTF_8;
    private boolean testMode = false;
    private String clientSecurityToken;
    private JsonWebKey clientSignatureKey;
    private JsonWebKey clientEncryptionKey;
    private JsonWebKey serverSignatureKey;
    private JsonWebKey serverEncryptionKey;

    /**
     * Constructor.
     *
     * @param clientSecurityToken
     *            pop or bearer authentication token.
     * @param clientSignatureKeyString
     *            string with client signing key (public + private parts) or null if
     *            not supported
     * @param serverEncryptionKeyString
     *            string with server encryption key (public only) or null if not
     *            supported
     * @param serverSignatureKeyString
     *            string with server signing key (public only) or null if not
     *            supported
     * @throws IOException
     *             throws IOException
     */
    public HttpMessageSecurity(String clientSecurityToken, String clientSignatureKeyString,
            String serverEncryptionKeyString, String serverSignatureKeyString) throws IOException {
        
        this(clientSecurityToken, clientSignatureKeyString, serverEncryptionKeyString, serverSignatureKeyString, MessageSecurityHelper.generateJsonWebKey());
    }


    /**
     * Constructor.
     *
     * @param clientSecurityToken
     *            pop or bearer authentication token.
     * @param clientSignatureKeyString
     *            string with client signing key (public + private parts) or null if
     *            not supported
     * @param serverEncryptionKeyString
     *            string with server encryption key (public only) or null if not
     *            supported
     * @param serverSignatureKeyString
     *            string with server signing key (public only) or null if not
     *            supported
     * @param clientEncryptionKey
     *            client encryption key (public + private parts) or null if
     *            not supported
     * @throws IOException
     *             throws IOException
     */
    public HttpMessageSecurity(String clientSecurityToken, String clientSignatureKeyString,
            String serverEncryptionKeyString, String serverSignatureKeyString, JsonWebKey clientEncryptionKey) throws IOException {
        
        this.clientSecurityToken = clientSecurityToken;

        if (clientSignatureKeyString != null && !clientSignatureKeyString.equals("")) {
            this.clientSignatureKey = MessageSecurityHelper.jsonWebKeyFromString(clientSignatureKeyString);
        }
        if (serverSignatureKeyString != null && !serverSignatureKeyString.equals("")) {
            this.serverSignatureKey = MessageSecurityHelper.jsonWebKeyFromString(serverSignatureKeyString);
        }
        if (serverEncryptionKeyString != null && !serverEncryptionKeyString.equals("")) {
            this.serverEncryptionKey = MessageSecurityHelper.jsonWebKeyFromString(serverEncryptionKeyString);
        }

        this.clientEncryptionKey = clientEncryptionKey;
    }

    /**
     * Constructor (tests only).
     *
     * @param clientSecurityToken
     *            pop or bearer authentication token.
     * @param clientEncryptionString
     *            string with client signing key (public + private parts) or null if
     *            not supported
     * @param clientSignatureKeyString
     *            string with client signing key (public + private parts) or null if
     *            not supported
     * @param serverEncryptionKeyString
     *            string with server encryption key (public only) or null if not
     *            supported
     * @param serverSignatureKeyString
     *            string with server signing key (public only) or null if not
     *            supported
     * @param testMode
     *            true for test mode (uses 0 for timestamp)
     * @throws IOException
     *             throws IOException
     */
    public HttpMessageSecurity(String clientSecurityToken, String clientEncryptionString,
            String clientSignatureKeyString, String serverEncryptionKeyString, String serverSignatureKeyString,
            boolean testMode) throws IOException {
        this(clientSecurityToken, clientSignatureKeyString, serverEncryptionKeyString, serverSignatureKeyString);
        this.testMode = testMode;
        if (clientEncryptionString != null && !clientEncryptionString.equals("")) {
            this.clientEncryptionKey = MessageSecurityHelper.jsonWebKeyFromString(clientEncryptionString);
        }
    }

    /**
     * Protects existing request. Replaces its body with encrypted version.
     *
     * @param request
     *            existing request.
     *
     * @return new request with encrypted body if supported or existing request.
     * 
     * @throws IOException throws IOException
     */
    public Request protectRequest(Request request) throws IOException {
        try {
            Request result = request.newBuilder().header(AUTHENTICATE, BEARER_TOKEP_REFIX + clientSecurityToken)
                    .build();

            if (!supportsProtection()) {
                return result;
            }

            Buffer buffer = new Buffer();
            request.body().writeTo(buffer);
            String currentbody = buffer.readUtf8();

            if (currentbody == null || currentbody.length() == 0) {
                return result;
            }

            JsonWebKey clientPublicEncryptionKey = MessageSecurityHelper.getJwkWithPublicKeyOnly(clientEncryptionKey);

            String payload = currentbody.substring(0, currentbody.length() - 1) + ",\"rek\":{\"jwk\":"
                    + clientPublicEncryptionKey.toString() + "}}";

            JWEObject jweObject = protectPayload(payload);

            JWSHeader jwsHeader = new JWSHeader("RS256", clientSignatureKey.kid(), clientSecurityToken,
                    getCurrentTimestamp(), "PoP", null);

            String jwsHeaderJsonb64 = MessageSecurityHelper.stringToBase64Url(jwsHeader.serialize());
            String protectedPayload = MessageSecurityHelper.stringToBase64Url(jweObject.serialize());
            byte[] data = (jwsHeaderJsonb64 + "." + protectedPayload).getBytes(MESSAGE_ENCODING);

            RsaKey clientSignatureRsaKey = new RsaKey(clientSignatureKey.kid(), clientSignatureKey.toRSA(true));
            Pair<byte[], String> signature = clientSignatureRsaKey.signAsync(getSha256(data), "RS256").get();

            JWSObject jwsObject = new JWSObject(jwsHeader, protectedPayload,
                    MessageSecurityHelper.bytesToBase64Url(signature.getKey()));

            RequestBody body = RequestBody.create(MediaType.parse("application/jose+json"), jwsObject.serialize());

            return result.newBuilder().method(request.method(), body).build();
        } catch (ExecutionException e) {
            // unexpected;
            return null;
        } catch (InterruptedException e) {
            // unexpected;
            return null;
        } catch (NoSuchAlgorithmException e) {
            // unexpected;
            return null;
        }
    }

    /**
     * Unprotects response if needed. Replaces its body with unencrypted version.
     *
     * @param response
     *            server response.
     *
     * @return new response with unencrypted body if supported or existing response.
     * 
     * @throws IOException throws IOException
     */
    public Response unprotectResponse(Response response) throws IOException {
        try {
            if (!supportsProtection() || !HttpHeaders.hasBody(response)) {
                return response;
            }

            if (!response.header("content-type").toLowerCase().contains("application/jose+json")) {
                return response;
            }

            JWSObject jwsObject = JWSObject.deserialize(response.body().string());
            JWSHeader jwsHeader = jwsObject.jwsHeader();

            if (!jwsHeader.kid().equals(serverSignatureKey.kid()) || !jwsHeader.alg().equals("RS256")) {
                throw new IOException("Invalid protected response");
            }

            byte[] data = (jwsObject.originalProtected() + "." + jwsObject.payload()).getBytes(MESSAGE_ENCODING);
            byte[] signature = MessageSecurityHelper.base64UrltoByteArray(jwsObject.signature());

            RsaKey serverSignatureRsaKey = new RsaKey(serverSignatureKey.kid(), serverSignatureKey.toRSA(false));
            boolean signed = serverSignatureRsaKey.verifyAsync(getSha256(data), signature, "RS256").get();
            if (!signed) {
                throw new IOException("Wrong signature.");
            }

            String decrypted = unprotectPayload(jwsObject.payload());

            MediaType contentType = response.body().contentType();
            ResponseBody body = ResponseBody.create(contentType, decrypted);
            return response.newBuilder().body(body).build();
        } catch (ExecutionException e) {
            // unexpected;
            return null;
        } catch (InterruptedException e) {
            // unexpected;
            return null;
        } catch (NoSuchAlgorithmException e) {
            // unexpected;
            return null;
        }
    }

    /**
     * Return current timestamp. Returns always 0 for tests.
     *
     * @return current timestamp or 0 for test mode.
     */
    private long getCurrentTimestamp() {
        if (testMode) {
            return 0;
        } else {
            return System.currentTimeMillis() / 1000;
        }
    }

    /**
     * Check if HttmMessageSecurity has all required keys.
     *
     * @return true if there is client signature key and two server keys.
     */
    private boolean supportsProtection() {
        return this.clientSignatureKey != null && this.serverSignatureKey != null && this.serverEncryptionKey != null;
    }

    /**
     * Encrypt provided payload and return proper JWEObject.
     *
     * @param payload Content to be encrypted. Content will be encrypted with
     *                UTF-8 representation of contents as per
     *                https://tools.ietf.org/html/rfc7515. It can
     *                represent anything.
     *
     * @return JWEObject with encrypted payload.
     * 
     * @throws IOException throws IOException
     */
    private JWEObject protectPayload(String payload) throws IOException {
        try {
            JWEHeader jweHeader = new JWEHeader("RSA-OAEP", serverEncryptionKey.kid(), "A128CBC-HS256");

            byte[] aesKeyBytes = generateAesKey();

            SymmetricKey aesKey = new SymmetricKey(UUID.randomUUID().toString(), aesKeyBytes);

            byte[] iv = generateAesIv();

            RsaKey serverEncryptionRsaKey = new RsaKey(serverEncryptionKey.kid(), serverEncryptionKey.toRSA(false));
            Triple<byte[], byte[], String> encryptedKey = serverEncryptionRsaKey
                    .encryptAsync(aesKeyBytes, null, null, "RSA-OAEP").get();

            Triple<byte[], byte[], String> cipher = aesKey
                    .encryptAsync(payload.getBytes(MESSAGE_ENCODING), iv,
                            MessageSecurityHelper.stringToBase64Url(jweHeader.serialize()).getBytes(MESSAGE_ENCODING), "A128CBC-HS256")
                    .get();

            JWEObject jweObject = new JWEObject(jweHeader,
                    MessageSecurityHelper.bytesToBase64Url((!testMode) ? encryptedKey.getLeft() : "key".getBytes(MESSAGE_ENCODING)),
                    MessageSecurityHelper.bytesToBase64Url(iv),
                    MessageSecurityHelper.bytesToBase64Url(cipher.getLeft()),
                    MessageSecurityHelper.bytesToBase64Url(cipher.getMiddle()));

            return jweObject;
        } catch (ExecutionException e) {
            // unexpected;
            return null;
        } catch (InterruptedException e) {
            // unexpected;
            return null;
        } catch (NoSuchAlgorithmException e) {
            // unexpected;
            return null;
        }
    }

    /**
     * Unencrypt encrypted payload.
     *
     * @param payload
     *            base64url serialized JWEObject.
     *
     * @return Unencrypted message.
     */
    private String unprotectPayload(String payload) throws IOException {
        try {
            JWEObject jweObject = JWEObject.deserialize(MessageSecurityHelper.base64UrltoString(payload));
            JWEHeader jweHeader = jweObject.jweHeader();

            if (!clientEncryptionKey.kid().equals(jweHeader.kid()) || !jweHeader.alg().equals("RSA-OAEP")
                    || !jweHeader.enc().equals("A128CBC-HS256")) {
                throw new IOException("Invalid protected response");
            }

            byte[] key = MessageSecurityHelper.base64UrltoByteArray(jweObject.encryptedKey());

            RsaKey clientEncryptionRsaKey = new RsaKey(clientEncryptionKey.kid(), clientEncryptionKey.toRSA(true));
            byte[] aesKeyBytes = clientEncryptionRsaKey.decryptAsync(key, null, null, null, "RSA-OAEP").get();

            SymmetricKey aesKey = new SymmetricKey(UUID.randomUUID().toString(), aesKeyBytes);
            byte[] result = aesKey.decryptAsync(MessageSecurityHelper.base64UrltoByteArray(jweObject.cipherText()),
                    MessageSecurityHelper.base64UrltoByteArray(jweObject.iv()),
                    jweObject.originalProtected().getBytes(MESSAGE_ENCODING),
                    MessageSecurityHelper.base64UrltoByteArray(jweObject.tag()), "A128CBC-HS256").get();

            return new String(result, MESSAGE_ENCODING);
        } catch (ExecutionException e) {
            // unexpected;
            return null;
        } catch (InterruptedException e) {
            // unexpected;
            return null;
        } catch (NoSuchAlgorithmException e) {
            // unexpected;
            return null;
        }
    }

    /**
     * Get SHA256 hash for byte array.
     *
     * @param data
     *            byte array.
     *
     * @return byte array with sha256 hash.
     */
    private byte[] getSha256(byte[] data) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        return digest.digest(data);
    }

    /**
     * Generates AES key.
     *
     * @return Random AES key or pre-defined value for test mode.
     */
    private byte[] generateAesKey() {
        byte[] bytes = new byte[32];
        if (!testMode) {
            SecureRandom random = new SecureRandom();
            random.nextBytes(bytes);
        } else {
            bytes = "TEST1234TEST1234TEST1234TEST1234".getBytes(MESSAGE_ENCODING);
        }
        return bytes;
    }

    /**
     * Generates initialization vector for AES encryption.
     *
     * @return Random IV or pre-defined value for test mode.
     */
    private byte[] generateAesIv() {
        byte[] bytes = new byte[16];
        if (!testMode) {
            SecureRandom random = new SecureRandom();
            random.nextBytes(bytes);
        } else {
            bytes = "TEST1234TEST1234".getBytes(MESSAGE_ENCODING);
        }
        return bytes;
    }
}

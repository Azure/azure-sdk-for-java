/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

 package com.microsoft.azure.keyvault.messagesecurity;

import com.microsoft.azure.keyvault.cryptography.*;

import com.microsoft.azure.keyvault.webkey.JsonWebKey;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import okio.Buffer;

/**
 * Implements message security protocol.
 * Encrypts requests & decrypts responses.
 */
public class HttpMessageSecurity {
    private static final String AUTHENTICATE = "Authorization";
    private static final String BEARER_TOKEP_REFIX = "Bearer ";

    private String clientSecurityToken;
    private JsonWebKey clientSignatureKey;
    private JsonWebKey clientEncryptionKey;
    private JsonWebKey serverSignatureKey;
    private JsonWebKey serverEncryptionKey;

    /**
     * Constructor
     * 
     * @param _clientSecurityToken
     *      pop or bearer authentication token.
     * @param _clientSignatureKeyString
     *      string with client signing key (public + private parts) or null if not supported
     * @param _serverEncryptionKeyString
     *      string with server encryption key (public only) or null if not supported
     * @param _serverSignatureKeyString
     *      string with server signing key (public only) or null if not supported
     */
    public HttpMessageSecurity(String _clientSecurityToken, String _clientSignatureKeyString, String _serverEncryptionKeyString, String _serverSignatureKeyString) throws IOException{
        this.clientSecurityToken = _clientSecurityToken;

        if (_clientSignatureKeyString != null && !_clientSignatureKeyString.equals("")){
            this.clientSignatureKey = MessageSecurityHelper.JsonWebKeyFromString(_clientSignatureKeyString);
        }
        if (_serverSignatureKeyString != null && !_serverSignatureKeyString.equals("")){
            this.serverSignatureKey = MessageSecurityHelper.JsonWebKeyFromString(_serverSignatureKeyString);
        }
        if (_serverEncryptionKeyString != null && !_serverEncryptionKeyString.equals("")){
            this.serverEncryptionKey = MessageSecurityHelper.JsonWebKeyFromString(_serverEncryptionKeyString);
        }

        this.clientEncryptionKey = MessageSecurityHelper.GenerateJsonWebKey();
    }

    /**
     * Protects existing request. Replaces its body with encrypted version.
     * 
     * @param request
     *      existing request.
     * 
     * @return
     *      new request with encrypted body if supported or existing request.
     */
    public Request protectRequest(Request request) throws IOException {
        try{
            Request result = request.newBuilder().header(AUTHENTICATE, BEARER_TOKEP_REFIX + clientSecurityToken).build();

            if(!supportsProtection()) {
                return result;
            }

            Buffer buffer = new Buffer();
            request.body().writeTo(buffer);
            String currentbody = buffer.readUtf8();

            JsonWebKey clientPublicEncryptionKey = MessageSecurityHelper.GetJwkWithPublicKeyOnly(clientEncryptionKey);

            String payload = currentbody.replace("}", ",\"rek\":{\"jwk\":" + clientPublicEncryptionKey.toString() + "}}");

            JWEObject jweObject = protectPayload(payload);

            JWSHeader jwsHeader = new JWSHeader("RS256",
                    clientEncryptionKey.kid(),
                    clientSecurityToken,
                    System.currentTimeMillis() / 1000,
                    "PoP",
                    null);

            String jwsHeaderJsonb64 = MessageSecurityHelper.stringToBase64Url(jwsHeader.serialize());
            String protectedPayload = MessageSecurityHelper.stringToBase64Url(jweObject.serialize());
            byte[] data = (jwsHeaderJsonb64 + "." + protectedPayload).getBytes();

            RsaKey clientSignatureRsaKey = new RsaKey(clientSignatureKey.kid(), clientSignatureKey.toRSA(true));
            Pair<byte[], String> signature = clientSignatureRsaKey.signAsync(data, "RS256").get();

            JWSObject jwsObject = new JWSObject(jwsHeader,
                    protectedPayload,
                    MessageSecurityHelper.bytesToBase64Url(signature.getKey()));

            RequestBody body = RequestBody.create(MediaType.parse("application/jose+json"), jwsObject.serialize());

            return result.newBuilder().post(body).build();
        } catch (ExecutionException e){
            // unexpected;
            return null;
        } catch (InterruptedException e){
            // unexpected;
            return null;
        } catch (NoSuchAlgorithmException e){
            // unexpected;
            return null;
        }
    }

    /**
     * Unprotects response if needed. Replaces its body with unencrypted version.
     * 
     * @param response
     *      server response.
     * 
     * @return
     *      new response with unencrypted body if supported or existing response.
     */
    public Response unprotectResponse(Response response) throws IOException{
        try{
            if (!supportsProtection())
                return response;

            JWSObject jwsObject = JWSObject.deserialize(response.body().string());
            JWSHeader jwsHeader = jwsObject.jwsHeader();

            if(!jwsHeader.kid().equals(serverSignatureKey.kid()) || !jwsHeader.alg().equals("RS256")){
                throw new IOException("Invalid protected response");
            }

            byte[] data = (jwsObject.original_protected() + "." + jwsObject.payload()).getBytes();
            byte[] signature = MessageSecurityHelper.base64UrltoByteArray(jwsObject.signature());

            RsaKey serverSignatureRsaKey = new RsaKey(serverSignatureKey.kid(), serverSignatureKey.toRSA(false));
            boolean signed = serverSignatureRsaKey.verifyAsync(data, signature, "RS256").get();
            if (!signed){
                throw new IOException("Wrong signature.");
            }

            String decrypted = unprotectPayload(jwsObject.payload());

            MediaType contentType = response.body().contentType();
            ResponseBody body = ResponseBody.create(contentType, decrypted);
            return response.newBuilder().body(body).build();
        } catch (ExecutionException e){
            // unexpected;
            return null;
        } catch (InterruptedException e){
            // unexpected;
            return null;
        } catch (NoSuchAlgorithmException e){
            // unexpected;
            return null;
        }
    }

    /**
     * Check if HttmMessageSecurity has all required keys.
     * 
     * @return
     *      true if there is client signature key and two server keys.
     */
    private boolean supportsProtection(){
        return this.clientSignatureKey != null &&
               this.serverSignatureKey != null &&
               this.serverEncryptionKey != null;
    }

    
    /**
     * Encrypt provided payload and return proper JWEObject.
     * 
     * @param payload
     *      string to be encrypted.
     * 
     * @return
     *      JWEObject with encrypted payload.
     */
    private JWEObject protectPayload(String payload) throws IOException{
        try{
            JWEHeader jweHeader = new JWEHeader("RSA-OAEP", serverEncryptionKey.kid(), "A128CBC-HS256");

            byte[] aesKeyBytes = new byte[32];
            new Random().nextBytes(aesKeyBytes);
            SymmetricKey aesKey = new SymmetricKey(UUID.randomUUID().toString(), aesKeyBytes);

            byte[] iv = new byte[16];
            new Random().nextBytes(iv);

            RsaKey serverEncryptionRsaKey = new RsaKey(serverEncryptionKey.kid(), serverEncryptionKey.toRSA(false));
            Triple<byte[], byte[], String> encrypted_key = serverEncryptionRsaKey.encryptAsync(aesKeyBytes, null, null, "RSA-OAEP").get();

            Triple<byte[], byte[], String> cipher = aesKey.encryptAsync(
                    payload.getBytes(),
                    iv,
                    MessageSecurityHelper.stringToBase64Url(jweHeader.serialize()).getBytes(),
                    "A128CBC-HS256").get();

            JWEObject jweObject = new JWEObject(jweHeader,
                    MessageSecurityHelper.bytesToBase64Url(encrypted_key.getLeft()),
                    MessageSecurityHelper.bytesToBase64Url(iv),
                    MessageSecurityHelper.bytesToBase64Url(cipher.getLeft()),
                    MessageSecurityHelper.bytesToBase64Url(cipher.getMiddle()));

            return jweObject;
        } catch (ExecutionException e){
            // unexpected;
            return null;
        } catch (InterruptedException e){
            // unexpected;
            return null;
        } catch (NoSuchAlgorithmException e){
            // unexpected;
            return null;
        }
    }

    /**
     * Unencrypt encrypted payload.
     * 
     * @param payload
     *      base64url serialized JWEObject.
     * 
     * @return
     *      Unencrypted message.
     */
    private String unprotectPayload(String payload) throws IOException {
        try {
            JWEObject jweObject = JWEObject.deserialize(MessageSecurityHelper.base64UrltoString(payload));
            JWEHeader jweHeader = jweObject.jweHeader();

            if (!clientEncryptionKey.kid().equals(jweHeader.kid()) ||
                    !jweHeader.alg().equals("RSA-OAEP") ||
                    !jweHeader.enc().equals("A128CBC-HS256")) {
                throw new IOException("Invalid protected response");
            }

            byte[] key = MessageSecurityHelper.base64UrltoByteArray(jweObject.encryptedKey());

            RsaKey clientEncryptionRsaKey = new RsaKey(clientEncryptionKey.kid(), clientEncryptionKey.toRSA(true));
            byte[] aesKeyBytes = clientEncryptionRsaKey.decryptAsync(key, null, null, null, "RSA-OAEP").get();

            SymmetricKey aesKey = new SymmetricKey(UUID.randomUUID().toString(), aesKeyBytes);
            byte[] result = aesKey.decryptAsync(MessageSecurityHelper.base64UrltoByteArray(jweObject.cipherText()),
                    MessageSecurityHelper.base64UrltoByteArray(jweObject.iv()),
                    jweObject.original_protected().getBytes(),
                    MessageSecurityHelper.base64UrltoByteArray(jweObject.tag()),
                    "A128CBC-HS256").get();

            return new String(result);
        } catch (ExecutionException e){
            // unexpected;
            return null;
        } catch (InterruptedException e){
            // unexpected;
            return null;
        } catch (NoSuchAlgorithmException e){
            // unexpected;
            return null;
        }

    }
}

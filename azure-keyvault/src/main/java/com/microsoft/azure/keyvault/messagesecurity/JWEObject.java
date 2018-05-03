/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.keyvault.messagesecurity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Json Web Encryption object class.
 */
class JWEObject {
    private JWEHeader jweHeader;
    private String original_protected;
    private String encryptedKey;
    private String iv;
    private String cipherText;
    private String tag;

    /**
     * Constructor.
     * 
     * @param jweHeader
     *      Corresponding jweHeader object.
     * @param encryptedKey
     *      base64url encrypted key.
     * @param iv
     *      base64url iv.
     * @param cipherText
     *      base64url encrypted data.
     * @param tag
     *      base64url authorization tag.
     */
    public JWEObject(JWEHeader jweHeader, String encryptedKey, String iv, String cipherText, String tag){
        this.jweHeader = jweHeader;
        this.encryptedKey = encryptedKey;
        this.iv = iv;
        this.cipherText = cipherText;
        this.tag = tag;
    }

    /**
     * Constructor.
     * 
     * @param jweHeaderB64
     *      base64url json with serialized jweHeader.
     * @param encryptedKey
     *      base64url encrypted key.
     * @param iv
     *      base64url iv.
     * @param cipherText
     *      base64url encrypted data.
     * @param tag
     *      base64url authorization tag.
     */
    @JsonCreator
    public JWEObject(@JsonProperty("protected") String jweHeaderB64,
                     @JsonProperty("encrypted_key") String encryptedKey,
                     @JsonProperty("iv") String iv,
                     @JsonProperty("ciphertext") String cipherText,
                     @JsonProperty("tag") String tag) throws Exception{
        this.jweHeader = JWEHeader.fromBase64String(jweHeaderB64);
        this.original_protected = jweHeaderB64;
        this.encryptedKey = encryptedKey;
        this.iv = iv;
        this.cipherText = cipherText;
        this.tag = tag;
    }

    /**
     * Compare two JweObject.
     * 
     * @return 
     *      true if JWEObject are identical.
     */
    public boolean equals(JWEObject other){
        return jweHeader.equals(other.jweHeader) &&
                encryptedKey.equals(other.encryptedKey) &&
                iv.equals(other.iv) &&
                cipherText.equals(other.cipherText) &&
                tag.equals(other.tag);
    }

    /**
     * Serialize JWEObject to json string.
     * 
     * @return 
     *      Json string with serialized JWEObject.
     */
    public String serialize() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }

    /**
     * Construct JWEObject from json string.
     * 
     * @param json
     *      json string.
     * 
     * @return 
     *      Constructed JWEObject
     */
    public static JWEObject deserialize(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json,JWEObject.class);
    }

    /**
     * JWEHeader object.
     */
    public JWEHeader jweHeader(){
        return jweHeader;
    }

    /**
     * base64url json with serialized jweHeader.
     */
    @JsonProperty("protected")
    public String protectedB64() throws Exception{
        return MessageSecurityHelper.stringToBase64Url(jweHeader.serialize());
    }

    /**
     * Original base64url with serialized jweHeader (when constructed from json string).
     */
    public String original_protected(){
        return original_protected;
    }

    /**
     * base64url encrypted key.
     */
    @JsonProperty("encrypted_key")
    public String encryptedKey() {
        return encryptedKey;
    }

    /**
     * base64url iv.
     */
    @JsonProperty("iv")
    public String iv() {
        return iv;
    }

    /**
     * base64url encrypted text.
     */
    @JsonProperty("ciphertext")
    public String cipherText() {
        return cipherText;
    }

    /**
     * base64url tag.
     */
    @JsonProperty("tag")
    public String tag() {
        return tag;
    }
}

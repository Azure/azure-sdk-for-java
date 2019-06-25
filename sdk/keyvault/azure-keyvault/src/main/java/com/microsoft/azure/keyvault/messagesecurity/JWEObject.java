// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.keyvault.messagesecurity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Objects;

/**
 * Json Web Encryption object class.
 */
class JWEObject {
    private JWEHeader jweHeader;
    private String originalProtected;
    private String encryptedKey;
    private String iv;
    private String cipherText;
    private String tag;

    /**
     * Constructor.
     * 
     * @param jweHeader
     *            Corresponding jweHeader object.
     * @param encryptedKey
     *            base64url encrypted key.
     * @param iv
     *            base64url iv.
     * @param cipherText
     *            base64url encrypted data.
     * @param tag
     *            base64url authorization tag.
     */
    JWEObject(JWEHeader jweHeader, String encryptedKey, String iv, String cipherText, String tag) {
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
     *            base64url json with serialized jweHeader.
     * @param encryptedKey
     *            base64url encrypted key.
     * @param iv
     *            base64url iv.
     * @param cipherText
     *            base64url encrypted data.
     * @param tag
     *            base64url authorization tag.
     */
    @JsonCreator
    JWEObject(@JsonProperty("protected") String jweHeaderB64, @JsonProperty("encrypted_key") String encryptedKey,
            @JsonProperty("iv") String iv, @JsonProperty("ciphertext") String cipherText,
            @JsonProperty("tag") String tag) throws Exception {
        this.jweHeader = JWEHeader.fromBase64String(jweHeaderB64);
        this.originalProtected = jweHeaderB64;
        this.encryptedKey = encryptedKey;
        this.iv = iv;
        this.cipherText = cipherText;
        this.tag = tag;
    }

    /**
     * Compare two JweObject.
     * 
     * @return true if JWEObject are identical.
     */
    public boolean equals(JWEObject other) {
        return jweHeader.equals(other.jweHeader) && encryptedKey.equals(other.encryptedKey) && iv.equals(other.iv)
                && cipherText.equals(other.cipherText) && tag.equals(other.tag);
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof JWEObject)) {
            return false;
        }

        return object == this || equals((JWEObject) object);
    }

    /**
     * Hash code for objects.
     * 
     * @return hashcode
     */
    public int hashCode() {
        return Objects.hash(this.jweHeader, this.encryptedKey, this.iv, this.cipherText, this.tag);
    }

    /**
     * Serialize JWEObject to json string.
     * 
     * @return Json string with serialized JWEObject.
     */
    public String serialize() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }

    /**
     * Construct JWEObject from json string.
     * 
     * @param json
     *            json string.
     * 
     * @return Constructed JWEObject
     */
    public static JWEObject deserialize(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json, JWEObject.class);
    }

    /**
     * JWEHeader object.
     */
    public JWEHeader jweHeader() {
        return jweHeader;
    }

    /**
     * base64url json with serialized jweHeader.
     */
    @JsonProperty("protected")
    public String protectedB64() throws Exception {
        return MessageSecurityHelper.stringToBase64Url(jweHeader.serialize());
    }

    /**
     * Original base64url with serialized jweHeader (when constructed from json
     * string).
     */
    public String originalProtected() {
        return originalProtected;
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

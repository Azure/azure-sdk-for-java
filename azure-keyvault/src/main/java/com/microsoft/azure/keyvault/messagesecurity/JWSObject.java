/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.keyvault.messagesecurity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Json Web Signature object class.
 */
class JWSObject {
    private JWSHeader jwsHeader;
    private String original_protected;
    private String payload;
    private String signature;

    /**
     * Constructor.
     * 
     * @param JWSHeader
     *      JWSHeader.
     * @param payload
     *      base64url protected payload (JWEObject).
     * @param signature
     *      base64url signature for (protected + "." + payload) data.
     */
    public JWSObject(JWSHeader jwsHeader, String payload, String signature){
        this.jwsHeader = jwsHeader;
        this.payload = payload;
        this.signature = signature;
    }

    /**
     * Constructor.
     * 
     * @param jwsHeaderB64
     *      base64 json string with JWSHeader.
     * @param payload
     *      base64url protected payload (JWEObject).
     * @param signature
     *      base64url signature for (protected + "." + payload) data.
     */
    @JsonCreator
    public JWSObject(
        @JsonProperty("protected") String jwsHeaderB64,
        @JsonProperty("payload") String payload,
        @JsonProperty("signature") String signature) throws Exception{
        this.jwsHeader = JWSHeader.fromBase64String(jwsHeaderB64);
        this.original_protected = jwsHeaderB64;
        this.payload = payload;
        this.signature = signature;
    }

    /**
     * Compare two JWSObject.
     * 
     * @return 
     *      true if JWSObjects are identical.
     */
    public boolean equals(JWSObject other){
        return this.payload.equals(other.payload) &&
                this.jwsHeader.equals(other.jwsHeader) &&
                this.signature.equals(other.signature);
    }

    /**
     * Serialize JWSObject to json string.
     * 
     * @return 
     *      Json string with serialized JWSObject.
     */
    public String serialize() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }

    /**
     * Construct JWSObject from json string.
     * 
     * @param json
     *      json string.
     * 
     * @return 
     *      Constructed JWSObject
     */
    public static JWSObject deserialize(String json) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json,JWSObject.class);
    }

    /**
     * Retrieve JWSHeader object.
     */
    public JWSHeader jwsHeader(){
        return jwsHeader;
    }

    /**
     * Original base64url with serialized jwsHeader (when constructed from json string).
     */
    public String original_protected(){
        return original_protected;
    }

    /**
     * base64 json string with JWSHeader.
     */
    @JsonProperty("protected")
    public String protectedB64() throws Exception {
        return MessageSecurityHelper.stringToBase64Url(jwsHeader.serialize());
    }

    /**
     * base64url protected payload (JWEObject).
     */
    @JsonProperty("payload")
    public String payload() {
        return payload;
    }

    /**
     * base64url signature for (protected + "." + payload) data.
     */
    @JsonProperty("signature")
    public String signature() {
        return signature;
    }
}

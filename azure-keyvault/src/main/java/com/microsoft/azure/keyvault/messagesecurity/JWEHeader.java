/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.keyvault.messagesecurity;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Json Web Encryption Header class.
 */
class JWEHeader {

    private String alg;
    private String kid;
    private String enc;

    /**
     * Constructor.
     */
    public JWEHeader(){

    }

    /**
     * Constructor.
     * 
     * @param alg
     *      Encryption algorithm (for key). (Supported: RSA-OAEP)
     * @param kid
     *      Key Id
     * @param enc
     *      Encryption algorithm (for data). (Supported: A128CBC-HS256) 
     */
    public JWEHeader(String alg, String kid, String enc){
        this.alg = alg;
        this.kid = kid;
        this.enc = enc;
    }

    /**
     * Compare two JWEHeaders.
     * 
     * @return 
     *      true if JWEHeaders are identical.
     */
    public boolean equals(JWEHeader other){
        return this.alg.equals(other.alg) &&
                this.kid.equals(other.kid) &&
                this.enc.equals(other.enc);
    }

    /**
     * Serialize JWEHeader to json string.
     * 
     * @return 
     *      Json string with serialized JWEHeader.
     */
    public String serialize() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }

    /**
     * Construct JWEHeader from json string.
     * 
     * @param json
     *      json string.
     * 
     * @return 
     *      Constructed JWEHeader
     */
    public static JWEHeader deserialize(String json) throws IOException{
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json,JWEHeader.class);
    }

    /**
     * Construct JWEHeader from base64url string.
     * 
     * @param base64
     *      base64 url string.
     * 
     * @return 
     *      Constructed JWEHeader
     */
    public static JWEHeader fromBase64String(String base64) throws IOException{
        String json = MessageSecurityHelper.base64UrltoString(base64);
        return deserialize(json);
    }

    /**
     * Encryption algorithm (for key). (Supported: RSA-OAEP)
     */
    @JsonProperty("alg")
    public String alg() {
        return alg;
    }

    /**
     * Key Id
     */
    @JsonProperty("kid")
    public String kid() {
        return kid;
    }

    /**
     * Encryption algorithm (for data). (Supported: A128CBC-HS256) 
     */
    @JsonProperty("enc")
    public String enc() {
        return enc;
    }
}

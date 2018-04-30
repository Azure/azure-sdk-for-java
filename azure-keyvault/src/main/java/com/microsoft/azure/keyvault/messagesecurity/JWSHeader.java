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
 * Json Web Signature header class.
 */
class JWSHeader {

    private String alg;
    private String kid;
    private String at;
    private long ts;
    private String p;
    private String typ;

    /**
     * Constructor.
     */ 
    public JWSHeader(){}

    /**
     * Constructor.
     * 
     * @param alg
     *      signing algorithm (RS256).
     * @param kid
     *      signing key id.
     * @param at
     *      authorization token.
     * @param ts
     *      timestamp.
     * @param typ
     *      authorization type (PoP).
     * @param p
     *      p <not used now>
     */
    public JWSHeader(String alg, String kid, String at, long ts, String typ, String p){
        this.alg = alg;
        this.kid = kid;
        this.at = at;
        this.ts = ts;
        this.p = p;
        this.typ = typ;
    }

    /**
     * Compare two JwsHeader.
     * 
     * @return 
     *      true if JWSHeaders are identical.
     */
    public boolean equals(JWSHeader other){
        return this.alg.equals(other.alg) &&
                this.kid.equals(other.kid) &&
                this.at.equals(other.at) &&
                this.ts == other.ts &&
                this.p.equals(other.p) &&
                this.typ.equals(other.typ);
    }
    
    /**
     * Serialize JWSHeader to json string.
     * 
     * @return 
     *      Json string with serialized JWSHeader.
     */
    public String serialize() throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(this);
    }

    /**
     * Construct JWSHeader from json string.
     * 
     * @param json
     *      json string.
     * 
     * @return 
     *      Constructed JWSHeader
     */
    public static JWSHeader deserialize(String json) throws IOException{
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json,JWSHeader.class);
    }

    /**
     * Construct JWSHeader from base64url string.
     * 
     * @param base64
     *      base64 url string.
     * 
     * @return 
     *      Constructed JWSHeader
     */
    public static JWSHeader fromBase64String(String base64) throws IOException {
        String json = MessageSecurityHelper.base64UrltoString(base64);
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(json,JWSHeader.class);
    }

    /**
     * signing algorithm (RS256).
     */
    @JsonProperty("alg")
    public String alg() {
        return alg;
    }

    /**
     * signing key id.
     */
    @JsonProperty("kid")
    public String kid() {
        return kid;
    }

    /**
     * authorization token.
     */
    @JsonProperty("at")
    public String at() {
        return at;
    }

    /**
     * timestamp.
     */
    @JsonProperty("ts")
    public long ts() {
        return ts;
    }

    /**
     * authorization type (PoP).
     */
    @JsonProperty("typ")
    public String typ() {
        return typ;
    }

    /**
     * p <not used now>
     */
    @JsonProperty("p")
    public String p() {
        return p;
    }
}

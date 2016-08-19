/**
 *
 * Copyright (c) Microsoft and contributors.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.microsoft.azure.keyvault.cryptography.test;

import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY, setterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
public class JsonWebKey {

    private String kid;

    @JsonProperty("kid")
    public String getKid() {
        return kid;
    }

    public void setKid(String kid) {
        this.kid = kid;
    }

    private String kty;

    @JsonProperty("kty")
    public String getKty() {
        return kty;
    }

    public void setKty(String kty) {
        this.kty = kty;
    }

    private String[] keyOps;

    @JsonProperty("key_ops")
    public String[] getKeyOps() {
        return keyOps;
    }

    public void setKeyOps(String[] keyOps) {
        this.keyOps = keyOps;
    }

    private byte[] n;

    @JsonProperty("n")
    @JsonSerialize(using = Base64UrlSerializer.class)
    @JsonDeserialize(using = Base64UrlDeserializer.class)
    public byte[] getN() {
        return n;
    }

    public void setN(byte[] n) {
        this.n = n;
    }

    private byte[] e;

    @JsonProperty("e")
    @JsonSerialize(using = Base64UrlSerializer.class)
    @JsonDeserialize(using = Base64UrlDeserializer.class)
    public byte[] getE() {
        return e;
    }

    public void setE(byte[] e) {
        this.e = e;
    }

    private byte[] d;

    @JsonProperty("d")
    @JsonSerialize(using = Base64UrlSerializer.class)
    @JsonDeserialize(using = Base64UrlDeserializer.class)
    public byte[] getD() {
        return d;
    }

    public void setD(byte[] d) {
        this.d = d;
    }

    private byte[] dp;

    @JsonProperty("dp")
    @JsonSerialize(using = Base64UrlSerializer.class)
    @JsonDeserialize(using = Base64UrlDeserializer.class)
    public byte[] getDP() {
        return dp;
    }

    public void setDP(byte[] dp) {
        this.dp = dp;
    }

    private byte[] dq;

    @JsonProperty("dq")
    @JsonSerialize(using = Base64UrlSerializer.class)
    @JsonDeserialize(using = Base64UrlDeserializer.class)
    public byte[] getDQ() {
        return dq;
    }

    public void setDQ(byte[] dq) {
        this.dq = dq;
    }

    private byte[] qi;

    @JsonProperty("qi")
    @JsonSerialize(using = Base64UrlSerializer.class)
    @JsonDeserialize(using = Base64UrlDeserializer.class)
    public byte[] getQI() {
        return qi;
    }

    public void setQI(byte[] qi) {
        this.qi = qi;
    }

    private byte[] p;

    @JsonProperty("p")
    @JsonSerialize(using = Base64UrlSerializer.class)
    @JsonDeserialize(using = Base64UrlDeserializer.class)
    public byte[] getP() {
        return p;
    }

    public void setP(byte[] p) {
        this.p = p;
    }

    private byte[] q;

    @JsonProperty("q")
    @JsonSerialize(using = Base64UrlSerializer.class)
    @JsonDeserialize(using = Base64UrlDeserializer.class)
    public byte[] getQ() {
        return q;
    }

    public void setQ(byte[] q) {
        this.q = q;
    }

    private byte[] k;

    @JsonProperty("k")
    @JsonSerialize(using = Base64UrlSerializer.class)
    @JsonDeserialize(using = Base64UrlDeserializer.class)
    public byte[] getk() {
        return k;
    }

    public void setK(byte[] k) {
        this.k = k;
    }

    private byte[] t;

    @JsonProperty("key_hsm")
    @JsonSerialize(using = Base64UrlSerializer.class)
    @JsonDeserialize(using = Base64UrlDeserializer.class)
    public byte[] getT() {
        return t;
    }

    public void setT(byte[] t) {
        this.t = t;
    }

    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonGenerationException e) {
            throw new IllegalStateException(e);
        } catch (JsonMappingException e) {
            throw new IllegalStateException(e);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private RSAPublicKeySpec getRSAPublicKeySpec() {

        return new RSAPublicKeySpec(toBigInteger(n), toBigInteger(e));
    }

    private RSAPrivateKeySpec getRSAPrivateKeySpec() {

        return new RSAPrivateCrtKeySpec(toBigInteger(n), toBigInteger(e), toBigInteger(d), toBigInteger(p), toBigInteger(q), toBigInteger(dp), toBigInteger(dq), toBigInteger(qi));
    }

    private PublicKey getRSAPublicKey(Provider provider) {

        try {
            RSAPublicKeySpec publicKeySpec = getRSAPublicKeySpec();
            KeyFactory       factory       = provider != null ? KeyFactory.getInstance("RSA", provider) : KeyFactory.getInstance("RSA");

            return factory.generatePublic(publicKeySpec);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    private PrivateKey getRSAPrivateKey(Provider provider) {

        try {
            RSAPrivateKeySpec privateKeySpec = getRSAPrivateKeySpec();
            KeyFactory        factory       = provider != null ? KeyFactory.getInstance("RSA", provider) : KeyFactory.getInstance("RSA");

            return factory.generatePrivate(privateKeySpec);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    private void checkRSACompatible() {
        if (!JsonWebKeyType.RSA.equals(kty) && !JsonWebKeyType.RSAHSM.equals(kty)) {
            throw new UnsupportedOperationException("Not an RSA key");
        }
    }

    private static byte[] toByteArray(BigInteger n) {
        byte[] result = n.toByteArray();
        if (result[0] == 0) {
            // The leading zero is used to let the number positive. Since RSA
            // parameters are always positive, we remove it.
            return Arrays.copyOfRange(result, 1, result.length);
        }
        return result;
    }

    private static BigInteger toBigInteger(byte[] b) {
        if (b[0] < 0) {
            // RSA parameters are always positive numbers, so if the first byte
            // is negative, we need to add a leading zero
            // to make the entire BigInteger positive.
            byte[] temp = new byte[1 + b.length];
            System.arraycopy(b, 0, temp, 1, b.length);
            b = temp;
        }
        return new BigInteger(b);
    }

    public static JsonWebKey fromRSA(KeyPair keyPair) {

        RSAPrivateCrtKey privateKey = (RSAPrivateCrtKey) keyPair.getPrivate();
        JsonWebKey key = null;

        if (privateKey != null) {

            key = new JsonWebKey();

            key.setKty(JsonWebKeyType.RSA);

            key.setN(toByteArray(privateKey.getModulus()));
            key.setE(toByteArray(privateKey.getPublicExponent()));
            key.setD(toByteArray(privateKey.getPrivateExponent()));
            key.setP(toByteArray(privateKey.getPrimeP()));
            key.setQ(toByteArray(privateKey.getPrimeQ()));
            key.setDP(toByteArray(privateKey.getPrimeExponentP()));
            key.setDQ(toByteArray(privateKey.getPrimeExponentQ()));
            key.setQI(toByteArray(privateKey.getCrtCoefficient()));
        } else {

            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

            key = new JsonWebKey();

            key.setKty(JsonWebKeyType.RSA);

            key.setN(toByteArray(publicKey.getModulus()));
            key.setE(toByteArray(publicKey.getPublicExponent()));
            key.setD(null);
            key.setP(null);
            key.setQ(null);
            key.setDP(null);
            key.setDQ(null);
            key.setQI(null);
        }

        return key;
    }

    public KeyPair toRSA() {
        return this.toRSA(false);
    }

    public KeyPair toRSA(boolean includePrivateParameters) {

    	return toRSA(includePrivateParameters, null);
    }

    public KeyPair toRSA(boolean includePrivateParameters, Provider provider) {

        // Must be RSA
        checkRSACompatible();

        if (includePrivateParameters) {
            return new KeyPair(getRSAPublicKey(provider), getRSAPrivateKey(provider));
        } else {
            return new KeyPair(getRSAPublicKey(provider), null);
        }
    }
}
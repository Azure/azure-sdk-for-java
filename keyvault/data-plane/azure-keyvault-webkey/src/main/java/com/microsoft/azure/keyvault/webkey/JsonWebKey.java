/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.keyvault.webkey;

import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPrivateCrtKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.EllipticCurve;
import java.security.spec.RSAPrivateCrtKeySpec;
import java.security.spec.RSAPrivateKeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableMap;

/**
 * As of http://tools.ietf.org/html/draft-ietf-jose-json-web-key-18.
 */
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY, setterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY)
public class JsonWebKey {

    /**
     * Key Identifier.
     */
    @JsonProperty(value = "kid")
    private String kid;

    /**
     * JsonWebKey key type (kty). Possible values include: 'EC', 'EC-HSM', 'RSA',
     * 'RSA-HSM', 'oct'.
     */
    @JsonProperty(value = "kty")
    private JsonWebKeyType kty;

    /**
     * The keyOps property.
     */
    @JsonProperty(value = "key_ops")
    private List<JsonWebKeyOperation> keyOps;

    /**
     * RSA modulus.
     */
    @JsonProperty(value = "n")
    private byte[] n;

    /**
     * RSA public exponent.
     */
    @JsonProperty(value = "e")
    private byte[] e;

    /**
     * RSA private exponent, or the D component of an EC private key.
     */
    @JsonProperty(value = "d")
    private byte[] d;

    /**
     * RSA Private Key Parameter.
     */
    @JsonProperty(value = "dp")
    private byte[] dp;

    /**
     * RSA Private Key Parameter.
     */
    @JsonProperty(value = "dq")
    private byte[] dq;

    /**
     * RSA Private Key Parameter.
     */
    @JsonProperty(value = "qi")
    private byte[] qi;

    /**
     * RSA secret prime.
     */
    @JsonProperty(value = "p")
    private byte[] p;

    /**
     * RSA secret prime, with p & q.
     */
    @JsonProperty(value = "q")
    private byte[] q;

    /**
     * Symmetric key.
     */
    @JsonProperty(value = "k")
    private byte[] k;

    /**
     * HSM Token, used with Bring Your Own Key.
     */
    @JsonProperty(value = "key_hsm")
    private byte[] t;

    /**
     * Elliptic curve name. For valid values, see JsonWebKeyCurveName. Possible
     * values include: 'P-256', 'P-384', 'P-521', 'SECP256K1'.
     */
    @JsonProperty(value = "crv")
    private JsonWebKeyCurveName crv;

    /**
     * X component of an EC public key.
     */
    @JsonProperty(value = "x")
    private byte[] x;

    /**
     * Y component of an EC public key.
     */
    @JsonProperty(value = "y")
    private byte[] y;

    /**
     * Get the kid value.
     *
     * @return the kid value
     */
    @JsonProperty("kid")
    public String kid() {
        return this.kid;
    }

    /**
     * Set the key identifier value.
     *
     * @param kid
     *            the kid value to set
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey withKid(String kid) {
        this.kid = kid;
        return this;
    }

    /**
     * Get the kty value.
     *
     * @return the kty value
     */
    @JsonProperty("kty")
    public JsonWebKeyType kty() {
        return this.kty;
    }

    /**
     * Set the key type value.
     *
     * @param kty
     *            the key type
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey withKty(JsonWebKeyType kty) {
        this.kty = kty;
        return this;
    }

    /**
     * Get the keyOps value.
     *
     * @return the keyOps value
     */
    @JsonProperty("key_ops")
    public List<JsonWebKeyOperation> keyOps() {
        return this.keyOps;
    }

    /**
     * Set the keyOps value.
     *
     * @param keyOps
     *            the keyOps value to set
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey withKeyOps(List<JsonWebKeyOperation> keyOps) {
        this.keyOps = keyOps;
        return this;
    }

    /**
     * Get the n value.
     *
     * @return the n value
     */
    @JsonProperty("n")
    @JsonSerialize(using = Base64UrlJsonSerializer.class)
    @JsonDeserialize(using = Base64UrlJsonDeserializer.class)
    public byte[] n() {
        return this.n;
    }

    /**
     * Set the n value.
     *
     * @param n
     *            the n value to set
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey withN(byte[] n) {
        this.n = n;
        return this;
    }

    /**
     * Get the e value.
     *
     * @return the e value
     */
    @JsonProperty("e")
    @JsonSerialize(using = Base64UrlJsonSerializer.class)
    @JsonDeserialize(using = Base64UrlJsonDeserializer.class)
    public byte[] e() {
        return this.e;
    }

    /**
     * Set the e value.
     *
     * @param e
     *            the e value to set
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey withE(byte[] e) {
        this.e = e;
        return this;
    }

    /**
     * Get the d value.
     *
     * @return the d value
     */
    @JsonProperty("d")
    @JsonSerialize(using = Base64UrlJsonSerializer.class)
    @JsonDeserialize(using = Base64UrlJsonDeserializer.class)
    public byte[] d() {
        return this.d;
    }

    /**
     * Set the d value.
     *
     * @param d
     *            the d value to set
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey withD(byte[] d) {
        this.d = d;
        return this;
    }

    /**
     * Get the RSA Private Key Parameter value.
     * 
     * @return the RSA Private Key Parameter value.
     */
    @JsonProperty("dp")
    @JsonSerialize(using = Base64UrlJsonSerializer.class)
    @JsonDeserialize(using = Base64UrlJsonDeserializer.class)
    public byte[] dp() {
        return this.dp;
    }

    /**
     * Set RSA Private Key Parameter value.
     * 
     * @param dp
     *            the RSA Private Key Parameter value to set.
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey withDp(byte[] dp) {
        this.dp = dp;
        return this;
    }

    /**
     * Get the RSA Private Key Parameter value.
     * 
     * @return the RSA Private Key Parameter value.
     */
    @JsonProperty("dq")
    @JsonSerialize(using = Base64UrlJsonSerializer.class)
    @JsonDeserialize(using = Base64UrlJsonDeserializer.class)
    public byte[] dq() {
        return this.dq;
    }

    /**
     * Set RSA Private Key Parameter value .
     * 
     * @param dq
     *            the RSA Private Key Parameter value to set.
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey withDq(byte[] dq) {
        this.dq = dq;
        return this;
    }

    /**
     * Get the RSA Private Key Parameter value.
     * 
     * @return the RSA Private Key Parameter value.
     */
    @JsonProperty("qi")
    @JsonSerialize(using = Base64UrlJsonSerializer.class)
    @JsonDeserialize(using = Base64UrlJsonDeserializer.class)
    public byte[] qi() {
        return this.qi;
    }

    /**
     * Set RSA Private Key Parameter value.
     * 
     * @param qi
     *            the RSA Private Key Parameter value to set.
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey withQi(byte[] qi) {
        this.qi = qi;
        return this;
    }

    /**
     * Get the RSA secret prime value.
     * 
     * @return the RSA secret prime value.
     */
    @JsonProperty("p")
    @JsonSerialize(using = Base64UrlJsonSerializer.class)
    @JsonDeserialize(using = Base64UrlJsonDeserializer.class)
    public byte[] p() {
        return this.p;
    }

    /**
     * Set the RSA secret prime value.
     * 
     * @param p
     *            the RSA secret prime value.
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey withP(byte[] p) {
        this.p = p;
        return this;
    }

    /**
     * Get RSA secret prime, with p &lt; q value.
     * 
     * @return the RSA secret prime, with p &lt; q value.
     */
    @JsonProperty("q")
    @JsonSerialize(using = Base64UrlJsonSerializer.class)
    @JsonDeserialize(using = Base64UrlJsonDeserializer.class)
    public byte[] q() {
        return this.q;
    }

    /**
     * Set the RSA secret prime, with p &lt; q value.
     * 
     * @param q
     *            the the RSA secret prime, with p &lt; q value to be set.
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey withQ(byte[] q) {
        this.q = q;
        return this;
    }

    /**
     * Get Symmetric key value.
     * 
     * @return the symmetric key value.
     */
    @JsonProperty("k")
    @JsonSerialize(using = Base64UrlJsonSerializer.class)
    @JsonDeserialize(using = Base64UrlJsonDeserializer.class)
    public byte[] k() {
        return this.k;
    }

    /**
     * Set the Symmetric key value.
     * 
     * @param k
     *            the symmetric key value to set.
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey withK(byte[] k) {
        this.k = k;
        return this;
    }

    /**
     * Get HSM Token value, used with Bring Your Own Key.
     * 
     * @return HSM Token, used with Bring Your Own Key.
     */
    @JsonProperty("key_hsm")
    @JsonSerialize(using = Base64UrlJsonSerializer.class)
    @JsonDeserialize(using = Base64UrlJsonDeserializer.class)
    public byte[] t() {
        return this.t;
    }

    /**
     * Set HSM Token value, used with Bring Your Own Key.
     * 
     * @param t
     *            HSM Token value to set, used with Bring Your Own Key
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey withT(byte[] t) {
        this.t = t;
        return this;
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

    /**
     * Get the crv value.
     *
     * @return the crv value
     */
    @JsonProperty("crv")
    public JsonWebKeyCurveName crv() {
        return this.crv;
    }

    /**
     * Set the crv value.
     *
     * @param crv
     *            the crv value to set
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey withCrv(JsonWebKeyCurveName crv) {
        this.crv = crv;
        return this;
    }

    /**
     * Get the x value.
     *
     * @return the x value
     */
    @JsonProperty("x")
    @JsonSerialize(using = Base64UrlJsonSerializer.class)
    @JsonDeserialize(using = Base64UrlJsonDeserializer.class)
    public byte[] x() {
        return this.x;
    }

    /**
     * Set the x value.
     *
     * @param x
     *            the x value to set
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey withX(byte[] x) {
        this.x = x;
        return this;
    }

    /**
     * Get the y value.
     *
     * @return the y value
     */
    @JsonProperty("y")
    @JsonSerialize(using = Base64UrlJsonSerializer.class)
    @JsonDeserialize(using = Base64UrlJsonDeserializer.class)
    public byte[] y() {
        return this.y;
    }

    /**
     * Set the y value.
     *
     * @param y
     *            the y value to set
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey withY(byte[] y) {
        this.y = y;
        return this;
    }

    /**
     * Get the RSA public key spec value.
     *
     * @return the RSA public key spec value
     */
    private RSAPublicKeySpec getRSAPublicKeySpec() {

        return new RSAPublicKeySpec(toBigInteger(n), toBigInteger(e));
    }

    /**
     * Get the RSA private key spec value.
     *
     * @return the RSA private key spec value
     */
    private RSAPrivateKeySpec getRSAPrivateKeySpec() {

        return new RSAPrivateCrtKeySpec(toBigInteger(n), toBigInteger(e), toBigInteger(d), toBigInteger(p),
                toBigInteger(q), toBigInteger(dp), toBigInteger(dq), toBigInteger(qi));
    }

    /**
     * Get the RSA public key value.
     *
     * @param provider
     *            the Java security provider.
     * @return the RSA public key value
     */
    private PublicKey getRSAPublicKey(Provider provider) {

        try {
            RSAPublicKeySpec publicKeySpec = getRSAPublicKeySpec();
            KeyFactory factory = provider != null ? KeyFactory.getInstance("RSA", provider)
                    : KeyFactory.getInstance("RSA");

            return factory.generatePublic(publicKeySpec);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Get the RSA private key value.
     *
     * @param provider
     *            the Java security provider.
     * @return the RSA private key value
     */
    private PrivateKey getRSAPrivateKey(Provider provider) {

        try {
            RSAPrivateKeySpec privateKeySpec = getRSAPrivateKeySpec();
            KeyFactory factory = provider != null ? KeyFactory.getInstance("RSA", provider)
                    : KeyFactory.getInstance("RSA");

            return factory.generatePrivate(privateKeySpec);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    private static PublicKey getECPublicKey(ECPoint ecPoint, ECParameterSpec curveSpec, Provider provider) {
        // Create public key spec with given point
        try {
            ECPublicKeySpec pubSpec = new ECPublicKeySpec(ecPoint, curveSpec);
            KeyFactory kf = provider != null ? KeyFactory.getInstance("EC", provider)
                    : KeyFactory.getInstance("EC", "SunEC");
            return (ECPublicKey) kf.generatePublic(pubSpec);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    private static PrivateKey getECPrivateKey(byte[] d, ECParameterSpec curveSpec, Provider provider) {
        try {
            ECPrivateKeySpec priSpec = new ECPrivateKeySpec(new BigInteger(1, d), curveSpec);
            KeyFactory kf = provider != null ? KeyFactory.getInstance("EC", provider)
                    : KeyFactory.getInstance("EC", "SunEC");
            return (ECPrivateKey) kf.generatePrivate(priSpec);
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Verifies if the key is an RSA key.
     */
    private void checkRSACompatible() {
        if (!JsonWebKeyType.RSA.equals(kty) && !JsonWebKeyType.RSA_HSM.equals(kty)) {
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

    /**
     * Converts RSA key pair to JSON web key.
     * 
     * @param keyPair
     *            RSA key pair
     * @return the JSON web key, converted from RSA key pair.
     */
    public static JsonWebKey fromRSA(KeyPair keyPair) {

        RSAPrivateCrtKey privateKey = (RSAPrivateCrtKey) keyPair.getPrivate();
        JsonWebKey key = null;

        if (privateKey != null) {

            key = new JsonWebKey().withKty(JsonWebKeyType.RSA).withN(toByteArray(privateKey.getModulus()))
                    .withE(toByteArray(privateKey.getPublicExponent()))
                    .withD(toByteArray(privateKey.getPrivateExponent())).withP(toByteArray(privateKey.getPrimeP()))
                    .withQ(toByteArray(privateKey.getPrimeQ())).withDp(toByteArray(privateKey.getPrimeExponentP()))
                    .withDq(toByteArray(privateKey.getPrimeExponentQ()))
                    .withQi(toByteArray(privateKey.getCrtCoefficient()));
        } else {

            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

            key = new JsonWebKey().withKty(JsonWebKeyType.RSA).withN(toByteArray(publicKey.getModulus()))
                    .withE(toByteArray(publicKey.getPublicExponent())).withD(null).withP(null).withQ(null).withDp(null)
                    .withDq(null).withQi(null);
        }

        return key;
    }

    /**
     * Converts JSON web key to RSA key pair.
     * 
     * @return RSA key pair
     */
    public KeyPair toRSA() {
        return this.toRSA(false);
    }

    /**
     * Converts JSON web key to RSA key pair and include the private key if set to
     * true.
     * 
     * @param includePrivateParameters
     *            true if the RSA key pair should include the private key. False
     *            otherwise.
     * @return RSA key pair
     */
    public KeyPair toRSA(boolean includePrivateParameters) {
        return toRSA(includePrivateParameters, null);
    }

    /**
     * Converts JSON web key to RSA key pair and include the private key if set to
     * true.
     * 
     * @param provider
     *            the Java security provider.
     * @param includePrivateParameters
     *            true if the RSA key pair should include the private key. False
     *            otherwise.
     * @return RSA key pair
     */
    public KeyPair toRSA(boolean includePrivateParameters, Provider provider) {

        // Must be RSA
        checkRSACompatible();

        if (includePrivateParameters) {
            return new KeyPair(getRSAPublicKey(provider), getRSAPrivateKey(provider));
        } else {
            return new KeyPair(getRSAPublicKey(provider), null);
        }
    }

    /**
     * Converts JSON web key to EC key pair and include the private key if set to
     * true.
     * 
     * @return EC key pair
     */
    public KeyPair toEC() {
        return toEC(false, null);
    }

    /**
     * Converts JSON web key to EC key pair and include the private key if set to
     * true.
     * 
     * @param includePrivateParameters
     *            true if the EC key pair should include the private key. False
     *            otherwise.
     * @return EC key pair
     */
    public KeyPair toEC(boolean includePrivateParameters) {
        return toEC(includePrivateParameters, null);
    }

    /**
     * Converts JSON web key to EC key pair and include the private key if set to
     * true.
     * 
     * @param includePrivateParameters
     *            true if the EC key pair should include the private key. False
     *            otherwise.
     * @param provider
     *            Java security provider
     * @return EC key pair
     */
    public KeyPair toEC(boolean includePrivateParameters, Provider provider) {

        if (provider == null) {
            // Our default provider for this class
            provider = Security.getProvider("SunEC");
        }

        if (!JsonWebKeyType.EC.equals(kty) && !JsonWebKeyType.EC_HSM.equals(kty)) {
            throw new IllegalArgumentException("Not an EC key.");
        }

        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC", provider);

            ECGenParameterSpec gps = new ECGenParameterSpec(CURVE_TO_SPEC_NAME.get(crv));
            kpg.initialize(gps);

            // Generate dummy keypair to get parameter spec.
            KeyPair apair = kpg.generateKeyPair();
            ECPublicKey apub = (ECPublicKey) apair.getPublic();
            ECParameterSpec aspec = apub.getParams();

            ECPoint ecPoint = new ECPoint(new BigInteger(1, x), new BigInteger(1, y));

            KeyPair realKeyPair;

            if (includePrivateParameters) {
                realKeyPair = new KeyPair(getECPublicKey(ecPoint, aspec, provider),
                        getECPrivateKey(d, aspec, provider));
            } else {
                realKeyPair = new KeyPair(getECPublicKey(ecPoint, aspec, provider), null);
            }

            return realKeyPair;
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Converts EC key pair to JSON web key.
     * 
     * @param keyPair
     *            EC key pair
     * @param provider
     *            Java security provider
     * @return the JSON web key, converted from EC key pair.
     */
    public static JsonWebKey fromEC(KeyPair keyPair, Provider provider) {

        ECPublicKey apub = (ECPublicKey) keyPair.getPublic();
        ECPoint point = apub.getW();
        ECPrivateKey apriv = (ECPrivateKey) keyPair.getPrivate();

        if (apriv != null) {
            return new JsonWebKey().withKty(JsonWebKeyType.EC).withCrv(getCurveFromKeyPair(keyPair, provider))
                    .withX(point.getAffineX().toByteArray()).withY(point.getAffineY().toByteArray())
                    .withD(apriv.getS().toByteArray()).withKty(JsonWebKeyType.EC);
        } else {
            return new JsonWebKey().withKty(JsonWebKeyType.EC).withCrv(getCurveFromKeyPair(keyPair, provider))
                    .withX(point.getAffineX().toByteArray()).withY(point.getAffineY().toByteArray())
                    .withKty(JsonWebKeyType.EC);
        }
    }

    // Matches the curve of the keyPair to supported curves.
    private static JsonWebKeyCurveName getCurveFromKeyPair(KeyPair keyPair, Provider provider) {

        try {
            ECPublicKey key = (ECPublicKey) keyPair.getPublic();
            ECParameterSpec spec = key.getParams();
            EllipticCurve crv = spec.getCurve();

            List<JsonWebKeyCurveName> curveList = Arrays.asList(JsonWebKeyCurveName.P_256, JsonWebKeyCurveName.P_384,
                    JsonWebKeyCurveName.P_521, JsonWebKeyCurveName.P_256K);

            for (JsonWebKeyCurveName curve : curveList) {
                ECGenParameterSpec gps = new ECGenParameterSpec(CURVE_TO_SPEC_NAME.get(curve));
                KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC", provider);
                kpg.initialize(gps);

                // Generate dummy keypair to get parameter spec.
                KeyPair apair = kpg.generateKeyPair();
                ECPublicKey apub = (ECPublicKey) apair.getPublic();
                ECParameterSpec aspec = apub.getParams();
                EllipticCurve acurve = aspec.getCurve();

                // Matches the parameter spec
                if (acurve.equals(crv)) {
                    return curve;
                }
            }

            // Did not find a supported curve.
            throw new NoSuchAlgorithmException("Curve not supported.");
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Converts AES key to JSON web key.
     * 
     * @param secretKey
     *            AES key
     * @return the JSON web key, converted from AES key.
     */
    public static JsonWebKey fromAes(SecretKey secretKey) {
        if (secretKey == null) {
            return null;
        }

        return new JsonWebKey().withK(secretKey.getEncoded()).withKty(JsonWebKeyType.OCT);
    }

    /**
     * Converts JSON web key to AES key.
     * 
     * @return AES key
     */
    public SecretKey toAes() {
        if (k == null) {
            return null;
        }

        SecretKey secretKey = new SecretKeySpec(k, "AES");
        return secretKey;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (obj instanceof JsonWebKey) {
            return this.equals((JsonWebKey) obj);
        }
        return super.equals(obj);
    }

    /**
     * Indicates whether some other {@link JsonWebKey} is "equal to" this one.
     * 
     * @param jwk
     *            the other {@link JsonWebKey} to compare with.
     * @return true if this {@link JsonWebKey} is the same as the jwk argument;
     *         false otherwise.
     */
    public boolean equals(JsonWebKey jwk) {
        if (jwk == null) {
            return false;
        }

        if (!Objects.equal(kid, jwk.kid)) {
            return false;
        }

        if (!Objects.equal(kty, jwk.kty)) {
            return false;
        }

        if (!Objects.equal(keyOps, jwk.keyOps)) {
            return false;
        }

        if (!Objects.equal(crv, jwk.crv)) {
            return false;
        }

        if (!Arrays.equals(k, jwk.k)) {
            return false;
        }

        // Public parameters
        if (!Arrays.equals(n, jwk.n)) {
            return false;
        }
        if (!Arrays.equals(e, jwk.e)) {
            return false;
        }

        // Private parameters
        if (!Arrays.equals(d, jwk.d)) {
            return false;
        }
        if (!Arrays.equals(dp, jwk.dp)) {
            return false;
        }
        if (!Arrays.equals(dq, jwk.dq)) {
            return false;
        }
        if (!Arrays.equals(qi, jwk.qi)) {
            return false;
        }
        if (!Arrays.equals(p, jwk.p)) {
            return false;
        }
        if (!Arrays.equals(q, jwk.q)) {
            return false;
        }
        if (!Arrays.equals(x, jwk.x)) {
            return false;
        }
        if (!Arrays.equals(y, jwk.y)) {
            return false;
        }

        // HSM token
        if (!Arrays.equals(t, jwk.t)) {
            return false;
        }

        return true;
    }

    /**
     * Verifies whether the {@link JsonWebKey} has private key.
     * 
     * @return true if the {@link JsonWebKey} has private key; false otherwise.
     */
    public boolean hasPrivateKey() {

        if (JsonWebKeyType.OCT.equals(kty)) {
            return k != null;
        }

        else if (JsonWebKeyType.RSA.equals(kty) || JsonWebKeyType.RSA_HSM.equals(kty)) {
            return (d != null && dp != null && dq != null && qi != null && p != null && q != null);
        }

        else if (JsonWebKeyType.EC.equals(kty) || JsonWebKeyType.EC_HSM.equals(kty)) {
            return (d != null);
        }

        return false;
    }

    /**
     * Verifies whether the {@link JsonWebKey} is valid.
     * 
     * @return true if the {@link JsonWebKey} is valid; false otherwise.
     */
    @JsonIgnore
    public boolean isValid() {
        if (kty == null) {
            return false;
        }

        if (keyOps != null) {
            final Set<JsonWebKeyOperation> set = new HashSet<JsonWebKeyOperation>(JsonWebKeyOperation.ALL_OPERATIONS);
            for (int i = 0; i < keyOps.size(); i++) {
                if (!set.contains(keyOps.get(i))) {
                    return false;
                }
            }
        }

        if (JsonWebKeyType.OCT.equals(kty)) {
            return isValidOctet();
        }

        else if (JsonWebKeyType.RSA.equals(kty)) {
            return isValidRsa();
        }

        else if (JsonWebKeyType.RSA_HSM.equals(kty)) {
            return isValidRsaHsm();
        }

        else if (JsonWebKeyType.EC.equals(kty)) {
            return isValidEc();
        }

        else if (JsonWebKeyType.EC_HSM.equals(kty)) {
            return isValidEcHsm();
        }

        return false;
    }

    private boolean isValidOctet() {
        if (k != null) {
            return true;
        }
        return false;
    }

    private boolean isValidRsa() {
        if (n == null || e == null) {
            return false;
        }

        return hasPrivateKey() || (d == null && dp == null && dq == null && qi == null && p == null && q == null);
    }

    private boolean isValidRsaHsm() {
        // MAY have public key parameters
        if ((n == null && e != null) || (n != null && e == null)) {
            return false;
        }

        // no private key
        if (hasPrivateKey()) {
            return false;
        }

        // MUST have ( T || ( N && E ) )
        boolean tokenParameters = t != null;
        boolean publicParameters = (n != null && e != null);

        if (tokenParameters && publicParameters) {
            return false;
        }

        return (tokenParameters || publicParameters);
    }

    private boolean isValidEc() {
        boolean ecPointParameters = (x != null && y != null);
        if (!ecPointParameters || crv == null) {
            return false;
        }

        return hasPrivateKey() || (d == null);
    }

    private boolean isValidEcHsm() {
        // MAY have public key parameters
        boolean ecPointParameters = (x != null && y != null);
        if ((ecPointParameters && crv == null) || (!ecPointParameters && crv != null)) {
            return false;
        }

        // no private key
        if (hasPrivateKey()) {
            return false;
        }

        // MUST have (T || (ecPointParameters && crv))
        boolean publicParameters = (ecPointParameters && crv != null);
        boolean tokenParameters = t != null;

        if (tokenParameters && publicParameters) {
            return false;
        }

        return (tokenParameters || publicParameters);
    }

    /**
     * Clear key materials.
     */
    public void clearMemory() {
        zeroArray(k);
        k = null;
        zeroArray(n);
        n = null;
        zeroArray(e);
        e = null;
        zeroArray(d);
        d = null;
        zeroArray(dp);
        dp = null;
        zeroArray(dq);
        dq = null;
        zeroArray(qi);
        qi = null;
        zeroArray(p);
        p = null;
        zeroArray(q);
        q = null;
        zeroArray(t);
        t = null;
        zeroArray(x);
        x = null;
        zeroArray(y);
        y = null;
    }

    private static void zeroArray(byte[] bytes) {
        if (bytes != null) {
            Arrays.fill(bytes, (byte) 0);
        }
    }

    @Override
    public int hashCode() {
        int hashCode = 48313; // setting it to a random prime number
        if (kid != null) {
            hashCode += kid.hashCode();
        }

        if (JsonWebKeyType.OCT.equals(kty)) {
            hashCode += hashCode(k);
        }

        else if (JsonWebKeyType.RSA.equals(kty)) {
            hashCode += hashCode(n);
        }

        else if (JsonWebKeyType.EC.equals(kty)) {
            hashCode += hashCode(x);
            hashCode += hashCode(y);
            hashCode += crv.hashCode();
        }

        else if (JsonWebKeyType.RSA_HSM.equals(kty) || JsonWebKeyType.EC_HSM.equals(kty)) {
            hashCode += hashCode(t);
        }

        return hashCode;
    }

    private static int hashCode(byte[] obj) {
        int hashCode = 0;

        if (obj == null || obj.length == 0) {
            return 0;
        }

        for (int i = 0; i < obj.length; i++) {
            hashCode = (hashCode << 3) | (hashCode >> 29) ^ obj[i];
        }
        return hashCode;
    }

    private static final Map<JsonWebKeyCurveName, String> CURVE_TO_SPEC_NAME = ImmutableMap
            .<JsonWebKeyCurveName, String>builder().put(JsonWebKeyCurveName.P_256, "secp256r1")
            .put(JsonWebKeyCurveName.P_384, "secp384r1").put(JsonWebKeyCurveName.P_521, "secp521r1")
            .put(JsonWebKeyCurveName.P_256K, "secp256k1").build();
}
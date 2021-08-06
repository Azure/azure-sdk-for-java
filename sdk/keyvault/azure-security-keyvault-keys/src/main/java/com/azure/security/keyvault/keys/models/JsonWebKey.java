// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.models;

import com.azure.core.util.logging.ClientLogger;
import com.azure.security.keyvault.keys.implementation.Base64UrlJsonDeserializer;
import com.azure.security.keyvault.keys.implementation.Base64UrlJsonSerializer;
import com.azure.security.keyvault.keys.implementation.ByteExtensions;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Collections;
import java.util.ArrayList;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

/**
 * As of http://tools.ietf.org/html/draft-ietf-jose-json-web-key-18.
 */
@JsonAutoDetect(getterVisibility = JsonAutoDetect.Visibility.PUBLIC_ONLY, setterVisibility =
    JsonAutoDetect.Visibility.PUBLIC_ONLY)
public class JsonWebKey {
    private final ClientLogger logger = new ClientLogger(JsonWebKey.class);

    /**
     * Key Identifier.
     */
    @JsonProperty(value = "kid")
    private String keyId;

    /**
     * JsonWebKey key type (kty). Possible values include: 'EC', 'EC-HSM', 'RSA',
     * 'RSA-HSM', 'oct', 'oct-HSM'.
     */
    @JsonProperty(value = "kty")
    private KeyType keyType;

    /**
     * The keyOps property.
     */
    @JsonProperty(value = "key_ops")
    private List<KeyOperation> keyOps;

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
     * Elliptic curve name. For valid values, see KeyCurveName. Possible
     * values include: 'P-256', 'P-384', 'P-521', 'SECP256K1'.
     */
    @JsonProperty(value = "crv")
    private KeyCurveName crv;

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
    public String getId() {
        return this.keyId;
    }

    /**
     * Set the key identifier value.
     *
     * @param keyId The keyId value to set
     *
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey setId(String keyId) {
        this.keyId = keyId;
        return this;
    }

    /**
     * Get the kty value.
     *
     * @return the kty value
     */
    @JsonProperty("kty")
    public KeyType getKeyType() {
        return this.keyType;
    }

    /**
     * Set the key type value.
     *
     * @param keyType The key type
     *
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey setKeyType(KeyType keyType) {
        this.keyType = keyType;
        return this;
    }

    /**
     * Get the immutable key operations list. The list cannot be modified.
     *
     * @return the key operations list
     */
    @JsonProperty("key_ops")
    public List<KeyOperation> getKeyOps() {
        return this.keyOps == null ? Collections.unmodifiableList(new ArrayList<KeyOperation>())
            : Collections.unmodifiableList(this.keyOps);
    }

    /**
     * Set the keyOps value.
     *
     * @param keyOps The keyOps value to set
     *
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey setKeyOps(List<KeyOperation> keyOps) {
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
    public byte[] getN() {
        return ByteExtensions.clone(this.n);
    }

    /**
     * Set the n value.
     *
     * @param n The n value to set
     *
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey setN(byte[] n) {
        this.n = ByteExtensions.clone(n);
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
    public byte[] getE() {
        return ByteExtensions.clone(this.e);
    }

    /**
     * Set the e value.
     *
     * @param e The e value to set
     *
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey setE(byte[] e) {
        this.e = ByteExtensions.clone(e);
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
    public byte[] getD() {
        return ByteExtensions.clone(this.d);
    }

    /**
     * Set the d value.
     *
     * @param d The d value to set
     *
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey setD(byte[] d) {
        this.d = ByteExtensions.clone(d);
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
    public byte[] getDp() {
        return ByteExtensions.clone(this.dp);
    }

    /**
     * Set RSA Private Key Parameter value.
     *
     * @param dp The RSA Private Key Parameter value to set.
     *
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey setDp(byte[] dp) {
        this.dp = ByteExtensions.clone(dp);
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
    public byte[] getDq() {
        return ByteExtensions.clone(this.dq);
    }

    /**
     * Set RSA Private Key Parameter value .
     *
     * @param dq The RSA Private Key Parameter value to set.
     *
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey setDq(byte[] dq) {
        this.dq = ByteExtensions.clone(dq);
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
    public byte[] getQi() {
        return ByteExtensions.clone(this.qi);
    }

    /**
     * Set RSA Private Key Parameter value.
     *
     * @param qi The RSA Private Key Parameter value to set.
     *
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey setQi(byte[] qi) {
        this.qi = ByteExtensions.clone(qi);
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
    public byte[] getP() {
        return ByteExtensions.clone(this.p);
    }

    /**
     * Set the RSA secret prime value.
     *
     * @param p The RSA secret prime value.
     *
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey setP(byte[] p) {
        this.p = ByteExtensions.clone(p);
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
    public byte[] getQ() {
        return ByteExtensions.clone(this.q);
    }

    /**
     * Set the RSA secret prime, with p &lt; q value.
     *
     * @param q The the RSA secret prime, with p &lt; q value to be set.
     *
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey setQ(byte[] q) {
        this.q = ByteExtensions.clone(q);
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
    public byte[] getK() {
        return ByteExtensions.clone(this.k);
    }

    /**
     * Set the Symmetric key value.
     *
     * @param k The symmetric key value to set.
     *
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey setK(byte[] k) {
        this.k = ByteExtensions.clone(k);
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
    public byte[] getT() {
        return ByteExtensions.clone(this.t);
    }

    /**
     * Set HSM Token value, used with Bring Your Own Key.
     *
     * @param t The HSM Token value to set, used with Bring Your Own Key
     *
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey setT(byte[] t) {
        this.t = ByteExtensions.clone(t);
        return this;
    }

    @Override
    public String toString() {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.writeValueAsString(this);
        } catch (JsonGenerationException e) {
            throw logger.logExceptionAsError(new IllegalStateException(e));
        } catch (JsonMappingException e) {
            throw logger.logExceptionAsError(new IllegalStateException(e));
        } catch (IOException e) {
            throw logger.logExceptionAsError(new IllegalStateException(e));
        }
    }

    /**
     * Get the crv value.
     *
     * @return the crv value
     */
    @JsonProperty("crv")
    public KeyCurveName getCurveName() {
        return this.crv;
    }

    /**
     * Set the crv value.
     *
     * @param crv The crv value to set
     *
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey setCurveName(KeyCurveName crv) {
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
    public byte[] getX() {
        return ByteExtensions.clone(this.x);
    }

    /**
     * Set the x value.
     *
     * @param x The x value to set
     *
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey setX(byte[] x) {
        this.x = ByteExtensions.clone(x);
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
    public byte[] getY() {
        return ByteExtensions.clone(this.y);
    }

    /**
     * Set the y value.
     *
     * @param y The y value to set
     *
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey setY(byte[] y) {
        this.y = ByteExtensions.clone(y);
        return this;
    }

    /**
     * Get the RSA public key spec value.
     *
     * @return the RSA public key spec value
     */
    private RSAPublicKeySpec getRsaPublicKeySpec() {

        return new RSAPublicKeySpec(toBigInteger(n), toBigInteger(e));
    }

    /**
     * Get the RSA private key spec value.
     *
     * @return the RSA private key spec value
     */
    private RSAPrivateKeySpec getRsaPrivateKeySpec() {

        return new RSAPrivateCrtKeySpec(toBigInteger(n), toBigInteger(e), toBigInteger(d), toBigInteger(p),
            toBigInteger(q), toBigInteger(dp), toBigInteger(dq), toBigInteger(qi));
    }

    /**
     * Get the RSA public key value.
     *
     * @param provider The Java security provider.
     *
     * @return the RSA public key value
     */
    private PublicKey getRsaPublicKey(Provider provider) {

        try {
            RSAPublicKeySpec publicKeySpec = getRsaPublicKeySpec();
            KeyFactory factory = provider != null ? KeyFactory.getInstance("RSA", provider)
                : KeyFactory.getInstance("RSA");

            return factory.generatePublic(publicKeySpec);
        } catch (GeneralSecurityException e) {
            throw logger.logExceptionAsError(new IllegalStateException(e));
        }
    }

    /**
     * Get the RSA private key value.
     *
     * @param provider The Java security provider.
     *
     * @return the RSA private key value
     */
    private PrivateKey getRsaPrivateKey(Provider provider) {

        try {
            RSAPrivateKeySpec privateKeySpec = getRsaPrivateKeySpec();
            KeyFactory factory = provider != null ? KeyFactory.getInstance("RSA", provider)
                : KeyFactory.getInstance("RSA");

            return factory.generatePrivate(privateKeySpec);
        } catch (GeneralSecurityException e) {
            throw logger.logExceptionAsError(new IllegalStateException(e));
        }
    }

    private static PublicKey getEcPublicKey(ECPoint ecPoint, ECParameterSpec curveSpec, Provider provider) {
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

    private static PrivateKey getEcPrivateKey(byte[] d, ECParameterSpec curveSpec, Provider provider) {
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
    private void checkRsaCompatible() {
        if (!KeyType.RSA.equals(keyType) && !KeyType.RSA_HSM.equals(keyType)) {
            throw logger.logExceptionAsError(new UnsupportedOperationException("Not an RSA key"));
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
     * @param keyPair Tbe RSA key pair
     *
     * @return the JSON web key, converted from RSA key pair.
     */
    public static JsonWebKey fromRsa(KeyPair keyPair) {

        RSAPrivateCrtKey privateKey = (RSAPrivateCrtKey) keyPair.getPrivate();
        JsonWebKey key = null;

        if (privateKey != null) {

            key = new JsonWebKey().setKeyType(KeyType.RSA).setN(toByteArray(privateKey.getModulus()))
                .setE(toByteArray(privateKey.getPublicExponent()))
                .setD(toByteArray(privateKey.getPrivateExponent())).setP(toByteArray(privateKey.getPrimeP()))
                .setQ(toByteArray(privateKey.getPrimeQ())).setDp(toByteArray(privateKey.getPrimeExponentP()))
                .setDq(toByteArray(privateKey.getPrimeExponentQ()))
                .setQi(toByteArray(privateKey.getCrtCoefficient()));
        } else {

            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

            key = new JsonWebKey().setKeyType(KeyType.RSA).setN(toByteArray(publicKey.getModulus()))
                .setE(toByteArray(publicKey.getPublicExponent())).setD(null).setP(null).setQ(null).setDp(null)
                .setDq(null).setQi(null);
        }

        return key;
    }

    /**
     * Converts RSA key pair to JSON web key.
     *
     * @param keyPair Tbe RSA key pair
     * @param keyOperations The key operations to set on the key
     *
     * @return the JSON web key, converted from RSA key pair.
     */
    public static JsonWebKey fromRsa(KeyPair keyPair, List<KeyOperation> keyOperations) {
        return fromRsa(keyPair).setKeyOps(keyOperations);
    }

    /**
     * Converts JSON web key to RSA key pair.
     *
     * @return RSA key pair
     */
    public KeyPair toRsa() {
        return this.toRsa(false);
    }

    /**
     * Converts JSON web key to RSA key pair and include the private key if set to
     * true.
     *
     * @param includePrivateParameters true if the RSA key pair should include the private key. False otherwise.
     *
     * @return RSA key pair
     */
    public KeyPair toRsa(boolean includePrivateParameters) {
        return toRsa(includePrivateParameters, null);
    }

    /**
     * Converts JSON web key to RSA key pair and include the private key if set to
     * true.
     *
     * @param provider The Java security provider.
     * @param includePrivateParameters true if the RSA key pair should include the private key. False otherwise.
     *
     * @return RSA key pair
     */
    public KeyPair toRsa(boolean includePrivateParameters, Provider provider) {

        // Must be RSA
        checkRsaCompatible();

        if (includePrivateParameters) {
            return new KeyPair(getRsaPublicKey(provider), getRsaPrivateKey(provider));
        } else {
            return new KeyPair(getRsaPublicKey(provider), null);
        }
    }

    /**
     * Converts JSON web key to EC key pair and include the private key if set to
     * true.
     *
     * @return EC key pair
     */
    public KeyPair toEc() {
        return toEc(false, null);
    }

    /**
     * Converts JSON web key to EC key pair and include the private key if set to
     * true.
     *
     * @param includePrivateParameters true if the EC key pair should include the private key. False otherwise.
     *
     * @return EC key pair
     */
    public KeyPair toEc(boolean includePrivateParameters) {
        return toEc(includePrivateParameters, null);
    }

    /**
     * Converts JSON web key to EC key pair and include the private key if set to
     * true.
     *
     * @param includePrivateParameters true if the EC key pair should include the private key. False otherwise.
     * @param provider The Java security provider
     *
     * @return EC key pair
     *
     * @throws IllegalArgumentException if the key type is not EC or EC HSM
     * @throws IllegalStateException if an instance of EC key pair cannot be generated
     */
    public KeyPair toEc(boolean includePrivateParameters, Provider provider) {

        if (provider == null) {
            // Our default provider for this class
            provider = Security.getProvider("SunEC");
        }

        if (!KeyType.EC.equals(keyType) && !KeyType.EC_HSM.equals(keyType)) {
            throw logger.logExceptionAsError(new IllegalArgumentException("Not an EC key."));
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
                realKeyPair = new KeyPair(getEcPublicKey(ecPoint, aspec, provider),
                    getEcPrivateKey(d, aspec, provider));
            } else {
                realKeyPair = new KeyPair(getEcPublicKey(ecPoint, aspec, provider), null);
            }

            return realKeyPair;
        } catch (GeneralSecurityException e) {
            throw logger.logExceptionAsError(new IllegalStateException(e));
        }
    }

    /**
     * Converts EC key pair to JSON web key.
     *
     * @param keyPair The EC key pair
     * @param provider The Java security provider
     *
     * @return the JSON web key, converted from EC key pair.
     */
    public static JsonWebKey fromEc(KeyPair keyPair, Provider provider) {

        ECPublicKey apub = (ECPublicKey) keyPair.getPublic();
        ECPoint point = apub.getW();
        ECPrivateKey apriv = (ECPrivateKey) keyPair.getPrivate();

        if (apriv != null) {
            return new JsonWebKey().setKeyType(KeyType.EC).setCurveName(getCurveFromKeyPair(keyPair, provider))
                .setX(point.getAffineX().toByteArray()).setY(point.getAffineY().toByteArray())
                .setD(apriv.getS().toByteArray()).setKeyType(KeyType.EC);
        } else {
            return new JsonWebKey().setKeyType(KeyType.EC).setCurveName(getCurveFromKeyPair(keyPair, provider))
                .setX(point.getAffineX().toByteArray()).setY(point.getAffineY().toByteArray())
                .setKeyType(KeyType.EC);
        }
    }

    /**
     * Converts EC key pair to JSON web key.
     *
     * @param keyPair The EC key pair
     * @param provider The Java security provider
     * @param keyOperations The key operations to set.
     *
     * @return the JSON web key, converted from EC key pair.
     */
    public static JsonWebKey fromEc(KeyPair keyPair, Provider provider, List<KeyOperation> keyOperations) {
        return fromEc(keyPair, provider).setKeyOps(keyOperations);
    }

    // Matches the curve of the keyPair to supported curves.
    private static KeyCurveName getCurveFromKeyPair(KeyPair keyPair, Provider provider) {

        try {
            ECPublicKey key = (ECPublicKey) keyPair.getPublic();
            ECParameterSpec spec = key.getParams();
            EllipticCurve crv = spec.getCurve();

            List<KeyCurveName> curveList = Arrays.asList(KeyCurveName.P_256, KeyCurveName.P_384,
                KeyCurveName.P_521, KeyCurveName.P_256K);

            for (KeyCurveName curve : curveList) {
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
     * @param secretKey The AES key
     *
     * @return the JSON web key, converted from AES key.
     */
    public static JsonWebKey fromAes(SecretKey secretKey) {
        if (secretKey == null) {
            return null;
        }

        return new JsonWebKey().setK(secretKey.getEncoded()).setKeyType(KeyType.OCT);
    }

    /**
     * Converts AES key to JSON web key.
     *
     * @param secretKey The AES key
     * @param keyOperations The key operations to set
     *
     * @return the JSON web key, converted from AES key.
     */
    public static JsonWebKey fromAes(SecretKey secretKey, List<KeyOperation> keyOperations) {
        return fromAes(secretKey).setKeyOps(keyOperations);
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
     * @param jwk The other {@link JsonWebKey} to compare with.
     *
     * @return true if this {@link JsonWebKey} is the same as the jwk argument;
     * false otherwise.
     */
    public boolean equals(JsonWebKey jwk) {
        if (jwk == null) {
            return false;
        }

        if (!objectEquals(keyId, jwk.keyId)) {
            return false;
        }

        if (!objectEquals(keyType, jwk.keyType)) {
            return false;
        }

        if (!objectEquals(keyOps, jwk.keyOps)) {
            return false;
        }

        if (!objectEquals(crv, jwk.crv)) {
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

        if (KeyType.OCT.equals(keyType)) {
            return k != null;
        } else if (KeyType.RSA.equals(keyType) || KeyType.RSA_HSM.equals(keyType)) {
            return (d != null && dp != null && dq != null && qi != null && p != null && q != null);
        } else if (KeyType.EC.equals(keyType) || KeyType.EC_HSM.equals(keyType)) {
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
        if (keyType == null) {
            return false;
        }

        if (keyOps != null) {
            final Set<KeyOperation> set =
                new HashSet<KeyOperation>(KeyOperation.values());
            for (int i = 0; i < keyOps.size(); i++) {
                if (!set.contains(keyOps.get(i))) {
                    return false;
                }
            }
        }

        if (KeyType.OCT.equals(keyType) || KeyType.OCT_HSM.equals(keyType)) {
            return isValidOctet();
        } else if (KeyType.RSA.equals(keyType)) {
            return isValidRsa();
        } else if (KeyType.RSA_HSM.equals(keyType)) {
            return isValidRsaHsm();
        } else if (KeyType.EC.equals(keyType)) {
            return isValidEc();
        } else if (KeyType.EC_HSM.equals(keyType)) {
            return isValidEcHsm();
        }

        return false;
    }

    private boolean isValidOctet() {
        return k != null;
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

        // MUST have ( T || ( n && E ) )
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
        if (keyId != null) {
            hashCode += keyId.hashCode();
        }

        if (KeyType.OCT.equals(keyType)) {
            hashCode += hashCode(k);
        } else if (KeyType.RSA.equals(keyType)) {
            hashCode += hashCode(n);
        } else if (KeyType.EC.equals(keyType)) {
            hashCode += hashCode(x);
            hashCode += hashCode(y);
            hashCode += crv.hashCode();
        } else if (KeyType.RSA_HSM.equals(keyType) || KeyType.EC_HSM.equals(keyType)) {
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

    private static final Map<KeyCurveName, String> CURVE_TO_SPEC_NAME = setupCurveToSpecMap();

    private static Map<KeyCurveName, String> setupCurveToSpecMap() {
        Map<KeyCurveName, String> curveToSpecMap = new HashMap<>();
        curveToSpecMap.put(KeyCurveName.P_256, "secp256r1");
        curveToSpecMap.put(KeyCurveName.P_384, "secp384r1");
        curveToSpecMap.put(KeyCurveName.P_521, "secp521r1");
        curveToSpecMap.put(KeyCurveName.P_256K, "secp256k1");
        return curveToSpecMap;
    }

    private boolean objectEquals(Object a, Object b) {
        if (a == null && b == null) {
            return true;
        } else if (a != null && b != null) {
            return a.equals(b);
        } else {
            return false;
        }
    }
}

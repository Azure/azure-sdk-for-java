// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.keyvault.certificates.models.webkey;

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
    private String keyId;

    /**
     * JsonWebKey key type (keyType). Possible values include: 'EC', 'EC-HSM', 'RSA',
     * 'RSA-HSM', 'oct'.
     */
    @JsonProperty(value = "kty")
    private JsonWebKeyType keyType;

    /**
     * The keyOps property.
     */
    @JsonProperty(value = "key_ops")
    private List<JsonWebKeyOperation> keyOps;

    /**
     * RSA modulus.
     */
    @JsonProperty(value = "n")
    private byte[] rsaModulus;

    /**
     * RSA public exponent.
     */
    @JsonProperty(value = "e")
    private byte[] rsaExponent;

    /**
     * RSA private exponent, or the D component of an EC private key.
     */
    @JsonProperty(value = "d")
    private byte[] rsaPrivateExponent;

    /**
     * RSA Private Key Parameter.
     */
    @JsonProperty(value = "dp")
    private byte[] rsaPrivateKeyParameterDp;

    /**
     * RSA Private Key Parameter.
     */
    @JsonProperty(value = "dq")
    private byte[] rsaPrivateKeyParameterDq;

    /**
     * RSA Private Key Parameter.
     */
    @JsonProperty(value = "qi")
    private byte[] rsaPrivateKeyParameterQi;

    /**
     * RSA secret prime.
     */
    @JsonProperty(value = "p")
    private byte[] rsaSecretPrime;

    /**
     * RSA secret prime, with rsaSecretPrime & rsaSecretPrimeBounded.
     */
    @JsonProperty(value = "q")
    private byte[] rsaSecretPrimeBounded;

    /**
     * Symmetric key.
     */
    @JsonProperty(value = "k")
    private byte[] symmetricKey;

    /**
     * HSM Token, used with Bring Your Own Key.
     */
    @JsonProperty(value = "key_hsm")
    private byte[] keyHsm;

    /**
     * Elliptic curve name. For valid values, see JsonWebKeyCurveName. Possible
     * values include: 'P-256', 'P-384', 'P-521', 'SECP256K1'.
     */
    @JsonProperty(value = "crv")
    private JsonWebKeyCurveName curve;

    /**
     * X component of an EC public key.
     */
    @JsonProperty(value = "x")
    private byte[] ecPublicKeyXComponent;

    /**
     * Y component of an EC public key.
     */
    @JsonProperty(value = "ecPublicKeyYComponent")
    private byte[] ecPublicKeyYComponent;

    /**
     * Get the keyId value.
     *
     * @return the keyId value
     */
    @JsonProperty("kid")
    public String keyId() {
        return this.keyId;
    }

    /**
     * Set the key identifier value.
     *
     * @param keyId The keyId value to set
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey keyId(String keyId) {
        this.keyId = keyId;
        return this;
    }

    /**
     * Get the keyType value.
     *
     * @return the keyType value
     */
    @JsonProperty("kty")
    public JsonWebKeyType keyType() {
        return this.keyType;
    }

    /**
     * Set the key type value.
     *
     * @param keyType The key type
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey keyType(JsonWebKeyType keyType) {
        this.keyType = keyType;
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
     * @param keyOps The keyOps value to set
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey keyOps(List<JsonWebKeyOperation> keyOps) {
        this.keyOps = keyOps;
        return this;
    }

    /**
     * Get the rsaModulus value.
     *
     * @return the rsaModulus value
     */
    @JsonProperty("n")
    @JsonSerialize(using = Base64UrlJsonSerializer.class)
    @JsonDeserialize(using = Base64UrlJsonDeserializer.class)
    public byte[] rsaModulus() {
        return ByteExtensions.clone(this.rsaModulus);
    }

    /**
     * Set the rsaModulus value.
     *
     * @param rsaModulus The rsaModulus value to set
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey rsaModulus(byte[] rsaModulus) {
        this.rsaModulus = ByteExtensions.clone(rsaModulus);
        return this;
    }

    /**
     * Get the rsaExponent value.
     *
     * @return the rsaExponent value
     */
    @JsonProperty("e")
    @JsonSerialize(using = Base64UrlJsonSerializer.class)
    @JsonDeserialize(using = Base64UrlJsonDeserializer.class)
    public byte[] rsaExponent() {
        return ByteExtensions.clone(this.rsaExponent);
    }

    /**
     * Set the rsaExponent value.
     *
     * @param rsaExponent The rsaExponent value to set
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey rsaExponent(byte[] rsaExponent) {
        this.rsaExponent = ByteExtensions.clone(rsaExponent);
        return this;
    }

    /**
     * Get the rsaPrivateExponent value.
     *
     * @return the rsaPrivateExponent value
     */
    @JsonProperty("d")
    @JsonSerialize(using = Base64UrlJsonSerializer.class)
    @JsonDeserialize(using = Base64UrlJsonDeserializer.class)
    public byte[] rsaPrivateExponent() {
        return ByteExtensions.clone(this.rsaPrivateExponent);
    }

    /**
     * Set the rsaPrivateExponent value.
     *
     * @param rsaPrivateExponent The rsaPrivateExponent value to set
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey rsaPrivateExponent(byte[] rsaPrivateExponent) {
        this.rsaPrivateExponent = ByteExtensions.clone(rsaPrivateExponent);
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
    public byte[] rsaPrivateKeyParameterDp() {
        return ByteExtensions.clone(this.rsaPrivateKeyParameterDp);
    }

    /**
     * Set RSA Private Key Parameter value.
     * 
     * @param dp The RSA Private Key Parameter value to set.
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey rsaPrivateKeyParameterDp(byte[] dp) {
        this.rsaPrivateKeyParameterDp = ByteExtensions.clone(dp);
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
    public byte[] rsaPrivateKeyParameterDq() {
        return ByteExtensions.clone(this.rsaPrivateKeyParameterDq);
    }

    /**
     * Set RSA Private Key Parameter value .
     * 
     * @param dq The RSA Private Key Parameter value to set.
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey rsaPrivateKeyParameterDq(byte[] dq) {
        this.rsaPrivateKeyParameterDq = ByteExtensions.clone(dq);
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
    public byte[] rsaPrivateKeyParameterQi() {
        return ByteExtensions.clone(this.rsaPrivateKeyParameterQi);
    }

    /**
     * Set RSA Private Key Parameter value.
     * 
     * @param qi The RSA Private Key Parameter value to set.
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey rsaPrivateKeyParameterQi(byte[] qi) {
        this.rsaPrivateKeyParameterQi = ByteExtensions.clone(qi);
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
    public byte[] rsaSecretPrime() {
        return ByteExtensions.clone(this.rsaSecretPrime);
    }

    /**
     * Set the RSA secret prime value.
     * 
     * @param rsaSecretPrime The RSA secret prime value.
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey rsaSecretPrime(byte[] rsaSecretPrime) {
        this.rsaSecretPrime = ByteExtensions.clone(rsaSecretPrime);
        return this;
    }

    /**
     * Get RSA secret prime, with rsaSecretPrime &lt; rsaSecretPrimeBounded value.
     * 
     * @return the RSA secret prime, with rsaSecretPrime &lt; rsaSecretPrimeBounded value.
     */
    @JsonProperty("q")
    @JsonSerialize(using = Base64UrlJsonSerializer.class)
    @JsonDeserialize(using = Base64UrlJsonDeserializer.class)
    public byte[] rsaSecretPrimeBounded() {
        return ByteExtensions.clone(this.rsaSecretPrimeBounded);
    }

    /**
     * Set the RSA secret prime, with rsaSecretPrime &lt; rsaSecretPrimeBounded value.
     * 
     * @param rsaSecretPrimeBounded The RSA secret prime, with rsaSecretPrime &lt; rsaSecretPrimeBounded value to be set.
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey rsaSecretPrimeBounded(byte[] rsaSecretPrimeBounded) {
        this.rsaSecretPrimeBounded = ByteExtensions.clone(rsaSecretPrimeBounded);
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
    public byte[] symmetricKey() {
        return ByteExtensions.clone(this.symmetricKey);
    }

    /**
     * Set the Symmetric key value.
     * 
     * @param symmetricKey The symmetric key value to set.
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey symmetricKey(byte[] symmetricKey) {
        this.symmetricKey = ByteExtensions.clone(symmetricKey);
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
    public byte[] keyHsm() {
        return ByteExtensions.clone(this.keyHsm);
    }

    /**
     * Set HSM Token value, used with Bring Your Own Key.
     * 
     * @param keyHsm The HSM Token value to set, used with Bring Your Own Key
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey keyHsm(byte[] keyHsm) {
        this.keyHsm = ByteExtensions.clone(keyHsm);
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
     * Get the curve value.
     *
     * @return the curve value
     */
    @JsonProperty("curve")
    public JsonWebKeyCurveName curve() {
        return this.curve;
    }

    /**
     * Set the curve value.
     *
     * @param curve The curve value to set
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey curve(JsonWebKeyCurveName curve) {
        this.curve = curve;
        return this;
    }

    /**
     * Get the ecPublicKeyXComponent value.
     *
     * @return the ecPublicKeyXComponent value
     */
    @JsonProperty("x")
    @JsonSerialize(using = Base64UrlJsonSerializer.class)
    @JsonDeserialize(using = Base64UrlJsonDeserializer.class)
    public byte[] ecPublicKeyXComponent() {
        return ByteExtensions.clone(this.ecPublicKeyXComponent);
    }

    /**
     * Set the ecPublicKeyXComponent value.
     *
     * @param xComponent The ecPublicKeyXComponent value to set
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey ecPublicKeyXComponent(byte[] xComponent) {
        this.ecPublicKeyXComponent = ByteExtensions.clone(xComponent);
        return this;
    }

    /**
     * Get the ecPublicKeyYComponent value.
     *
     * @return the ecPublicKeyYComponent value
     */
    @JsonProperty("y")
    @JsonSerialize(using = Base64UrlJsonSerializer.class)
    @JsonDeserialize(using = Base64UrlJsonDeserializer.class)
    public byte[] ecPublicKeyYComponent() {
        return ByteExtensions.clone(this.ecPublicKeyYComponent);
    }

    /**
     * Set the ecPublicKeyYComponent value.
     *
     * @param yComponent The ecPublicKeyYComponent value to set
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey ecPublicKeyYComponent(byte[] yComponent) {
        this.ecPublicKeyYComponent = ByteExtensions.clone(yComponent);
        return this;
    }

    /**
     * Get the RSA public key spec value.
     *
     * @return the RSA public key spec value
     */
    private RSAPublicKeySpec getRSAPublicKeySpec() {

        return new RSAPublicKeySpec(toBigInteger(rsaModulus), toBigInteger(rsaExponent));
    }

    /**
     * Get the RSA private key spec value.
     *
     * @return the RSA private key spec value
     */
    private RSAPrivateKeySpec getRSAPrivateKeySpec() {

        return new RSAPrivateCrtKeySpec(toBigInteger(rsaModulus), toBigInteger(rsaExponent), toBigInteger(rsaPrivateExponent), toBigInteger(rsaSecretPrime),
                toBigInteger(rsaSecretPrimeBounded), toBigInteger(rsaPrivateKeyParameterDp), toBigInteger(rsaPrivateKeyParameterDq), toBigInteger(rsaPrivateKeyParameterQi));
    }

    /**
     * Get the RSA public key value.
     *
     * @param provider The Java security provider.
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
     * @param provider The Java security provider.
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
        if (!JsonWebKeyType.RSA.equals(keyType) && !JsonWebKeyType.RSA_HSM.equals(keyType)) {
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
     * @param keyPair The RSA key pair
     * @return the JSON web key, converted from RSA key pair.
     */
    public static JsonWebKey fromRSA(KeyPair keyPair) {

        RSAPrivateCrtKey privateKey = (RSAPrivateCrtKey) keyPair.getPrivate();
        JsonWebKey key = null;

        if (privateKey != null) {

            key = new JsonWebKey().keyType(JsonWebKeyType.RSA).rsaModulus(toByteArray(privateKey.getModulus()))
                    .rsaExponent(toByteArray(privateKey.getPublicExponent()))
                    .rsaPrivateExponent(toByteArray(privateKey.getPrivateExponent())).rsaSecretPrime(toByteArray(privateKey.getPrimeP()))
                    .rsaSecretPrimeBounded(toByteArray(privateKey.getPrimeQ())).rsaPrivateKeyParameterDp(toByteArray(privateKey.getPrimeExponentP()))
                    .rsaPrivateKeyParameterDq(toByteArray(privateKey.getPrimeExponentQ()))
                    .rsaPrivateKeyParameterQi(toByteArray(privateKey.getCrtCoefficient()));
        } else {

            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

            key = new JsonWebKey().keyType(JsonWebKeyType.RSA).rsaModulus(toByteArray(publicKey.getModulus()))
                    .rsaExponent(toByteArray(publicKey.getPublicExponent())).rsaPrivateExponent(null).rsaSecretPrime(null).rsaSecretPrimeBounded(null).rsaPrivateKeyParameterDp(null)
                    .rsaPrivateKeyParameterDq(null).rsaPrivateKeyParameterQi(null);
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
     * @param includePrivateParameters true if the RSA key pair should include the private key. False otherwise.
     * @return RSA key pair
     */
    public KeyPair toRSA(boolean includePrivateParameters) {
        return toRSA(includePrivateParameters, null);
    }

    /**
     * Converts JSON web key to RSA key pair and include the private key if set to
     * true.
     * 
     * @param provider The Java security provider.
     * @param includePrivateParameters  true if the RSA key pair should include the private key. False otherwise.
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
     * @param includePrivateParameters true if the EC key pair should include the private key. False otherwise.
     * @return EC key pair
     */
    public KeyPair toEC(boolean includePrivateParameters) {
        return toEC(includePrivateParameters, null);
    }

    /**
     * Converts JSON web key to EC key pair and include the private key if set to
     * true.
     * 
     * @param includePrivateParameters true if the EC key pair should include the private key. False otherwise.
     * @param provider The Java security provider
     * @return EC key pair
     */
    public KeyPair toEC(boolean includePrivateParameters, Provider provider) {

        if (provider == null) {
            // Our default provider for this class
            provider = Security.getProvider("SunEC");
        }

        if (!JsonWebKeyType.EC.equals(keyType) && !JsonWebKeyType.EC_HSM.equals(keyType)) {
            throw new IllegalArgumentException("Not an EC key.");
        }

        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC", provider);

            ECGenParameterSpec gps = new ECGenParameterSpec(CURVE_TO_SPEC_NAME.get(curve));
            kpg.initialize(gps);

            // Generate dummy keypair to get parameter spec.
            KeyPair apair = kpg.generateKeyPair();
            ECPublicKey apub = (ECPublicKey) apair.getPublic();
            ECParameterSpec aspec = apub.getParams();

            ECPoint ecPoint = new ECPoint(new BigInteger(1, ecPublicKeyXComponent), new BigInteger(1, ecPublicKeyYComponent));

            KeyPair realKeyPair;

            if (includePrivateParameters) {
                realKeyPair = new KeyPair(getECPublicKey(ecPoint, aspec, provider),
                        getECPrivateKey(rsaPrivateExponent, aspec, provider));
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
     * @param keyPair The EC key pair
     * @param provider The Java security provider
     * @return the JSON web key, converted from EC key pair.
     */
    public static JsonWebKey fromEC(KeyPair keyPair, Provider provider) {

        ECPublicKey apub = (ECPublicKey) keyPair.getPublic();
        ECPoint point = apub.getW();
        ECPrivateKey apriv = (ECPrivateKey) keyPair.getPrivate();

        if (apriv != null) {
            return new JsonWebKey().keyType(JsonWebKeyType.EC).curve(getCurveFromKeyPair(keyPair, provider))
                    .ecPublicKeyXComponent(point.getAffineX().toByteArray()).ecPublicKeyYComponent(point.getAffineY().toByteArray())
                    .rsaPrivateExponent(apriv.getS().toByteArray()).keyType(JsonWebKeyType.EC);
        } else {
            return new JsonWebKey().keyType(JsonWebKeyType.EC).curve(getCurveFromKeyPair(keyPair, provider))
                    .ecPublicKeyXComponent(point.getAffineX().toByteArray()).ecPublicKeyYComponent(point.getAffineY().toByteArray())
                    .keyType(JsonWebKeyType.EC);
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
     * @param secretKey The AES key
     * @return the JSON web key, converted from AES key.
     */
    public static JsonWebKey fromAes(SecretKey secretKey) {
        if (secretKey == null) {
            return null;
        }

        return new JsonWebKey().symmetricKey(secretKey.getEncoded()).keyType(JsonWebKeyType.OCT);
    }

    /**
     * Converts JSON web key to AES key.
     * 
     * @return AES key
     */
    public SecretKey toAes() {
        if (symmetricKey == null) {
            return null;
        }

        SecretKey secretKey = new SecretKeySpec(symmetricKey, "AES");
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
     * @return true if this {@link JsonWebKey} is the same as the jwk argument; false otherwise.
     */
    public boolean equals(JsonWebKey jwk) {
        if (jwk == null) {
            return false;
        }

        if (!Objects.equal(keyId, jwk.keyId)) {
            return false;
        }

        if (!Objects.equal(keyType, jwk.keyType)) {
            return false;
        }

        if (!Objects.equal(keyOps, jwk.keyOps)) {
            return false;
        }

        if (!Objects.equal(curve, jwk.curve)) {
            return false;
        }

        if (!Arrays.equals(symmetricKey, jwk.symmetricKey)) {
            return false;
        }

        // Public parameters
        if (!Arrays.equals(rsaModulus, jwk.rsaModulus)) {
            return false;
        }
        if (!Arrays.equals(rsaExponent, jwk.rsaExponent)) {
            return false;
        }

        // Private parameters
        if (!Arrays.equals(rsaPrivateExponent, jwk.rsaPrivateExponent)) {
            return false;
        }
        if (!Arrays.equals(rsaPrivateKeyParameterDp, jwk.rsaPrivateKeyParameterDp)) {
            return false;
        }
        if (!Arrays.equals(rsaPrivateKeyParameterDq, jwk.rsaPrivateKeyParameterDq)) {
            return false;
        }
        if (!Arrays.equals(rsaPrivateKeyParameterQi, jwk.rsaPrivateKeyParameterQi)) {
            return false;
        }
        if (!Arrays.equals(rsaSecretPrime, jwk.rsaSecretPrime)) {
            return false;
        }
        if (!Arrays.equals(rsaSecretPrimeBounded, jwk.rsaSecretPrimeBounded)) {
            return false;
        }
        if (!Arrays.equals(ecPublicKeyXComponent, jwk.ecPublicKeyXComponent)) {
            return false;
        }
        if (!Arrays.equals(ecPublicKeyYComponent, jwk.ecPublicKeyYComponent)) {
            return false;
        }

        // HSM token
        if (!Arrays.equals(keyHsm, jwk.keyHsm)) {
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

        if (JsonWebKeyType.OCT.equals(keyType)) {
            return symmetricKey != null;
        } else if (JsonWebKeyType.RSA.equals(keyType) || JsonWebKeyType.RSA_HSM.equals(keyType)) {
            return (rsaPrivateExponent != null && rsaPrivateKeyParameterDp != null && rsaPrivateKeyParameterDq != null && rsaPrivateKeyParameterQi != null && rsaSecretPrime != null && rsaSecretPrimeBounded != null);
        } else if (JsonWebKeyType.EC.equals(keyType) || JsonWebKeyType.EC_HSM.equals(keyType)) {
            return (rsaPrivateExponent != null);
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
            final Set<JsonWebKeyOperation> set = new HashSet<JsonWebKeyOperation>(JsonWebKeyOperation.ALL_OPERATIONS);
            for (int i = 0; i < keyOps.size(); i++) {
                if (!set.contains(keyOps.get(i))) {
                    return false;
                }
            }
        }

        if (JsonWebKeyType.OCT.equals(keyType)) {
            return isValidOctet();
        } else if (JsonWebKeyType.RSA.equals(keyType)) {
            return isValidRsa();
        } else if (JsonWebKeyType.RSA_HSM.equals(keyType)) {
            return isValidRsaHsm();
        } else if (JsonWebKeyType.EC.equals(keyType)) {
            return isValidEc();
        } else if (JsonWebKeyType.EC_HSM.equals(keyType)) {
            return isValidEcHsm();
        }

        return false;
    }

    private boolean isValidOctet() {
        if (symmetricKey != null) {
            return true;
        }
        return false;
    }

    private boolean isValidRsa() {
        if (rsaModulus == null || rsaExponent == null) {
            return false;
        }

        return hasPrivateKey() || (rsaPrivateExponent == null && rsaPrivateKeyParameterDp == null && rsaPrivateKeyParameterDq == null && rsaPrivateKeyParameterQi == null && rsaSecretPrime == null && rsaSecretPrimeBounded == null);
    }

    private boolean isValidRsaHsm() {
        // MAY have public key parameters
        if ((rsaModulus == null && rsaExponent != null) || (rsaModulus != null && rsaExponent == null)) {
            return false;
        }

        // no private key
        if (hasPrivateKey()) {
            return false;
        }

        // MUST have ( T || ( N && E ) )
        boolean tokenParameters = keyHsm != null;
        boolean publicParameters = (rsaModulus != null && rsaExponent != null);

        if (tokenParameters && publicParameters) {
            return false;
        }

        return (tokenParameters || publicParameters);
    }

    private boolean isValidEc() {
        boolean ecPointParameters = (ecPublicKeyXComponent != null && ecPublicKeyYComponent != null);
        if (!ecPointParameters || curve == null) {
            return false;
        }

        return hasPrivateKey() || (rsaPrivateExponent == null);
    }

    private boolean isValidEcHsm() {
        // MAY have public key parameters
        boolean ecPointParameters = (ecPublicKeyXComponent != null && ecPublicKeyYComponent != null);
        if ((ecPointParameters && curve == null) || (!ecPointParameters && curve != null)) {
            return false;
        }

        // no private key
        if (hasPrivateKey()) {
            return false;
        }

        // MUST have (T || (ecPointParameters && curve))
        boolean publicParameters = (ecPointParameters && curve != null);
        boolean tokenParameters = keyHsm != null;

        if (tokenParameters && publicParameters) {
            return false;
        }

        return (tokenParameters || publicParameters);
    }

    /**
     * Clear key materials.
     */
    public void clearMemory() {
        zeroArray(symmetricKey);
        symmetricKey = null;
        zeroArray(rsaModulus);
        rsaModulus = null;
        zeroArray(rsaExponent);
        rsaExponent = null;
        zeroArray(rsaPrivateExponent);
        rsaPrivateExponent = null;
        zeroArray(rsaPrivateKeyParameterDp);
        rsaPrivateKeyParameterDp = null;
        zeroArray(rsaPrivateKeyParameterDq);
        rsaPrivateKeyParameterDq = null;
        zeroArray(rsaPrivateKeyParameterQi);
        rsaPrivateKeyParameterQi = null;
        zeroArray(rsaSecretPrime);
        rsaSecretPrime = null;
        zeroArray(rsaSecretPrimeBounded);
        rsaSecretPrimeBounded = null;
        zeroArray(keyHsm);
        keyHsm = null;
        zeroArray(ecPublicKeyXComponent);
        ecPublicKeyXComponent = null;
        zeroArray(ecPublicKeyYComponent);
        ecPublicKeyYComponent = null;
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

        if (JsonWebKeyType.OCT.equals(keyType)) {
            hashCode += hashCode(symmetricKey);
        } else if (JsonWebKeyType.RSA.equals(keyType)) {
            hashCode += hashCode(rsaModulus);
        } else if (JsonWebKeyType.EC.equals(keyType)) {
            hashCode += hashCode(ecPublicKeyXComponent);
            hashCode += hashCode(ecPublicKeyYComponent);
            hashCode += curve.hashCode();
        } else if (JsonWebKeyType.RSA_HSM.equals(keyType) || JsonWebKeyType.EC_HSM.equals(keyType)) {
            hashCode += hashCode(keyHsm);
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

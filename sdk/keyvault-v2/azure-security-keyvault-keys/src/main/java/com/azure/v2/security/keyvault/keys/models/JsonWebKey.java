// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.keys.models;

import com.azure.v2.security.keyvault.keys.implementation.KeyVaultKeysUtils;
import io.clientcore.core.annotations.Metadata;
import io.clientcore.core.annotations.MetadataProperties;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.models.CoreException;
import io.clientcore.core.serialization.json.JsonReader;
import io.clientcore.core.serialization.json.JsonSerializable;
import io.clientcore.core.serialization.json.JsonToken;
import io.clientcore.core.serialization.json.JsonWriter;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.math.BigInteger;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * As of http://tools.ietf.org/html/draft-ietf-jose-json-web-key-18.
 */
@Metadata(properties = { MetadataProperties.FLUENT })
public class JsonWebKey implements JsonSerializable<JsonWebKey> {
    private static final ClientLogger LOGGER = new ClientLogger(JsonWebKey.class);

    /**
     * Key Identifier.
     */
    private String keyId;

    /**
     * JsonWebKey key type (kty). Possible values include: 'EC', 'EC-HSM', 'RSA',
     * 'RSA-HSM', 'oct', and 'oct-HSM'.
     */
    private KeyType keyType;

    /**
     * The keyOps property.
     */
    private List<KeyOperation> keyOps;

    /**
     * RSA modulus.
     */
    private byte[] n;

    /**
     * RSA public exponent.
     */
    private byte[] e;

    /**
     * RSA private exponent, or the D component of an EC private key.
     */
    private byte[] d;

    /**
     * RSA Private Key Parameter.
     */
    private byte[] dp;

    /**
     * RSA Private Key Parameter.
     */
    private byte[] dq;

    /**
     * RSA Private Key Parameter.
     */
    private byte[] qi;

    /**
     * RSA secret prime.
     */
    private byte[] p;

    /**
     * RSA secret prime, with p & q.
     */
    private byte[] q;

    /**
     * Symmetric key.
     */
    private byte[] k;

    /**
     * HSM Token, used with Bring Your Own Key.
     */
    private byte[] t;

    /**
     * Elliptic curve name. For valid values, see KeyCurveName. Possible
     * values include: 'P-256', 'P-384', 'P-521', and 'SECP256K1'.
     */
    private KeyCurveName crv;

    /**
     * X component of an EC public key.
     */
    private byte[] x;

    /**
     * Y component of an EC public key.
     */
    private byte[] y;

    /**
     * Creates a new instance of {@link JsonWebKey}.
     */
    public JsonWebKey() {
    }

    /**
     * Get the kid value.
     *
     * @return the kid value
     */
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
    public List<KeyOperation> getKeyOps() {
        return this.keyOps == null
            ? Collections.unmodifiableList(new ArrayList<KeyOperation>())
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
    public byte[] getN() {
        return arrayCopy(this.n);
    }

    /**
     * Set the n value.
     *
     * @param n The n value to set
     *
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey setN(byte[] n) {
        this.n = arrayCopy(n);
        return this;
    }

    /**
     * Get the e value.
     *
     * @return the e value
     */
    public byte[] getE() {
        return arrayCopy(this.e);
    }

    /**
     * Set the e value.
     *
     * @param e The e value to set
     *
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey setE(byte[] e) {
        this.e = arrayCopy(e);
        return this;
    }

    /**
     * Get the d value.
     *
     * @return the d value
     */
    public byte[] getD() {
        return arrayCopy(this.d);
    }

    /**
     * Set the d value.
     *
     * @param d The d value to set
     *
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey setD(byte[] d) {
        this.d = arrayCopy(d);
        return this;
    }

    /**
     * Get the RSA Private Key Parameter value.
     *
     * @return the RSA Private Key Parameter value.
     */
    public byte[] getDp() {
        return arrayCopy(this.dp);
    }

    /**
     * Set RSA Private Key Parameter value.
     *
     * @param dp The RSA Private Key Parameter value to set.
     *
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey setDp(byte[] dp) {
        this.dp = arrayCopy(dp);
        return this;
    }

    /**
     * Get the RSA Private Key Parameter value.
     *
     * @return the RSA Private Key Parameter value.
     */
    public byte[] getDq() {
        return arrayCopy(this.dq);
    }

    /**
     * Set RSA Private Key Parameter value .
     *
     * @param dq The RSA Private Key Parameter value to set.
     *
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey setDq(byte[] dq) {
        this.dq = arrayCopy(dq);
        return this;
    }

    /**
     * Get the RSA Private Key Parameter value.
     *
     * @return the RSA Private Key Parameter value.
     */
    public byte[] getQi() {
        return arrayCopy(this.qi);
    }

    /**
     * Set RSA Private Key Parameter value.
     *
     * @param qi The RSA Private Key Parameter value to set.
     *
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey setQi(byte[] qi) {
        this.qi = arrayCopy(qi);
        return this;
    }

    /**
     * Get the RSA secret prime value.
     *
     * @return the RSA secret prime value.
     */
    public byte[] getP() {
        return arrayCopy(this.p);
    }

    /**
     * Set the RSA secret prime value.
     *
     * @param p The RSA secret prime value.
     *
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey setP(byte[] p) {
        this.p = arrayCopy(p);
        return this;
    }

    /**
     * Get RSA secret prime, with p &lt; q value.
     *
     * @return the RSA secret prime, with p &lt; q value.
     */
    public byte[] getQ() {
        return arrayCopy(this.q);
    }

    /**
     * Set the RSA secret prime, with p &lt; q value.
     *
     * @param q The the RSA secret prime, with p &lt; q value to be set.
     *
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey setQ(byte[] q) {
        this.q = arrayCopy(q);
        return this;
    }

    /**
     * Get Symmetric key value.
     *
     * @return the symmetric key value.
     */
    public byte[] getK() {
        return arrayCopy(this.k);
    }

    /**
     * Set the Symmetric key value.
     *
     * @param k The symmetric key value to set.
     *
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey setK(byte[] k) {
        this.k = arrayCopy(k);
        return this;
    }

    /**
     * Get HSM Token value, used with Bring Your Own Key.
     *
     * @return HSM Token, used with Bring Your Own Key.
     */
    public byte[] getT() {
        return arrayCopy(this.t);
    }

    /**
     * Set HSM Token value, used with Bring Your Own Key.
     *
     * @param t The HSM Token value to set, used with Bring Your Own Key
     *
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey setT(byte[] t) {
        this.t = arrayCopy(t);
        return this;
    }

    // TODO (vcolin7): Figure out how to print this as a JSON string with the new clientcore.
    /*@Override
    public String toString() {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            JsonWriter writer = new JsonProviders.createWriter(outputStream)) {
            this.toJson(writer).flush();
    
            return outputStream.toString(StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw LOGGER.logThrowableAsError(new IllegalStateException(e));
        }
    }*/

    /**
     * Get the crv value.
     *
     * @return the crv value
     */
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
    public byte[] getX() {
        return arrayCopy(this.x);
    }

    /**
     * Set the x value.
     *
     * @param x The x value to set
     *
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey setX(byte[] x) {
        this.x = arrayCopy(x);
        return this;
    }

    /**
     * Get the y value.
     *
     * @return the y value
     */
    public byte[] getY() {
        return arrayCopy(this.y);
    }

    /**
     * Set the y value.
     *
     * @param y The y value to set
     *
     * @return the JsonWebKey object itself.
     */
    public JsonWebKey setY(byte[] y) {
        this.y = arrayCopy(y);
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
            KeyFactory factory
                = provider != null ? KeyFactory.getInstance("RSA", provider) : KeyFactory.getInstance("RSA");

            return factory.generatePublic(publicKeySpec);
        } catch (GeneralSecurityException e) {
            throw LOGGER.throwableAtError().log(e, CoreException::from);
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
            KeyFactory factory
                = provider != null ? KeyFactory.getInstance("RSA", provider) : KeyFactory.getInstance("RSA");

            return factory.generatePrivate(privateKeySpec);
        } catch (GeneralSecurityException e) {
            throw LOGGER.throwableAtError().log(e, CoreException::from);
        }
    }

    private static PublicKey getEcPublicKey(ECPoint ecPoint, ECParameterSpec curveSpec, Provider provider) {
        // Create public key spec with given point
        try {
            ECPublicKeySpec pubSpec = new ECPublicKeySpec(ecPoint, curveSpec);
            KeyFactory kf
                = provider != null ? KeyFactory.getInstance("EC", provider) : KeyFactory.getInstance("EC", "SunEC");
            return (ECPublicKey) kf.generatePublic(pubSpec);
        } catch (GeneralSecurityException e) {
            throw LOGGER.throwableAtError().log(e, CoreException::from);
        }
    }

    private static PrivateKey getEcPrivateKey(byte[] d, ECParameterSpec curveSpec, Provider provider) {
        try {
            ECPrivateKeySpec priSpec = new ECPrivateKeySpec(new BigInteger(1, d), curveSpec);
            KeyFactory kf
                = provider != null ? KeyFactory.getInstance("EC", provider) : KeyFactory.getInstance("EC", "SunEC");
            return (ECPrivateKey) kf.generatePrivate(priSpec);
        } catch (GeneralSecurityException e) {
            throw LOGGER.throwableAtError().log(e, CoreException::from);
        }
    }

    /**
     * Verifies if the key is an RSA key.
     */
    private void checkRsaCompatible() {
        if (!KeyType.RSA.equals(keyType) && !KeyType.RSA_HSM.equals(keyType)) {
            throw LOGGER.throwableAtError()
                .addKeyValue("keyType", keyType.getValue())
                .log("Not an RSA key", UnsupportedOperationException::new);
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

            key = new JsonWebKey().setKeyType(KeyType.RSA)
                .setN(toByteArray(privateKey.getModulus()))
                .setE(toByteArray(privateKey.getPublicExponent()))
                .setD(toByteArray(privateKey.getPrivateExponent()))
                .setP(toByteArray(privateKey.getPrimeP()))
                .setQ(toByteArray(privateKey.getPrimeQ()))
                .setDp(toByteArray(privateKey.getPrimeExponentP()))
                .setDq(toByteArray(privateKey.getPrimeExponentQ()))
                .setQi(toByteArray(privateKey.getCrtCoefficient()));
        } else {

            RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();

            key = new JsonWebKey().setKeyType(KeyType.RSA)
                .setN(toByteArray(publicKey.getModulus()))
                .setE(toByteArray(publicKey.getPublicExponent()))
                .setD(null)
                .setP(null)
                .setQ(null)
                .setDp(null)
                .setDq(null)
                .setQi(null);
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
        if (!KeyType.EC.equals(keyType) && !KeyType.EC_HSM.equals(keyType)) {
            throw LOGGER.throwableAtError()
                .addKeyValue("keyType", keyType.getValue())
                .log("Not an EC key.", IllegalArgumentException::new);
        }

        if (provider == null) {
            // Our default provider for this class.
            provider = Security.getProvider("SunEC");
        }

        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC", provider);

            ECGenParameterSpec gps = new ECGenParameterSpec(getCurveSpecName(crv));
            kpg.initialize(gps);

            // Generate dummy keypair to get parameter spec.
            KeyPair keyPair = kpg.generateKeyPair();
            ECPublicKey publicKey = (ECPublicKey) keyPair.getPublic();
            ECParameterSpec ecParameterSpec = publicKey.getParams();

            ECPoint ecPoint = new ECPoint(new BigInteger(1, x), new BigInteger(1, y));

            KeyPair realKeyPair;

            if (includePrivateParameters) {
                realKeyPair = new KeyPair(getEcPublicKey(ecPoint, ecParameterSpec, provider),
                    getEcPrivateKey(d, ecParameterSpec, provider));
            } else {
                realKeyPair = new KeyPair(getEcPublicKey(ecPoint, ecParameterSpec, provider), null);
            }

            return realKeyPair;
        } catch (GeneralSecurityException e) {
            throw LOGGER.throwableAtError().log(e, CoreException::from);
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
        ECPublicKey publicKey = (ECPublicKey) keyPair.getPublic();
        JsonWebKey jsonWebKey = new JsonWebKey().setKeyType(KeyType.EC)
            .setCurveName(getCurveFromKeyPair(keyPair, provider))
            .setX(publicKey.getW().getAffineX().toByteArray())
            .setY(publicKey.getW().getAffineY().toByteArray())
            .setKeyType(KeyType.EC);
        ECPrivateKey ecPrivateKey = (ECPrivateKey) keyPair.getPrivate();

        if (ecPrivateKey != null) {
            jsonWebKey.setD(ecPrivateKey.getS().toByteArray());
        }

        return jsonWebKey;
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
            ECPublicKey publicKey = (ECPublicKey) keyPair.getPublic();
            EllipticCurve ellipticCurve = publicKey.getParams().getCurve();

            for (KeyCurveName curve : KNOWN_CURVE_NAMES) {
                ECGenParameterSpec genParameterSpec = new ECGenParameterSpec(getCurveSpecName(curve));
                KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("EC", provider);
                keyPairGenerator.initialize(genParameterSpec);

                // Generate dummy key pair to get parameter spec.
                KeyPair generatedKeyPair = keyPairGenerator.generateKeyPair();
                ECPublicKey generatedPublicKey = (ECPublicKey) generatedKeyPair.getPublic();
                EllipticCurve generatedEllipticCurve = generatedPublicKey.getParams().getCurve();

                // Matches the parameter spec
                if (ellipticCurve.equals(generatedEllipticCurve)) {
                    return curve;
                }
            }

            // Did not find a supported curve.
            throw LOGGER.throwableAtError().log("Unsupported curve.", IllegalArgumentException::new);
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException e) {
            throw LOGGER.throwableAtError().log(e, CoreException::from);
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

        return new SecretKeySpec(k, "AES");
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (obj instanceof JsonWebKey) {
            return this.equals((JsonWebKey) obj);
        }

        return false;
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

        return Objects.equals(keyId, jwk.keyId)
            && Objects.equals(keyType, jwk.keyType)
            && Objects.equals(keyOps, jwk.keyOps)
            && Objects.equals(crv, jwk.crv)
            && Arrays.equals(k, jwk.k)
            && Arrays.equals(n, jwk.n)
            && Arrays.equals(e, jwk.e)
            && Arrays.equals(d, jwk.d)
            && Arrays.equals(dp, jwk.dp)
            && Arrays.equals(dq, jwk.dq)
            && Arrays.equals(qi, jwk.qi)
            && Arrays.equals(p, jwk.p)
            && Arrays.equals(q, jwk.q)
            && Arrays.equals(x, jwk.x)
            && Arrays.equals(y, jwk.y)
            && Arrays.equals(t, jwk.t);
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
    public boolean isValid() {
        if (keyType == null) {
            return false;
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

        for (byte b : obj) {
            hashCode = (hashCode << 3) | (hashCode >> 29) ^ b;
        }
        return hashCode;
    }

    private static String getCurveSpecName(KeyCurveName curveName) {
        if (curveName == null) {
            return null;
        }

        if (curveName == KeyCurveName.P_256) {
            return "secp256r1";
        } else if (curveName == KeyCurveName.P_384) {
            return "secp384r1";
        } else if (curveName == KeyCurveName.P_521) {
            return "secp521r1";
        } else if (curveName == KeyCurveName.P_256K) {
            return "secp256k1";
        } else {
            return null;
        }
    }

    private static final List<KeyCurveName> KNOWN_CURVE_NAMES
        = Arrays.asList(KeyCurveName.P_256, KeyCurveName.P_384, KeyCurveName.P_521, KeyCurveName.P_256K);

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();

        jsonWriter.writeStringField("kid", keyId);
        jsonWriter.writeStringField("kty", Objects.toString(keyType, null));
        jsonWriter.writeArrayField("key_ops", keyOps, (writer, op) -> writer.writeString(Objects.toString(op, null)));
        jsonWriter.writeStringField("n", KeyVaultKeysUtils.base64UrlJsonSerialization(n));
        jsonWriter.writeStringField("e", KeyVaultKeysUtils.base64UrlJsonSerialization(e));
        jsonWriter.writeStringField("d", KeyVaultKeysUtils.base64UrlJsonSerialization(d));
        jsonWriter.writeStringField("dp", KeyVaultKeysUtils.base64UrlJsonSerialization(dp));
        jsonWriter.writeStringField("dq", KeyVaultKeysUtils.base64UrlJsonSerialization(dq));
        jsonWriter.writeStringField("qi", KeyVaultKeysUtils.base64UrlJsonSerialization(qi));
        jsonWriter.writeStringField("p", KeyVaultKeysUtils.base64UrlJsonSerialization(p));
        jsonWriter.writeStringField("q", KeyVaultKeysUtils.base64UrlJsonSerialization(q));
        jsonWriter.writeStringField("k", KeyVaultKeysUtils.base64UrlJsonSerialization(k));
        jsonWriter.writeStringField("key_hsm", KeyVaultKeysUtils.base64UrlJsonSerialization(t));
        jsonWriter.writeStringField("crv", Objects.toString(crv, null));
        jsonWriter.writeStringField("x", KeyVaultKeysUtils.base64UrlJsonSerialization(x));
        jsonWriter.writeStringField("y", KeyVaultKeysUtils.base64UrlJsonSerialization(y));

        return jsonWriter.writeEndObject();
    }

    /**
     * Reads a JSON stream into a {@link JsonWebKey}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @return An instance of {@link JsonWebKey} that the JSON stream represented, may return null.
     * @throws IOException If a {@link JsonWebKey} fails to be read from the {@code jsonReader}.
     */
    public static JsonWebKey fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            JsonWebKey key = new JsonWebKey();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("kid".equals(fieldName)) {
                    key.keyId = reader.getString();
                } else if ("kty".equals(fieldName)) {
                    key.keyType = KeyType.fromValue(reader.getString());
                } else if ("key_ops".equals(fieldName)) {
                    key.keyOps = reader.readArray(arrayReader -> KeyOperation.fromValue(arrayReader.getString()));
                } else if ("n".equals(fieldName)) {
                    key.n = KeyVaultKeysUtils.base64UrlJsonDeserialization(reader.getString());
                } else if ("e".equals(fieldName)) {
                    key.e = KeyVaultKeysUtils.base64UrlJsonDeserialization(reader.getString());
                } else if ("d".equals(fieldName)) {
                    key.d = KeyVaultKeysUtils.base64UrlJsonDeserialization(reader.getString());
                } else if ("dp".equals(fieldName)) {
                    key.dp = KeyVaultKeysUtils.base64UrlJsonDeserialization(reader.getString());
                } else if ("dq".equals(fieldName)) {
                    key.dq = KeyVaultKeysUtils.base64UrlJsonDeserialization(reader.getString());
                } else if ("qi".equals(fieldName)) {
                    key.qi = KeyVaultKeysUtils.base64UrlJsonDeserialization(reader.getString());
                } else if ("p".equals(fieldName)) {
                    key.p = KeyVaultKeysUtils.base64UrlJsonDeserialization(reader.getString());
                } else if ("q".equals(fieldName)) {
                    key.q = KeyVaultKeysUtils.base64UrlJsonDeserialization(reader.getString());
                } else if ("k".equals(fieldName)) {
                    key.k = KeyVaultKeysUtils.base64UrlJsonDeserialization(reader.getString());
                } else if ("key_hsm".equals(fieldName)) {
                    key.t = KeyVaultKeysUtils.base64UrlJsonDeserialization(reader.getString());
                } else if ("crv".equals(fieldName)) {
                    key.crv = KeyCurveName.fromValue(reader.getString());
                } else if ("x".equals(fieldName)) {
                    key.x = KeyVaultKeysUtils.base64UrlJsonDeserialization(reader.getString());
                } else if ("y".equals(fieldName)) {
                    key.y = KeyVaultKeysUtils.base64UrlJsonDeserialization(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }

            return key;
        });
    }

    /**
     * Creates a copy of the source byte array.
     *
     * @param source Array to make copy of
     * @return A copy of the array, or null if source was null.
     */
    private static byte[] arrayCopy(byte[] source) {
        if (source == null) {
            return null;
        }

        return Arrays.copyOf(source, source.length);
    }
}

package com.microsoft.azure.keyvault.cryptography.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECPoint;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.apache.commons.lang3.tuple.Pair;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.junit.BeforeClass;
import org.junit.Test;

import com.google.common.collect.ImmutableMap;
import com.microsoft.azure.keyvault.cryptography.EcKey;
import com.microsoft.azure.keyvault.webkey.JsonWebKeyCurveName;
import com.microsoft.azure.keyvault.webkey.JsonWebKey;
import com.microsoft.azure.keyvault.webkey.JsonWebKeyType;

public class ECKeyTest {

	// A Content Encryption Key, or Message.
    static final byte[] CEK = { 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte)0x88, (byte)0x99, (byte)0xAA, (byte)0xBB, (byte)0xCC, (byte)0xDD, (byte)0xEE, (byte)0xFF };    
    static MessageDigest DIGEST_256;
    static MessageDigest DIGEST_384;
    static MessageDigest DIGEST_512;
    static KeyPairGenerator EC_KEY_GENERATOR;
    static Map<JsonWebKeyCurveName, MessageDigest> CURVE_TO_DIGEST;
    static List<JsonWebKeyCurveName> CURVE_LIST;
       
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    	EC_KEY_GENERATOR = KeyPairGenerator.getInstance("EC", new BouncyCastleProvider());

    	DIGEST_256 = MessageDigest.getInstance("SHA-256");
    	DIGEST_384 = MessageDigest.getInstance("SHA-384");
    	DIGEST_512 = MessageDigest.getInstance("SHA-512");
    	
    	CURVE_TO_DIGEST = ImmutableMap.<JsonWebKeyCurveName, MessageDigest>builder()
			.put(JsonWebKeyCurveName.P_256, DIGEST_256)
			.put(JsonWebKeyCurveName.P_384, DIGEST_384)
			.put(JsonWebKeyCurveName.P_521, DIGEST_512)
			.put(JsonWebKeyCurveName.SECP256K1, DIGEST_256)
			.build();
    	
    	CURVE_LIST = Arrays.asList(JsonWebKeyCurveName.P_256, JsonWebKeyCurveName.P_384, JsonWebKeyCurveName.P_521, JsonWebKeyCurveName.SECP256K1);
    }
    
    @Test
    public void testCurves() throws Exception {
    	for (JsonWebKeyCurveName crv : CURVE_LIST) {
    		EcKey key = new EcKey("keyId", crv);
    		doSignVerify(key, CURVE_TO_DIGEST.get(crv));
    	}
    }
    
    @Test(expected = NoSuchAlgorithmException.class)
    public void testUnsupportedCurve() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, InstantiationException, IllegalAccessException, ClassNotFoundException {
    	EcKey key = new EcKey("keyId", new JsonWebKeyCurveName("not an algo"));
    }
    
    @Test
    public void testDefaultKey() throws Exception {
    	EcKey key = new EcKey("keyId");
    	doSignVerify(key, DIGEST_256);
    }
    
    @Test
    public void testWithBCProvider() throws Exception {
    	// BC provider is the default anyway.
    	EcKey key = new EcKey("keyId", JsonWebKeyCurveName.P_256, new BouncyCastleProvider());
    	doSignVerify(key, DIGEST_256);
    }
    
    @Test
    public void testWithKeyPair() throws Exception {
    	for (JsonWebKeyCurveName crv : CURVE_LIST) {
        	ECGenParameterSpec gps = new ECGenParameterSpec(EcKey.CURVE_TO_SPEC_NAME.get(crv));
        	EC_KEY_GENERATOR.initialize(gps);
        	KeyPair keyPair = EC_KEY_GENERATOR.generateKeyPair();
        	
        	final String name = "keyid";
        	EcKey key = new EcKey(name, keyPair);
        	assertEquals(name, key.getKid());
        	assertEquals(crv, key.getCurve());
        	doSignVerify(key, CURVE_TO_DIGEST.get(crv));
    	}
    }
    
    @Test(expected = NoSuchAlgorithmException.class)
    public void testWithNotCurveKeyPair() throws Exception {
    	ECGenParameterSpec gps = new ECGenParameterSpec("secp192k1");
    	EC_KEY_GENERATOR.initialize(gps);
    	KeyPair keyPair = EC_KEY_GENERATOR.generateKeyPair();
    	
    	final String name = "keyid";
    	EcKey key = new EcKey(name, keyPair);
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void testFromJsonWebKeyPublicOnly() throws Exception {
    	ECGenParameterSpec gps = new ECGenParameterSpec(EcKey.P256);
    	EC_KEY_GENERATOR.initialize(gps);
    	KeyPair keyPair = EC_KEY_GENERATOR.generateKeyPair();
    	
    	ECPublicKey apub = (ECPublicKey) keyPair.getPublic();
    	ECPoint point = apub.getW();
    	
    	JsonWebKey jwk = new JsonWebKey()
    			.withKid("kid")
    			.withCrv(JsonWebKeyCurveName.P_256)
    			.withX(point.getAffineX().toByteArray())
    			.withY(point.getAffineY().toByteArray())
    			.withKty(JsonWebKeyType.EC);
    
    	assertFalse(jwk.hasPrivateKey());
    	
    	EcKey newKey = EcKey.fromJsonWebKey(jwk, false);
    	assertEquals("kid", newKey.getKid());
    	doSignVerify(newKey, DIGEST_256);
    }
    
    @Test
    public void testFromJsonWebKey() throws Exception {
    	ECGenParameterSpec gps = new ECGenParameterSpec(EcKey.P384);
    	EC_KEY_GENERATOR.initialize(gps);
    	KeyPair keyPair = EC_KEY_GENERATOR.generateKeyPair();
    	
    	ECPublicKey apub = (ECPublicKey) keyPair.getPublic();
    	ECPoint point = apub.getW();
    	ECPrivateKey apriv = (ECPrivateKey) keyPair.getPrivate();
    	
    	JsonWebKey jwk = new JsonWebKey()
    			.withKid("kid")
    			.withCrv(JsonWebKeyCurveName.P_384)
    			.withX(point.getAffineX().toByteArray())
    			.withY(point.getAffineY().toByteArray())
    			.withD(apriv.getS().toByteArray())
    			.withKty(JsonWebKeyType.EC);
    
    	assertTrue(jwk.hasPrivateKey());
    	
    	EcKey newKey = EcKey.fromJsonWebKey(jwk, true);
    	assertEquals("kid", newKey.getKid());
    	doSignVerify(newKey, DIGEST_384);
    }
    
    @Test
    public void testToJsonWebKey() throws Exception {
    	ECGenParameterSpec gps = new ECGenParameterSpec(EcKey.P521);
    	EC_KEY_GENERATOR.initialize(gps);
    	KeyPair keyPair = EC_KEY_GENERATOR.generateKeyPair();
    	
    	ECPublicKey apub = (ECPublicKey) keyPair.getPublic();
    	ECPoint point = apub.getW();
    	ECPrivateKey apriv = (ECPrivateKey) keyPair.getPrivate();
    	
    	JsonWebKey jwk = new JsonWebKey()
    			.withKid("kid")
    			.withCrv(JsonWebKeyCurveName.P_521)
    			.withX(point.getAffineX().toByteArray())
    			.withY(point.getAffineY().toByteArray())
    			.withD(apriv.getS().toByteArray())
    			.withKty(JsonWebKeyType.EC);
    	
    	EcKey newKey = new EcKey("kid", keyPair);
    	
    	JsonWebKey newJwk = newKey.toJsonWebKey();
    	//set missing parameters
    	newJwk.withKid("kid");
    	
    	assertEquals(jwk, newJwk);	
    }
    
    private void doSignVerify(EcKey key, MessageDigest digest) throws IOException, NoSuchAlgorithmException, InterruptedException, ExecutionException {
    	String algorithm = key.getDefaultSignatureAlgorithm();
    	byte[] hash = digest.digest(CEK);
    	
    	//Use sign and verify to test each other.
    	Pair<byte[], String> signature = key.signAsync(hash, algorithm).get();
    	assertEquals(signature.getRight(), algorithm);
    	boolean result = key.verifyAsync(hash, signature.getLeft(), algorithm).get();
    	assertTrue(result);
    	
    	//Check that key denies invalid digest.
    	BigInteger bigInt = new BigInteger(hash);
    	BigInteger shiftInt = bigInt.shiftRight(4);
       	byte [] shifted = shiftInt.toByteArray();
    	boolean incorrectResult = key.verifyAsync(shifted, signature.getLeft(), algorithm).get();
    	assertFalse(incorrectResult);
   
    	key.close();
    }
}

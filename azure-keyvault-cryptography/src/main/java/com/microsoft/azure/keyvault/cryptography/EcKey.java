package com.microsoft.azure.keyvault.cryptography;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Provider;
import java.security.Security;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.EllipticCurve;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import com.google.common.collect.ImmutableMap;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.microsoft.azure.keyvault.core.IKey;
import com.microsoft.azure.keyvault.cryptography.algorithms.Ecdsa;
import com.microsoft.azure.keyvault.cryptography.algorithms.Ecdsa256;
import com.microsoft.azure.keyvault.cryptography.algorithms.Es256;
import com.microsoft.azure.keyvault.cryptography.algorithms.Es384;
import com.microsoft.azure.keyvault.cryptography.algorithms.Es512;
import com.microsoft.azure.keyvault.webkey.JsonWebKey;
import com.microsoft.azure.keyvault.webkey.JsonWebKeyCurveName;


public class EcKey implements IKey {
	
	public static final String P256 = "secp256r1";
	public static final String P384 = "secp384r1";
	public static final String P521 = "secp521r1";
	public static final String SECP265K1 = "secp256k1";
	public static final Map<JsonWebKeyCurveName, String> CURVE_TO_SIGNATURE = ImmutableMap.<JsonWebKeyCurveName, String>builder()
			.put(JsonWebKeyCurveName.P_256, Es256.ALGORITHM_NAME)
			.put(JsonWebKeyCurveName.P_384, Es384.ALGORITHM_NAME)
			.put(JsonWebKeyCurveName.P_521, Es512.ALGORITHM_NAME)
			.put(JsonWebKeyCurveName.SECP256K1, Ecdsa256.ALGORITHM_NAME)
			.build();
	public static final Map<JsonWebKeyCurveName, String> CURVE_TO_SPEC_NAME = ImmutableMap.<JsonWebKeyCurveName, String>builder()
			.put(JsonWebKeyCurveName.P_256, P256)
			.put(JsonWebKeyCurveName.P_384, P384)
			.put(JsonWebKeyCurveName.P_521, P521)
			.put(JsonWebKeyCurveName.SECP256K1, SECP265K1)
			.build();			
	

	private final String _kid;
	private final KeyPair _keyPair;
	private final Provider _provider;
	private final JsonWebKeyCurveName _curve;
	
	protected final String _signatureAlgorithm;
	protected String defaultEncryptionAlgorithm;

	public static JsonWebKeyCurveName getDefaultCurve() {
		return JsonWebKeyCurveName.P_256;
	}

	/**
	 * Constructor.
	 * 
	 * Generates a new EcKey with a P_256 curve and a randomly generated kid.
	 * 
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidAlgorithmParameterException
	 */
	public EcKey() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
		this(UUID.randomUUID().toString());
	}
	
	/**
	 * Constructor.
	 * 
	 * Generates a new EcKey with a P_256 curve and the given kid.
	 * 
	 * @param kid
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidAlgorithmParameterException
	 */
	public EcKey(String kid) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
		this(kid, getDefaultCurve(), Security.getProvider("SunEC"));
	}
	
	/**
	 * Constructor.
	 * 
	 * Generates a new EcKey with the given curve and kid.
	 * @param kid
	 * @param curve
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidAlgorithmParameterException
	 */
	public EcKey(String kid, JsonWebKeyCurveName curve) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
		this(kid, curve, Security.getProvider("SunEC"));
	}
	
	/**
	 * Constructor.
	 * 
	 * Generates a new EcKey with the given curve and kid.
	 * @param kid
	 * @param curve
	 * @param provider Java security provider
	 * @throws InvalidAlgorithmParameterException
	 * @throws NoSuchAlgorithmException
	 */
	public EcKey(String kid, JsonWebKeyCurveName curve, Provider provider) throws InvalidAlgorithmParameterException, NoSuchAlgorithmException {
		_kid = kid;
		_provider = provider;
		_curve = curve;
		
		_signatureAlgorithm = CURVE_TO_SIGNATURE.get(curve);
		if (_signatureAlgorithm == null) {
			throw new NoSuchAlgorithmException("Curve not supported.");
		}
		
		final KeyPairGenerator generator = KeyPairGenerator.getInstance("EC", provider);
		ECGenParameterSpec gps = new ECGenParameterSpec(CURVE_TO_SPEC_NAME.get(curve));

		generator.initialize(gps);
		_keyPair = generator.generateKeyPair();
		
	}
	
	/**
	 * Constructor.
	 * 
	 * Generates a new EcKey with the given keyPair.
	 * The keyPair must be an ECKey.
	 * @param kid
	 * @param keyPair
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidAlgorithmParameterException
	 */
	public EcKey(String kid, KeyPair keyPair) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {
		this(kid, keyPair, Security.getProvider("SunEC"));
	}
	
	/**
	 * Constructor.
	 * 
	 * Generates a new EcKey with the given keyPair.
	 * The keyPair must be an ECKey.
	 * @param kid
	 * @param keyPair
	 * @param provider Java security provider
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidAlgorithmParameterException
	 */
	public EcKey(String kid, KeyPair keyPair, Provider provider) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException {

		if (Strings.isNullOrWhiteSpace(kid)) {
			throw new IllegalArgumentException("Please provide a kid");
		}
		
        if (keyPair == null) {
            throw new IllegalArgumentException("Please provide an ECKey");
        }

        if (keyPair.getPublic() == null || !(keyPair.getPublic() instanceof ECPublicKey)) {
            throw new IllegalArgumentException("The keyPair provided is not an ECKey");
        }
        
        _kid      = kid;
        _keyPair  = keyPair;
        _provider = provider;
        _curve = getCurveFromKeyPair(keyPair);
        _signatureAlgorithm = CURVE_TO_SIGNATURE.get(_curve);
		if (_signatureAlgorithm == null) {
			throw new IllegalArgumentException("Curve not supported.");
		}
	}
	
	/**
	 * Converts JSON web key to EC key pair, does not include the private key.
	 * @param jwk
	 * @return EcKey
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidAlgorithmParameterException
	 * @throws InvalidKeySpecException
	 * @throws NoSuchProviderException
	 */
	public static EcKey fromJsonWebKey(JsonWebKey jwk) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeySpecException, NoSuchProviderException {
		return fromJsonWebKey(jwk, false, null);
	}
	
	/**
	 * Converts JSON web key to EC key pair and include the private key if set to true.
	 * @param jwk
	 * @param includePrivateParameters true if the EC key pair should include the private key. False otherwise.
	 * @return EcKey
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidAlgorithmParameterException
	 * @throws InvalidKeySpecException
	 * @throws NoSuchProviderException
	 */
	public static EcKey fromJsonWebKey(JsonWebKey jwk, boolean includePrivateParameters) throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, InvalidKeySpecException, NoSuchProviderException {
		return fromJsonWebKey(jwk, includePrivateParameters, null);
	}
	
	/**
	 * Converts JSON web key to EC key pair and include the private key if set to true.
	 * @param jwk
	 * @param includePrivateParameters true if the EC key pair should include the private key. False otherwise.
	 * @param provider the Java Security Provider
	 * @return EcKey
	 */
	public static EcKey fromJsonWebKey(JsonWebKey jwk, boolean includePrivateParameters, Provider provider) {
		try {
			if (jwk.kid() != null) {
				return new EcKey(jwk.kid(), jwk.toEC(includePrivateParameters, provider));
			} else {
				throw new IllegalArgumentException("Json Web Key should have a kid");
			}
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException(e);
		}
	}
	
	/**
	 * Converts EcKey to JSON web key.
	 * @return
	 */
	public JsonWebKey toJsonWebKey() {
		return JsonWebKey.fromEC(_keyPair, _provider);
	}
	
	// Matches the curve of the keyPair to supported curves.
	private JsonWebKeyCurveName getCurveFromKeyPair(KeyPair keyPair) {
		try {
			ECPublicKey key = (ECPublicKey) keyPair.getPublic();
			ECParameterSpec spec = key.getParams();
			EllipticCurve crv = spec.getCurve();
			
			List<JsonWebKeyCurveName> curveList = Arrays.asList(JsonWebKeyCurveName.P_256, JsonWebKeyCurveName.P_384, JsonWebKeyCurveName.P_521, JsonWebKeyCurveName.SECP256K1);
			
			for (JsonWebKeyCurveName curve : curveList) {
				ECGenParameterSpec gps = new ECGenParameterSpec(CURVE_TO_SPEC_NAME.get(curve));
				KeyPairGenerator kpg = KeyPairGenerator.getInstance("EC", _provider);
				kpg.initialize(gps);
				
				// Generate dummy keypair to get parameter spec.
				KeyPair apair = kpg.generateKeyPair();
				ECPublicKey apub = (ECPublicKey) apair.getPublic();
				ECParameterSpec aspec = apub.getParams();
				EllipticCurve acurve = aspec.getCurve();
				
				//Matches the parameter spec
				if (acurve.equals(crv)) {
					return curve;
				}
			}
			
			//Did not find a supported curve.
			throw new IllegalArgumentException ("Curve not supported.");
		} catch (GeneralSecurityException e) {
			throw new IllegalStateException(e);
		}
	}
	
	/**
	 * @return curve of the key
	 */
	public JsonWebKeyCurveName getCurve() {
		return _curve;
	}
	
	/**
	 * 
	 * @return the underlying keyPair of the key
	 */
	public KeyPair getKeyPair() {
		return _keyPair;
	}
	
	@Override
	public void close() throws IOException {
        // Intentionally empty
	}

	@Override
	public String getDefaultEncryptionAlgorithm() {
		return null;
	}

	@Override
	public String getDefaultKeyWrapAlgorithm() {
		return null;
	}

	@Override
	public String getDefaultSignatureAlgorithm() {
		return _signatureAlgorithm;
	}

	@Override
	public String getKid() {
		return _kid;
	}

	@Override
	public ListenableFuture<byte[]> decryptAsync(byte[] ciphertext, byte[] iv, byte[] authenticationData,
			byte[] authenticationTag, String algorithm) throws NoSuchAlgorithmException {
		throw new UnsupportedOperationException("Decrypt Async is not supported");
	}

	@Override
	public ListenableFuture<Triple<byte[], byte[], String>> encryptAsync(byte[] plaintext, byte[] iv,
			byte[] authenticationData, String algorithm) throws NoSuchAlgorithmException {
		throw new UnsupportedOperationException("Encrypt Async is not supported");
	}

	@Override
	public ListenableFuture<Pair<byte[], String>> wrapKeyAsync(byte[] key, String algorithm)
			throws NoSuchAlgorithmException {
		throw new UnsupportedOperationException("Wrap key is not supported");
	}

	@Override
	public ListenableFuture<byte[]> unwrapKeyAsync(byte[] encryptedKey, String algorithm)
			throws NoSuchAlgorithmException {
		throw new UnsupportedOperationException("Unwrap key is not supported");
	}

	@Override
	public ListenableFuture<Pair<byte[], String>> signAsync(byte[] digest, String algorithm) throws NoSuchAlgorithmException {
        
        if (_keyPair.getPrivate() == null) {
        	throw new UnsupportedOperationException("Sign is not supported without a private key.");
        }
		
		if (digest == null) {
			throw new IllegalArgumentException("Please provide a digest to sign.");
		}
		
		if (Strings.isNullOrWhiteSpace(algorithm)) {
			throw new IllegalArgumentException("Please provide a signature algorithm to use.");
		}
		
        // Interpret the requested algorithm
		Algorithm baseAlgorithm = AlgorithmResolver.Default.get(algorithm);

       if (baseAlgorithm == null || !(baseAlgorithm instanceof AsymmetricSignatureAlgorithm)) {
            throw new NoSuchAlgorithmException(algorithm);
        }
       
		Ecdsa algo = (Ecdsa) baseAlgorithm;
		ISignatureTransform signer = algo.createSignatureTransform(_keyPair, _provider);
		
		try {
			return Futures.immediateFuture(Pair.of(signer.sign(digest), algorithm));
		} catch (Exception e) {
			return Futures.immediateFailedFuture(e);
		}
	}

	@Override
	public ListenableFuture<Boolean> verifyAsync(byte[] digest, byte[] signature, String algorithm) throws NoSuchAlgorithmException {

        if (digest == null) {
            throw new IllegalArgumentException("Please provide a digest input.");
        }

        if (Strings.isNullOrWhiteSpace(algorithm)) {
            throw new IllegalArgumentException("Please provide an algorithm");
        }

        // Interpret the requested algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.Default.get(algorithm);
        
        if (baseAlgorithm == null || !(baseAlgorithm instanceof AsymmetricSignatureAlgorithm)) {
            throw new NoSuchAlgorithmException(algorithm);
        }
        
        Ecdsa algo = (Ecdsa) baseAlgorithm;

        ISignatureTransform signer = algo.createSignatureTransform(_keyPair, _provider);
        
        try {
			return Futures.immediateFuture(signer.verify(digest, signature));
		} catch (Exception e) {
			return Futures.immediateFailedFuture(e);
		}
	}

}

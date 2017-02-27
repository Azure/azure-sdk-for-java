/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.keyvault.cryptography.algorithms;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import com.microsoft.azure.keyvault.cryptography.AsymmetricSignatureAlgorithm;
import com.microsoft.azure.keyvault.cryptography.ISignatureTransform;
import com.microsoft.azure.keyvault.cryptography.Strings;

public abstract class RsaSignature extends AsymmetricSignatureAlgorithm {

	private static final BigInteger twoFiveSix   = new BigInteger("256");
	private static final byte[]     sha256Prefix = new byte[] { 0x30, 0x31, 0x30, 0x0d, 0x06, 0x09, 0x60, (byte) 0x86, 0x48, 0x01, 0x65, 0x03, 0x04, 0x02, 0x01, 0x05, 0x00, 0x04, 0x20 };

    protected RsaSignature(String name) {
        super(name);
    }
    
    protected int getOctetLength(int bits) {
    	return ( bits % 8 > 0 ) ? bits >> 3 + 1 : bits >> 3; 
    }
    
    /*
     * See https://tools.ietf.org/html/rfc3447#section-4.2
     */
    protected BigInteger OS2IP(byte[] x) {
    	
    	if ( x == null || x.length == 0 ) {
    		throw new IllegalArgumentException("x");
    	}
    	
    	return new BigInteger(1,x);
    }
    
    /*
     * See https://tools.ietf.org/html/rfc3447#section-4.1
     */
    protected byte[] I2OSP(BigInteger x, int xLen) {
    	
    	if ( x == null ) {
    		throw new IllegalArgumentException("x");
    	}
    	
    	if ( xLen <= 0 ) {
    		throw new IllegalArgumentException("xLen");
    	}
    	
    	if ( x.compareTo( twoFiveSix.pow(xLen) ) == 1 ) {
    		throw new IllegalArgumentException("integer too large");
    	}
    	
    	byte[] bytes  = x.toByteArray();
    	
    	if ( bytes.length > xLen ) {
    		throw new IllegalArgumentException("integer too large");
    	}

    	byte[] result = new byte[xLen];
    	
		System.arraycopy(bytes, 0, result, xLen - bytes.length, bytes.length);
		
		return result;
	}

    /*
     * See https://tools.ietf.org/html/rfc3447#section-5.2.1
     */
    protected BigInteger RSASP1(RSAPrivateKey K, BigInteger m) {
    	
    	if ( K == null ) {
    		throw new IllegalArgumentException("K");
    	}
    	
    	if ( m == null ) {
    		throw new IllegalArgumentException("m");
    	}
    	
    	BigInteger n = K.getModulus();
    	BigInteger d = K.getPrivateExponent();
    	
    	if ( m.compareTo(BigInteger.ONE) == -1 || m.compareTo(n) != -1 ) {
    		throw new IllegalArgumentException("message representative out of range");
    	}
    	
    	return m.modPow(d, n);
    }
    
    /*
     * See https://tools.ietf.org/html/rfc3447#section-5.2.2
     */
    protected BigInteger RSAVP1(RSAPublicKey K, BigInteger s) {
    	
    	if ( K == null ) {
    		throw new IllegalArgumentException("K");
    	}
    	
    	if ( s == null ) {
    		throw new IllegalArgumentException("s");
    	}
    	BigInteger n = K.getModulus();
    	BigInteger e = K.getPublicExponent();
    	
    	if ( s.compareTo(BigInteger.ONE) == -1 || s.compareTo(n) != -1 ) {
    		throw new IllegalArgumentException("message representative out of range");
    	}
    	
    	return s.modPow(e, n);
    }
    
    /*
     * See https://tools.ietf.org/html/rfc3447#section-9.2
     */
    protected byte[] EMSA_PKCS1_V1_5_ENCODE(byte[] m, int emLen, String algorithm) throws NoSuchAlgorithmException {
    	
    	// Check m
    	if ( m == null || m.length == 0 ) {
    		throw new IllegalArgumentException("m");
    	}
    	
    	MessageDigest messageDigest   = null;
    	
    	// Check algorithm
    	if ( Strings.isNullOrWhiteSpace(algorithm) ) {
    		throw new IllegalArgumentException("algorithm");
    	}

    	// Only supported algorithms
    	if ( algorithm.equals("SHA-256") ) {
    		
    		// Initialize digest
    		messageDigest   = MessageDigest.getInstance("SHA-256");
    	} else {
    		throw new IllegalArgumentException("algorithm");
    	}
    	
		// Hash the message
		byte[] digest = messageDigest.digest(m);
		
		// Construct T, the DER encoded DigestInfo structure
		return EMSA_PKCS1_V1_5_ENCODE_HASH(digest, emLen, algorithm);
    }
    
    /*
     * See https://tools.ietf.org/html/rfc3447#section-9.2
     */
    protected byte[] EMSA_PKCS1_V1_5_ENCODE_HASH(byte[] h, int emLen, String algorithm) throws NoSuchAlgorithmException {
    	
    	// Check m
    	if ( h == null || h.length == 0 ) {
    		throw new IllegalArgumentException("m");
    	}
    	
    	byte[] algorithmPrefix = null;
    	
    	// Check algorithm
    	if ( Strings.isNullOrWhiteSpace(algorithm) ) {
    		throw new IllegalArgumentException("algorithm");
    	}

    	// Only supported algorithms
    	if ( algorithm.equals("SHA-256") ) {
    		
    		// Initialize prefix and digest
    		algorithmPrefix = sha256Prefix;
    		
    		if ( h.length != 32 ) {
    			throw new IllegalArgumentException("h is incorrect length for SHA-256");
    		}
    	} else {
    		throw new IllegalArgumentException("algorithm");
    	}
    	
		
		// Construct T, the DER encoded DigestInfo structure
		byte[] T      = new byte[algorithmPrefix.length + h.length];
		
		System.arraycopy(algorithmPrefix, 0, T, 0, algorithmPrefix.length);
		System.arraycopy(h, 0, T, algorithmPrefix.length, h.length);
		
		if ( emLen < T.length + 11 ) {
			throw new IllegalArgumentException("intended encoded message length too short");
		}
		
		// Construct PS
		byte[] PS = new byte[emLen - T.length - 3];
		
		for ( int i = 0; i < PS.length; i++ ) PS[i] = (byte) 0xff;
		
		// Construct EM
		byte[] EM = new byte[PS.length + T.length + 3];
		
		EM[0] = 0x00; EM[1] = 0x01; EM[PS.length + 2] = 0x00;
		
		System.arraycopy(PS, 0, EM, 2, PS.length);
		System.arraycopy(T, 0, EM, PS.length + 3, T.length);

		return EM;
    }

    public abstract ISignatureTransform createSignatureTransform(KeyPair keyPair);
}

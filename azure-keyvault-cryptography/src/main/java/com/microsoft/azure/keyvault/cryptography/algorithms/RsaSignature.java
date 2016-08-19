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

package com.microsoft.azure.keyvault.cryptography.algorithms;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;

import com.microsoft.azure.keyvault.cryptography.AsymmetricSignatureAlgorithm;
import com.microsoft.azure.keyvault.cryptography.Strings;

public abstract class RsaSignature extends AsymmetricSignatureAlgorithm {

	private static final BigInteger twoFiveSix   = new BigInteger("256");
	private static final byte[]     sha256Prefix = new byte[] { 0x30, 0x31, 0x30, 0x0d, 0x06, 0x09, 0x60, (byte) 0x86, 0x48, 0x01, 0x65, 0x03, 0x04, 0x02, 0x01, 0x05, 0x00, 0x04, 0x20 };

    protected RsaSignature(String name) {
        super(name);
    }
    
    protected static byte[] toByteArray(BigInteger n) {
        byte[] result = n.toByteArray();
        if (result[0] == 0) {
            // The leading zero is used to let the number positive. Since RSA
            // parameters are always positive, we remove it.
            return Arrays.copyOfRange(result, 1, result.length);
        }
        return result;
    }

    protected static BigInteger toBigInteger(byte[] b) {
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
    	
    	byte[]        algorithmPrefix = null;
    	MessageDigest messageDigest   = null;
    	
    	// Check algorithm
    	if ( Strings.isNullOrWhiteSpace(algorithm) ) {
    		throw new IllegalArgumentException("algorithm");
    	}

    	// Only supported algorithms
    	if ( algorithm.equals("SHA-256") ) {
    		
    		// Initialize prefix and digest
    		algorithmPrefix = sha256Prefix;
    		messageDigest   = MessageDigest.getInstance("SHA-256");
    	} else {
    		throw new IllegalArgumentException("algorithm");
    	}
    	
    	if ( algorithmPrefix == null || messageDigest == null ) {
    		throw new IllegalArgumentException("initialization with arguments failed");
    	}
    	
		// Hash the message
		byte[] digest = messageDigest.digest(m);
		
		// Construct T, the DER encoded DigestInfo structure
		byte[] T      = new byte[algorithmPrefix.length + digest.length];
		
		System.arraycopy(algorithmPrefix, 0, T, 0, algorithmPrefix.length);
		System.arraycopy(digest, 0, T, algorithmPrefix.length, digest.length);
		
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

}

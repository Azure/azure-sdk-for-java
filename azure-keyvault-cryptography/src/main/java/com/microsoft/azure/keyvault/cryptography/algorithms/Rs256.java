/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.keyvault.cryptography.algorithms;

import java.math.BigInteger;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

import com.microsoft.azure.keyvault.cryptography.ByteExtensions;
import com.microsoft.azure.keyvault.cryptography.ISignatureTransform;

/**
 *
 */
public class Rs256 extends RsaSignature {
	
	static final String RsaNone = "RSA/ECB/PKCS1Padding";
	
	class Rs256SignatureTransform implements ISignatureTransform {

		private final KeyPair  _keyPair;
		private final int      _emLen;
		
		Rs256SignatureTransform(KeyPair keyPair) {
			_keyPair  = keyPair;
			
			BigInteger modulus = ((RSAPublicKey)_keyPair.getPublic()).getModulus();
			
			_emLen    = getOctetLength( modulus.bitLength() );
			
		}

		@Override
		public byte[] sign(byte[] digest) throws NoSuchAlgorithmException {
			// Signing isn't just a case of encrypting the digest, there is much more to do.
			// For details of the algorithm, see https://tools.ietf.org/html/rfc3447#section-8.2
			
			if ( _keyPair.getPrivate() == null ) {
				// TODO
			}
			
			// Construct the encoded message
			byte[] EM = EMSA_PKCS1_V1_5_ENCODE_HASH(digest, _emLen, "SHA-256");
			
			// Convert to integer message
			BigInteger s = OS2IP(EM);
			
			// RSASP1(s)
			s = RSASP1((RSAPrivateKey)_keyPair.getPrivate(), s);
			
			// Convert to octet sequence
			return I2OSP(s, _emLen );
		}

		@Override
		public boolean verify(byte[] digest, byte[] signature) throws NoSuchAlgorithmException {
			
			if ( signature.length != _emLen ) {
				throw new IllegalArgumentException( "invalid signature length");
			}
			
			// Convert to integer signature
			BigInteger s = OS2IP(signature);
			
			// Convert integer message
			BigInteger m = RSAVP1((RSAPublicKey)_keyPair.getPublic(), s);
			
			byte[] EM  = I2OSP(m, _emLen );
			byte[] EM2 = EMSA_PKCS1_V1_5_ENCODE_HASH(digest, _emLen, "SHA-256");
			
			// Use constant time compare
			return ByteExtensions.sequenceEqualConstantTime(EM, EM2);
		}
		
	}

    public final static String ALGORITHM_NAME = "RS256";

    public Rs256() {
        super(ALGORITHM_NAME);
    }
    
    @Override
    public ISignatureTransform createSignatureTransform(KeyPair keyPair) {
    	
    	return new Rs256SignatureTransform(keyPair);
    }
}

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
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

/**
 *
 */
public class Rs256 extends RsaSignature {
	
	static final String RsaNone = "RSA/ECB/PKCS1Padding";
	
	public class Rs256Signer {
		
		private final KeyPair  _keyPair;
		private final int      _emLen;
		
		private final BigInteger _n;
		
		Rs256Signer(KeyPair keyPair) {
			
			_keyPair  = keyPair;
			_n = ((RSAPublicKey)_keyPair.getPublic()).getModulus();
			
			_emLen = getOctetLength( _n.bitLength() );
		}
		
		public byte[] sign(final byte[] digest) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
			// Signing isn't just a case of encrypting the digest, there is much more to do.
			// For details of the algorithm, see https://tools.ietf.org/html/rfc3447#section-8.2
			
			// Construct the encoded message
			byte[] EM = EMSA_PKCS1_V1_5_ENCODE(digest, _emLen, "SHA-256");
			
			// Convert to integer message
			BigInteger s = OS2IP(EM);
			
			// RSASP1(s)
			s = RSASP1((RSAPrivateKey)_keyPair.getPrivate(), s);
			
			// Convert to octet sequence
			return I2OSP(s, getOctetLength( _n.bitLength() ) );
		}
	}
	
	public class Rs256Verifier {
		
		private final KeyPair  _keyPair;
		private final BigInteger _n;
		private final int        _emLength;
		
		Rs256Verifier(KeyPair keyPair) {
			_keyPair  = keyPair;
			_n = ((RSAPublicKey)_keyPair.getPublic()).getModulus();
			_emLength = getOctetLength( _n.bitLength() );
		}
		
		public boolean verify(final byte[] signature, final byte[] digest) throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
			
			if ( signature.length != getOctetLength( _n.bitLength() ) ) {
				throw new IllegalBlockSizeException();
			}
			
			// Convert to integer signature
			BigInteger s = OS2IP(signature);
			
			// Convert integer message
			BigInteger m = RSAVP1((RSAPublicKey)_keyPair.getPublic(), s);
			
			
			byte[] EM  = I2OSP(m, getOctetLength( _n.bitLength() ) );
			byte[] EM2 = EMSA_PKCS1_V1_5_ENCODE(digest, _emLength, "SHA-256");
			
			// TODO: Need constant time compare
			if ( EM.length != EM2.length )
				return false;
			
			for ( int i = 0; i < digest.length; i++ ) {
				if ( EM[i] != EM2[i] )
					return false;
			}
			
			return true;
		}
	}

    public final static String AlgorithmName = "RS256";

    public Rs256() {
        super(AlgorithmName);
    }
    
    public Rs256Signer createSigner(KeyPair keyPair) {
    	
    	return new Rs256Signer(keyPair);
    }
    
    public Rs256Verifier createVerifier(KeyPair keyPair) {
    	return new Rs256Verifier(keyPair);
    }
}

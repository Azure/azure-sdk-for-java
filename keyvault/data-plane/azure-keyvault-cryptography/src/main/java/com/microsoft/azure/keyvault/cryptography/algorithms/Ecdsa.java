package com.microsoft.azure.keyvault.cryptography.algorithms;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.Provider;
import java.security.Signature;

import com.microsoft.azure.keyvault.cryptography.AsymmetricSignatureAlgorithm;
import com.microsoft.azure.keyvault.cryptography.ISignatureTransform;

public abstract class Ecdsa extends AsymmetricSignatureAlgorithm {

	protected Ecdsa() {
		super("NONEwithEDCSA");
	}
	
	public ISignatureTransform createSignatureTransform(KeyPair key, Provider provider) {
		return new EcdsaSignatureTransform(key, provider);
	}
	
	abstract void checkDigestLength(byte[] digest);
	
	
	class EcdsaSignatureTransform implements ISignatureTransform {
	    private final String ALGORITHM = "NONEwithECDSA";
		private final KeyPair _keyPair;
		
		private final Provider _provider;
		
		public EcdsaSignatureTransform(KeyPair keyPair, Provider provider) {
			_keyPair = keyPair;
			_provider = provider;
		}
		
		@Override
		public byte[] sign(byte[] digest) throws GeneralSecurityException {
		    checkDigestLength(digest);
			Signature signature = Signature.getInstance(ALGORITHM, _provider);
			signature.initSign(_keyPair.getPrivate());
			signature.update(digest);
			return signature.sign();
		}

		@Override
		public boolean verify(byte[] digest, byte[] signature) throws GeneralSecurityException {
			Signature verify = Signature.getInstance(ALGORITHM, _provider);
	         checkDigestLength(digest);
			verify.initVerify(_keyPair.getPublic());
			verify.update(digest);
			return verify.verify(signature);
		}
		
	}
}

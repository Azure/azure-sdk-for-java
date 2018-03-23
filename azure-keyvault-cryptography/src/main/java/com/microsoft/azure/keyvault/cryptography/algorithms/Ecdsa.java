package com.microsoft.azure.keyvault.cryptography.algorithms;

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.Provider;
import java.security.Signature;

import com.microsoft.azure.keyvault.cryptography.AsymmetricSignatureAlgorithm;
import com.microsoft.azure.keyvault.cryptography.ISignatureTransform;

public abstract class Ecdsa extends AsymmetricSignatureAlgorithm {
		
	protected Ecdsa(String name) {
		super(name);
	}
	
	public ISignatureTransform createSignatureTransform(KeyPair key, String algorithm, Provider provider) {
		return new EcdsaSignatureTransform(key, algorithm, provider);
	}
	
	
	static class EcdsaSignatureTransform implements ISignatureTransform {

		private final KeyPair _keyPair;
		private final String _algorithm;
		private final Provider _provider;
		
		public EcdsaSignatureTransform(KeyPair keyPair, String algorithm, Provider provider) {
			_keyPair = keyPair;
			_algorithm = algorithm;
			_provider = provider;
		}
		
		@Override
		public byte[] sign(byte[] digest) throws GeneralSecurityException {			
			Signature signature = Signature.getInstance(_algorithm, _provider);
			signature.initSign(_keyPair.getPrivate());
			signature.update(digest);
			return signature.sign();
		}

		@Override
		public boolean verify(byte[] digest, byte[] signature) throws GeneralSecurityException {
			Signature verify = Signature.getInstance(_algorithm, _provider);
			verify.initVerify(_keyPair.getPublic());
			verify.update(digest);
			return verify.verify(signature);
		}
		
	}
}

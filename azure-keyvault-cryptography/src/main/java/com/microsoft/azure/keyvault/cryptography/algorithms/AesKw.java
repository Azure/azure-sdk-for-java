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

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.microsoft.azure.keyvault.cryptography.ICryptoTransform;
import com.microsoft.azure.keyvault.cryptography.KeyWrapAlgorithm;

public abstract class AesKw extends KeyWrapAlgorithm {

    static final byte[] _defaultIv  = new byte[] { (byte) 0xA6, (byte) 0xA6, (byte) 0xA6, (byte) 0xA6, (byte) 0xA6, (byte) 0xA6, (byte) 0xA6, (byte) 0xA6 };
    static final String _cipherName = "AESWrap";

    class AesKwDecryptor implements ICryptoTransform {

        final Cipher _cipher;

        AesKwDecryptor(byte[] key, byte[] iv, Provider provider) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {

            if (provider == null) {
                _cipher = Cipher.getInstance(_cipherName);
            } else {
                _cipher = Cipher.getInstance(_cipherName, provider);
            }

            // The default provider does not support the specification of IV. This
            // is guarded by the CreateEncrypter wrapper method and the iv parameter
            // can be ignored when using the default provider 
            if (provider == null ) {
                _cipher.init(Cipher.UNWRAP_MODE, new SecretKeySpec(key, "AES"));
            } else {
                _cipher.init(Cipher.UNWRAP_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
            }
        }

        @Override
        public byte[] doFinal(byte[] plaintext) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException, NoSuchAlgorithmException {

            return _cipher.unwrap(plaintext, "AESWrap", Cipher.SECRET_KEY).getEncoded();
        }

    }

    class AesKwEncryptor implements ICryptoTransform {

        final Cipher _cipher;

        AesKwEncryptor(byte[] key, byte[] iv, Provider provider) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {

            if (provider == null) {
                _cipher = Cipher.getInstance(_cipherName);
            } else {
                _cipher = Cipher.getInstance(_cipherName, provider);
            }

            // The default provider does not support the specification of IV. This
            // is guarded by the CreateEncrypter wrapper method and the iv parameter
            // can be ignored when using the default provider 
            if (provider == null ) {
                _cipher.init(Cipher.WRAP_MODE, new SecretKeySpec(key, "AES"));
            } else {
                _cipher.init(Cipher.WRAP_MODE, new SecretKeySpec(key, "AES"), new IvParameterSpec(iv));
            }
        }

        @Override
        public byte[] doFinal(byte[] plaintext) throws IllegalBlockSizeException, BadPaddingException, InvalidKeyException {

            return _cipher.wrap(new SecretKeySpec(plaintext, "AES"));
        }

    }

    protected AesKw(String name) {
        super(name);
    }

    @Override
    public ICryptoTransform CreateEncryptor(byte[] key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {

        return CreateEncryptor(key, null, null);
    }

    @Override
    public ICryptoTransform CreateEncryptor(byte[] key, Provider provider) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {

        return CreateEncryptor(key, null, provider);
    }

    @Override
    public ICryptoTransform CreateEncryptor(byte[] key, byte[] iv) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {

        return CreateEncryptor(key, iv, null);
    }

    @Override
    public ICryptoTransform CreateEncryptor(byte[] key, byte[] iv, Provider provider) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {

        if (key == null) {
            throw new IllegalArgumentException("key");
        }

        if (key.length != 128 >> 3 && key.length != 192 >> 3 && key.length != 256 >> 3) {
            throw new IllegalArgumentException("key length must be 128, 192 or 256 bits");
        }

        if (iv != null ) {
        	// iv length must be 64 bits
        	if ( iv.length != 8) {
	            throw new IllegalArgumentException("iv length must be 64 bits");
        	}
        	// iv cannot be specified with the default provider
        	if (provider == null) {
        		throw new IllegalArgumentException("user specified iv is not supported with the default provider");
        	}
        }

        return new AesKwEncryptor(key, iv == null ? _defaultIv : iv, provider);

    }

    @Override
    public ICryptoTransform CreateDecryptor(byte[] key) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {

        return CreateDecryptor(key, null, null);
    }

    @Override
    public ICryptoTransform CreateDecryptor(byte[] key, Provider provider) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {

        return CreateDecryptor(key, null, provider);
    }

    @Override
    public ICryptoTransform CreateDecryptor(byte[] key, byte[] iv) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {
        return CreateDecryptor(key, iv, null);
    }

    @Override
    public ICryptoTransform CreateDecryptor(byte[] key, byte[] iv, Provider provider) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException {

        if (key == null) {
            throw new IllegalArgumentException("key");
        }

        if (key.length != 128 >> 3 && key.length != 192 >> 3 && key.length != 256 >> 3) {
            throw new IllegalArgumentException("key length must be 128, 192 or 256 bits");
        }


        if (iv != null ) {
        	// iv length must be 64 bits
        	if ( iv.length != 8) {
	            throw new IllegalArgumentException("iv length must be 64 bits");
        	}
        	// iv cannot be specified with the default provider
        	if (provider == null) {
        		throw new IllegalArgumentException("user specified iv is not supported with the default provider");
        	}
        }

        return new AesKwDecryptor(key, iv == null ? _defaultIv : iv, provider);
    }

}

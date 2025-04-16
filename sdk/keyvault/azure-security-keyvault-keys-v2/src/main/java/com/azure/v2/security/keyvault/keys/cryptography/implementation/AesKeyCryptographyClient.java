// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.security.keyvault.keys.cryptography.implementation;

import com.azure.v2.security.keyvault.keys.cryptography.models.DecryptParameters;
import com.azure.v2.security.keyvault.keys.cryptography.models.DecryptResult;
import com.azure.v2.security.keyvault.keys.cryptography.models.EncryptParameters;
import com.azure.v2.security.keyvault.keys.cryptography.models.EncryptResult;
import com.azure.v2.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.azure.v2.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.azure.v2.security.keyvault.keys.cryptography.models.SignResult;
import com.azure.v2.security.keyvault.keys.cryptography.models.SignatureAlgorithm;
import com.azure.v2.security.keyvault.keys.cryptography.models.UnwrapResult;
import com.azure.v2.security.keyvault.keys.cryptography.models.VerifyResult;
import com.azure.v2.security.keyvault.keys.cryptography.models.WrapResult;
import com.azure.v2.security.keyvault.keys.models.JsonWebKey;
import com.azure.v2.security.keyvault.keys.models.KeyOperation;
import io.clientcore.core.http.models.RequestOptions;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Objects;

import static com.azure.v2.security.keyvault.keys.cryptography.implementation.CryptographyUtils.verifyKeyPermissions;

class AesKeyCryptographyClient extends LocalKeyCryptographyClient {
    static final int AES_BLOCK_SIZE = 16;
    private final byte[] aesKey;

    AesKeyCryptographyClient(JsonWebKey jsonWebKey, CryptographyClientImpl implClient) {
        super(jsonWebKey, implClient);

        aesKey = jsonWebKey.toAes().getEncoded();

        if (aesKey == null || aesKey.length == 0) {
            throw new IllegalArgumentException("The provided JSON Web Key cannot be null or empty.");
        }
    }

    private static void validateEncryptionAlgorithm(EncryptionAlgorithm algorithm) {
        if (isGcm(algorithm)) {
            throw new UnsupportedOperationException("AES-GCM is not supported for local cryptography operations.");
        }

        if (!isAes(algorithm)) {
            throw new IllegalArgumentException("Encryption algorithm provided is not supported: " + algorithm);
        }
    }

    private static byte[] generateIv(int sizeInBytes) throws NoSuchAlgorithmException {
        SecureRandom randomSecureRandom = SecureRandom.getInstance("SHA1PRNG");
        byte[] iv = new byte[sizeInBytes];

        randomSecureRandom.nextBytes(iv);

        return iv;
    }

    private static boolean isAes(EncryptionAlgorithm encryptionAlgorithm) {
        return (encryptionAlgorithm == EncryptionAlgorithm.A128CBC
            || encryptionAlgorithm == EncryptionAlgorithm.A192CBC
            || encryptionAlgorithm == EncryptionAlgorithm.A256CBC
            || encryptionAlgorithm == EncryptionAlgorithm.A128CBCPAD
            || encryptionAlgorithm == EncryptionAlgorithm.A192CBCPAD
            || encryptionAlgorithm == EncryptionAlgorithm.A256CBCPAD);
    }

    private static boolean isGcm(EncryptionAlgorithm encryptionAlgorithm) {
        return (encryptionAlgorithm == EncryptionAlgorithm.A128GCM
            || encryptionAlgorithm == EncryptionAlgorithm.A192GCM
            || encryptionAlgorithm == EncryptionAlgorithm.A256GCM);
    }

    @Override
    public EncryptResult encrypt(EncryptionAlgorithm algorithm, byte[] plaintext, RequestOptions requestOptions) {
        try {
            return encryptInternal(algorithm, plaintext, null, null, requestOptions);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public EncryptResult encrypt(EncryptParameters encryptParameters, RequestOptions requestOptions) {
        Objects.requireNonNull(encryptParameters, "Encrypt parameters cannot be null.");

        try {
            return encryptInternal(encryptParameters.getAlgorithm(), encryptParameters.getPlainText(),
                encryptParameters.getIv(), encryptParameters.getAdditionalAuthenticatedData(), requestOptions);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private EncryptResult encryptInternal(EncryptionAlgorithm algorithm, byte[] plaintext, byte[] iv,
        byte[] additionalAuthenticatedData, RequestOptions requestOptions)
        throws BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeyException,
        NoSuchAlgorithmException, NoSuchPaddingException, IOException {

        Objects.requireNonNull(algorithm, "Encryption algorithm cannot be null.");
        Objects.requireNonNull(plaintext, "Plaintext cannot be null.");

        // Interpret the algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (!(baseAlgorithm instanceof SymmetricEncryptionAlgorithm)) {
            if (implClient != null) {
                return implClient.encrypt(algorithm, plaintext, null, null, requestOptions);
            }

            throw new NoSuchAlgorithmException(algorithm.toString());
        }

        verifyKeyPermissions(jsonWebKey, KeyOperation.ENCRYPT);
        validateEncryptionAlgorithm(algorithm);

        SymmetricEncryptionAlgorithm symmetricEncryptionAlgorithm = (SymmetricEncryptionAlgorithm) baseAlgorithm;

        if (iv == null) {
            if (isAes(algorithm)) {
                try {
                    iv = generateIv(AES_BLOCK_SIZE);
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException("Could not generate iv for this local operation.", e);
                }
            } else {
                throw new IllegalArgumentException("Encryption algorithm provided is not supported: " + algorithm);
            }
        }

        byte[] ciphertext = symmetricEncryptionAlgorithm.createEncryptor(aesKey, iv, additionalAuthenticatedData, null)
            .doFinal(plaintext);

        return new EncryptResult(ciphertext, algorithm, jsonWebKey.getId(), iv, null, additionalAuthenticatedData);
    }

    @Override
    public DecryptResult decrypt(EncryptionAlgorithm algorithm, byte[] ciphertext, RequestOptions requestOptions) {
        try {
            return decryptInternal(algorithm, ciphertext, null, null, null, requestOptions);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DecryptResult decrypt(DecryptParameters decryptParameters, RequestOptions requestOptions) {
        Objects.requireNonNull(decryptParameters, "Decrypt parameters cannot be null.");

        try {
            return decryptInternal(decryptParameters.getAlgorithm(), decryptParameters.getCipherText(),
                decryptParameters.getIv(), decryptParameters.getAdditionalAuthenticatedData(),
                decryptParameters.getAuthenticationTag(), requestOptions);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private DecryptResult decryptInternal(EncryptionAlgorithm algorithm, byte[] ciphertext, byte[] iv,
        byte[] additionalAuthenticatedData, byte[] authenticationTag, RequestOptions requestOptions)
        throws BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeyException,
        NoSuchAlgorithmException, NoSuchPaddingException, IOException {

        Objects.requireNonNull(algorithm, "Encryption algorithm cannot be null.");
        Objects.requireNonNull(ciphertext, "Ciphertext cannot be null.");

        // Interpret the algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (!(baseAlgorithm instanceof SymmetricEncryptionAlgorithm)) {
            if (implClient != null) {
                return implClient.decrypt(algorithm, ciphertext, requestOptions);
            }

            throw new NoSuchAlgorithmException(algorithm.toString());
        }

        verifyKeyPermissions(jsonWebKey, KeyOperation.DECRYPT);
        validateEncryptionAlgorithm(algorithm);

        SymmetricEncryptionAlgorithm symmetricEncryptionAlgorithm = (SymmetricEncryptionAlgorithm) baseAlgorithm;

        Objects.requireNonNull(iv, "'iv' cannot be null in local decryption operations.");

        byte[] plaintext = symmetricEncryptionAlgorithm.createDecryptor(aesKey, iv, additionalAuthenticatedData,
            authenticationTag).doFinal(ciphertext);

        return new DecryptResult(plaintext, algorithm, jsonWebKey.getId());
    }

    @Override
    public SignResult sign(SignatureAlgorithm algorithm, byte[] digest, RequestOptions requestOptions) {
        throw new UnsupportedOperationException("The sign operation not supported for OCT/symmetric keys.");
    }

    public VerifyResult verify(SignatureAlgorithm algorithm, byte[] digest, byte[] signature,
        RequestOptions requestOptions) {

        throw new UnsupportedOperationException("The verify operation is not supported for OCT/symmetric keys.");
    }

    @Override
    public WrapResult wrapKey(KeyWrapAlgorithm algorithm, byte[] keyToWrap, RequestOptions requestOptions) {
        Objects.requireNonNull(algorithm, "Key wrap algorithm cannot be null.");
        Objects.requireNonNull(keyToWrap, "Key content to be wrapped cannot be null.");

        // Interpret the algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (!(baseAlgorithm instanceof LocalKeyWrapAlgorithm)) {
            if (implClient != null) {
                try {
                    return implClient.wrapKey(algorithm, keyToWrap, requestOptions);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }

            throw new RuntimeException(new NoSuchAlgorithmException(algorithm.toString()));
        }

        verifyKeyPermissions(jsonWebKey, KeyOperation.WRAP_KEY);

        LocalKeyWrapAlgorithm localKeyWrapAlgorithm = (LocalKeyWrapAlgorithm) baseAlgorithm;

        ICryptoTransform transform;

        try {
            transform = localKeyWrapAlgorithm.createEncryptor(aesKey, null, null);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }

        byte[] encrypted;

        try {
            encrypted = transform.doFinal(keyToWrap);
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new RuntimeException(e);
            }
        }

        return new WrapResult(encrypted, algorithm, jsonWebKey.getId());
    }

    @Override
    public UnwrapResult unwrapKey(KeyWrapAlgorithm algorithm, byte[] encryptedKey, RequestOptions requestOptions) {
        Objects.requireNonNull(algorithm, "Key wrap algorithm cannot be null.");
        Objects.requireNonNull(encryptedKey, "Encrypted key content to be unwrapped cannot be null.");

        verifyKeyPermissions(jsonWebKey, KeyOperation.UNWRAP_KEY);

        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (!(baseAlgorithm instanceof LocalKeyWrapAlgorithm)) {
            if (implClient != null) {
                try {
                    return implClient.unwrapKey(algorithm, encryptedKey, requestOptions);
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            }

            throw new RuntimeException(new NoSuchAlgorithmException(algorithm.toString()));
        }

        verifyKeyPermissions(jsonWebKey, KeyOperation.UNWRAP_KEY);

        LocalKeyWrapAlgorithm localKeyWrapAlgorithm = (LocalKeyWrapAlgorithm) baseAlgorithm;

        try {
            byte[] decrypted = localKeyWrapAlgorithm.createDecryptor(aesKey, null, null).doFinal(encryptedKey);

            return new UnwrapResult(decrypted, algorithm, jsonWebKey.getId());
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SignResult signData(SignatureAlgorithm algorithm, byte[] data, RequestOptions requestOptions) {
        return sign(algorithm, data, requestOptions);
    }

    public VerifyResult verifyData(SignatureAlgorithm algorithm, byte[] data, byte[] signature,
        RequestOptions requestOptions) {

        return verify(algorithm, data, signature, requestOptions);
    }
}

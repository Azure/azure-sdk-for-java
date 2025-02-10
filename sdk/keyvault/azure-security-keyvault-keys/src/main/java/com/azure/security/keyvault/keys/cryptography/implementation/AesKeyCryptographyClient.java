// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography.implementation;

import com.azure.core.util.Context;
import com.azure.security.keyvault.keys.cryptography.models.DecryptParameters;
import com.azure.security.keyvault.keys.cryptography.models.DecryptResult;
import com.azure.security.keyvault.keys.cryptography.models.EncryptParameters;
import com.azure.security.keyvault.keys.cryptography.models.EncryptResult;
import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.SignResult;
import com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.UnwrapResult;
import com.azure.security.keyvault.keys.cryptography.models.VerifyResult;
import com.azure.security.keyvault.keys.cryptography.models.WrapResult;
import com.azure.security.keyvault.keys.models.JsonWebKey;
import com.azure.security.keyvault.keys.models.KeyOperation;
import reactor.core.publisher.Mono;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Objects;

import static com.azure.security.keyvault.keys.cryptography.implementation.CryptographyUtils.verifyKeyPermissions;

class AesKeyCryptographyClient extends LocalKeyCryptographyClient {
    private final byte[] aesKey;

    static final int AES_BLOCK_SIZE = 16;

    AesKeyCryptographyClient(JsonWebKey jsonWebKey, CryptographyClientImpl implClient) {
        super(jsonWebKey, implClient);

        aesKey = jsonWebKey.toAes().getEncoded();

        if (aesKey == null || aesKey.length == 0) {
            throw new IllegalArgumentException("The provided JSON Web Key cannot be null or empty.");
        }
    }

    @Override
    public Mono<EncryptResult> encryptAsync(EncryptionAlgorithm algorithm, byte[] plaintext, Context context) {
        try {
            return encryptInternalAsync(algorithm, plaintext, null, null, context);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public EncryptResult encrypt(EncryptionAlgorithm algorithm, byte[] plaintext, Context context) {
        try {
            return encryptInternal(algorithm, plaintext, null, null, context);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Mono<EncryptResult> encryptAsync(EncryptParameters encryptParameters, Context context) {
        Objects.requireNonNull(encryptParameters, "Encrypt parameters cannot be null.");

        try {
            return encryptInternalAsync(encryptParameters.getAlgorithm(), encryptParameters.getPlainText(),
                encryptParameters.getIv(), encryptParameters.getAdditionalAuthenticatedData(), context);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public EncryptResult encrypt(EncryptParameters encryptParameters, Context context) {
        Objects.requireNonNull(encryptParameters, "Encrypt parameters cannot be null.");

        try {
            return encryptInternal(encryptParameters.getAlgorithm(), encryptParameters.getPlainText(),
                encryptParameters.getIv(), encryptParameters.getAdditionalAuthenticatedData(), context);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Mono<EncryptResult> encryptInternalAsync(EncryptionAlgorithm algorithm, byte[] plaintext, byte[] iv,
        byte[] additionalAuthenticatedData, Context context) throws NoSuchAlgorithmException {

        Objects.requireNonNull(algorithm, "Encryption algorithm cannot be null.");
        Objects.requireNonNull(plaintext, "Plaintext cannot be null.");

        // Interpret the algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (!(baseAlgorithm instanceof SymmetricEncryptionAlgorithm)) {
            if (implClient != null) {
                return implClient.encryptAsync(algorithm, plaintext, context);
            }

            throw new NoSuchAlgorithmException(algorithm.toString());
        }

        verifyKeyPermissions(jsonWebKey, KeyOperation.ENCRYPT);
        validateEncryptionAlgorithm(algorithm);

        SymmetricEncryptionAlgorithm symmetricEncryptionAlgorithm = (SymmetricEncryptionAlgorithm) baseAlgorithm;

        final byte[] finalIv;

        if (iv == null) {
            if (isAes(algorithm)) {
                try {
                    finalIv = generateIv(AES_BLOCK_SIZE);
                } catch (NoSuchAlgorithmException e) {
                    throw new RuntimeException("Could not generate iv for this local operation.", e);
                }
            } else {
                throw new IllegalArgumentException("Encryption algorithm provided is not supported: " + algorithm);
            }
        } else {
            finalIv = iv;
        }

        return Mono.fromCallable(() -> {
            byte[] ciphertext
                = symmetricEncryptionAlgorithm.createEncryptor(aesKey, finalIv, additionalAuthenticatedData, null)
                    .doFinal(plaintext);

            return new EncryptResult(ciphertext, algorithm, jsonWebKey.getId(), finalIv, null,
                additionalAuthenticatedData);
        });
    }

    private EncryptResult encryptInternal(EncryptionAlgorithm algorithm, byte[] plaintext, byte[] iv,
        byte[] additionalAuthenticatedData, Context context) throws BadPaddingException, IllegalBlockSizeException,
        InvalidAlgorithmParameterException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {

        Objects.requireNonNull(algorithm, "Encryption algorithm cannot be null.");
        Objects.requireNonNull(plaintext, "Plaintext cannot be null.");

        // Interpret the algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (!(baseAlgorithm instanceof SymmetricEncryptionAlgorithm)) {
            if (implClient != null) {
                return implClient.encrypt(algorithm, plaintext, context);
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
    public Mono<DecryptResult> decryptAsync(EncryptionAlgorithm algorithm, byte[] ciphertext, Context context) {
        try {
            return decryptInternalAsync(algorithm, ciphertext, null, null, null, context);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DecryptResult decrypt(EncryptionAlgorithm algorithm, byte[] ciphertext, Context context) {
        try {
            return decryptInternal(algorithm, ciphertext, null, null, null, context);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Mono<DecryptResult> decryptAsync(DecryptParameters decryptParameters, Context context) {
        Objects.requireNonNull(decryptParameters, "Decrypt parameters cannot be null.");

        try {
            return decryptInternalAsync(decryptParameters.getAlgorithm(), decryptParameters.getCipherText(),
                decryptParameters.getIv(), decryptParameters.getAdditionalAuthenticatedData(),
                decryptParameters.getAuthenticationTag(), context);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public DecryptResult decrypt(DecryptParameters decryptParameters, Context context) {
        Objects.requireNonNull(decryptParameters, "Decrypt parameters cannot be null.");

        try {
            return decryptInternal(decryptParameters.getAlgorithm(), decryptParameters.getCipherText(),
                decryptParameters.getIv(), decryptParameters.getAdditionalAuthenticatedData(),
                decryptParameters.getAuthenticationTag(), context);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Mono<DecryptResult> decryptInternalAsync(EncryptionAlgorithm algorithm, byte[] ciphertext, byte[] iv,
        byte[] additionalAuthenticatedData, byte[] authenticationTag, Context context) throws NoSuchAlgorithmException {

        Objects.requireNonNull(algorithm, "Encryption algorithm cannot be null.");
        Objects.requireNonNull(ciphertext, "Ciphertext cannot be null.");

        // Interpret the algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (!(baseAlgorithm instanceof SymmetricEncryptionAlgorithm)) {
            if (implClient != null) {
                return implClient.decryptAsync(algorithm, ciphertext, context);
            }

            throw new NoSuchAlgorithmException(algorithm.toString());
        }

        verifyKeyPermissions(jsonWebKey, KeyOperation.DECRYPT);
        validateEncryptionAlgorithm(algorithm);

        SymmetricEncryptionAlgorithm symmetricEncryptionAlgorithm = (SymmetricEncryptionAlgorithm) baseAlgorithm;

        Objects.requireNonNull(iv, "'iv' cannot be null in local decryption operations.");

        return Mono.fromCallable(() -> {
            byte[] plaintext = symmetricEncryptionAlgorithm
                .createDecryptor(aesKey, iv, additionalAuthenticatedData, authenticationTag)
                .doFinal(ciphertext);

            return new DecryptResult(plaintext, algorithm, jsonWebKey.getId());
        });
    }

    private DecryptResult decryptInternal(EncryptionAlgorithm algorithm, byte[] ciphertext, byte[] iv,
        byte[] additionalAuthenticatedData, byte[] authenticationTag, Context context)
        throws BadPaddingException, IllegalBlockSizeException, InvalidAlgorithmParameterException, InvalidKeyException,
        NoSuchAlgorithmException, NoSuchPaddingException {

        Objects.requireNonNull(algorithm, "Encryption algorithm cannot be null.");
        Objects.requireNonNull(ciphertext, "Ciphertext cannot be null.");

        // Interpret the algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (!(baseAlgorithm instanceof SymmetricEncryptionAlgorithm)) {
            if (implClient != null) {
                return implClient.decrypt(algorithm, ciphertext, context);
            }

            throw new NoSuchAlgorithmException(algorithm.toString());
        }

        verifyKeyPermissions(jsonWebKey, KeyOperation.DECRYPT);
        validateEncryptionAlgorithm(algorithm);

        SymmetricEncryptionAlgorithm symmetricEncryptionAlgorithm = (SymmetricEncryptionAlgorithm) baseAlgorithm;

        Objects.requireNonNull(iv, "'iv' cannot be null in local decryption operations.");

        byte[] plaintext
            = symmetricEncryptionAlgorithm.createDecryptor(aesKey, iv, additionalAuthenticatedData, authenticationTag)
                .doFinal(ciphertext);

        return new DecryptResult(plaintext, algorithm, jsonWebKey.getId());
    }

    private static void validateEncryptionAlgorithm(EncryptionAlgorithm algorithm) {
        if (isGcm(algorithm)) {
            throw new UnsupportedOperationException("AES-GCM is not supported for local cryptography operations.");
        }

        if (!isAes(algorithm)) {
            throw new IllegalArgumentException("Encryption algorithm provided is not supported: " + algorithm);
        }
    }

    @Override
    public Mono<SignResult> signAsync(SignatureAlgorithm algorithm, byte[] digest, Context context) {
        throw new UnsupportedOperationException("The sign operation not supported for OCT/symmetric keys.");
    }

    @Override
    public SignResult sign(SignatureAlgorithm algorithm, byte[] digest, Context context) {
        throw new UnsupportedOperationException("The sign operation not supported for OCT/symmetric keys.");
    }

    @Override
    public Mono<VerifyResult> verifyAsync(SignatureAlgorithm algorithm, byte[] digest, byte[] signature,
        Context context) {
        throw new UnsupportedOperationException("The verify operation is not supported for OCT/symmetric keys.");
    }

    public VerifyResult verify(SignatureAlgorithm algorithm, byte[] digest, byte[] signature, Context context) {
        throw new UnsupportedOperationException("The verify operation is not supported for OCT/symmetric keys.");
    }

    @Override
    public Mono<WrapResult> wrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] keyToWrap, Context context) {
        Objects.requireNonNull(algorithm, "Key wrap algorithm cannot be null.");
        Objects.requireNonNull(keyToWrap, "Key content to be wrapped cannot be null.");

        // Interpret the algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (!(baseAlgorithm instanceof LocalKeyWrapAlgorithm)) {
            if (implClient != null) {
                return implClient.wrapKeyAsync(algorithm, keyToWrap, context);
            }

            throw new RuntimeException(new NoSuchAlgorithmException(algorithm.toString()));
        }

        verifyKeyPermissions(jsonWebKey, KeyOperation.WRAP_KEY);

        LocalKeyWrapAlgorithm localKeyWrapAlgorithm = (LocalKeyWrapAlgorithm) baseAlgorithm;

        return Mono.fromCallable(() -> {
            byte[] encrypted = localKeyWrapAlgorithm.createEncryptor(aesKey, null, null).doFinal(keyToWrap);

            return new WrapResult(encrypted, algorithm, jsonWebKey.getId());
        });
    }

    @Override
    public WrapResult wrapKey(KeyWrapAlgorithm algorithm, byte[] keyToWrap, Context context) {
        Objects.requireNonNull(algorithm, "Key wrap algorithm cannot be null.");
        Objects.requireNonNull(keyToWrap, "Key content to be wrapped cannot be null.");

        // Interpret the algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (!(baseAlgorithm instanceof LocalKeyWrapAlgorithm)) {
            if (implClient != null) {
                return implClient.wrapKey(algorithm, keyToWrap, context);
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
    public Mono<UnwrapResult> unwrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] encryptedKey, Context context) {
        Objects.requireNonNull(algorithm, "Key wrap algorithm cannot be null.");
        Objects.requireNonNull(encryptedKey, "Encrypted key content to be unwrapped cannot be null.");

        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (!(baseAlgorithm instanceof LocalKeyWrapAlgorithm)) {
            if (implClient != null) {
                return implClient.unwrapKeyAsync(algorithm, encryptedKey, context);
            }

            throw new RuntimeException(new NoSuchAlgorithmException(algorithm.toString()));
        }

        verifyKeyPermissions(jsonWebKey, KeyOperation.UNWRAP_KEY);

        LocalKeyWrapAlgorithm localKeyWrapAlgorithm = (LocalKeyWrapAlgorithm) baseAlgorithm;

        return Mono.fromCallable(() -> {
            byte[] decrypted = localKeyWrapAlgorithm.createDecryptor(aesKey, null, null).doFinal(encryptedKey);

            return new UnwrapResult(decrypted, algorithm, jsonWebKey.getId());
        });
    }

    @Override
    public UnwrapResult unwrapKey(KeyWrapAlgorithm algorithm, byte[] encryptedKey, Context context) {
        Objects.requireNonNull(algorithm, "Key wrap algorithm cannot be null.");
        Objects.requireNonNull(encryptedKey, "Encrypted key content to be unwrapped cannot be null.");

        verifyKeyPermissions(jsonWebKey, KeyOperation.UNWRAP_KEY);

        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (!(baseAlgorithm instanceof LocalKeyWrapAlgorithm)) {
            if (implClient != null) {
                return implClient.unwrapKey(algorithm, encryptedKey, context);
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
    public Mono<SignResult> signDataAsync(SignatureAlgorithm algorithm, byte[] data, Context context) {
        return signAsync(algorithm, data, context);
    }

    @Override
    public SignResult signData(SignatureAlgorithm algorithm, byte[] data, Context context) {
        return sign(algorithm, data, context);
    }

    @Override
    public Mono<VerifyResult> verifyDataAsync(SignatureAlgorithm algorithm, byte[] data, byte[] signature,
        Context context) {
        return verifyAsync(algorithm, data, signature, context);
    }

    public VerifyResult verifyData(SignatureAlgorithm algorithm, byte[] data, byte[] signature, Context context) {
        return verify(algorithm, data, signature, context);
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
}

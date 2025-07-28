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
import io.clientcore.core.http.models.RequestContext;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.models.CoreException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.GeneralSecurityException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Objects;

import static com.azure.v2.security.keyvault.keys.cryptography.implementation.CryptographyUtils.verifyKeyPermissions;

class AesKeyCryptographyClient extends LocalKeyCryptographyClient {
    private static final ClientLogger LOGGER = new ClientLogger(AesKeyCryptographyClient.class);
    static final int AES_BLOCK_SIZE = 16;
    private final byte[] aesKey;

    AesKeyCryptographyClient(JsonWebKey jsonWebKey, CryptographyClientImpl implClient) {
        super(jsonWebKey, implClient);

        aesKey = jsonWebKey.toAes().getEncoded();

        if (aesKey == null || aesKey.length == 0) {
            throw LOGGER.throwableAtError()
                .log("The provided JSON Web Key cannot be null or empty.", IllegalArgumentException::new);
        }
    }

    private static void validateEncryptionAlgorithm(EncryptionAlgorithm algorithm) {
        if (isGcm(algorithm)) {
            throw LOGGER.throwableAtError()
                .addKeyValue("algorithm", algorithm.getValue())
                .log("AES-GCM is not supported for local cryptography operations.", UnsupportedOperationException::new);
        }

        if (!isAes(algorithm)) {
            throw LOGGER.throwableAtError()
                .addKeyValue("algorithm", algorithm.getValue())
                .log("Encryption algorithm provided is not supported.", IllegalArgumentException::new);
        }
    }

    private static byte[] generateIv(int sizeInBytes) {
        try {
            SecureRandom randomSecureRandom = SecureRandom.getInstance("SHA1PRNG");
            byte[] iv = new byte[sizeInBytes];

            randomSecureRandom.nextBytes(iv);

            return iv;
        } catch (NoSuchAlgorithmException e) {
            throw LOGGER.throwableAtError()
                .addKeyValue("algorithm", "SHA1PRNG")
                .log("Could not generate iv for this local operation.", e, CoreException::from);
        }
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
    public EncryptResult encrypt(EncryptionAlgorithm algorithm, byte[] plaintext, RequestContext requestContext) {
        return encryptInternal(algorithm, plaintext, null, null, requestContext);
    }

    @Override
    public EncryptResult encrypt(EncryptParameters encryptParameters, RequestContext requestContext) {
        Objects.requireNonNull(encryptParameters, "Encrypt parameters cannot be null.");

        return encryptInternal(encryptParameters.getAlgorithm(), encryptParameters.getPlainText(),
            encryptParameters.getIv(), encryptParameters.getAdditionalAuthenticatedData(), requestContext);
    }

    private EncryptResult encryptInternal(EncryptionAlgorithm algorithm, byte[] plaintext, byte[] iv,
        byte[] additionalAuthenticatedData, RequestContext requestContext) {

        Objects.requireNonNull(algorithm, "Encryption algorithm cannot be null.");
        Objects.requireNonNull(plaintext, "Plaintext cannot be null.");

        // Interpret the algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (!(baseAlgorithm instanceof SymmetricEncryptionAlgorithm)) {
            if (implClient != null) {
                return implClient.encrypt(algorithm, plaintext, null, null, requestContext);
            }

            throw LOGGER.throwableAtError()
                .addKeyValue("algorithm", algorithm.getValue())
                .log("Unsupported encryption algorithm.", CoreException::from);
        }

        verifyKeyPermissions(jsonWebKey, KeyOperation.ENCRYPT, LOGGER);
        validateEncryptionAlgorithm(algorithm);

        SymmetricEncryptionAlgorithm symmetricEncryptionAlgorithm = (SymmetricEncryptionAlgorithm) baseAlgorithm;

        if (iv == null) {
            if (isAes(algorithm)) {
                iv = generateIv(AES_BLOCK_SIZE);
            } else {
                throw LOGGER.throwableAtError()
                    .addKeyValue("algorithm", algorithm.getValue())
                    .log("Unsupported encryption algorithm provided.", CoreException::from);
            }
        }

        byte[] ciphertext;
        try {
            ciphertext = symmetricEncryptionAlgorithm.createEncryptor(aesKey, iv, additionalAuthenticatedData, null)
                .doFinal(plaintext);
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
            | BadPaddingException | InvalidAlgorithmParameterException ex) {
            throw LOGGER.throwableAtError().log(ex, CoreException::from);
        }

        return new EncryptResult(ciphertext, algorithm, jsonWebKey.getId(), iv, null, additionalAuthenticatedData);
    }

    @Override
    public DecryptResult decrypt(EncryptionAlgorithm algorithm, byte[] ciphertext, RequestContext requestContext) {
        return decryptInternal(algorithm, ciphertext, null, null, null, requestContext);
    }

    @Override
    public DecryptResult decrypt(DecryptParameters decryptParameters, RequestContext requestContext) {
        Objects.requireNonNull(decryptParameters, "Decrypt parameters cannot be null.");

        return decryptInternal(decryptParameters.getAlgorithm(), decryptParameters.getCipherText(),
            decryptParameters.getIv(), decryptParameters.getAdditionalAuthenticatedData(),
            decryptParameters.getAuthenticationTag(), requestContext);
    }

    private DecryptResult decryptInternal(EncryptionAlgorithm algorithm, byte[] ciphertext, byte[] iv,
        byte[] additionalAuthenticatedData, byte[] authenticationTag, RequestContext requestContext) {

        Objects.requireNonNull(algorithm, "Encryption algorithm cannot be null.");
        Objects.requireNonNull(ciphertext, "Ciphertext cannot be null.");

        // Interpret the algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (!(baseAlgorithm instanceof SymmetricEncryptionAlgorithm)) {
            if (implClient != null) {
                return implClient.decrypt(algorithm, ciphertext, requestContext);
            }

            throw LOGGER.throwableAtError()
                .addKeyValue("algorithm", algorithm.getValue())
                .log("Unsupported encryption algorithm.", CoreException::from);
        }

        verifyKeyPermissions(jsonWebKey, KeyOperation.DECRYPT, LOGGER);
        validateEncryptionAlgorithm(algorithm);

        SymmetricEncryptionAlgorithm symmetricEncryptionAlgorithm = (SymmetricEncryptionAlgorithm) baseAlgorithm;

        Objects.requireNonNull(iv, "'iv' cannot be null in local decryption operations.");

        byte[] plaintext;

        try {
            plaintext = symmetricEncryptionAlgorithm
                .createDecryptor(aesKey, iv, additionalAuthenticatedData, authenticationTag)
                .doFinal(ciphertext);
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
            | BadPaddingException | InvalidAlgorithmParameterException ex) {
            throw LOGGER.throwableAtError().log(ex, CoreException::from);
        }

        return new DecryptResult(plaintext, algorithm, jsonWebKey.getId());
    }

    @Override
    public SignResult sign(SignatureAlgorithm algorithm, byte[] digest, RequestContext requestContext) {
        throw LOGGER.throwableAtError()
            .log("The sign operation not supported for OCT/symmetric keys.", UnsupportedOperationException::new);
    }

    @Override
    public VerifyResult verify(SignatureAlgorithm algorithm, byte[] digest, byte[] signature,
        RequestContext requestContext) {

        throw LOGGER.throwableAtError()
            .log("The verify operation not supported for OCT/symmetric keys.", UnsupportedOperationException::new);
    }

    @Override
    public WrapResult wrapKey(KeyWrapAlgorithm algorithm, byte[] keyToWrap, RequestContext requestContext) {
        Objects.requireNonNull(algorithm, "Key wrap algorithm cannot be null.");
        Objects.requireNonNull(keyToWrap, "Key content to be wrapped cannot be null.");

        // Interpret the algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (!(baseAlgorithm instanceof LocalKeyWrapAlgorithm)) {
            if (implClient != null) {
                return implClient.wrapKey(algorithm, keyToWrap, requestContext);
            }

            throw LOGGER.throwableAtError()
                .addKeyValue("algorithm", algorithm.getValue())
                .log("Algorithm not supported.", IllegalArgumentException::new);
        }

        verifyKeyPermissions(jsonWebKey, KeyOperation.WRAP_KEY, LOGGER);

        LocalKeyWrapAlgorithm localKeyWrapAlgorithm = (LocalKeyWrapAlgorithm) baseAlgorithm;

        ICryptoTransform transform;

        try {
            transform = localKeyWrapAlgorithm.createEncryptor(aesKey, null, null);
        } catch (GeneralSecurityException e) {
            throw LOGGER.throwableAtError().log(e, CoreException::from);
        }

        byte[] encrypted;

        try {
            encrypted = transform.doFinal(keyToWrap);
        } catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException | NoSuchAlgorithmException e) {
            throw LOGGER.throwableAtError().log(e, CoreException::from);
        }

        return new WrapResult(encrypted, algorithm, jsonWebKey.getId());
    }

    @Override
    public UnwrapResult unwrapKey(KeyWrapAlgorithm algorithm, byte[] encryptedKey, RequestContext requestContext) {
        Objects.requireNonNull(algorithm, "Key wrap algorithm cannot be null.");
        Objects.requireNonNull(encryptedKey, "Encrypted key content to be unwrapped cannot be null.");

        verifyKeyPermissions(jsonWebKey, KeyOperation.UNWRAP_KEY, LOGGER);

        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (!(baseAlgorithm instanceof LocalKeyWrapAlgorithm)) {
            if (implClient != null) {
                return implClient.unwrapKey(algorithm, encryptedKey, requestContext);
            }

            throw LOGGER.throwableAtError()
                .addKeyValue("algorithm", algorithm.getValue())
                .log("Algorithm not supported.", IllegalArgumentException::new);
        }

        verifyKeyPermissions(jsonWebKey, KeyOperation.UNWRAP_KEY, LOGGER);

        LocalKeyWrapAlgorithm localKeyWrapAlgorithm = (LocalKeyWrapAlgorithm) baseAlgorithm;

        try {
            byte[] decrypted = localKeyWrapAlgorithm.createDecryptor(aesKey, null, null).doFinal(encryptedKey);

            return new UnwrapResult(decrypted, algorithm, jsonWebKey.getId());
        } catch (GeneralSecurityException e) {
            throw LOGGER.throwableAtError().log(e, CoreException::from);
        }
    }

    @Override
    public SignResult signData(SignatureAlgorithm algorithm, byte[] data, RequestContext requestContext) {
        return sign(algorithm, data, requestContext);
    }

    @Override
    public VerifyResult verifyData(SignatureAlgorithm algorithm, byte[] data, byte[] signature,
        RequestContext requestContext) {

        return verify(algorithm, data, signature, requestContext);
    }
}

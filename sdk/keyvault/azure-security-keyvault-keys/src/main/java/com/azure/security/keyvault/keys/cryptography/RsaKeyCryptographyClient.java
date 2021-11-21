// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
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
import reactor.core.publisher.Mono;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

class RsaKeyCryptographyClient extends LocalKeyCryptographyClient {
    private KeyPair keyPair;
    private final ClientLogger logger = new ClientLogger(RsaKeyCryptographyClient.class);

    /*
     * Creates a RsaKeyCryptographyClient that uses {@code serviceClient) to service requests
     *
     * @param keyPair the key pair to use for cryptography operations.
     */
    RsaKeyCryptographyClient(CryptographyServiceClient serviceClient) {
        super(serviceClient);
    }

    RsaKeyCryptographyClient(JsonWebKey key, CryptographyServiceClient serviceClient) {
        super(serviceClient);

        keyPair = key.toRsa(key.hasPrivateKey());
    }

    private KeyPair getKeyPair(JsonWebKey key) {
        if (keyPair == null) {
            keyPair = key.toRsa(key.hasPrivateKey());
        }

        return keyPair;
    }

    @Override
    Mono<EncryptResult> encryptAsync(EncryptionAlgorithm algorithm, byte[] plaintext, Context context,
                                     JsonWebKey jsonWebKey) {
        Objects.requireNonNull(algorithm, "'algorithm' cannot be null.");
        Objects.requireNonNull(plaintext, "'plaintext' cannot be null.");

        return encryptInternal(algorithm, plaintext, context, jsonWebKey);
    }

    @Override
    Mono<EncryptResult> encryptAsync(EncryptParameters encryptParameters, Context context, JsonWebKey jsonWebKey) {
        Objects.requireNonNull(encryptParameters, "'encryptParameters' cannot be null.");
        Objects.requireNonNull(encryptParameters.getAlgorithm(), "encryptParameters.getAlgorithm() cannot be null.");
        Objects.requireNonNull(encryptParameters.getPlainText(), "encryptParameters.getPlainText() cannot be null.");

        return encryptInternal(encryptParameters.getAlgorithm(), encryptParameters.getPlainText(), context, jsonWebKey);
    }

    private Mono<EncryptResult> encryptInternal(EncryptionAlgorithm algorithm, byte[] plaintext, Context context,
                                                JsonWebKey jsonWebKey) {
        keyPair = getKeyPair(jsonWebKey);

        // Interpret the requested algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if (serviceCryptoAvailable()) {
                return serviceClient.encrypt(algorithm, plaintext, context);
            }

            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        } else if (!(baseAlgorithm instanceof AsymmetricEncryptionAlgorithm)) {
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        }

        if (keyPair.getPublic() == null) {
            if (serviceCryptoAvailable()) {
                return serviceClient.encrypt(algorithm, plaintext, context);
            }

            return Mono.error(new IllegalArgumentException(
                "Public portion of the key not available to perform encrypt operation"));
        }

        AsymmetricEncryptionAlgorithm algo = (AsymmetricEncryptionAlgorithm) baseAlgorithm;

        ICryptoTransform transform;

        try {
            transform = algo.createEncryptor(keyPair);

            return Mono.just(new EncryptResult(transform.doFinal(plaintext), algorithm,
                jsonWebKey.getId()));
        } catch (InvalidKeyException
            | NoSuchAlgorithmException
            | NoSuchPaddingException
            | IllegalBlockSizeException
            | BadPaddingException e) {

            return Mono.error(e);
        }
    }

    @Override
    Mono<DecryptResult> decryptAsync(EncryptionAlgorithm algorithm, byte[] ciphertext, Context context,
                                     JsonWebKey jsonWebKey) {
        Objects.requireNonNull(algorithm, "'algorithm' cannot be null.");
        Objects.requireNonNull(ciphertext, "'ciphertext' cannot be null.");

        return decryptInternal(algorithm, ciphertext, context, jsonWebKey);
    }

    @Override
    Mono<DecryptResult> decryptAsync(DecryptParameters decryptParameters, Context context, JsonWebKey jsonWebKey) {
        Objects.requireNonNull(decryptParameters, "'decryptOptions' cannot be null.");
        Objects.requireNonNull(decryptParameters.getAlgorithm(), "decryptParameters.getAlgorithm() cannot be null.");
        Objects.requireNonNull(decryptParameters.getCipherText(), "decryptParameters.getCipherText() cannot be null.");

        return decryptInternal(decryptParameters.getAlgorithm(), decryptParameters.getCipherText(), context,
            jsonWebKey);
    }

    Mono<DecryptResult> decryptInternal(EncryptionAlgorithm algorithm, byte[] ciphertext, Context context,
                                        JsonWebKey jsonWebKey) {
        keyPair = getKeyPair(jsonWebKey);

        // Interpret the requested algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if (serviceCryptoAvailable()) {
                return serviceClient.decrypt(algorithm, ciphertext, context);
            }

            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        } else if (!(baseAlgorithm instanceof AsymmetricEncryptionAlgorithm)) {
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        }

        if (keyPair.getPrivate() == null) {
            if (serviceCryptoAvailable()) {
                return serviceClient.decrypt(algorithm, ciphertext, context);
            }

            return Mono.error(new IllegalArgumentException(
                "Private portion of the key not available to perform decrypt operation"));
        }

        AsymmetricEncryptionAlgorithm algo = (AsymmetricEncryptionAlgorithm) baseAlgorithm;

        ICryptoTransform transform;

        try {
            transform = algo.createDecryptor(keyPair);

            return Mono.just(new DecryptResult(transform.doFinal(ciphertext), algorithm,
                jsonWebKey.getId()));
        } catch (InvalidKeyException
            | NoSuchAlgorithmException
            | NoSuchPaddingException
            | IllegalBlockSizeException
            | BadPaddingException e) {

            return Mono.error(e);
        }
    }

    @Override
    Mono<SignResult> signAsync(SignatureAlgorithm algorithm, byte[] digest, Context context, JsonWebKey key) {
        if (serviceCryptoAvailable()) {
            return serviceClient.sign(algorithm, digest, context);
        } else {
            return FluxUtil.monoError(logger,
                new UnsupportedOperationException("Sign operation on Local RSA key is not supported currently."));
        }
    }

    @Override
    Mono<VerifyResult> verifyAsync(SignatureAlgorithm algorithm, byte[] digest, byte[] signature, Context context,
                                   JsonWebKey key) {
        if (serviceCryptoAvailable()) {
            return serviceClient.verify(algorithm, digest, signature, context);
        } else {
            return FluxUtil.monoError(logger,
                new UnsupportedOperationException("Verify operation on Local RSA key is not supported currently."));
        }
    }

    @Override
    Mono<WrapResult> wrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] key, Context context, JsonWebKey jsonWebKey) {
        keyPair = getKeyPair(jsonWebKey);

        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if (serviceCryptoAvailable()) {
                return serviceClient.wrapKey(algorithm, key, context);
            }
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        } else if (!(baseAlgorithm instanceof AsymmetricEncryptionAlgorithm)) {
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        }

        if (keyPair.getPublic() == null) {
            if (serviceCryptoAvailable()) {
                return serviceClient.wrapKey(algorithm, key, context);
            }
            return Mono.error(new IllegalArgumentException(
                "Public portion of the key not available to perform wrap key operation"));
        }

        AsymmetricEncryptionAlgorithm algo = (AsymmetricEncryptionAlgorithm) baseAlgorithm;

        ICryptoTransform transform;

        try {
            transform = algo.createEncryptor(keyPair);
            return Mono.just(new WrapResult(transform.doFinal(key), algorithm, jsonWebKey.getId()));
        } catch (InvalidKeyException
            | NoSuchAlgorithmException
            | NoSuchPaddingException
            | IllegalBlockSizeException
            | BadPaddingException e) {
            return Mono.error(e);
        }
    }

    @Override
    Mono<UnwrapResult> unwrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] encryptedKey, Context context,
                                      JsonWebKey jsonWebKey) {
        keyPair = getKeyPair(jsonWebKey);

        // Interpret the requested algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if (serviceCryptoAvailable()) {
                return serviceClient.unwrapKey(algorithm, encryptedKey, context);
            }
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        } else if (!(baseAlgorithm instanceof AsymmetricEncryptionAlgorithm)) {
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        }

        if (keyPair.getPrivate() == null) {
            if (serviceCryptoAvailable()) {
                return serviceClient.unwrapKey(algorithm, encryptedKey, context);
            }
            return Mono.error(new IllegalArgumentException(
                "Private portion of the key not available to perform unwrap operation"));
        }

        AsymmetricEncryptionAlgorithm algo = (AsymmetricEncryptionAlgorithm) baseAlgorithm;

        ICryptoTransform transform;

        try {
            transform = algo.createDecryptor(keyPair);
            return Mono.just(new UnwrapResult(transform.doFinal(encryptedKey), algorithm, jsonWebKey.getId()));
        } catch (InvalidKeyException
            | NoSuchAlgorithmException
            | NoSuchPaddingException
            | IllegalBlockSizeException
            | BadPaddingException e) {
            return Mono.error(e);
        }
    }

    @Override
    Mono<SignResult> signDataAsync(SignatureAlgorithm algorithm, byte[] data, Context context, JsonWebKey key) {
        try {
            HashAlgorithm hashAlgorithm = SignatureHashResolver.DEFAULT.get(algorithm);
            MessageDigest md = MessageDigest.getInstance(hashAlgorithm.toString());
            md.update(data);
            byte[] digest = md.digest();
            return signAsync(algorithm, digest, context, key);
        } catch (NoSuchAlgorithmException e) {
            return Mono.error(e);
        }
    }

    @Override
    Mono<VerifyResult> verifyDataAsync(SignatureAlgorithm algorithm, byte[] data, byte[] signature, Context context,
                                       JsonWebKey key) {
        HashAlgorithm hashAlgorithm = SignatureHashResolver.DEFAULT.get(algorithm);
        try {
            MessageDigest md = MessageDigest.getInstance(hashAlgorithm.toString());
            md.update(data);
            byte[] digest = md.digest();
            return verifyAsync(algorithm, digest, signature, context, key);
        } catch (NoSuchAlgorithmException e) {
            return Mono.error(e);
        }
    }

    private boolean serviceCryptoAvailable() {
        return serviceClient != null;
    }

}

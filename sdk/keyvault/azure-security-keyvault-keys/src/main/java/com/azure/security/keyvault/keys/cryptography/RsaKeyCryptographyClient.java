// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.util.Context;
import com.azure.security.keyvault.keys.cryptography.models.DecryptResult;
import com.azure.security.keyvault.keys.cryptography.models.EncryptionAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.EncryptResult;
import com.azure.security.keyvault.keys.cryptography.models.UnwrapResult;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.SignatureAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.SignResult;
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

class RsaKeyCryptographyClient extends LocalKeyCryptographyClient {
    private KeyPair keyPair;

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
        keyPair = getKeyPair(jsonWebKey);

        // Interpret the requested algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.Default.get(algorithm.toString());

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
            return Mono.just(new EncryptResult(transform.doFinal(plaintext), algorithm, jsonWebKey.getId()));
        } catch (InvalidKeyException
            | NoSuchAlgorithmException
            | NoSuchPaddingException
            | IllegalBlockSizeException
            | BadPaddingException e) {
            return Mono.error(e);
        }
    }

    @Override
    Mono<DecryptResult> decryptAsync(EncryptionAlgorithm algorithm, byte[] cipherText, Context context,
                                     JsonWebKey jsonWebKey) {

        keyPair = getKeyPair(jsonWebKey);

        Algorithm baseAlgorithm = AlgorithmResolver.Default.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if (serviceCryptoAvailable()) {
                return serviceClient.decrypt(algorithm, cipherText, context);
            }
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        } else if (!(baseAlgorithm instanceof AsymmetricEncryptionAlgorithm)) {
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        }

        if (keyPair.getPrivate() == null) {
            if (serviceCryptoAvailable()) {
                return serviceClient.decrypt(algorithm, cipherText, context);
            }
            return Mono.error(new IllegalArgumentException(
                "Private portion of the key not available to perform decrypt operation"));
        }

        AsymmetricEncryptionAlgorithm algo = (AsymmetricEncryptionAlgorithm) baseAlgorithm;

        ICryptoTransform transform;

        try {
            transform = algo.createDecryptor(keyPair);
            return Mono.just(new DecryptResult(transform.doFinal(cipherText), algorithm, jsonWebKey.getId()));
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

        return serviceClient.sign(algorithm, digest, context);
    }

    @Override
    Mono<VerifyResult> verifyAsync(SignatureAlgorithm algorithm, byte[] digest, byte[] signature, Context context,
                                   JsonWebKey key) {

        return serviceClient.verify(algorithm, digest, signature, context);
        // do a service call for now.
    }

    @Override
    Mono<WrapResult> wrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] key, Context context, JsonWebKey jsonWebKey) {

        keyPair = getKeyPair(jsonWebKey);

        Algorithm baseAlgorithm = AlgorithmResolver.Default.get(algorithm.toString());

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
        Algorithm baseAlgorithm = AlgorithmResolver.Default.get(algorithm.toString());

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

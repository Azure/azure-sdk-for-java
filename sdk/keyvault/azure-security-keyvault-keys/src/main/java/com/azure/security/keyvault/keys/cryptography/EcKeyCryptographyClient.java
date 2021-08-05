// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.util.Context;
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

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.security.MessageDigest;

class EcKeyCryptographyClient extends LocalKeyCryptographyClient {
    private final ClientLogger logger = new ClientLogger(EcKeyCryptographyClient.class);

    private KeyPair keyPair;
    private final CryptographyServiceClient serviceClient;
    private final Provider provider;

    /**
     * Creates a EcKeyCryptographyClient that uses {@code service} to service requests
     *
     * @param serviceClient the client to use for service side cryptography operations.
     */
    EcKeyCryptographyClient(CryptographyServiceClient serviceClient) {
        super(serviceClient);
        this.serviceClient = serviceClient;
        this.provider = null;
    }

    EcKeyCryptographyClient(JsonWebKey key, CryptographyServiceClient serviceClient) {
        super(serviceClient);
        this.provider = Security.getProvider("SunEC");
        this.keyPair = key.toEc(key.hasPrivateKey(), provider);
        this.serviceClient = serviceClient;
    }

    private KeyPair getKeyPair(JsonWebKey key) {
        if (keyPair == null) {
            keyPair = key.toEc(key.hasPrivateKey());
        }
        return keyPair;
    }

    @Override
    Mono<EncryptResult> encryptAsync(EncryptionAlgorithm algorithm, byte[] plaintext, Context context, JsonWebKey key) {
        throw logger.logExceptionAsError(new UnsupportedOperationException(
            "Encrypt operation is not supported for EC key"));
    }

    @Override
    Mono<EncryptResult> encryptAsync(EncryptParameters options, Context context, JsonWebKey key) {
        throw logger.logExceptionAsError(new UnsupportedOperationException(
            "Encrypt operation is not supported for EC key"));
    }

    @Override
    Mono<DecryptResult> decryptAsync(EncryptionAlgorithm algorithm, byte[] plaintext, Context context, JsonWebKey key) {
        throw logger.logExceptionAsError(new UnsupportedOperationException(
            "Encrypt operation is not supported for EC key"));
    }

    @Override
    Mono<DecryptResult> decryptAsync(DecryptParameters options, Context context, JsonWebKey key) {
        throw logger.logExceptionAsError(new UnsupportedOperationException(
            "Decrypt operation is not supported for EC key"));
    }

    @Override
    Mono<SignResult> signAsync(SignatureAlgorithm algorithm, byte[] digest, Context context, JsonWebKey key) {
        keyPair = getKeyPair(key);

        // Interpret the requested algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if (serviceCryptoAvailable()) {
                return serviceClient.sign(algorithm, digest, context);
            }
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        } else if (!(baseAlgorithm instanceof AsymmetricSignatureAlgorithm)) {
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        }

        if (keyPair.getPrivate() == null) {
            if (serviceCryptoAvailable()) {
                return serviceClient.sign(algorithm, digest, context);
            }
            return Mono.error(new IllegalArgumentException(
                "Private portion of the key not available to perform sign operation"));
        }

        Ecdsa algo;
        if (baseAlgorithm instanceof Ecdsa) {
            algo = (Ecdsa) baseAlgorithm;
        } else {
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        }

        ISignatureTransform signer = algo.createSignatureTransform(keyPair, provider);

        try {
            return Mono.just(new SignResult(signer.sign(digest), algorithm, key.getId()));
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    @Override
    Mono<VerifyResult> verifyAsync(SignatureAlgorithm algorithm, byte[] digest, byte[] signature, Context context,
                                   JsonWebKey key) {

        keyPair = getKeyPair(key);

        // Interpret the requested algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if (serviceCryptoAvailable()) {
                return serviceClient.verify(algorithm, digest, signature, context);
            }
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        } else if (!(baseAlgorithm instanceof AsymmetricSignatureAlgorithm)) {
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        }

        if (keyPair.getPublic() == null) {
            if (serviceCryptoAvailable()) {
                return serviceClient.verify(algorithm, digest, signature, context);
            }
            return Mono.error(new IllegalArgumentException(
                "Public portion of the key not available to perform verify operation"));
        }

        Ecdsa algo;
        if (baseAlgorithm instanceof Ecdsa) {
            algo = (Ecdsa) baseAlgorithm;
        } else {
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        }

        ISignatureTransform signer = algo.createSignatureTransform(keyPair, provider);

        try {
            return Mono.just(new VerifyResult(signer.verify(digest, signature), algorithm, key.getId()));
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    @Override
    Mono<WrapResult> wrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] key, Context context, JsonWebKey webKey) {
        return Mono.error(new UnsupportedOperationException("Wrap key operation is not supported for EC key"));
    }

    @Override
    Mono<UnwrapResult> unwrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] encryptedKey, Context context,
                                      JsonWebKey key) {
        throw logger.logExceptionAsError(new UnsupportedOperationException(
            "Unwrap key operation is not supported for Ec key"));
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
        try {
            HashAlgorithm hashAlgorithm = SignatureHashResolver.DEFAULT.get(algorithm);
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

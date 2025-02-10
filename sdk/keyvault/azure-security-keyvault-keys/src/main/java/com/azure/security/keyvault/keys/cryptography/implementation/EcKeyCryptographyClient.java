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

import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Provider;
import java.security.Security;
import java.util.Objects;

import static com.azure.security.keyvault.keys.cryptography.implementation.CryptographyUtils.verifyKeyPermissions;

class EcKeyCryptographyClient extends LocalKeyCryptographyClient {
    private final KeyPair ecKeyPair;
    private final Provider provider;

    EcKeyCryptographyClient(JsonWebKey jsonWebKey, CryptographyClientImpl implClient) {
        super(jsonWebKey, implClient);

        provider = Security.getProvider("SunEC");
        ecKeyPair = jsonWebKey.toEc(jsonWebKey.hasPrivateKey(), provider);
    }

    @Override
    public Mono<EncryptResult> encryptAsync(EncryptionAlgorithm algorithm, byte[] plaintext, Context context) {
        throw new UnsupportedOperationException("The encrypt operation is not supported for EC keys.");
    }

    @Override
    public EncryptResult encrypt(EncryptionAlgorithm algorithm, byte[] plaintext, Context context) {
        throw new UnsupportedOperationException("The encrypt operation is not supported for EC keys.");
    }

    @Override
    public Mono<EncryptResult> encryptAsync(EncryptParameters options, Context context) {
        throw new UnsupportedOperationException("The encrypt operation is not supported for EC keys.");
    }

    @Override
    public EncryptResult encrypt(EncryptParameters options, Context context) {
        throw new UnsupportedOperationException("The encrypt operation is not supported for EC keys.");
    }

    @Override
    public Mono<DecryptResult> decryptAsync(EncryptionAlgorithm algorithm, byte[] plaintext, Context context) {
        throw new UnsupportedOperationException("The decrypt operation is not supported for EC keys.");
    }

    @Override
    public DecryptResult decrypt(EncryptionAlgorithm algorithm, byte[] plaintext, Context context) {
        throw new UnsupportedOperationException("The decrypt operation is not supported for EC keys.");
    }

    @Override
    public Mono<DecryptResult> decryptAsync(DecryptParameters options, Context context) {
        throw new UnsupportedOperationException("The decrypt operation is not supported for EC keys.");
    }

    @Override
    public DecryptResult decrypt(DecryptParameters options, Context context) {
        throw new UnsupportedOperationException("The decrypt operation is not supported for EC keys.");
    }

    @Override
    public Mono<SignResult> signAsync(SignatureAlgorithm algorithm, byte[] digest, Context context) {
        Objects.requireNonNull(algorithm, "Signature algorithm cannot be null.");
        Objects.requireNonNull(digest, "Digest content cannot be null.");

        // Interpret the requested algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if (implClient != null) {
                return implClient.signAsync(algorithm, digest, context);
            }

            throw new RuntimeException(new NoSuchAlgorithmException(algorithm.toString()));
        } else if (!(baseAlgorithm instanceof AsymmetricSignatureAlgorithm)) {
            throw new RuntimeException(new NoSuchAlgorithmException(algorithm.toString()));
        }

        if (ecKeyPair.getPrivate() == null) {
            if (implClient != null) {
                return implClient.signAsync(algorithm, digest, context);
            }

            throw new IllegalArgumentException(
                "The private portion of the key is not locally available to perform the sign operation.");
        }

        verifyKeyPermissions(jsonWebKey, KeyOperation.SIGN);

        Ecdsa algo;

        if (baseAlgorithm instanceof Ecdsa) {
            algo = (Ecdsa) baseAlgorithm;
        } else {
            throw new RuntimeException(new NoSuchAlgorithmException(algorithm.toString()));
        }

        ISignatureTransform signer = algo.createSignatureTransform(ecKeyPair, provider);

        return Mono.fromCallable(() -> new SignResult(signer.sign(digest), algorithm, jsonWebKey.getId()));
    }

    @Override
    public SignResult sign(SignatureAlgorithm algorithm, byte[] digest, Context context) {
        Objects.requireNonNull(algorithm, "Signature algorithm cannot be null.");
        Objects.requireNonNull(digest, "Digest content cannot be null.");

        // Interpret the requested algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if (implClient != null) {
                return implClient.sign(algorithm, digest, context);
            }

            throw new RuntimeException(new NoSuchAlgorithmException(algorithm.toString()));
        } else if (!(baseAlgorithm instanceof AsymmetricSignatureAlgorithm)) {
            throw new RuntimeException(new NoSuchAlgorithmException(algorithm.toString()));
        }

        if (ecKeyPair.getPrivate() == null) {
            if (implClient != null) {
                return implClient.sign(algorithm, digest, context);
            }

            throw new IllegalArgumentException(
                "The private portion of the key is not locally available to perform the sign operation.");
        }

        verifyKeyPermissions(jsonWebKey, KeyOperation.SIGN);

        Ecdsa algo;

        if (baseAlgorithm instanceof Ecdsa) {
            algo = (Ecdsa) baseAlgorithm;
        } else {
            throw new RuntimeException(new NoSuchAlgorithmException(algorithm.toString()));
        }

        ISignatureTransform signer = algo.createSignatureTransform(ecKeyPair, provider);

        try {
            return new SignResult(signer.sign(digest), algorithm, jsonWebKey.getId());
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public Mono<VerifyResult> verifyAsync(SignatureAlgorithm algorithm, byte[] digest, byte[] signature,
        Context context) {
        Objects.requireNonNull(algorithm, "Signature algorithm cannot be null.");
        Objects.requireNonNull(digest, "Digest content cannot be null.");
        Objects.requireNonNull(signature, "Signature to be verified cannot be null.");

        // Interpret the requested algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if (implClient != null) {
                return implClient.verifyAsync(algorithm, digest, signature, context);
            }

            throw new RuntimeException(new NoSuchAlgorithmException(algorithm.toString()));
        } else if (!(baseAlgorithm instanceof AsymmetricSignatureAlgorithm)) {
            throw new RuntimeException(new NoSuchAlgorithmException(algorithm.toString()));
        }

        if (ecKeyPair.getPublic() == null) {
            if (implClient != null) {
                return implClient.verifyAsync(algorithm, digest, signature, context);
            }

            throw new IllegalArgumentException(
                "The public portion of the key is not locally available to perform the verify operation.");
        }

        verifyKeyPermissions(jsonWebKey, KeyOperation.VERIFY);

        Ecdsa algo;

        if (baseAlgorithm instanceof Ecdsa) {
            algo = (Ecdsa) baseAlgorithm;
        } else {
            throw new RuntimeException(new NoSuchAlgorithmException(algorithm.toString()));
        }

        ISignatureTransform signer = algo.createSignatureTransform(ecKeyPair, provider);

        return Mono
            .fromCallable(() -> new VerifyResult(signer.verify(digest, signature), algorithm, jsonWebKey.getId()));
    }

    @Override
    public VerifyResult verify(SignatureAlgorithm algorithm, byte[] digest, byte[] signature, Context context) {
        Objects.requireNonNull(algorithm, "Signature algorithm cannot be null.");
        Objects.requireNonNull(digest, "Digest content cannot be null.");
        Objects.requireNonNull(signature, "Signature to be verified cannot be null.");

        // Interpret the requested algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if (implClient != null) {
                return implClient.verify(algorithm, digest, signature, context);
            }

            throw new RuntimeException(new NoSuchAlgorithmException(algorithm.toString()));
        } else if (!(baseAlgorithm instanceof AsymmetricSignatureAlgorithm)) {
            throw new RuntimeException(new NoSuchAlgorithmException(algorithm.toString()));
        }

        if (ecKeyPair.getPublic() == null) {
            if (implClient != null) {
                return implClient.verify(algorithm, digest, signature, context);
            }

            throw new IllegalArgumentException(
                "The public portion of the key is not locally available to perform the verify operation.");
        }

        verifyKeyPermissions(jsonWebKey, KeyOperation.VERIFY);

        Ecdsa algo;

        if (baseAlgorithm instanceof Ecdsa) {
            algo = (Ecdsa) baseAlgorithm;
        } else {
            throw new RuntimeException(new NoSuchAlgorithmException(algorithm.toString()));
        }

        ISignatureTransform signer = algo.createSignatureTransform(ecKeyPair, provider);

        try {
            return new VerifyResult(signer.verify(digest, signature), algorithm, jsonWebKey.getId());
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new RuntimeException(e);
            }
        }
    }

    @Override
    public Mono<WrapResult> wrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] keyToWrap, Context context) {
        throw new UnsupportedOperationException("The key wrap operation is not supported for EC keys.");
    }

    @Override
    public WrapResult wrapKey(KeyWrapAlgorithm algorithm, byte[] keyToWrap, Context context) {
        throw new UnsupportedOperationException("The key wrap operation is not supported for EC keys.");
    }

    @Override
    public Mono<UnwrapResult> unwrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] encryptedKey, Context context) {
        throw new UnsupportedOperationException("The key unwrap operation is not supported for EC keys.");
    }

    @Override
    public UnwrapResult unwrapKey(KeyWrapAlgorithm algorithm, byte[] encryptedKey, Context context) {
        throw new UnsupportedOperationException("The key unwrap operation is not supported for EC keys.");
    }

    @Override
    public Mono<SignResult> signDataAsync(SignatureAlgorithm algorithm, byte[] data, Context context) {
        try {
            return signAsync(algorithm, calculateDigest(algorithm, data), context);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public SignResult signData(SignatureAlgorithm algorithm, byte[] data, Context context) {
        try {
            return sign(algorithm, calculateDigest(algorithm, data), context);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Mono<VerifyResult> verifyDataAsync(SignatureAlgorithm algorithm, byte[] data, byte[] signature,
        Context context) {
        try {
            return verifyAsync(algorithm, calculateDigest(algorithm, data), signature, context);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public VerifyResult verifyData(SignatureAlgorithm algorithm, byte[] data, byte[] signature, Context context) {
        try {
            return verify(algorithm, calculateDigest(algorithm, data), signature, context);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static byte[] calculateDigest(SignatureAlgorithm algorithm, byte[] data) throws NoSuchAlgorithmException {
        HashAlgorithm hashAlgorithm = SignatureHashResolver.DEFAULT.get(algorithm);
        MessageDigest md = MessageDigest.getInstance(Objects.toString(hashAlgorithm, null));

        md.update(data);

        return md.digest();
    }
}

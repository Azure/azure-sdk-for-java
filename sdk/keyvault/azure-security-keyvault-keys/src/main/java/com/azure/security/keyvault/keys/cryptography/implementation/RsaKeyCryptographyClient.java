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

import java.security.GeneralSecurityException;
import java.security.KeyPair;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;

import static com.azure.security.keyvault.keys.cryptography.implementation.CryptographyUtils.verifyKeyPermissions;

class RsaKeyCryptographyClient extends LocalKeyCryptographyClient {
    private final KeyPair rsaKeyPair;

    RsaKeyCryptographyClient(JsonWebKey jsonWebKey, CryptographyClientImpl implClient) {
        super(jsonWebKey, implClient);

        rsaKeyPair = jsonWebKey.toRsa(jsonWebKey.hasPrivateKey());
    }

    @Override
    public Mono<EncryptResult> encryptAsync(EncryptionAlgorithm algorithm, byte[] plaintext, Context context) {
        Objects.requireNonNull(algorithm, "Encryption algorithm cannot be null.");
        Objects.requireNonNull(plaintext, "Plaintext cannot be null.");

        // Interpret the requested algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if (implClient != null) {
                return implClient.encryptAsync(algorithm, plaintext, context);
            }

            throw new RuntimeException(new NoSuchAlgorithmException(algorithm.toString()));
        } else if (!(baseAlgorithm instanceof AsymmetricEncryptionAlgorithm)) {
            throw new RuntimeException(new NoSuchAlgorithmException(algorithm.toString()));
        }

        if (rsaKeyPair.getPublic() == null) {
            if (implClient != null) {
                return implClient.encryptAsync(algorithm, plaintext, context);
            }

            throw new IllegalArgumentException(
                "The public portion of the key is not available to perform the encrypt operation.");
        }

        verifyKeyPermissions(jsonWebKey, KeyOperation.ENCRYPT);

        AsymmetricEncryptionAlgorithm algo = (AsymmetricEncryptionAlgorithm) baseAlgorithm;

        return Mono.fromCallable(() -> {
            ICryptoTransform transform = algo.createEncryptor(rsaKeyPair);

            return new EncryptResult(transform.doFinal(plaintext), algorithm, jsonWebKey.getId());
        });
    }

    @Override
    public EncryptResult encrypt(EncryptionAlgorithm algorithm, byte[] plaintext, Context context) {
        Objects.requireNonNull(algorithm, "Encryption algorithm cannot be null.");
        Objects.requireNonNull(plaintext, "Plaintext cannot be null.");

        // Interpret the requested algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if (implClient != null) {
                return implClient.encrypt(algorithm, plaintext, context);
            }

            throw new RuntimeException(new NoSuchAlgorithmException(algorithm.toString()));
        } else if (!(baseAlgorithm instanceof AsymmetricEncryptionAlgorithm)) {
            throw new RuntimeException(new NoSuchAlgorithmException(algorithm.toString()));
        }

        if (rsaKeyPair.getPublic() == null) {
            if (implClient != null) {
                return implClient.encrypt(algorithm, plaintext, context);
            }

            throw new IllegalArgumentException(
                "The public portion of the key is not available to perform the encrypt operation.");
        }

        verifyKeyPermissions(jsonWebKey, KeyOperation.ENCRYPT);

        AsymmetricEncryptionAlgorithm algo = (AsymmetricEncryptionAlgorithm) baseAlgorithm;

        try {
            ICryptoTransform transform = algo.createEncryptor(rsaKeyPair);

            return new EncryptResult(transform.doFinal(plaintext), algorithm, jsonWebKey.getId());
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Mono<EncryptResult> encryptAsync(EncryptParameters encryptParameters, Context context) {
        return Mono.fromCallable(() -> encrypt(encryptParameters, context));
    }

    @Override
    public EncryptResult encrypt(EncryptParameters encryptParameters, Context context) {
        Objects.requireNonNull(encryptParameters, "Encrypt parameters cannot be null.");

        return encrypt(encryptParameters.getAlgorithm(), encryptParameters.getPlainText(), context);
    }

    @Override
    public Mono<DecryptResult> decryptAsync(EncryptionAlgorithm algorithm, byte[] ciphertext, Context context) {
        Objects.requireNonNull(algorithm, "Encryption algorithm cannot be null.");
        Objects.requireNonNull(ciphertext, "Ciphertext cannot be null.");

        // Interpret the requested algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if (implClient != null) {
                return implClient.decryptAsync(algorithm, ciphertext, context);
            }

            throw new RuntimeException(new NoSuchAlgorithmException(algorithm.toString()));
        } else if (!(baseAlgorithm instanceof AsymmetricEncryptionAlgorithm)) {
            throw new RuntimeException(new NoSuchAlgorithmException(algorithm.toString()));
        }

        if (rsaKeyPair.getPrivate() == null) {
            if (implClient != null) {
                return implClient.decryptAsync(algorithm, ciphertext, context);
            }

            throw new IllegalArgumentException(
                "The private portion of the key is not available to perform the decrypt operation.");
        }

        verifyKeyPermissions(jsonWebKey, KeyOperation.DECRYPT);

        AsymmetricEncryptionAlgorithm algo = (AsymmetricEncryptionAlgorithm) baseAlgorithm;

        return Mono.fromCallable(() -> {
            ICryptoTransform transform = algo.createDecryptor(rsaKeyPair);

            return new DecryptResult(transform.doFinal(ciphertext), algorithm, jsonWebKey.getId());
        });
    }

    @Override
    public DecryptResult decrypt(EncryptionAlgorithm algorithm, byte[] ciphertext, Context context) {
        Objects.requireNonNull(algorithm, "Encryption algorithm cannot be null.");
        Objects.requireNonNull(ciphertext, "Ciphertext cannot be null.");

        // Interpret the requested algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if (implClient != null) {
                return implClient.decrypt(algorithm, ciphertext, context);
            }

            throw new RuntimeException(new NoSuchAlgorithmException(algorithm.toString()));
        } else if (!(baseAlgorithm instanceof AsymmetricEncryptionAlgorithm)) {
            throw new RuntimeException(new NoSuchAlgorithmException(algorithm.toString()));
        }

        if (rsaKeyPair.getPrivate() == null) {
            if (implClient != null) {
                return implClient.decrypt(algorithm, ciphertext, context);
            }

            throw new IllegalArgumentException(
                "The private portion of the key is not available to perform the decrypt operation.");
        }

        verifyKeyPermissions(jsonWebKey, KeyOperation.DECRYPT);

        AsymmetricEncryptionAlgorithm algo = (AsymmetricEncryptionAlgorithm) baseAlgorithm;

        try {
            ICryptoTransform transform = algo.createDecryptor(rsaKeyPair);

            return new DecryptResult(transform.doFinal(ciphertext), algorithm, jsonWebKey.getId());
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Mono<DecryptResult> decryptAsync(DecryptParameters decryptParameters, Context context) {
        return Mono.fromCallable(() -> decrypt(decryptParameters, context));
    }

    @Override
    public DecryptResult decrypt(DecryptParameters decryptParameters, Context context) {
        Objects.requireNonNull(decryptParameters, "Decrypt parameters cannot be null.");

        return decrypt(decryptParameters.getAlgorithm(), decryptParameters.getCipherText(), context);
    }

    @Override
    public Mono<SignResult> signAsync(SignatureAlgorithm algorithm, byte[] digest, Context context) {
        return implClient != null
            ? implClient.signAsync(algorithm, digest, context)
            : Mono.error(
                new UnsupportedOperationException("The sign operation on local RSA key is not currently supported."));
    }

    @Override
    public SignResult sign(SignatureAlgorithm algorithm, byte[] digest, Context context) {
        if (implClient != null) {
            return implClient.sign(algorithm, digest, context);
        } else {
            throw new UnsupportedOperationException("The sign operation on local RSA key is not currently supported.");
        }
    }

    @Override
    public Mono<VerifyResult> verifyAsync(SignatureAlgorithm algorithm, byte[] digest, byte[] signature,
        Context context) {
        return implClient != null
            ? implClient.verifyAsync(algorithm, digest, signature, context)
            : Mono.error(new UnsupportedOperationException(
                "The verify operation on a local RSA key is not currently supported."));
    }

    @Override
    public VerifyResult verify(SignatureAlgorithm algorithm, byte[] digest, byte[] signature, Context context) {
        if (implClient != null) {
            return implClient.verify(algorithm, digest, signature, context);
        } else {
            throw new UnsupportedOperationException(
                "The verify operation on a local RSA key is not currently supported.");
        }
    }

    @Override
    public Mono<WrapResult> wrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] keyToWrap, Context context) {
        Objects.requireNonNull(algorithm, "Key wrap algorithm cannot be null.");
        Objects.requireNonNull(keyToWrap, "Key content to be wrapped cannot be null.");

        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if (implClient != null) {
                return implClient.wrapKeyAsync(algorithm, keyToWrap, context);
            }

            throw new RuntimeException(new NoSuchAlgorithmException(algorithm.toString()));
        } else if (!(baseAlgorithm instanceof AsymmetricEncryptionAlgorithm)) {
            throw new RuntimeException(new NoSuchAlgorithmException(algorithm.toString()));
        }

        if (rsaKeyPair.getPublic() == null) {
            if (implClient != null) {
                return implClient.wrapKeyAsync(algorithm, keyToWrap, context);
            }

            throw new IllegalArgumentException(
                "The public portion of the key is not available to perform the key wrap operation.");
        }

        verifyKeyPermissions(jsonWebKey, KeyOperation.WRAP_KEY);

        AsymmetricEncryptionAlgorithm algo = (AsymmetricEncryptionAlgorithm) baseAlgorithm;

        return Mono.fromCallable(() -> {
            ICryptoTransform transform = algo.createEncryptor(rsaKeyPair);

            return new WrapResult(transform.doFinal(keyToWrap), algorithm, jsonWebKey.getId());
        });
    }

    @Override
    public WrapResult wrapKey(KeyWrapAlgorithm algorithm, byte[] keyToWrap, Context context) {
        Objects.requireNonNull(algorithm, "Key wrap algorithm cannot be null.");
        Objects.requireNonNull(keyToWrap, "Key content to be wrapped cannot be null.");

        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if (implClient != null) {
                return implClient.wrapKey(algorithm, keyToWrap, context);
            }

            throw new RuntimeException(new NoSuchAlgorithmException(algorithm.toString()));
        } else if (!(baseAlgorithm instanceof AsymmetricEncryptionAlgorithm)) {
            throw new RuntimeException(new NoSuchAlgorithmException(algorithm.toString()));
        }

        if (rsaKeyPair.getPublic() == null) {
            if (implClient != null) {
                return implClient.wrapKey(algorithm, keyToWrap, context);
            }

            throw new IllegalArgumentException(
                "The public portion of the key is not available to perform the key wrap operation.");
        }

        verifyKeyPermissions(jsonWebKey, KeyOperation.WRAP_KEY);

        AsymmetricEncryptionAlgorithm algo = (AsymmetricEncryptionAlgorithm) baseAlgorithm;

        try {
            ICryptoTransform transform = algo.createEncryptor(rsaKeyPair);

            return new WrapResult(transform.doFinal(keyToWrap), algorithm, jsonWebKey.getId());
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Mono<UnwrapResult> unwrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] encryptedKey, Context context) {
        Objects.requireNonNull(algorithm, "Key wrap algorithm cannot be null.");
        Objects.requireNonNull(encryptedKey, "Encrypted key content to be unwrapped cannot be null.");

        // Interpret the requested algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if (implClient != null) {
                return implClient.unwrapKeyAsync(algorithm, encryptedKey, context);
            }

            throw new RuntimeException(new NoSuchAlgorithmException(algorithm.toString()));
        } else if (!(baseAlgorithm instanceof AsymmetricEncryptionAlgorithm)) {
            throw new RuntimeException(new NoSuchAlgorithmException(algorithm.toString()));
        }

        if (rsaKeyPair.getPrivate() == null) {
            if (implClient != null) {
                return implClient.unwrapKeyAsync(algorithm, encryptedKey, context);
            }

            throw new IllegalArgumentException(
                "The private portion of the key is not available to perform the key unwrap operation.");
        }

        verifyKeyPermissions(jsonWebKey, KeyOperation.UNWRAP_KEY);

        AsymmetricEncryptionAlgorithm algo = (AsymmetricEncryptionAlgorithm) baseAlgorithm;

        return Mono.fromCallable(() -> {
            ICryptoTransform transform = algo.createDecryptor(rsaKeyPair);

            return new UnwrapResult(transform.doFinal(encryptedKey), algorithm, jsonWebKey.getId());
        });
    }

    @Override
    public UnwrapResult unwrapKey(KeyWrapAlgorithm algorithm, byte[] encryptedKey, Context context) {
        Objects.requireNonNull(algorithm, "Key wrap algorithm cannot be null.");
        Objects.requireNonNull(encryptedKey, "Encrypted key content to be unwrapped cannot be null.");

        // Interpret the requested algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.DEFAULT.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if (implClient != null) {
                return implClient.unwrapKey(algorithm, encryptedKey, context);
            }

            throw new RuntimeException(new NoSuchAlgorithmException(algorithm.toString()));
        } else if (!(baseAlgorithm instanceof AsymmetricEncryptionAlgorithm)) {
            throw new RuntimeException(new NoSuchAlgorithmException(algorithm.toString()));
        }

        if (rsaKeyPair.getPrivate() == null) {
            if (implClient != null) {
                return implClient.unwrapKey(algorithm, encryptedKey, context);
            }

            throw new IllegalArgumentException(
                "The private portion of the key is not available to perform the key unwrap operation.");
        }

        verifyKeyPermissions(jsonWebKey, KeyOperation.UNWRAP_KEY);

        AsymmetricEncryptionAlgorithm algo = (AsymmetricEncryptionAlgorithm) baseAlgorithm;

        try {
            ICryptoTransform transform = algo.createDecryptor(rsaKeyPair);

            return new UnwrapResult(transform.doFinal(encryptedKey), algorithm, jsonWebKey.getId());
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        }
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

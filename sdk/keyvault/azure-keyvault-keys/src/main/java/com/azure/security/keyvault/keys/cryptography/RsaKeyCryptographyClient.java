package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.util.Context;
import com.azure.security.keyvault.keys.models.webkey.JsonWebKey;
import reactor.core.publisher.Mono;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;

class RsaKeyCryptographyClient {
    private CryptographyServiceClient serviceClient;
    private KeyPair keyPair;

    /*
     * Creates a RsaKeyCryptographyClient that uses {@code serviceClient) to service requests
     *
     * @param keyPair the key pair to use for cryptography operations.
     */
    RsaKeyCryptographyClient(CryptographyServiceClient serviceClient) {
        this.serviceClient = serviceClient;
    }

    RsaKeyCryptographyClient(JsonWebKey key, CryptographyServiceClient serviceClient) {
        keyPair = key.toEC(key.hasPrivateKey());
        this.serviceClient = serviceClient;
    }

    private KeyPair getKeyPair(JsonWebKey key) {
        if(keyPair == null){
            keyPair = key.toRSA(key.hasPrivateKey());
        }
        return keyPair;
    }

    Mono<EncryptResult> encryptAsync(EncryptionAlgorithm algorithm, byte[] plaintext, byte[] iv, byte[] authenticationData, Context context, JsonWebKey jsonWebKey) {
        keyPair = getKeyPair(jsonWebKey);

        if(iv != null || authenticationData != null) {
            Mono.error(new IllegalArgumentException("iv and authenticationData parameters are not allowed for Rsa encrypt operation"));
        }

        // Interpret the requested algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.Default.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if(serviceCryptoAvailable()) {
                return serviceClient.encrypt(algorithm, plaintext, context);
            }
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        } else if (!(baseAlgorithm instanceof AsymmetricEncryptionAlgorithm)) {
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        }

        if(keyPair.getPublic() == null){
            if(serviceCryptoAvailable()) {
                return serviceClient.encrypt(algorithm, plaintext, context);
            }
            return Mono.error(new IllegalArgumentException("Public portion of the key not available to perform encrypt operation"));
        }

        AsymmetricEncryptionAlgorithm algo = (AsymmetricEncryptionAlgorithm) baseAlgorithm;

        ICryptoTransform transform;

        try {
            transform = algo.CreateEncryptor(keyPair);
            return Mono.just(new EncryptResult(transform.doFinal(plaintext), (byte[]) null, algorithm));
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
            return Mono.error(e);
        }
    }

    Mono<DecryptResult> decryptAsync(EncryptionAlgorithm algorithm, byte[] cipherText, byte[] iv, byte[] authenticationData, byte[] authenticationTag, Context context, JsonWebKey jsonWebKey) {

        if(iv != null || authenticationData != null || authenticationTag != null){
            return Mono.error(new IllegalArgumentException("iv, authenticationData and authenticationTag parameters are not supported for Rsa decrypt operation"));
        }

        keyPair = getKeyPair(jsonWebKey);

        Algorithm baseAlgorithm = AlgorithmResolver.Default.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if(serviceCryptoAvailable()) {
                return serviceClient.decrypt(algorithm, cipherText, context);
            }
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        } else if (!(baseAlgorithm instanceof AsymmetricEncryptionAlgorithm)) {
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        }

        if(keyPair.getPrivate() == null) {
            if(serviceCryptoAvailable()) {
                return serviceClient.decrypt(algorithm, cipherText, context);
            }
            return Mono.error(new IllegalArgumentException("Private portion of the key not available to perform decrypt operation"));
        }

        AsymmetricEncryptionAlgorithm algo = (AsymmetricEncryptionAlgorithm) baseAlgorithm;

        ICryptoTransform transform;

        try {
            transform = algo.CreateDecryptor(keyPair);
            return Mono.just(new DecryptResult(transform.doFinal(cipherText)));
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
            return Mono.error(e);
        }
    }

    Mono<SignResult> signAsync(SignatureAlgorithm algorithm, byte[] digest, Context context, JsonWebKey key) {

        return serviceClient.sign(algorithm, digest, context);
    }

    Mono<VerifyResult> verifyAsync(SignatureAlgorithm algorithm, byte[] digest, byte[] signature, Context context, JsonWebKey key) {

        return serviceClient.verify(algorithm, digest, signature, context);
        // do a service call for now.
    }

    Mono<KeyWrapResult> wrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] key, Context context, JsonWebKey jsonWebKey) {

        keyPair = getKeyPair(jsonWebKey);

        Algorithm baseAlgorithm = AlgorithmResolver.Default.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if(serviceCryptoAvailable()) {
                return serviceClient.wrapKey(algorithm, key, context);
            }
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        } else if (!(baseAlgorithm instanceof AsymmetricEncryptionAlgorithm)) {
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        }

        if(keyPair.getPublic() == null) {
            if(serviceCryptoAvailable()) {
                return serviceClient.wrapKey(algorithm, key, context);
            }
            return Mono.error(new IllegalArgumentException("Public portion of the key not available to perform wrap key operation"));
        }

        AsymmetricEncryptionAlgorithm algo = (AsymmetricEncryptionAlgorithm) baseAlgorithm;

        ICryptoTransform transform;

        try {
            transform = algo.CreateEncryptor(keyPair);
            return Mono.just(new KeyWrapResult(transform.doFinal(key), algorithm));
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
            return Mono.error(e);
        }
    }

    Mono<KeyUnwrapResult> unwrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] encryptedKey, Context context, JsonWebKey jsonWebKey) {

        keyPair = getKeyPair(jsonWebKey);

        // Interpret the requested algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.Default.get(algorithm.toString());

        if (baseAlgorithm == null) {
            if(serviceCryptoAvailable()) {
                return serviceClient.unwrapKey(algorithm, encryptedKey, context);
            }
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        } else if (!(baseAlgorithm instanceof AsymmetricEncryptionAlgorithm)) {
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        }

        if (keyPair.getPrivate() == null){
            if(serviceCryptoAvailable()) {
                return serviceClient.unwrapKey(algorithm, encryptedKey, context);
            }
            return Mono.error(new IllegalArgumentException("Private portion of the key not available to perform unwrap operation"));
        }

        AsymmetricEncryptionAlgorithm algo = (AsymmetricEncryptionAlgorithm) baseAlgorithm;

        ICryptoTransform transform;

        try {
            transform = algo.CreateDecryptor(keyPair);
            return Mono.just(new KeyUnwrapResult(transform.doFinal(encryptedKey)));
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException e) {
            return Mono.error(e);
        }
    }


    Mono<SignResult> signDataAsync(SignatureAlgorithm algorithm, byte[] data, Context context, JsonWebKey key) {
        try {
            HashAlgorithm hashAlgorithm = SignatureHashResolver.Default.get(algorithm);
            MessageDigest md = MessageDigest.getInstance(hashAlgorithm.toString());
            md.update(data);
            byte[] digest = md.digest();
            return signAsync(algorithm, digest, context, key);
        } catch (NoSuchAlgorithmException e){
            return Mono.error(e);
        }
    }


    Mono<VerifyResult> verifyDataAsync(SignatureAlgorithm algorithm, byte[] data, byte[] signature, Context context, JsonWebKey key) {
        HashAlgorithm hashAlgorithm = SignatureHashResolver.Default.get(algorithm);
        try {
            MessageDigest md = MessageDigest.getInstance(hashAlgorithm.toString());
            md.update(data);
            byte[] digest = md.digest();
            return verifyAsync(algorithm, digest, signature, context, key);
        } catch (NoSuchAlgorithmException e){
            return Mono.error(e);
        }
    }

    private boolean serviceCryptoAvailable(){
        return serviceClient != null ;
    }

}

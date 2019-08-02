package com.azure.security.keyvault.keys.cryptography;

import com.azure.core.util.Context;
import com.azure.security.keyvault.keys.models.webkey.JsonWebKey;
import reactor.core.publisher.Mono;

import java.security.NoSuchAlgorithmException;

class SymmetricKeyCryptographyClient extends LocalKeyCryptographyClient {
    private CryptographyServiceClient serviceClient;
    private byte[] key;

    /*
     * Creates a RsaKeyCryptographyClient that uses {@code serviceClient) to service requests
     *
     * @param key the key pair to use for cryptography operations.
     */
    SymmetricKeyCryptographyClient(CryptographyServiceClient serviceClient) {
        super(serviceClient);
    }

    SymmetricKeyCryptographyClient(JsonWebKey key, CryptographyServiceClient serviceClient) {
        super(serviceClient);
        this.key = key.toAes().getEncoded();
    }

    private byte[] getKey(JsonWebKey key) {
        if(this.key == null){
            this.key = key.toAes().getEncoded();
        }
        return this.key;
    }

    Mono<EncryptResult> encryptAsync(EncryptionAlgorithm algorithm, byte[] plaintext, byte[] iv, byte[] authenticationData, Context context, JsonWebKey jsonWebKey) {
        key = getKey(jsonWebKey);

        Algorithm baseAlgorithm = AlgorithmResolver.Default.get(algorithm.toString());

        if (baseAlgorithm == null || !(baseAlgorithm instanceof SymmetricEncryptionAlgorithm)) {
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        }

        SymmetricEncryptionAlgorithm algo = (SymmetricEncryptionAlgorithm) baseAlgorithm;

        ICryptoTransform transform;

        try {
            transform = algo.CreateEncryptor(key, iv, authenticationData);
        } catch (Exception e) {
            return Mono.error(e);
        }

        byte[] cipherText;

        try {
            cipherText = transform.doFinal(plaintext);
        } catch (Exception e) {
            return Mono.error(e);
        }

        byte[] authenticationTag = null;

        if (transform instanceof IAuthenticatedCryptoTransform) {

            IAuthenticatedCryptoTransform authenticatedTransform = (IAuthenticatedCryptoTransform) transform;

            authenticationTag = authenticatedTransform.getTag().clone();
        }

        return Mono.just(new EncryptResult(cipherText, authenticationTag, algorithm));
    }

    Mono<DecryptResult> decryptAsync(EncryptionAlgorithm algorithm, byte[] cipherText, byte[] iv, byte[] authenticationData, byte[] authenticationTag, Context context, JsonWebKey jsonWebKey) {

        key = getKey(jsonWebKey);

        // Interpret the algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.Default.get(algorithm.toString());

        if (baseAlgorithm == null || !(baseAlgorithm instanceof SymmetricEncryptionAlgorithm)) {
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        }

        SymmetricEncryptionAlgorithm algo = (SymmetricEncryptionAlgorithm) baseAlgorithm;

        ICryptoTransform transform;

        try {
            transform = algo.CreateDecryptor(key, iv, authenticationData, authenticationTag);
        } catch (Exception e) {
            return Mono.error(e);
        }

        try {
            return Mono.just(new DecryptResult(transform.doFinal(cipherText)));
        } catch (Exception e) {
            return Mono.error(e);
        }
    }

    Mono<SignResult> signAsync(SignatureAlgorithm algorithm, byte[] digest, Context context, JsonWebKey key) {
        return Mono.error(new UnsupportedOperationException("Sign operation not supported for OCT/Symmetric key"));
    }

    Mono<VerifyResult> verifyAsync(SignatureAlgorithm algorithm, byte[] digest, byte[] signature, Context context, JsonWebKey key) {
        return Mono.error(new UnsupportedOperationException("Verify operation not supported for OCT/Symmetric key"));
    }

    Mono<KeyWrapResult> wrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] key, Context context, JsonWebKey jsonWebKey) {

        this.key = getKey(jsonWebKey);

        if (key == null || key.length == 0) {
            throw new IllegalArgumentException("key");
        }

        // Interpret the algorithm
        Algorithm baseAlgorithm = AlgorithmResolver.Default.get(algorithm.toString());

        if (baseAlgorithm == null || !(baseAlgorithm instanceof LocalKeyWrapAlgorithm)) {
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        }

        LocalKeyWrapAlgorithm algo = (LocalKeyWrapAlgorithm) baseAlgorithm;

        ICryptoTransform transform = null;

        try {
            transform = algo.CreateEncryptor(this.key, null, null);
        } catch (Exception e) {
            return Mono.error(e);
        }

        byte[] encrypted = null;

        try {
            encrypted = transform.doFinal(key);
        } catch (Exception e) {
            return Mono.error(e);
        }

        return Mono.just(new KeyWrapResult(encrypted, algorithm));
    }

    Mono<KeyUnwrapResult> unwrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] encryptedKey, Context context, JsonWebKey jsonWebKey) {

        key = getKey(jsonWebKey);

        Algorithm baseAlgorithm = AlgorithmResolver.Default.get(algorithm.toString());

        if (baseAlgorithm == null || !(baseAlgorithm instanceof LocalKeyWrapAlgorithm)) {
            return Mono.error(new NoSuchAlgorithmException(algorithm.toString()));
        }

        LocalKeyWrapAlgorithm algo = (LocalKeyWrapAlgorithm) baseAlgorithm;

        ICryptoTransform transform;

        try {
            transform = algo.CreateDecryptor(key, null, null);
        } catch (Exception e) {
            return Mono.error(e);
        }

        byte[] decrypted;

        try {
            decrypted = transform.doFinal(encryptedKey);
        } catch (Exception e) {
            return Mono.error(e);
        }

        return Mono.just(new KeyUnwrapResult(decrypted));
    }

    Mono<SignResult> signDataAsync(SignatureAlgorithm algorithm, byte[] data, Context context, JsonWebKey key) {
        return signAsync(algorithm, data, context, key);
    }

    Mono<VerifyResult> verifyDataAsync(SignatureAlgorithm algorithm, byte[] data, byte[] signature, Context context, JsonWebKey key) {
        return verifyAsync(algorithm, data, signature, context, key);
    }

    private boolean serviceCryptoAvailable(){
        return serviceClient != null ;
    }

}

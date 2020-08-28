// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.core.credential.TokenCredential;
import com.azure.core.exception.AzureException;
import com.azure.cosmos.implementation.encryption.AesCryptoServiceProvider;
import com.azure.cosmos.implementation.guava25.base.Function;
import com.azure.cosmos.implementation.guava25.collect.ImmutableList;
import com.azure.cosmos.implementation.guava25.collect.ImmutableMap;
import com.azure.security.keyvault.keys.KeyAsyncClient;
import com.azure.security.keyvault.keys.cryptography.CryptographyAsyncClient;
import com.azure.security.keyvault.keys.cryptography.models.KeyWrapAlgorithm;
import com.azure.security.keyvault.keys.cryptography.models.UnwrapResult;
import com.azure.security.keyvault.keys.cryptography.models.WrapResult;
import com.azure.security.keyvault.keys.models.JsonWebKey;
import com.azure.security.keyvault.keys.models.KeyOperation;
import com.azure.security.keyvault.keys.models.KeyProperties;
import com.azure.security.keyvault.keys.models.KeyType;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import reactor.core.publisher.Mono;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.util.Map;

// TODO: moderakh cleanup on the comments when ready
public class KeyVaultAccessClientTests {
    // Test Class for Mocking KeyClient methods.
    static class TestKeyClientProvider {
        // TODO: add tailing "/' to the keys? dotnet?
        private final static Map<String, String> keyinfo = ImmutableMap.of(
            "testkey1", "Recoverable",
            "testkey2", "nothingset");

        /**
         * Simulates a GetKeyAsync method of KeyVault SDK.
         *
         * @param name
         * @param version
         * @return
         */
        public Mono<KeyVaultKey> GetKeyAsync(String name, String version) {
            System.out.println("Accessing Key via Test GetKeyAsync");

            // simulate a RequestFailed Exception
            if (name.contains(KeyVaultTestConstants.ValidateRequestFailedEx)) {
                //                        throw new RequestFailedException("Service Unavailable");
                throw new AzureException("Service Unavailable");
            }

            // simulate a case to return a Null Key.
            if (name.contains(KeyVaultTestConstants.ValidateNullKeyVaultKey)) {
                KeyVaultKey mockedResponseNullKeyVault = Mockito.mock(KeyVaultKey.class);

                //                        mockedResponseNullKeyVault.SetupGet(r => r.Value).Returns((KeyVaultKey)null);

                return Mono.justOrEmpty(null);
                //                        return Task.FromResult(mockedResponseNullKeyVault.Object);
            }

            String recoverlevel = this.keyinfo.get(name);

            Function<KeyProperties, KeyModelFactory.KeyPropertiesBuilder> func1 =
                tp -> new KeyModelFactory.KeyPropertiesBuilder(tp).withRecoveryLevel(recoverlevel);

            Function<JsonWebKey, KeyModelFactory.JsonWebKeyBuilder> func2 =
                jwk -> new KeyModelFactory.JsonWebKeyBuilder(jwk)
                    .withKeyType(KeyType.EC).withKeyOps(ImmutableList.of(KeyOperation.SIGN, KeyOperation.VERIFY));

            KeyVaultKey mockKey = new KeyModelFactory.KeyVaultKeyBuilder()
                .withJsonWebKeyBuilder(func2).withKeyProperties(func1).toKeyVaultKey();

            return Mono.just(mockKey);
        }

        KeyAsyncClient createTestKeyClient(URI vaultUri, TokenCredential credential) {
            if (vaultUri == null || credential == null) {
                throw new IllegalArgumentException("Value is null.");
            }
            KeyAsyncClient client = Mockito.mock(KeyAsyncClient.class);
            Mockito.doAnswer(new Answer() {
                @Override
                public Mono<KeyVaultKey> answer(InvocationOnMock invocationOnMock) throws Throwable {
                    String name = invocationOnMock.getArgument(0);
                    return GetKeyAsync(name, null);
                }
            }).when(client).getKey(ArgumentMatchers.anyString());
            return client;
        }
    }

    /// Factory Class for KeyClientFactory.
    /// Returns an instance of TestKeyClient for mocking KeyClient.
    public static class KeyClientTestFactory extends KeyClientFactory {
        @Override
        public KeyAsyncClient getKeyClient(KeyVaultKeyUriProperties keyVaultKeyUriProperties,
                                           TokenCredential tokenCredential) {
            return new TestKeyClientProvider().createTestKeyClient(keyVaultKeyUriProperties.getKeyVaultUri(),
                tokenCredential);
        }
    }

    static class TestCryptographyClientProvider {
        URI keyId;
        TokenCredential credential;

        CryptographyAsyncClient createTestCryptographyClient(URI keyId, TokenCredential credential) {
            if (keyId == null || credential == null) {
                throw new IllegalArgumentException("Value is null.");
            }

            ////        /// <summary>
            ////        /// Initializes a new instance of the TestCryptographyClient class for the specified keyid.
            ////        /// </summary>
            ////        /// <param name="keyid"></param>
            ////        /// <param name="credential"></param>
            ////        internal TestCryptographyClient(Uri keyid, TokenCredential credential)
            ////        {
            ////            if( keyid == null || credential == null)
            ////            {
            ////                throw new ArgumentNullException("Value is null.");
            ////            }
            ////            this.keyId = keyid;
            ////            this.credential = credential;
            ////        }

            this.keyId = keyId;
            this.credential = credential;

            CryptographyAsyncClient client = Mockito.mock(CryptographyAsyncClient.class);
            Mockito.doAnswer(new Answer() {
                @Override
                public Mono<WrapResult> answer(InvocationOnMock invocationOnMock) throws Throwable {
                    KeyWrapAlgorithm algo = invocationOnMock.getArgument(0);
                    byte[] key = invocationOnMock.getArgument(1);
                    return WrapKeyAsync(algo, key);
                }
            }).when(client).wrapKey(ArgumentMatchers.any(), ArgumentMatchers.any());

            Mockito.doAnswer(new Answer() {
                @Override
                public Mono<UnwrapResult> answer(InvocationOnMock invocationOnMock) throws Throwable {
                    KeyWrapAlgorithm algo = invocationOnMock.getArgument(0);
                    byte[] encryptedData = invocationOnMock.getArgument(1);
                    return UnwrapKeyAsync(algo, encryptedData);
                }
            }).when(client).unwrapKey(ArgumentMatchers.any(), ArgumentMatchers.any());
            return client;
        }

        byte[] secretkey = new byte[] { 0x12, 0x10, 0x20, 0x40, 060, 0x23, 0x12, 0x19, 0x22, 0x10, 0x09, 0x12,
            (byte) 0x99, 0x12, 0x11, 0x22 };
        byte[] iv = new byte[] { (byte) 0x99, (byte) 0x99, (byte) 0x88, (byte) 0x88, 0x77, 0x77, 0x66, 0x66, 0x55,
            0x55, 0x44, 0x44, 0x33, 0x33, 0x22, 0x22 };


        //                /// <summary>
        //                /// Simulates WrapKeyAsync method of KeyVault SDK.
        //                /// </summary>
        //                /// <param name="algorithm"> Encryption Algorithm </param>
        //                /// <param name="key"> Key to be wrapped </param>
        //                /// <param name="cancellationToken"> cancellation token </param>
        //                /// <returns></returns>
        public Mono<WrapResult> WrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] key) {

            if (key == null || key.length == 0) {
                return Mono.error(new IllegalArgumentException("Key is Null."));
            }

            byte[] wrappedKey = this.Encrypt(key, this.secretkey, this.iv);

            // simulate a null wrapped key
            if (this.keyId.toString().contains(KeyVaultTestConstants.ValidateNullWrappedKey)) {
                wrappedKey = null;
            }

            String keyid = "12345678910";


            WrapResult mockWrapResult = new WrapResult(wrappedKey, KeyVaultConstants.RsaOaep256, keyid);

            //                    WrapResult mockWrapResult = CryptographyModelFactory.WrapResult(keyId: keyid,
            //                    key:wrappedKey, algorithm: KeyVaultConstants.RsaOaep256);

            return Mono.just(mockWrapResult);
        }

        /// <summary>
        /// Simulates UnwrapKeyAsync of KeyVault SDK.
        /// </summary>
        /// <param name="algorithm"> Encryption Algorithm </param>
        /// <param name="encryptedKey"> Key to be unwrapped </param>
        /// <param name="cancellationToken"> cancellation token </param>
        /// <returns></returns>
        public Mono<UnwrapResult> UnwrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] encryptedKey) {
            if (encryptedKey == null || encryptedKey.length == 0) {
                throw new IllegalArgumentException("Key is Null.");
            }

            byte[] unwrappedKey = this.Decrypt(encryptedKey, this.secretkey, this.iv);

            // simulate a null unwrapped key.
            if (this.keyId.toString().contains(KeyVaultTestConstants.ValidateNullUnwrappedKey)) {
                unwrappedKey = null;
            }

            String keyid = "12345678910";
            UnwrapResult mockUnwrapResult = new UnwrapResult(unwrappedKey, KeyVaultConstants.RsaOaep256, keyid);

            return Mono.just(mockUnwrapResult);
        }

        private byte[] PerformCryptography(byte[] data, AesCryptoServiceProvider.ICryptoTransform cryptoTransform) {
            //                    new AesCryptoServiceProvider.ICryptoTransform(ms, cryptoTransform, CryptoStreamMode
            //                    .Write);

            return cryptoTransform.transformFinalBlock(data, 0, data.length);
            //
            //                    using (CryptoStream cryptoStream = new CryptoStream(ms, cryptoTransform,
            //                    CryptoStreamMode.Write))
            //                    {
            //                        cryptoStream.Write(data, 0, data.Length);
            //                        cryptoStream.FlushFinalBlock();
            //
            //                        return ms.ToArray();
            //               }
        }

        private byte[] Encrypt(byte[] data, byte[] key, byte[] iv) {
            try {
                AesCryptoServiceProvider.ICryptoTransform encryptor = createICryptoTransform(key, iv,
                    Cipher.ENCRYPT_MODE);
                return this.PerformCryptography(data, encryptor);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }


            //                    using (Aes aes = Aes.Create())
            //                    {
            //                        aes.KeySize = 128;
            //                        aes.BlockSize = 128;
            //                        aes.Padding = PaddingMode.Zeros;
            //
            //                        aes.Key = key;
            //                        aes.IV = iv;
            //
            //                        using (ICryptoTransform encryptor = aes.CreateEncryptor(aes.Key, aes.IV))
            //                        {
            //                            return this.PerformCryptography(data, encryptor);
            //                        }
            //                    }
        }

        private byte[] Decrypt(byte[] data, byte[] key, byte[] iv) {
            try {
                AesCryptoServiceProvider.ICryptoTransform encryptor = createICryptoTransform(key, iv,
                    Cipher.DECRYPT_MODE);
                return this.PerformCryptography(data, encryptor);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        private AesCryptoServiceProvider.ICryptoTransform createICryptoTransform(byte[] key, byte[] iv, int mode) {
            try {
                String algoName = "AES/CBC/NoPadding";
                SecretKeySpec secretKeySpec;
                Cipher cipher = Cipher.getInstance(algoName);
                secretKeySpec = new SecretKeySpec(key, algoName);

                cipher.init(mode, secretKeySpec, new IvParameterSpec(iv));
                return new AesCryptoServiceProvider.ICryptoTransform(cipher);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

    }


    //    /// <summary>
    //    /// Test Class for mocking CryptographyClient methods.
    //    /// </summary>
    //    internal class TestCryptographyClient : CryptographyClient
    //    {
    //        Uri keyId { get; }
    //        TokenCredential credential { get; }
    //
    //        byte[] secretkey = new byte[16] { 0x12, 0x10, 0x20, 0x40, 060, 0x23, 0x12, 0x19, 0x22, 0x10, 0x09,
    //        0x12, 0x99, 0x12, 0x11, 0x22 };
    //        byte[] iv = new byte[16] { 0x99, 0x99, 0x88, 0x88, 0x77, 0x77, 0x66, 0x66, 0x55, 0x55, 0x44, 0x44,
    //        0x33, 0x33, 0x22, 0x22 };
    //
    //        /// <summary>
    //        /// Initializes a new instance of the TestCryptographyClient class for the specified keyid.
    //        /// </summary>
    //        /// <param name="keyid"></param>
    //        /// <param name="credential"></param>
    //        internal TestCryptographyClient(Uri keyid, TokenCredential credential)
    //        {
    //            if( keyid == null || credential == null)
    //            {
    //                throw new ArgumentNullException("Value is null.");
    //            }
    //            this.keyId = keyid;
    //            this.credential = credential;
    //        }
    //
    //        /// <summary>
    //        /// Simulates WrapKeyAsync method of KeyVault SDK.
    //        /// </summary>
    //        /// <param name="algorithm"> Encryption Algorithm </param>
    //        /// <param name="key"> Key to be wrapped </param>
    //        /// <param name="cancellationToken"> cancellation token </param>
    //        /// <returns></returns>
    //        public override Task<WrapResult> WrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] key, CancellationToken
    //        cancellationToken = default)
    //        {
    //
    //            if(key.IsNullOrEmpty())
    //            {
    //                throw new ArgumentNullException("Key is Null.");
    //            }
    //
    //            byte[] wrappedKey = this.Encrypt(key, this.secretkey, this.iv);
    //
    //            // simulate a null wrapped key
    //            if (this.keyId.ToString().Contains(KeyVaultTestConstants.ValidateNullWrappedKey))
    //            {
    //                wrappedKey = null;
    //            }
    //
    //            string keyid = "12345678910";
    //            WrapResult mockWrapResult = CryptographyModelFactory.WrapResult(keyId: keyid, key:wrappedKey,
    //            algorithm: KeyVaultConstants.RsaOaep256);
    //
    //            return Task.FromResult(mockWrapResult);
    //        }
    //
    //        /// <summary>
    //        /// Simulates UnwrapKeyAsync of KeyVault SDK.
    //        /// </summary>
    //        /// <param name="algorithm"> Encryption Algorithm </param>
    //        /// <param name="encryptedKey"> Key to be unwrapped </param>
    //        /// <param name="cancellationToken"> cancellation token </param>
    //        /// <returns></returns>
    //        public override Task<UnwrapResult> UnwrapKeyAsync(KeyWrapAlgorithm algorithm, byte[] encryptedKey,
    //        CancellationToken cancellationToken = default)
    //        {
    //            if (encryptedKey.IsNullOrEmpty())
    //            {
    //                throw new ArgumentNullException("Key is Null.");
    //            }
    //
    //            byte[] unwrappedKey = this.Decrypt(encryptedKey, this.secretkey, this.iv);
    //
    //            // simulate a null unwrapped key.
    //            if (this.keyId.ToString().Contains(KeyVaultTestConstants.ValidateNullUnwrappedKey))
    //            {
    //                unwrappedKey = null;
    //            }
    //
    //            string keyid = "12345678910";
    //            UnwrapResult mockUnwrapResult = CryptographyModelFactory.UnwrapResult(keyId: keyid,
    //            key:unwrappedKey, algorithm: KeyVaultConstants.RsaOaep256);
    //            return Task.FromResult(mockUnwrapResult);
    //        }
    //
    //        private byte[] PerformCryptography(byte[] data, ICryptoTransform cryptoTransform)
    //        {
    //            using (MemoryStream ms = new MemoryStream())
    //            using (CryptoStream cryptoStream = new CryptoStream(ms, cryptoTransform, CryptoStreamMode.Write))
    //            {
    //                cryptoStream.Write(data, 0, data.Length);
    //                cryptoStream.FlushFinalBlock();
    //
    //                return ms.ToArray();
    //            }
    //        }
    //
    //        private byte[] Encrypt(byte[] data, byte[] key, byte[] iv)
    //        {
    //            using (Aes aes = Aes.Create())
    //            {
    //                aes.KeySize = 128;
    //                aes.BlockSize = 128;
    //                aes.Padding = PaddingMode.Zeros;
    //
    //                aes.Key = key;
    //                aes.IV = iv;
    //
    //                using (ICryptoTransform encryptor = aes.CreateEncryptor(aes.Key, aes.IV))
    //                {
    //                    return this.PerformCryptography(data, encryptor);
    //                }
    //            }
    //        }
    //
    //        private byte[] Decrypt(byte[] data, byte[] key, byte[] iv)
    //        {
    //            using (Aes aes = Aes.Create())
    //            {
    //                aes.KeySize = 128;
    //                aes.BlockSize = 128;
    //                aes.Padding = PaddingMode.Zeros;
    //
    //                aes.Key = key;
    //                aes.IV = iv;
    //
    //                using (ICryptoTransform decryptor = aes.CreateDecryptor(aes.Key, aes.IV))
    //                {
    //                    return this.PerformCryptography(data, decryptor);
    //                }
    //            }
    //        }
    //    }

    /**
     * Factory Class for CryptographyClient.
     * Returns an instance of TestCryptographyClient for mocking CryptographyClient.
     */
    public static class CryptographyClientFactoryTestFactory extends CryptographyClientFactory {
        public CryptographyAsyncClient getCryptographyClient(KeyVaultKeyUriProperties keyVaultKeyUriProperties,
                                                             TokenCredential tokenCred) {
            return new TestCryptographyClientProvider().createTestCryptographyClient(keyVaultKeyUriProperties.getKeyUri(), tokenCred);
        }
    }

    public static class KeyVaultTestConstants {
        public static final String ValidateNullWrappedKey = "nullWrappedKeyByte";
        public static final String ValidateNullUnwrappedKey = "nullUnwrappedKeyByte";
        public static final String ValidateRequestFailedEx = "requestFailed/";
        public static final String ValidateNullKeyVaultKey = "nullKeyVaultKey/";
    }
}

//
//Copyright © Microsoft Corporation, All Rights Reserved
//
//Licensed under the Apache License, Version 2.0 (the "License");
//you may not use this file except in compliance with the License.
//You may obtain a copy of the License at
//
//http://www.apache.org/licenses/LICENSE-2.0
//
//THIS CODE IS PROVIDED *AS IS* BASIS, WITHOUT WARRANTIES OR CONDITIONS
//OF ANY KIND, EITHER EXPRESS OR IMPLIED, INCLUDING WITHOUT LIMITATION
//ANY IMPLIED WARRANTIES OR CONDITIONS OF TITLE, FITNESS FOR A
//PARTICULAR PURPOSE, MERCHANTABILITY OR NON-INFRINGEMENT.
//
//See the Apache License, Version 2.0 for the specific language
//governing permissions and limitations under the License.

package com.microsoft.azure.keyvault.extensions.test;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.fail;

import java.security.Provider;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.codec.binary.Base64;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.microsoft.azure.keyvault.core.IKey;
import com.microsoft.azure.keyvault.extensions.KeyVaultKeyResolver;
import com.microsoft.azure.keyvault.models.KeyBundle;
import com.microsoft.azure.keyvault.models.SecretBundle;
import com.microsoft.azure.keyvault.requests.CreateKeyRequest;
import com.microsoft.azure.keyvault.requests.SetSecretRequest;
import com.microsoft.azure.keyvault.webkey.JsonWebKeyType;

public class KeyVaultKeyResolverBCProviderTest extends KeyVaultClientIntegrationTestBase {

    private Provider _provider = null;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
        try {
            _provider = (Provider) Class.forName("org.bouncycastle.jce.provider.BouncyCastleProvider").newInstance();
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(ex.getMessage());
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(ex.getMessage());
        } catch (InstantiationException ex) {
            throw new RuntimeException(ex.getMessage());
        }
    }

    @After
    public void tearDown() throws Exception {
    }

    private static final String KEY_NAME    = "JavaExtensionKey";
    private static final String SECRET_NAME = "JavaExtensionSecret";
    
    private static final Base64 _base64 = new Base64(-1, null, true);

    @Test
    public void KeyVault_KeyVaultKeyResolver_Key() throws InterruptedException, ExecutionException
    {
        try {
            // Create a key on a vault.
            CreateKeyRequest           request  = new CreateKeyRequest.Builder(getVaultUri(), KEY_NAME, JsonWebKeyType.RSA).build();
            KeyBundle bundle = keyVaultClient.createKey(request);
    
            if ( bundle != null )
            {
                try
                {
                    // ctor with client
                    KeyVaultKeyResolver resolver = new KeyVaultKeyResolver( keyVaultClient, _provider );
    
                    Future<IKey> baseKeyFuture    = resolver.resolveKeyAsync( bundle.keyIdentifier().baseIdentifier() );
                    Future<IKey> versionKeyFuture = resolver.resolveKeyAsync( bundle.keyIdentifier().identifier() );
    
                    IKey baseKey    = baseKeyFuture.get();
                    IKey versionKey = versionKeyFuture.get();
    
                    Assert.assertEquals( baseKey.getKid(), versionKey.getKid() );
                }
                finally
                {
                    // Delete the key
                    keyVaultClient.deleteKey( getVaultUri(), KEY_NAME );
                }
            }
        }
        catch ( Exception ex )
        {
            Assert.fail(ex.getMessage());
        }
    }

     /* 
      * Test resolving a key from a 128bit secret encoded as base64 in a vault using various KeyVaultKeyResolver constructors.
      */
     @Test
     public void KeyVault_KeyVaultKeyResolver_Secret128Base64() throws InterruptedException, ExecutionException
     {
         // Arrange
         byte[] keyBytes = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F };
         byte[] CEK      = { 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte) 0x88, (byte) 0x99, (byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD, (byte) 0xEE, (byte) 0xFF };
         byte[] EK       = { 0x1F, (byte) 0xA6, (byte) 0x8B, 0x0A, (byte) 0x81, 0x12, (byte) 0xB4, 0x47, (byte) 0xAE, (byte) 0xF3, 0x4B, (byte) 0xD8, (byte) 0xFB, 0x5A, 0x7B, (byte) 0x82, (byte) 0x9D, 0x3E, (byte) 0x86, 0x23, 0x71, (byte) 0xD2, (byte) 0xCF, (byte) 0xE5 };

         try {

             SetSecretRequest request      = new SetSecretRequest.Builder(getVaultUri(), SECRET_NAME, _base64.encodeAsString(keyBytes)).withContentType("application/octet-stream").build();
             SecretBundle     secretBundle = keyVaultClient.setSecret( request );
    
             if ( secretBundle != null )
             {
                 try {
                     // ctor with client
                     KeyVaultKeyResolver resolver = new KeyVaultKeyResolver( keyVaultClient );
    
                     IKey baseKey    = resolver.resolveKeyAsync( secretBundle.secretIdentifier().baseIdentifier() ).get();
                     IKey versionKey = resolver.resolveKeyAsync( secretBundle.secretIdentifier().identifier() ).get();
    
                     // Check for correct key identifiers
                     Assert.assertEquals( baseKey.getKid(), versionKey.getKid() );
    
                     // Ensure key operations give the expected results
                     byte[] encrypted = null;
    
                     try {
                         encrypted = baseKey.wrapKeyAsync(CEK, "A128KW").get().getLeft();
                     } catch (Exception e) {
                         fail(e.getMessage());
                     }
    
                     // Assert
                     assertArrayEquals(EK, encrypted);
    
                     try {
                         encrypted = versionKey.wrapKeyAsync(CEK, "A128KW").get().getLeft();
                     } catch (Exception e) {
                         fail(e.getMessage());
                     }
    
                     // Assert
                     assertArrayEquals(EK, encrypted);
                 }
                 finally
                 {
                     // Delete the secret
                     keyVaultClient.deleteSecret( getVaultUri(), SECRET_NAME );
                 }
             }
         }
         catch ( Exception ex ) {
             Assert.fail(ex.getMessage());
         }
     }

     /* 
      * Test resolving a key from a 128bit secret encoded as base64 in a vault using various KeyVaultKeyResolver constructors.
      */
     @Test
     public void KeyVault_KeyVaultKeyResolver_Secret192Base64() throws InterruptedException, ExecutionException
     {
         // Arrange
         byte[] keyBytes = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17 };
         byte[] CEK      = { 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte) 0x88, (byte) 0x99, (byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD, (byte) 0xEE, (byte) 0xFF };
         byte[] EK       = { (byte) 0x96, 0x77, (byte) 0x8B, 0x25, (byte) 0xAE, 0x6C, (byte) 0xA4, 0x35, (byte) 0xF9, 0x2B, 0x5B, (byte) 0x97, (byte) 0xC0, 0x50, (byte) 0xAE, (byte) 0xD2, 0x46, (byte) 0x8A, (byte) 0xB8, (byte) 0xA1, 0x7A, (byte) 0xD8, 0x4E, 0x5D };

         try {
             SetSecretRequest request      = new SetSecretRequest.Builder(getVaultUri(), SECRET_NAME, _base64.encodeAsString(keyBytes)).withContentType("application/octet-stream").build();
             SecretBundle     secretBundle = keyVaultClient.setSecret( request );
    
             if ( secretBundle != null )
             {
                 try
                 {
                     // ctor with client
                     KeyVaultKeyResolver resolver = new KeyVaultKeyResolver( keyVaultClient, _provider );
    
                     IKey baseKey    = resolver.resolveKeyAsync( secretBundle.secretIdentifier().baseIdentifier() ).get();
                     IKey versionKey = resolver.resolveKeyAsync( secretBundle.secretIdentifier().identifier() ).get();
    
                     // Check for correct key identifiers
                     Assert.assertEquals( baseKey.getKid(), versionKey.getKid() );
    
                     // Ensure key operations give the expected results
                     byte[] encrypted = null;
    
                     try {
                         encrypted = baseKey.wrapKeyAsync(CEK, "A192KW").get().getLeft();
                     } catch (Exception e) {
                         fail(e.getMessage());
                     }
    
                     // Assert
                     assertArrayEquals(EK, encrypted);
    
                     try {
                         encrypted = versionKey.wrapKeyAsync(CEK, "A192KW").get().getLeft();
                     } catch (Exception e) {
                         fail(e.getMessage());
                     }
    
                     // Assert
                     assertArrayEquals(EK, encrypted);
                 }
                 finally
                 {
                     // Delete the key
                     keyVaultClient.deleteSecret( getVaultUri(), SECRET_NAME );
                 }
             }
         } catch (Exception ex) {
             Assert.fail(ex.getMessage());
         }
     }

     /* 
      * Test resolving a key from a 256bit secret encoded as base64 in a vault using various KeyVaultKeyResolver constructors.
      */
     @Test
     public void KeyVault_KeyVaultKeyResolver_Secret256Base64() throws InterruptedException, ExecutionException
     {
         // Arrange
         byte[] keyBytes = { 0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0A, 0x0B, 0x0C, 0x0D, 0x0E, 0x0F, 0x10, 0x11, 0x12, 0x13, 0x14, 0x15, 0x16, 0x17, 0x18, 0x19, 0x1A, 0x1B, 0x1C, 0x1D, 0x1E, 0x1F };
         byte[] CEK      = { 0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77, (byte) 0x88, (byte) 0x99, (byte) 0xAA, (byte) 0xBB, (byte) 0xCC, (byte) 0xDD, (byte) 0xEE, (byte) 0xFF };
         byte[] EK       = { 0x64, (byte) 0xE8, (byte) 0xC3, (byte) 0xF9, (byte) 0xCE, 0x0F, 0x5B, (byte) 0xA2, 0x63, (byte) 0xE9, 0x77, 0x79, 0x05, (byte) 0x81, (byte) 0x8A, 0x2A, (byte) 0x93, (byte) 0xC8, 0x19, 0x1E, 0x7D, 0x6E, (byte) 0x8A, (byte) 0xE7 };

         try {
             SetSecretRequest request      = new SetSecretRequest.Builder(getVaultUri(), SECRET_NAME, _base64.encodeAsString(keyBytes)).withContentType("application/octet-stream").build();
             SecretBundle     secretBundle = keyVaultClient.setSecret( request );
    
             if ( secretBundle != null )
             {
                 try
                 {
                     // ctor with client
                     KeyVaultKeyResolver resolver = new KeyVaultKeyResolver( keyVaultClient, _provider );
    
                     IKey baseKey    = resolver.resolveKeyAsync( secretBundle.secretIdentifier().baseIdentifier() ).get();
                     IKey versionKey = resolver.resolveKeyAsync( secretBundle.secretIdentifier().identifier() ).get();
    
                     // Check for correct key identifiers
                     Assert.assertEquals( baseKey.getKid(), versionKey.getKid() );
    
                     // Ensure key operations give the expected results
                     byte[] encrypted = null;
    
                     try {
                         encrypted = baseKey.wrapKeyAsync(CEK, "A256KW").get().getLeft();
                     } catch (Exception e) {
                         fail(e.getMessage());
                     }
    
                     // Assert
                     assertArrayEquals(EK, encrypted);
    
                     try {
                         encrypted = versionKey.wrapKeyAsync(CEK, "A256KW").get().getLeft();
                     } catch (Exception e) {
                         fail(e.getMessage());
                     }
    
                     // Assert
                     assertArrayEquals(EK, encrypted);
                 }
                 finally
                 {
                     // Delete the key
                     keyVaultClient.deleteSecret( getVaultUri(), SECRET_NAME );
                 }
             }
         } catch ( Exception ex ) {
             fail(ex.getMessage());
         }
     }
}

package com.microsoft.azure.keyvault.extensions.test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.junit.Assert;
import org.junit.Test;

import com.microsoft.azure.keyvault.core.IKey;
import com.microsoft.azure.keyvault.extensions.KeyVaultKeyResolver;
import com.microsoft.azure.keyvault.models.KeyBundle;

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

public class KeyVaultKeyResolverTest extends KeyVaultExtensionsIntegrationTestBase {

	private static final String KEY_NAME = "JavaExtensionKey";

	@Test
	public void KeyVault_KeyVaultKeyResolver_Key() throws InterruptedException, ExecutionException
	{
		// Create a key on a vault.
		Future<KeyBundle> result = keyVaultClient.createKeyAsync(getVaultUri(), KEY_NAME, "RSA", null, null, null, null);
		KeyBundle bundle = result.get();

		if ( bundle != null )
		{
			try
			{
				// ctor with client
				KeyVaultKeyResolver resolver = new KeyVaultKeyResolver( keyVaultClient );

				Future<IKey> baseKeyFuture    = resolver.resolveKeyAsync( bundle.getKeyIdentifier().getBaseIdentifier() );
				Future<IKey> versionKeyFuture = resolver.resolveKeyAsync( bundle.getKeyIdentifier().getIdentifier() );

				IKey baseKey    = baseKeyFuture.get();
				IKey versionKey = versionKeyFuture.get();

				Assert.assertEquals( baseKey.getKid(), versionKey.getKid() );
			}
			finally
			{
				// Delete the key
				Future<KeyBundle> deletedKeyFuture = keyVaultClient.deleteKeyAsync( getVaultUri(), KEY_NAME );
				
				deletedKeyFuture.get();
			}
		}
	}

	/*

     /// <summary>
     /// Test resolving a key from a 128bit secret encoded as base64 in a vault using various KeyVaultKeyResolver constructors.
     /// </summary>
     [Fact]
     public async Task KeyVault_KeyVaultKeyResolver_Secret128Base64()
     {
         // Arrange
         var client   = CreateKeyVaultClient();
         var vault    = ConfigurationManager.AppSettings["VaultUrl"];

         var keyBytes = new byte[128 >> 3];

         new RNGCryptoServiceProvider().GetNonZeroBytes( keyBytes );

         var secret   = await client.SetSecretAsync( vault, "TestSecret", Convert.ToBase64String( keyBytes ), null, "application/octet-stream" ).ConfigureAwait( false );

         if ( secret != null )
         {
             try
             {
                 // ctor with client
                 var resolver = new KeyVaultKeyResolver( client );

                 var baseKey    = await resolver.ResolveKeyAsync( secret.SecretIdentifier.BaseIdentifier, default( CancellationToken ) ).ConfigureAwait( false );
                 var versionKey = await resolver.ResolveKeyAsync( secret.SecretIdentifier.Identifier, default( CancellationToken ) ).ConfigureAwait( false );

                 Assert.Equal( baseKey.Kid, versionKey.Kid );

                 // ctor with authentication callback
                 resolver = new KeyVaultKeyResolver( GetAccessToken );

                 baseKey = await resolver.ResolveKeyAsync( secret.SecretIdentifier.BaseIdentifier, default( CancellationToken ) ).ConfigureAwait( false );
                 versionKey = await resolver.ResolveKeyAsync( secret.SecretIdentifier.Identifier, default( CancellationToken ) ).ConfigureAwait( false );

                 Assert.Equal( baseKey.Kid, versionKey.Kid );

                 // ctor with vault name and client
                 resolver = new KeyVaultKeyResolver( vault, client );

                 baseKey = await resolver.ResolveKeyAsync( secret.SecretIdentifier.BaseIdentifier, default( CancellationToken ) ).ConfigureAwait( false );
                 versionKey = await resolver.ResolveKeyAsync( secret.SecretIdentifier.Identifier, default( CancellationToken ) ).ConfigureAwait( false );

                 Assert.Equal( baseKey.Kid, versionKey.Kid );

                 // ctor with vault name and authentication callback
                 resolver = new KeyVaultKeyResolver( vault, GetAccessToken );

                 baseKey = await resolver.ResolveKeyAsync( secret.SecretIdentifier.BaseIdentifier, default( CancellationToken ) ).ConfigureAwait( false );
                 versionKey = await resolver.ResolveKeyAsync( secret.SecretIdentifier.Identifier, default( CancellationToken ) ).ConfigureAwait( false );

                 Assert.Equal( baseKey.Kid, versionKey.Kid );
             }
             finally
             {
                 // Delete the key
                 client.DeleteSecretAsync( vault, "TestSecret" ).GetAwaiter().GetResult();
             }
         }
     }

     /// <summary>
     /// Test resolving a key from a 192bit secret encoded as base64 in a vault using various KeyVaultKeyResolver constructors.
     /// </summary>
     [Fact]
     public async Task KeyVault_KeyVaultKeyResolver_Secret192Base64()
     {
         // Arrange
         var client   = CreateKeyVaultClient();
         var vault    = ConfigurationManager.AppSettings["VaultUrl"];

         var keyBytes = new byte[192 >> 3];

         new RNGCryptoServiceProvider().GetNonZeroBytes( keyBytes );

         var secret   = await client.SetSecretAsync( vault, "TestSecret", Convert.ToBase64String( keyBytes ), null, "application/octet-stream" ).ConfigureAwait( false );

         if ( secret != null )
         {
             try
             {
                 // ctor with client
                 var resolver = new KeyVaultKeyResolver( client );

                 var baseKey    = await resolver.ResolveKeyAsync( secret.SecretIdentifier.BaseIdentifier, default( CancellationToken ) ).ConfigureAwait( false );
                 var versionKey = await resolver.ResolveKeyAsync( secret.SecretIdentifier.Identifier, default( CancellationToken ) ).ConfigureAwait( false );

                 Assert.Equal( baseKey.Kid, versionKey.Kid );

                 // ctor with authentication callback
                 resolver = new KeyVaultKeyResolver( GetAccessToken );

                 baseKey = await resolver.ResolveKeyAsync( secret.SecretIdentifier.BaseIdentifier, default( CancellationToken ) ).ConfigureAwait( false );
                 versionKey = await resolver.ResolveKeyAsync( secret.SecretIdentifier.Identifier, default( CancellationToken ) ).ConfigureAwait( false );

                 Assert.Equal( baseKey.Kid, versionKey.Kid );

                 // ctor with vault name and client
                 resolver = new KeyVaultKeyResolver( vault, client );

                 baseKey = await resolver.ResolveKeyAsync( secret.SecretIdentifier.BaseIdentifier, default( CancellationToken ) ).ConfigureAwait( false );
                 versionKey = await resolver.ResolveKeyAsync( secret.SecretIdentifier.Identifier, default( CancellationToken ) ).ConfigureAwait( false );

                 Assert.Equal( baseKey.Kid, versionKey.Kid );

                 // ctor with vault name and authentication callback
                 resolver = new KeyVaultKeyResolver( vault, GetAccessToken );

                 baseKey = await resolver.ResolveKeyAsync( secret.SecretIdentifier.BaseIdentifier, default( CancellationToken ) ).ConfigureAwait( false );
                 versionKey = await resolver.ResolveKeyAsync( secret.SecretIdentifier.Identifier, default( CancellationToken ) ).ConfigureAwait( false );

                 Assert.Equal( baseKey.Kid, versionKey.Kid );
             }
             finally
             {
                 // Delete the key
                 client.DeleteSecretAsync( vault, "TestSecret" ).GetAwaiter().GetResult();
             }
         }
     }

     /// <summary>
     /// Test resolving a key from a 256bit secret encoded as base64 in a vault using various KeyVaultKeyResolver constructors.
     /// </summary>
     [Fact]
     public async Task KeyVault_KeyVaultKeyResolver_Secret256Base64()
     {
         // Arrange
         var client   = CreateKeyVaultClient();
         var vault    = ConfigurationManager.AppSettings["VaultUrl"];

         var keyBytes = new byte[256 >> 3];

         new RNGCryptoServiceProvider().GetNonZeroBytes( keyBytes );

         var secret   = await client.SetSecretAsync( vault, "TestSecret", Convert.ToBase64String( keyBytes ), null, "application/octet-stream" ).ConfigureAwait( false );

         if ( secret != null )
         {
             try
             {
                 // ctor with client
                 var resolver = new KeyVaultKeyResolver( client );

                 var baseKey    = await resolver.ResolveKeyAsync( secret.SecretIdentifier.BaseIdentifier, default( CancellationToken ) ).ConfigureAwait( false );
                 var versionKey = await resolver.ResolveKeyAsync( secret.SecretIdentifier.Identifier, default( CancellationToken ) ).ConfigureAwait( false );

                 Assert.Equal( baseKey.Kid, versionKey.Kid );

                 // ctor with authentication callback
                 resolver = new KeyVaultKeyResolver( GetAccessToken );

                 baseKey = await resolver.ResolveKeyAsync( secret.SecretIdentifier.BaseIdentifier, default( CancellationToken ) ).ConfigureAwait( false );
                 versionKey = await resolver.ResolveKeyAsync( secret.SecretIdentifier.Identifier, default( CancellationToken ) ).ConfigureAwait( false );

                 Assert.Equal( baseKey.Kid, versionKey.Kid );

                 // ctor with vault name and client
                 resolver = new KeyVaultKeyResolver( vault, client );

                 baseKey = await resolver.ResolveKeyAsync( secret.SecretIdentifier.BaseIdentifier, default( CancellationToken ) ).ConfigureAwait( false );
                 versionKey = await resolver.ResolveKeyAsync( secret.SecretIdentifier.Identifier, default( CancellationToken ) ).ConfigureAwait( false );

                 Assert.Equal( baseKey.Kid, versionKey.Kid );

                 // ctor with vault name and authentication callback
                 resolver = new KeyVaultKeyResolver( vault, GetAccessToken );

                 baseKey = await resolver.ResolveKeyAsync( secret.SecretIdentifier.BaseIdentifier, default( CancellationToken ) ).ConfigureAwait( false );
                 versionKey = await resolver.ResolveKeyAsync( secret.SecretIdentifier.Identifier, default( CancellationToken ) ).ConfigureAwait( false );

                 Assert.Equal( baseKey.Kid, versionKey.Kid );
             }
             finally
             {
                 // Delete the key
                 client.DeleteSecretAsync( vault, "TestSecret" ).GetAwaiter().GetResult();
             }
         }
     }
	 */
}

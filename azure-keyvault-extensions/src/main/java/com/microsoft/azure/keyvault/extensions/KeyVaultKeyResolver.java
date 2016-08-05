/**
 *
 * Copyright (c) Microsoft and contributors.  All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.microsoft.azure.keyvault.extensions;

import java.security.Provider;
import org.apache.commons.codec.binary.Base64;

import com.google.common.base.Function;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.microsoft.azure.keyvault.KeyIdentifier;
import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.SecretIdentifier;
import com.microsoft.azure.keyvault.core.IKey;
import com.microsoft.azure.keyvault.core.IKeyResolver;
import com.microsoft.azure.keyvault.cryptography.SymmetricKey;
import com.microsoft.azure.keyvault.models.KeyBundle;
import com.microsoft.azure.keyvault.models.SecretBundle;

public class KeyVaultKeyResolver implements IKeyResolver {

    static final Base64 _base64 = new Base64(-1, null, true);
    
    class FutureKeyFromKey implements Function<KeyBundle, IKey> {

        protected FutureKeyFromKey() {
            super();
        }

        @Override
        public IKey apply(KeyBundle keyBundle) {

            if (keyBundle != null) {
                return new KeyVaultKey(_client, keyBundle);
            }

            return null;
        }
    }

    class FutureKeyFromSecret implements Function<SecretBundle, IKey> {

        protected FutureKeyFromSecret() {
            super();
        }

        @Override
        public IKey apply(SecretBundle secretBundle) {

            if (secretBundle != null && secretBundle.contentType().equalsIgnoreCase("application/octet-stream")) {
                byte[] keyBytes = _base64.decode(secretBundle.value());

                if (keyBytes != null) {
                    return new SymmetricKey(secretBundle.id(), keyBytes, _provider );
                }
            }

            return null;
        }
    }

    private final KeyVaultClient _client;
    private final Provider       _provider;

    public KeyVaultKeyResolver(KeyVaultClient client) {
        _client   = client;
        _provider = null;
    }
    
    public KeyVaultKeyResolver(KeyVaultClient client, Provider provider) {
    	_client   = client;
    	_provider = provider;
    }

    private ListenableFuture<IKey> resolveKeyFromSecretAsync(String kid) {
    	
    	FutureServiceCall<SecretBundle> futureCall = new FutureServiceCall<SecretBundle>();
    	ListenableFuture<IKey>          result     = Futures.transform(futureCall, new FutureKeyFromSecret());
    	
    	futureCall.setServiceCall( _client.getSecret(kid, futureCall.getServiceCallback()) );
    	
    	return result;
    }

    private ListenableFuture<IKey> resolveKeyFromKeyAsync(String kid) {
    	
    	FutureServiceCall<KeyBundle> futureCall = new FutureServiceCall<KeyBundle>();
    	ListenableFuture<IKey>       result     = Futures.transform(futureCall, new FutureKeyFromKey());
    	
    	futureCall.setServiceCall( _client.getKey(kid, futureCall.getServiceCallback()) );
    	
    	return result;
    }

    @Override
    public ListenableFuture<IKey> resolveKeyAsync(String kid) {

        if (KeyIdentifier.isKeyIdentifier(kid)) {
            return resolveKeyFromKeyAsync(kid);
        } else if (SecretIdentifier.isSecretIdentifier(kid)) {
            return resolveKeyFromSecretAsync(kid);
        }

        return Futures.immediateFuture(null);
    }

}

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
import java.util.concurrent.Future;

import org.apache.commons.codec.binary.Base64;

import com.microsoft.azure.keyvault.implementation.KeyVaultClient;
import com.microsoft.azure.keyvault.core.IKey;
import com.microsoft.azure.keyvault.core.IKeyResolver;
import com.microsoft.azure.keyvault.cryptography.SymmetricKey;
import com.microsoft.azure.keyvault.models.KeyBundle;
import com.microsoft.azure.keyvault.implementation.KeyIdentifier;
import com.microsoft.azure.keyvault.models.SecretBundle;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceResponse;
import com.microsoft.azure.keyvault.implementation.SecretIdentifier;

public class KeyVaultKeyResolver implements IKeyResolver {

    static final Base64 _base64 = new Base64(-1, null, true);
    
    class FutureKeyFromKey extends FutureAdapter<KeyBundle, IKey> {

        protected FutureKeyFromKey() {
            super();
        }

        @Override
        protected IKey translate(KeyBundle keyBundle) {

            if (keyBundle != null) {
                return new KeyVaultKey(_client, keyBundle);
            }

            return null;
        }
    }

    class FutureKeyFromSecret extends FutureAdapter<SecretBundle, IKey> {


        protected FutureKeyFromSecret() {
            super();
        }

        @Override
        protected IKey translate(SecretBundle secretBundle) {

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

    private Future<IKey> resolveKeyFromSecretAsync(String kid) {
    	
    	FutureKeyFromSecret result = new FutureKeyFromSecret();
    	
    	result.setServiceCall( _client.getSecret(kid, result) );
    	
    	return result;
    }

    private Future<IKey> resolveKeyFromKeyAsync(String kid) {
    	
    	FutureKeyFromKey result = new FutureKeyFromKey();
    	
    	
        result.setServiceCall( _client.getKey(kid, result ) );

        return result;
    }

    @Override
    public Future<IKey> resolveKeyAsync(String kid) {

        if (KeyIdentifier.isKeyIdentifier(kid)) {
            return resolveKeyFromKeyAsync(kid);
        } else if (SecretIdentifier.isSecretIdentifier(kid)) {
            return resolveKeyFromSecretAsync(kid);
        }

        return new FutureImmediate<IKey>(null);
    }

}

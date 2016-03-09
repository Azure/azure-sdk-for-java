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

import com.microsoft.azure.keyvault.KeyVaultClient;
import com.microsoft.azure.keyvault.core.IKey;
import com.microsoft.azure.keyvault.core.IKeyResolver;
import com.microsoft.azure.keyvault.models.KeyBundle;
import com.microsoft.azure.keyvault.models.KeyIdentifier;
import com.microsoft.azure.keyvault.models.Secret;
import com.microsoft.azure.keyvault.models.SecretIdentifier;

public class KeyVaultKeyResolver implements IKeyResolver {

    static final Base64 _base64 = new Base64(-1, null, true);
    
    class FutureKeyFromKey extends FutureAdapter<KeyBundle, IKey> {

        protected FutureKeyFromKey(Future<KeyBundle> inner) {
            super(inner);
        }

        @Override
        protected IKey translate(KeyBundle keyBundle) {

            if (keyBundle != null) {
                return new KeyVaultKey(_client, keyBundle);
            }

            return null;
        }
    }

    class FutureKeyFromSecret extends FutureAdapter<Secret, IKey> {


        protected FutureKeyFromSecret(Future<Secret> inner) {
            super(inner);
        }

        @Override
        protected IKey translate(Secret secretBundle) {

            if (secretBundle != null && secretBundle.getContentType().equalsIgnoreCase("application/octet-stream")) {
                byte[] keyBytes = _base64.decode(secretBundle.getValue());

                if (keyBytes != null) {
                    return new SymmetricKey(secretBundle.getId(), keyBytes, _provider );
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
        return new FutureKeyFromSecret(_client.getSecretAsync(kid));
    }

    private Future<IKey> resolveKeyFromKeyAsync(String kid) {
        return new FutureKeyFromKey(_client.getKeyAsync(kid));
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

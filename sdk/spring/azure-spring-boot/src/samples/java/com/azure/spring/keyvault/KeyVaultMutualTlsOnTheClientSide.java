// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.keyvault;

import org.apache.http.conn.ssl.TrustSelfSignedStrategy;
import org.apache.http.ssl.PrivateKeyDetails;
import org.apache.http.ssl.PrivateKeyStrategy;
import org.apache.http.ssl.SSLContexts;

import javax.net.ssl.SSLContext;
import java.net.Socket;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.util.Map;

public class KeyVaultMutualTlsOnTheClientSide {

    KeyStore ks = KeyStore.getInstance("AzureKeyVault");

    public KeyVaultMutualTlsOnTheClientSide() throws KeyStoreException,
        UnrecoverableKeyException, NoSuchAlgorithmException, KeyManagementException {
    }

    SSLContext sslContext = SSLContexts.custom()
                                       .loadKeyMaterial(ks, "".toCharArray(), new ClientPrivateKeyStrategy())
                                       .loadTrustMaterial(ks, new TrustSelfSignedStrategy())
                                       .build();

    private static class ClientPrivateKeyStrategy implements PrivateKeyStrategy {
        @Override
        public String chooseAlias(Map<String, PrivateKeyDetails> map, Socket socket) {
            return "self-signed";
        }
    }
}

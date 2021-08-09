// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.keyvault.jca;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.security.KeyStore;
import java.security.Security;

/**
 * The ServerSSL sample.
 */
public class ServerSSLSample {

    public static void main(String[] args) throws Exception {
        KeyVaultJcaProvider provider = new KeyVaultJcaProvider();
        Security.addProvider(provider);

        KeyStore keyStore = KeyVaultKeyStore.getKeyVaultKeyStoreBySystemProperty();

        KeyManagerFactory managerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        managerFactory.init(keyStore, "".toCharArray());

        SSLContext context = SSLContext.getInstance("TLS");
        context.init(managerFactory.getKeyManagers(), null, null);

        SSLServerSocketFactory socketFactory = context.getServerSocketFactory();
        SSLServerSocket serverSocket = (SSLServerSocket) socketFactory.createServerSocket(8765);
    }

}

/**
 * Copyright Microsoft Corporation
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.microsoft.windowsazure.services.management;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.SecureRandom;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

public class SSLContextFactory {

    public static SSLContext createSSLContext(ConnectionCredential cred) throws GeneralSecurityException, IOException {
        return createSSLContext(cred.getKeyStore(), cred.getKeyPasswd(), cred.getKeyStoreType());
    }

    public static SSLContext createSSLContext(InputStream keyStoreStream, String keyStreamPasswd, KeyStoreType type)
            throws GeneralSecurityException, IOException {
        // Could Proxy KeyManagers to include only those with a specific alias for multi-cert file....
        KeyManager[] keyManagers = getKeyManagers(keyStoreStream, keyStreamPasswd, type.name());
        // note: may want to broaden this to SSLv3, SSLv2, SSL, etc...
        SSLContext context = SSLContext.getInstance("TLS");
        // use default TrustManager and SecureRandom
        context.init(keyManagers, null, new SecureRandom());
        HttpsURLConnection.setDefaultSSLSocketFactory(context.getSocketFactory());

        return context;
    }

    private static KeyManager[] getKeyManagers(InputStream keyStoreStream, String keyStreamPasswd, String type)
            throws IOException, GeneralSecurityException {

        KeyStore ks = KeyStore.getInstance(type);
        ks.load(keyStoreStream, keyStreamPasswd.toCharArray());
        keyStoreStream.close();

        String alg = KeyManagerFactory.getDefaultAlgorithm();
        KeyManagerFactory kmFact = KeyManagerFactory.getInstance(alg);
        kmFact.init(ks, keyStreamPasswd.toCharArray());

        return kmFact.getKeyManagers();
    }

}
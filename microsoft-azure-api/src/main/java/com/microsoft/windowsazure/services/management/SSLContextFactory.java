/*
 * 
 * The author contributes this code to the public domain,
 * retaining no rights and incurring no responsibilities for its use in whole or in part.
 */
package com.microsoft.windowsazure.services.management;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.security.KeyStore;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;

/**
 * Note: as it stands this provides a TLS SSLContext from jks and pfx stores. It could be modified to
 * support other protocols (SSLv3, SSLv2, etc) and different key stores...
 * 
 * Note that older .pfx files may need to be updated to pkcs12 format....
 * 
 */
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
        context.init(keyManagers, null, null);
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
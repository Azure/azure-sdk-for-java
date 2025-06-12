package org.example;

import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.security.KeyStore;

public class DefaultTrustManagerRetriever {
    public static X509TrustManager getDefaultTrustManager() throws Exception {
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
        tmf.init((KeyStore) null);
        return (X509TrustManager) tmf.getTrustManagers()[0];
    }
}

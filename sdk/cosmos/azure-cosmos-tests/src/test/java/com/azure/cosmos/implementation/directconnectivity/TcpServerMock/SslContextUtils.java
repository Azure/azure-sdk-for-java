// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation.directconnectivity.TcpServerMock;

import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.InputStream;
import java.security.KeyStore;

/**
 *
 *  The .jks file can be generated as following:
 *     // openssl req -x509 -nodes -days 365 -newkey rsa:4096 -keyout testKey.pem -out testCert.crt  -subj /CN=localhost
 *     // keytool -import -v -trustcacerts -alias client-alias -file testCert.pem -keystore client.jks -keypass rntbdTest -storepass rntbdTest
 *     // openssl pkcs12 -export -in cert.pem -inkey testKey.pem -certfile testCert.pem -out keystore.p12
 *     // keytool -importkeystore -srckeystore keystore.p12 -srcstoretype pkcs12 -destkeystore server.jks -deststoretype JKS
 *
 */
public class SslContextUtils {
    private static final String STOREPASS = "rntbdTest";
    private static final Logger logger = LoggerFactory.getLogger(SslContextUtils.class);

    public static SslContext CreateSslContext(String keyStore, boolean isServer) {
        SslContext sslContext = null;

        try {
            final ClassLoader classloader = SslContextUtils.class.getClassLoader();
            final InputStream inputStream = classloader.getResourceAsStream(keyStore);

            final KeyStore trustStore = KeyStore.getInstance("jks");
            trustStore.load(inputStream, STOREPASS.toCharArray());

            final KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            keyManagerFactory.init(trustStore, STOREPASS.toCharArray());

            final TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(trustStore);

            if (isServer) {
                sslContext = SslContextBuilder.forServer(keyManagerFactory).trustManager(trustManagerFactory).build();
            } else {
                sslContext = SslContextBuilder.forClient().keyManager(keyManagerFactory).trustManager(trustManagerFactory).build();
            }
        } catch (Exception exception) {
            logger.error("Initializing sslContext failed {}", exception);
        }

        return sslContext;
    }
}

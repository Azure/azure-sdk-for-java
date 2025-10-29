// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation.util;

import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SNIServerName;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Collections;

public final class IdentitySslUtil {
    public static final HostnameVerifier ALL_HOSTS_ACCEPT_HOSTNAME_VERIFIER;

    static {
        ALL_HOSTS_ACCEPT_HOSTNAME_VERIFIER = new HostnameVerifier() {
            @SuppressWarnings("BadHostnameVerifier")
            @Override
            public boolean verify(String hostname, SSLSession session) {
                return true;
            }
        };
    }

    private IdentitySslUtil() {
    }

    /**
     *
     * Pins the specified HTTPS URL Connection to work against a specific server-side certificate with
     * the specified thumbprint only.
     *
     * @param httpsUrlConnection The https url connection to configure
     * @param certificateThumbprint The thumbprint of the certificate
     * @param logger The {@link ClientLogger} used to log any errors that occur in this method call.
     */
    public static void addTrustedCertificateThumbprint(HttpsURLConnection httpsUrlConnection,
        String certificateThumbprint, ClientLogger logger) {
        //We expect the connection to work against a specific server side certificate only, so it's safe to disable the
        // host name verification.
        if (httpsUrlConnection.getHostnameVerifier() != ALL_HOSTS_ACCEPT_HOSTNAME_VERIFIER) {
            httpsUrlConnection.setHostnameVerifier(ALL_HOSTS_ACCEPT_HOSTNAME_VERIFIER);
        }

        // Create a Trust manager that trusts only certificate with specified thumbprint.
        TrustManager[] certificateTrust = new TrustManager[] { new X509TrustManager() {
            public X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[] { };
            }

            public void checkClientTrusted(X509Certificate[] certificates, String authenticationType)
                throws CertificateException {
                throw logger.logExceptionAsError(new RuntimeException("No client side certificate configured."));
            }

            public void checkServerTrusted(X509Certificate[] certificates, String authenticationType)
                throws CertificateException {
                if (certificates == null || certificates.length == 0) {
                    throw logger
                        .logExceptionAsError(new RuntimeException("Did not receive any certificate from the server."));
                }

                for (X509Certificate x509Certificate : certificates) {
                    String sslCertificateThumbprint = extractCertificateThumbprint(x509Certificate, logger);
                    if (certificateThumbprint.equalsIgnoreCase(sslCertificateThumbprint)) {
                        return;
                    }
                }
                throw logger.logExceptionAsError(
                    new RuntimeException("Thumbprint of certificates received did not match the expected thumbprint."));
            }
        } };

        SSLSocketFactory sslSocketFactory;
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, certificateTrust, null);
            sslSocketFactory = sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw logger.logExceptionAsError(new RuntimeException("Error Creating SSL Context", e));
        }

        // Pin the connection to a specific certificate with specified thumbprint.
        if (httpsUrlConnection.getSSLSocketFactory() != sslSocketFactory) {
            httpsUrlConnection.setSSLSocketFactory(sslSocketFactory);
        }
    }

    private static String extractCertificateThumbprint(Certificate certificate, ClientLogger logger) {
        try {
            StringBuilder thumbprint = new StringBuilder();
            MessageDigest messageDigest = MessageDigest.getInstance("SHA-1");

            byte[] encodedCertificate;

            try {
                encodedCertificate = certificate.getEncoded();
            } catch (CertificateEncodingException e) {
                throw new RuntimeException(e);
            }

            byte[] updatedDigest = messageDigest.digest(encodedCertificate);

            for (byte b : updatedDigest) {
                int unsignedByte = b & 0xff;

                if (unsignedByte < 16) {
                    thumbprint.append("0");
                }
                thumbprint.append(Integer.toHexString(unsignedByte));
            }
            return thumbprint.toString();
        } catch (NoSuchAlgorithmException e) {
            throw logger.logExceptionAsError(new RuntimeException(e));
        }
    }

    public static final class SniSslSocketFactory extends SSLSocketFactory {
        private final SSLSocketFactory sslSocketFactory;
        private final String sniName;

        public SniSslSocketFactory(SSLSocketFactory sslSocketFactory, String sniName) {
            this.sslSocketFactory = sslSocketFactory;
            this.sniName = sniName;
        }

        @Override
        public Socket createSocket(Socket s, String host, int port, boolean autoClose) throws IOException {
            Socket sslSocket = (SSLSocket) sslSocketFactory.createSocket(s, host, port, autoClose);
            configureSni(sslSocket);
            return sslSocket;
        }

        @Override
        public Socket createSocket(String host, int port) throws IOException {
            Socket socket = sslSocketFactory.createSocket(host, port);
            configureSni(socket);
            return socket;
        }

        @Override
        public Socket createSocket(String host, int port, java.net.InetAddress localAddress, int localPort)
            throws IOException {
            Socket socket = sslSocketFactory.createSocket(host, port, localAddress, localPort);
            configureSni(socket);
            return socket;
        }

        @Override
        public Socket createSocket(java.net.InetAddress host, int port) throws IOException {
            Socket socket = sslSocketFactory.createSocket(host, port);
            configureSni(socket);
            return socket;
        }

        @Override
        public Socket createSocket(java.net.InetAddress address, int port, java.net.InetAddress localAddress,
            int localPort) throws IOException {
            Socket socket = sslSocketFactory.createSocket(address, port, localAddress, localPort);
            configureSni(socket);
            return socket;
        }

        @Override
        public String[] getDefaultCipherSuites() {
            return sslSocketFactory.getDefaultCipherSuites();
        }

        @Override
        public String[] getSupportedCipherSuites() {
            return sslSocketFactory.getSupportedCipherSuites();
        }

        private void configureSni(Socket socket) {
            if (socket instanceof SSLSocket && !CoreUtils.isNullOrEmpty(sniName)) {
                SSLSocket sslSocket = (SSLSocket) socket;
                SSLParameters sslParameters = sslSocket.getSSLParameters();
                sslParameters.setServerNames(Collections.singletonList(new RawSniServerName(sniName)));
                sslSocket.setSSLParameters(sslParameters);
            }
        }
    }

    public static final class RawSniServerName extends SNIServerName {
        public RawSniServerName(String sniHost) {
            super(0, sniHost.getBytes(StandardCharsets.UTF_8));
        }
    }
}

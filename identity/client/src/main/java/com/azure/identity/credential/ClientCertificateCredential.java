package com.azure.identity.credential;

import com.azure.core.implementation.util.Base64Util;
import com.microsoft.aad.adal4j.AsymmetricKeyCredential;
import com.microsoft.aad.adal4j.AuthenticationCallback;
import com.microsoft.aad.adal4j.AuthenticationContext;
import com.microsoft.aad.adal4j.AuthenticationResult;
import reactor.core.Exceptions;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An AAD credential that acquires a token with a client certificate for an AAD application.
 */
public class ClientCertificateCredential extends AadCredential<ClientCertificateCredential> {
    private File clientCertificate;
    private String clientCertificatePassword;

    /**
     * Creates a ClientSecretCredential with default AAD endpoint https://login.microsoftonline.com.
     */
    public ClientCertificateCredential() {
        super();
    }

    /**
     * Sets the client certificate for authenticating to AAD.
     * @param pemCertificate the PEM file containing the certificate
     * @return the credential itself
     */
    public ClientCertificateCredential pemCertificate(File pemCertificate) {
        this.clientCertificate = pemCertificate;
        return this;
    }

    /**
     * Sets the client certificate for authenticating to AAD.
     * @param pfxCertificate the password protected PFX file containing the certificate
     * @param clientCertificatePassword the password protecting the PFX file
     * @return the credential itself
     */
    public ClientCertificateCredential pfxCertificate(File pfxCertificate, String clientCertificatePassword) {
        this.clientCertificate = pfxCertificate;
        this.clientCertificatePassword = clientCertificatePassword;
        return this;
    }

    @Override
    public Mono<AuthenticationResult> authenticateAsync(String resource) {
        validate();
        if (clientCertificate == null) {
            throw new IllegalArgumentException("Non-null value must be provided for clientCertificate property in ClientCertificateCredential");
        }
        return acquireAccessToken(resource);
    }

    private Mono<AuthenticationResult> acquireAccessToken(String resource) {
        String authorityUrl = aadEndpoint().replaceAll("/+$", "") + "/" + tenantId();
        ExecutorService executor = Executors.newSingleThreadExecutor();
        AuthenticationContext context;
        try {
            context = new AuthenticationContext(authorityUrl, false, executor);
        } catch (MalformedURLException mue) {
            executor.shutdown();
            throw Exceptions.propagate(mue);
        }
        return Mono.create((Consumer<MonoSink<AuthenticationResult>>) callback -> {
            try {
                if (clientCertificatePassword != null) {
                    context.acquireToken(
                        resource,
                        AsymmetricKeyCredential.create(clientId(), new FileInputStream(clientCertificate), clientCertificatePassword),
                        new AuthenticationCallback<AuthenticationResult>() {
                            @Override
                            public void onSuccess(AuthenticationResult o) {
                                callback.success(o);
                            }

                            @Override
                            public void onFailure(Throwable throwable) {
                                callback.error(throwable);
                            }
                        });
                } else {
                    context.acquireToken(
                        resource,
                        AsymmetricKeyCredential.create(clientId(), privateKeyFromPem(Files.readAllBytes(clientCertificate.toPath())), publicKeyFromPem(Files.readAllBytes(clientCertificate.toPath()))),
                        null).get();
                }
            } catch (Exception e) {
                callback.error(e);
            }
        }).doFinally(s -> executor.shutdown());
    }

    private static PrivateKey privateKeyFromPem(byte[] pem) {
        Pattern pattern = Pattern.compile("(?s)-----BEGIN PRIVATE KEY-----.*-----END PRIVATE KEY-----");
        Matcher matcher = pattern.matcher(new String(pem));
        if (!matcher.find()) {
            throw new IllegalArgumentException("Certificate file provided is not a valid PEM file.");
        }
        String base64 = matcher.group()
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\n", "")
            .replace("\r", "");
        byte[] key = Base64Util.decodeString(base64);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(key);
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new RuntimeException(e);
        }
    }

    private static X509Certificate publicKeyFromPem(byte[] pem) {
        Pattern pattern = Pattern.compile("(?s)-----BEGIN CERTIFICATE-----.*-----END CERTIFICATE-----");
        Matcher matcher = pattern.matcher(new String(pem));
        if (!matcher.find()) {
            throw new IllegalArgumentException("PEM certificate provided does not contain -----BEGIN CERTIFICATE-----END CERTIFICATE----- block");
        }
        try {
            CertificateFactory factory = CertificateFactory.getInstance("X.509");
            InputStream stream = new ByteArrayInputStream(matcher.group().getBytes());
            return (X509Certificate) factory.generateCertificate(stream);
        } catch (CertificateException e) {
            throw new RuntimeException(e);
        }
    }
}

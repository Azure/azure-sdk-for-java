// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.confidentialledger;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.netty.NettyAsyncHttpClientBuilder;
import com.azure.core.http.policy.AddDatePolicy;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpLogDetailLevel;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.HttpLoggingPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.http.policy.HttpPolicyProviders;
import com.azure.core.http.policy.RequestIdPolicy;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.test.TestBase;
import com.azure.core.util.Base64Util;
import com.azure.core.util.Configuration;
import com.azure.core.util.logging.ClientLogger;
import com.azure.identity.DefaultAzureCredentialBuilder;
import io.netty.handler.ssl.SslContextBuilder;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConfidentialLedgerClientTestBase extends TestBase {
    private static final ClientLogger LOGGER = new ClientLogger(ConfidentialLedgerClientTestBase.class);

    protected String getConfidentialLedgerUrl() {
        String endpoint = interceptorManager.isPlaybackMode()
            ? "https://localhost:8080"
            : Configuration.getGlobalConfiguration().get("CONFIDENTIALLEDGER_URL");
        Objects.requireNonNull(endpoint);
        return endpoint;
    }

    protected String getConfidentialIdentityUrl() {
        String endpoint = interceptorManager.isPlaybackMode()
            ? "https://localhost:8080"
            : Configuration.getGlobalConfiguration().get("CONFIDENTIALLEDGER_IDENTITY_URL");
        Objects.requireNonNull(endpoint);
        return endpoint;
    }

    <T> T clientSetup(Function<HttpPipeline, T> clientBuilder) {
        TokenCredential credential = null;

        if (!interceptorManager.isPlaybackMode()) {
            credential = new DefaultAzureCredentialBuilder().build();
        }

        HttpClient httpClient;

        // Closest to API goes first, closest to wire goes last.
        final List<HttpPipelinePolicy> policies = new ArrayList<>();
        policies.add(new RequestIdPolicy());
        policies.add(new AddDatePolicy());

        HttpPolicyProviders.addBeforeRetryPolicies(policies);
        if (credential != null) {
            policies.add(new BearerTokenAuthenticationPolicy(credential, ConfidentialLedgerClientBuilder.DEFAULT_SCOPES));
        }

        policies.add(new RetryPolicy());

        HttpPolicyProviders.addAfterRetryPolicies(policies);
        policies.add(new HttpLoggingPolicy(new HttpLogOptions().setLogLevel(HttpLogDetailLevel.BODY_AND_HEADERS)));

        if (interceptorManager.isPlaybackMode()) {
            httpClient = interceptorManager.getPlaybackClient();
        } else {
            try {
                byte[] pemKey = Files.readAllBytes(Paths.get(
                    Configuration.getGlobalConfiguration().get("CONFIDENTIALLEDGER_KEY_PATH")));
                reactor.netty.http.client.HttpClient reactorClient = reactor.netty.http.client.HttpClient.create()
                    .secure(sslContextSpec -> sslContextSpec.sslContext(SslContextBuilder.forClient()
                        .trustManager(publicKeyFromPem(pemKey))));
                httpClient = new NettyAsyncHttpClientBuilder(reactorClient).wiretap(true).build();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        policies.add(interceptorManager.getRecordPolicy());

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient)
            .build();

        T client;
        client = clientBuilder.apply(pipeline);

        return Objects.requireNonNull(client);
    }

    static PrivateKey privateKeyFromPem(byte[] pem) {
        Pattern pattern = Pattern.compile("(?s)-----BEGIN PRIVATE KEY-----.*-----END PRIVATE KEY-----");
        Matcher matcher = pattern.matcher(new String(pem, StandardCharsets.UTF_8));
        if (!matcher.find()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "Certificate file provided is not a valid PEM file."));
        }
        String base64 = matcher.group()
            .replace("-----BEGIN PRIVATE KEY-----", "")
            .replace("-----END PRIVATE KEY-----", "")
            .replace("\n", "")
            .replace("\r", "");
        byte[] key = Base64Util.decode(base64.getBytes(StandardCharsets.UTF_8));
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(key);
        try {
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return kf.generatePrivate(spec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw LOGGER.logExceptionAsError(new IllegalStateException(e));
        }
    }

    /**
     * Extracts the X509Certificate certificate/certificate-chain from a PEM certificate.
     * @param pem the contents of a PEM certificate.
     * @return the {@link List} of X509Certificate certificate
     */
    static List<X509Certificate> publicKeyFromPem(byte[] pem) {
        Pattern pattern = Pattern.compile("(?s)-----BEGIN CERTIFICATE-----.*?-----END CERTIFICATE-----");
        Matcher matcher = pattern.matcher(new String(pem, StandardCharsets.UTF_8));

        List<X509Certificate> x509CertificateList = new ArrayList<>();
        while (matcher.find()) {
            try {
                CertificateFactory factory = CertificateFactory.getInstance("X.509");
                InputStream stream = new ByteArrayInputStream(matcher.group().getBytes(StandardCharsets.UTF_8));
                x509CertificateList.add((X509Certificate) factory.generateCertificate(stream));
            } catch (CertificateException e) {
                throw LOGGER.logExceptionAsError(new IllegalStateException(e));
            }
        }

        if (x509CertificateList.size() == 0) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException(
                "PEM certificate provided does not contain -----BEGIN CERTIFICATE-----END CERTIFICATE----- block"));
        }

        return x509CertificateList;
    }
}

package com.azure.security.attestation;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.policy.BearerTokenAuthenticationPolicy;
import com.azure.core.http.policy.HttpPipelinePolicy;
import com.azure.core.test.TestBase;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.identity.EnvironmentCredentialBuilder;
import com.azure.security.attestation.models.AttestationType;
import com.azure.security.attestation.models.JsonWebKey;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.params.provider.Arguments;
import reactor.core.publisher.Mono;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class AttestationClientTestBase extends TestBase {

    private static final String dataPlaneScope ="https://attest.azure.net/.default";

    enum ClientTypes {
        Shared,
        Isolated,
        Aad,
    }

    ClientTypes classifyClient(@NotNull String clientUri)
    {
        assertNotNull(clientUri);
        String regionShortName = Configuration.getGlobalConfiguration().get("locationShortName");
        String sharedUri = "https://shared" + regionShortName + "." + regionShortName + ".test.attest.azure.net";
        if (sharedUri.equals(clientUri)) {
            return ClientTypes.Shared;
        } else if (Configuration.getGlobalConfiguration().get("ATTESTATION_ISOLATED_URL").equals(clientUri)) {
            return ClientTypes.Isolated;
        } else if (Configuration.getGlobalConfiguration().get("ATTESTATION_AAD_URL").equals(clientUri)) {
            return ClientTypes.Aad;
        }
        throw new IllegalArgumentException();
    }

    InputStream base64ToStream(String base64)
    {
        byte[] decoded = Base64.getDecoder().decode(base64);
        return new ByteArrayInputStream(decoded);
    }

    AttestationClientBuilder getBuilder(HttpClient httpClient, String clientUri) {
        return new AttestationClientBuilder().pipeline(getHttpPipeline(httpClient)).instanceUrl(clientUri);
    }

    HttpPipeline getHttpPipeline(HttpClient httpClient) {
        TokenCredential credential = null;

        if (!interceptorManager.isPlaybackMode()) {
            credential = new EnvironmentCredentialBuilder().httpClient(httpClient).build();
        }

        final List<HttpPipelinePolicy> policies = new ArrayList<>();
        if (credential != null) {
            policies.add(new BearerTokenAuthenticationPolicy(credential, dataPlaneScope));
        }

        if (getTestMode() == TestMode.RECORD) {
            policies.add(interceptorManager.getRecordPolicy());
        }

        return new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
            .build();
    }

    Mono<JWTClaimsSet> verifyAttestationToken(HttpClient httpClient, String clientUri, String attestationToken) throws ParseException {
        SignedJWT token = SignedJWT.parse(attestationToken);

        return getSigningCertificateByKeyId(token, httpClient, clientUri)
            .flatMap(cert -> {
                PublicKey key = cert.getPublicKey();
                RSAPublicKey rsaKey = (RSAPublicKey) key;

                RSASSAVerifier verifier = new RSASSAVerifier(rsaKey);
                try {
                    assertTrue(token.verify(verifier));
                } catch (JOSEException e) {
                    e.printStackTrace();
                }


                JWTClaimsSet claims = null;
                try {
                    claims = token.getJWTClaimsSet();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                assertNotNull(claims);
                return Mono.just(claims);
            });
    }

    /**
     * Create a JWS Signer from the specified PKCS8 encoded signing key.
     * @param signingKeyBase64 Base64 encoded PKCS8 encoded RSA Private key.
     * @return JWSSigner created over the specified signing key.
     * @throws NoSuchAlgorithmException - should never  throws this.
     * @throws InvalidKeySpecException - Can throw this if the key is invalid.
     */
    @NotNull
    JWSSigner getJwsSigner(String signingKeyBase64) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] signingKey = Base64.getDecoder().decode(signingKeyBase64);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(signingKey);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        return new RSASSASigner(privateKey);
    }

    /**
     * Find the signing certificate associated with the specified SignedJWT token.
     *
     * This method depends on the token
     * @param token - MAA generated token on which to find the certificate.
     * @param client - Http Client used to retrieve signing certificates.
     * @param clientUri - Base URI for the attestation client.
     * @return X509Certificate which will have been used to sign the token.
     */
    Mono<X509Certificate> getSigningCertificateByKeyId(SignedJWT token, HttpClient client, String clientUri) {
        AttestationClientBuilder builder = getBuilder(client, clientUri);
        return builder.buildSigningCertificatesAsyncClient().get()
            .map(keySet -> {
                CertificateFactory cf = null;
                try {
                    cf = CertificateFactory.getInstance("X.509");
                } catch (CertificateException e) {
                    e.printStackTrace();
                }

                String keyId = token.getHeader().getKeyID();

                for (JsonWebKey key : keySet.getKeys()) {
                    if (keyId.equals(key.getKid())) {
                        Certificate cert = null;
                        try {
                            assert cf != null;
                            cert = cf.generateCertificate(base64ToStream(key.getX5C().get(0)));
                        } catch (CertificateException e) {
                            e.printStackTrace();
                        }

                        assertTrue(cert instanceof X509Certificate);

                        return (X509Certificate) cert;

                    }
                }
                fail();
                throw new RuntimeException(String.format("Key %s not found in JSON Web Key Set", keyId));
            });
    }

    static Stream<Arguments> getAttestationClients() {
        // when this issues is closed, the newer version of junit will have better support for
        // cartesian product of arguments - https://github.com/junit-team/junit5/issues/1427
        List<Arguments> argumentsList = new ArrayList<>();
        String regionShortName = Configuration.getGlobalConfiguration().get("locationShortName");
        getHttpClients().forEach(httpClient -> Stream.of(
            "https://shared" + regionShortName + "." + regionShortName + ".test.attest.azure.net",
            Configuration.getGlobalConfiguration().get("ATTESTATION_ISOLATED_URL"),
            Configuration.getGlobalConfiguration().get("ATTESTATION_AAD_URL"))
            .forEach(clientUri -> argumentsList.add(Arguments.of(httpClient, clientUri))));
        return argumentsList.stream();
    }

    static Stream<Arguments> getPolicyClients() {
        List<Arguments> argumentsList = new ArrayList<>();
        getAttestationClients().forEach(clientParams -> Arrays.asList(
            AttestationType.OPEN_ENCLAVE,
            AttestationType.TPM,
            AttestationType.SGX_ENCLAVE)
            .forEach(attestationType -> argumentsList.add(Arguments.of(clientParams.get()[0], clientParams.get()[1], attestationType))));
        return argumentsList.stream();
    }


}

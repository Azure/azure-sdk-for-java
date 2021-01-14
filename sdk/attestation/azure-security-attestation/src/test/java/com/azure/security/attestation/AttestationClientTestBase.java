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
import com.azure.security.attestation.models.JsonWebKeySet;
import com.azure.security.attestation.models.PolicyResponse;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JOSEObject;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import net.minidev.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.provider.Arguments;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.PublicKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.*;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class AttestationClientTestBase extends TestBase {

    private static String dataPlaneScope ="https://attest.azure.net/.default";

    enum ClientTypes {
        Shared,
        Isolated,
        Aad,
    }

    ClientTypes classifyClient(String clientUri)
    {
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

        HttpPipeline pipeline = new HttpPipelineBuilder()
            .policies(policies.toArray(new HttpPipelinePolicy[0]))
            .httpClient(httpClient == null ? interceptorManager.getPlaybackClient() : httpClient)
            .build();

        return pipeline;
    }

    /**
     * Find the signing certificate associated with the specified SignedJWT token.
     *
     * This method depends on the token
     * @param token - MAA generated token on which to find the certificate.
     * @param client - Http Client used to retrieve signing certificates.
     * @param clientUri - Base URI for the attestation client.
     * @return X509Certificate which will have been used to sign the token.
     * @throws CertificateException if the certificate is invalid.
     * @throws RuntimeException if the "keyId" field in the token is not in the signing certificates.
     */
    X509Certificate getSigningCertificateByKeyId(SignedJWT token, HttpClient client, String clientUri)
        throws CertificateException {
        AttestationClientBuilder builder = getBuilder(client, clientUri);
        JsonWebKeySet keySet = builder.buildSigningCertificatesClient().get();
        CertificateFactory cf = CertificateFactory.getInstance("X.509");

        String keyId = token.getHeader().getKeyID();

        for (JsonWebKey key : keySet.getKeys()) {
            if (keyId.equals(key.getKid())) {
                Certificate cert = cf.generateCertificate(base64ToStream(key.getX5C().get(0)));

                assertTrue(cert instanceof X509Certificate);

                X509Certificate x5c = (X509Certificate) cert;

                return x5c;

            }
        }
        assertTrue(false);
        throw new RuntimeException(String.format("Key {0} not found in JSON Web Key Set", keyId));
    }

    /**
     * Verifies the response to the GetMetadataConfiguration (/.well-known/open-id-metadata) API.
     * @param clientUri - URI associated with the operation.
     * @param metadataConfigResponse - Object representing the metadata configuration.
     */
    void verifyMetadataConfigurationResponse(String clientUri, Object metadataConfigResponse) {
        assertTrue(metadataConfigResponse instanceof LinkedHashMap);

        @SuppressWarnings("unchecked")
        LinkedHashMap<String, Object> metadataConfig = (LinkedHashMap<String, Object>) metadataConfigResponse;

        assertEquals(clientUri, metadataConfig.get("issuer"));
        assertEquals(clientUri +"/certs", metadataConfig.get("jwks_uri"));
    }

    /**
     * Verifies the response to the GetSigningCertificates (/certs) API.
     *
     * Each certificate returned needs to be a valid X.509 certificate.
     * We also verify that self signed certificates are signed with the known trusted roots.
     * @param clientUri
     * @param certs
     * @throws CertificateException
     */
    void verifySigningCertificatesResponse(String clientUri, JsonWebKeySet certs) throws CertificateException {
        Assertions.assertTrue(certs.getKeys().size() > 1);

        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        certs.getKeys().forEach(key -> {
            assertNotNull(key.getKid());
            assertNotNull(key.getX5C());
            Assertions.assertNotEquals(0, key.getX5C().size());
            key.getX5C().forEach (base64cert -> {
                try {
                    Certificate cert = cf.generateCertificate(base64ToStream(base64cert));

                    Assertions.assertTrue(cert instanceof X509Certificate);

                    X509Certificate x5c = (X509Certificate) cert;

                    Set<String> nonCriticalExtensions = x5c.getNonCriticalExtensionOIDs();
                    Set<String> criticalExtensions = x5c.getCriticalExtensionOIDs();

//                    if (x5c.getExtensionValue("1.2.840.113556.10.1.1") != null) {
                    // If the certificate is self signed, it should be associated
                    // with either the Microsoft root CA, the VBS self signed root, or the instance.
                    if (x5c.getIssuerDN().equals(x5c.getSubjectDN())) {
                        if (x5c.getIssuerDN().toString().contains("Microsoft Root Certificate Authority")) {
                            assertEquals("CN=Microsoft Root Certificate Authority 2011, O=Microsoft Corporation, L=Redmond, ST=Washington, C=US", x5c.getIssuerDN().getName());
                        }
                        else if (x5c.getIssuerDN().toString().contains("AttestationService-LocalTest-ReportSigning")) {
                            assertEquals("CN=AttestationService-LocalTest-ReportSigning", x5c.getIssuerDN().getName());
                        }
                        else {
                            assertEquals("CN=" + clientUri, x5c.getSubjectDN().getName());
                        }
                    }
                } catch (CertificateException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            });
        });
    }

    /**
     * Verifies the basic response for a Get Attestation Policy API - this simply verifies that the server
     * returns a valid JWT and that the JWT contains a base64url encoded attestation policy.
     * @param client
     * @param clientUri
     * @param attestationType
     * @param policyResponse
     * @throws ParseException
     * @throws CertificateException
     * @throws JOSEException
     * @throws UnsupportedEncodingException
     */
    void verifyBasicGetAttestationPolicyResponse(HttpClient client, String clientUri, AttestationType attestationType, PolicyResponse policyResponse) throws ParseException, CertificateException, JOSEException, UnsupportedEncodingException {
        assertNotNull(policyResponse);
        assertNotNull(policyResponse.getToken());

        SignedJWT token = SignedJWT.parse(policyResponse.getToken());

        X509Certificate cert = getSigningCertificateByKeyId(token, client, clientUri);
        PublicKey key = cert.getPublicKey();
        RSAPublicKey rsaKey = (RSAPublicKey)key;

        RSASSAVerifier verifier = new RSASSAVerifier(rsaKey);
        assertTrue(token.verify(verifier));
        JWTClaimsSet claims = token.getJWTClaimsSet();
        String policyDocument = claims.getClaims().get("x-ms-policy").toString();

        JOSEObject policyJose = JOSEObject.parse(policyDocument);
        JSONObject jsonObject = policyJose.getPayload().toJSONObject();
        if (jsonObject != null) {
            assertTrue(jsonObject.containsKey("AttestationPolicy"));
            String base64urlPolicy = jsonObject.getAsString("AttestationPolicy");

            byte[] attestationPolicyUtf8 = Base64.getUrlDecoder().decode(base64urlPolicy);
            String attestationPolicy = new String(attestationPolicyUtf8, "UTF-8");

            assertNotNull(attestationPolicy);
        }
        else
        {
            // TPM is allowed to have an empty attestation policy, all the other AttestationTypes have policies.
            assertEquals("Tpm", attestationType.toString());
        }
    }


    static Stream<Arguments> getAttestationClients() {
        // when this issues is closed, the newer version of junit will have better support for
        // cartesian product of arguments - https://github.com/junit-team/junit5/issues/1427
        List<Arguments> argumentsList = new ArrayList<>();
        String regionShortName = Configuration.getGlobalConfiguration().get("locationShortName");
        getHttpClients().forEach(httpClient -> Arrays.asList(
            "https://shared" + regionShortName + "." + regionShortName + ".test.attest.azure.net",
            Configuration.getGlobalConfiguration().get("ATTESTATION_ISOLATED_URL"),
            Configuration.getGlobalConfiguration().get("ATTESTATION_AAD_URL")).stream()
            .forEach(clientUri -> argumentsList.add(Arguments.of(httpClient, clientUri))));
        return argumentsList.stream();
    }

    static Stream<Arguments> getPolicyClients() {
        List<Arguments> argumentsList = new ArrayList<>();
        getAttestationClients().forEach(clientParams -> Arrays.asList(
            AttestationType.OPEN_ENCLAVE,
            AttestationType.TPM,
            AttestationType.SGX_ENCLAVE).stream()
            .forEach(attestationType -> argumentsList.add(Arguments.of(clientParams.get()[0], clientParams.get()[1], attestationType))));
        return argumentsList.stream();
    }


}

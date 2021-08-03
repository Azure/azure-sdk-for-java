package com.azure.security.attestation;

import com.azure.core.http.HttpClient;
import com.azure.core.test.TestMode;
import com.azure.security.attestation.models.AttestSgxEnclaveRequest;
import com.azure.security.attestation.models.AttestationResponse;
import com.azure.security.attestation.models.AttestationType;
import com.azure.security.attestation.models.JsonWebKeySet;
import com.azure.security.attestation.models.PolicyResponse;
import com.nimbusds.jose.JOSEObject;
import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.jupiter.api.Assertions;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Base64;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReadmeSamples extends AttestationClientTestBase {


    void testAttestSgxEnclave(HttpClient httpClient, String clientUri) {

        AttestationClientBuilder attestationBuilder = getBuilder(httpClient, clientUri);

        AttestationClient client = attestationBuilder.buildAttestationClient();

        byte[] decodedRuntimeData = new byte[1];
        byte[] decodedSgxQuote = new byte[1];

        AttestSgxEnclaveRequest request = new AttestSgxEnclaveRequest();
        request.setQuote(decodedSgxQuote);
        RuntimeData runtimeData = new RuntimeData();
        runtimeData.setDataType(DataType.BINARY);
        runtimeData.setData(decodedRuntimeData);
        request.setRuntimeData(runtimeData);
        AttestationResponse response = client.attestSgxEnclave(request);

        JWTClaimsSet claims = null;
        claims = verifyAttestationToken(httpClient, clientUri, response.getToken()).block();

        assertNotNull(claims);
        assertTrue(claims.getClaims().containsKey("iss"));

        // In playback mode, the client URI is bogus and thus cannot be relied on for test purposes.
        if (testContextManager.getTestMode() != TestMode.PLAYBACK) {
            Assertions.assertEquals(clientUri, claims.getClaims().get("iss"));
        }

    }

    void attestationPolicyGet(HttpClient httpClient, String clientUri) {
        PolicyClient client = getBuilder(httpClient, clientUri).buildPolicyClient();

        PolicyResponse policyResponse = client.get(AttestationType.SGX_ENCLAVE);
        verifyAttestationToken(httpClient, clientUri, policyResponse.getToken())
            .subscribe(claims -> {
                if (claims != null) {

                    String policyDocument = claims.getClaims().get("x-ms-policy").toString();

                    JOSEObject policyJose = null;
                    try {
                        policyJose = JOSEObject.parse(policyDocument);
                    } catch (ParseException e) {
                        throw logger.logExceptionAsError(new RuntimeException(e.toString()));
                    }
                    assert policyJose != null;
                    Map<String, Object> jsonObject = policyJose.getPayload().toJSONObject();
                    if (jsonObject != null) {
                        assertTrue(jsonObject.containsKey("AttestationPolicy"));
                        String base64urlPolicy = jsonObject.get("AttestationPolicy").toString();

                        byte[] attestationPolicyUtf8 = Base64.getUrlDecoder().decode(base64urlPolicy);
                        String attestationPolicy;
                        attestationPolicy = new String(attestationPolicyUtf8, StandardCharsets.UTF_8);
                        // Inspect the retrieved policy.
                    }
                }
            });
    }

    void signingCertificatesGet(HttpClient httpClient, String clientUri) {

        AttestationClientBuilder attestationBuilder = getBuilder(httpClient, clientUri);

        JsonWebKeySet certs = attestationBuilder.buildSigningCertificatesClient().get();
    }

}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.attestation;

import com.azure.core.http.HttpClient;
import com.azure.core.test.TestMode;
import com.azure.security.attestation.models.AttestationOptions;
import com.azure.security.attestation.models.AttestationResult;
import com.azure.security.attestation.models.AttestationSigner;
import com.azure.security.attestation.models.AttestationType;
import com.azure.security.attestation.models.PolicyResponse;
import com.nimbusds.jose.JOSEObject;

import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.util.Base64;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReadmeSamples extends AttestationClientTestBase {

    void testAttestSgxEnclave(HttpClient httpClient, String clientUri) {

        AttestationClientBuilder attestationBuilder = getBuilder(httpClient, clientUri);

        AttestationClient client = attestationBuilder.buildAttestationClient();

        byte[] decodedRuntimeData = new byte[1];
        byte[] decodedSgxQuote = new byte[1];

        AttestationOptions options = AttestationOptions
            .fromEvidence(decodedSgxQuote)
                .setRunTimeData(decodedRuntimeData)
                    .interpretRunTimeDataAsBinary();
        AttestationResult result = client.attestSgxEnclave(options);

        assertNotNull(result.getIssuer());

        // In playback mode, the client URI is bogus and thus cannot be relied on for test purposes.
        if (testContextManager.getTestMode() != TestMode.PLAYBACK) {
            assertEquals(clientUri, result.getIssuer());
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

        AttestationSigner[] certs = attestationBuilder.buildAttestationClient().getAttestationSigners();
    }

}

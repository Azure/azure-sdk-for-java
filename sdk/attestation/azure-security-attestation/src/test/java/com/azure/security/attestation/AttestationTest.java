// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.attestation;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
import com.azure.core.test.annotation.LiveOnly;
import com.azure.core.test.utils.TestUtils;
import com.azure.core.util.BinaryData;
import com.azure.core.util.Context;
import com.azure.core.util.logging.LogLevel;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.security.attestation.models.AttestationData;
import com.azure.security.attestation.models.AttestationDataInterpretation;
import com.azure.security.attestation.models.AttestationOptions;
import com.azure.security.attestation.models.AttestationPolicySetOptions;
import com.azure.security.attestation.models.AttestationResponse;
import com.azure.security.attestation.models.AttestationResult;
import com.azure.security.attestation.models.AttestationSigningKey;
import com.azure.security.attestation.models.AttestationTokenValidationOptions;
import com.azure.security.attestation.models.AttestationType;
import com.azure.security.attestation.models.PolicyModification;
import com.azure.security.attestation.models.PolicyResult;
import com.azure.security.attestation.models.TpmAttestationResult;
import io.opentelemetry.api.trace.Span;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.util.Arrays;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.azure.core.util.tracing.Tracer.PARENT_TRACE_CONTEXT_KEY;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@LiveOnly
public class AttestationTest extends AttestationClientTestBase {
    // LiveOnly because "JWT cannot be stored in recordings."
    private static final String RUNTIME_DATA = "CiAgICAgICAgewogICAgICAgICAgICAiandrIiA6IHsKICAgICAgICAgICAgICAgICJrdHk"
        + "iOiJFQyIsCiAgICAgICAgICAgICAgICAidXNlIjoic2lnIiwKICAgICAgICAgICAgICAgICJjcnYiOiJQLTI1NiIsCiAgICAgICAgICAgICA"
        + "gICAieCI6IjE4d0hMZUlnVzl3Vk42VkQxVHhncHF5MkxzellrTWY2SjhualZBaWJ2aE0iLAogICAgICAgICAgICAgICAgInkiOiJjVjRkUzR"
        + "VYUxNZ1BfNGZZNGo4aXI3Y2wxVFhsRmRBZ2N4NTVvN1RrY1NBIgogICAgICAgICAgICB9CiAgICAgICAgfQogICAgICAgIA";


    private static final String OPEN_ENCLAVE_REPORT = "AQAAAAIAAADkEQAAAAAAAAMAAgAAAAAABQAKAJOacjP3nEyplAoNs5V_Bgc42MPz"
        + "Go7hPWS_h-3tExJrAAAAABERAwX_gAYAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAUAAAAAAAAABwAAAAAAAAC3"
        + "eSAmGL7LY2do5dkC8o1SQiJzX6-1OeqboHw_wXGhwgAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAALBpElSroIHE1xsKbdbjAKTcu"
        + "6UtnfhXCC9QjQPENQaoAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
        + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABAAEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
        + "AAAAAAAAAAAAAAAA7RGp65ffwXBToyppkucdBPfsmW5FUZq3EJNq-0j5BB0AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADAQAAB4"
        + "iv_XjOJsrFMrPvIYOBCeMR2q6xB08KluTNAtIgpZQUIzLNyy78Gmb5LE77UIVye2sao77dOGiz3wP2f5jhEE5iovgPhy6-Qg8JQkqe8XJI6B"
        + "5ZlWsfq3E7u9EvH7ZZ33MihT7aM-sXca4u92L8OIhpM2cfJguOSAS3Q4pR4NdRERAwX_gAYAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
        + "AAAAAAAAAAAAAAAAABUAAAAAAAAABwAAAAAAAAA_sKzghp0uMPKOhtcMdmQDpU-7zWWO7ODhuUipFVkXQAAAAAAAAAAAAAAAAAAAAAAAAAAA"
        + "AAAAAAAAAAAAAAAAjE9XddeWUD6WE393xoqCmgBWrI3tcBQLCBsJRJDFe_8AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
        + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAABAAUAAAAAAAAAAAAAAAAAAAAAAAAA"
        + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD9rOmAu-jSSf1BAj_cC0mu7YCnx4QosD78yj3sQX81IAAAAAAAAA"
        + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAH5Au8JZ_dpXiLYaE1TtyGjGz0dtFZa7eGooRGTQzoJJuR8Xj-zUvyCKE4ABy0pajfE8lOGSUHuJ"
        + "oifisJNAhg4gAAABAgMEBQYHCAkKCwwNDg8QERITFBUWFxgZGhscHR4fBQDIDQAALS0tLS1CRUdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUVm"
        + "ekNDQkNhZ0F3SUJBZ0lVRk5xSnZZZTU4ZXlpUjI2Yzd0L2lxU0pNYnFNd0NnWUlLb1pJemowRUF3SXcKY1RFak1DRUdBMVVFQXd3YVNXNTBa"
        + "V3dnVTBkWUlGQkRTeUJRY205alpYTnpiM0lnUTBFeEdqQVlCZ05WQkFvTQpFVWx1ZEdWc0lFTnZjbkJ2Y21GMGFXOXVNUlF3RWdZRFZRUUhE"
        + "QXRUWVc1MFlTQkRiR0Z5WVRFTE1Ba0dBMVVFCkNBd0NRMEV4Q3pBSkJnTlZCQVlUQWxWVE1CNFhEVEl4TURReU1USXdOVGt6T0ZvWERUSTRN"
        + "RFF5TVRJd05Ua3oKT0Zvd2NERWlNQ0FHQTFVRUF3d1pTVzUwWld3Z1UwZFlJRkJEU3lCRFpYSjBhV1pwWTJGMFpURWFNQmdHQTFVRQpDZ3dS"
        + "U1c1MFpXd2dRMjl5Y0c5eVlYUnBiMjR4RkRBU0JnTlZCQWNNQzFOaGJuUmhJRU5zWVhKaE1Rc3dDUVlEClZRUUlEQUpEUVRFTE1Ba0dBMVVF"
        + "QmhNQ1ZWTXdXVEFUQmdjcWhrak9QUUlCQmdncWhrak9QUU1CQndOQ0FBUTgKU2V1NWV4WCtvMGNkclhkeEtHMGEvQXRzdnVlNVNoUFpmOHgw"
        + "a2czc0xSM2E5TzVHWWYwcW1XSkptL0c4bzZyVgpvbVI2Nmh3cFJXNlpqSm9ocXdvT280SUNtekNDQXBjd0h3WURWUjBqQkJnd0ZvQVUwT2lx"
        + "Mm5YWCtTNUpGNWc4CmV4UmwwTlh5V1Uwd1h3WURWUjBmQkZnd1ZqQlVvRktnVUlaT2FIUjBjSE02THk5aGNHa3VkSEoxYzNSbFpITmwKY25a"
        + "cFkyVnpMbWx1ZEdWc0xtTnZiUzl6WjNndlkyVnlkR2xtYVdOaGRHbHZiaTkyTWk5d1kydGpjbXcvWTJFOQpjSEp2WTJWemMyOXlNQjBHQTFV"
        + "ZERnUVdCQlFzbnhWelhVWnhwRkd5YUtXdzhWZmdOZXBjcHpBT0JnTlZIUThCCkFmOEVCQU1DQnNBd0RBWURWUjBUQVFIL0JBSXdBRENDQWRR"
        + "R0NTcUdTSWI0VFFFTkFRU0NBY1V3Z2dIQk1CNEcKQ2lxR1NJYjRUUUVOQVFFRUVEeEI4dUNBTVU0bmw1ZlBFaktxdG8wd2dnRmtCZ29xaGtp"
        + "RytFMEJEUUVDTUlJQgpWREFRQmdzcWhraUcrRTBCRFFFQ0FRSUJFVEFRQmdzcWhraUcrRTBCRFFFQ0FnSUJFVEFRQmdzcWhraUcrRTBCCkRR"
        + "RUNBd0lCQWpBUUJnc3Foa2lHK0UwQkRRRUNCQUlCQkRBUUJnc3Foa2lHK0UwQkRRRUNCUUlCQVRBUkJnc3EKaGtpRytFMEJEUUVDQmdJQ0FJ"
        + "QXdFQVlMS29aSWh2aE5BUTBCQWdjQ0FRWXdFQVlMS29aSWh2aE5BUTBCQWdnQwpBUUF3RUFZTEtvWklodmhOQVEwQkFna0NBUUF3RUFZTEtv"
        + "WklodmhOQVEwQkFnb0NBUUF3RUFZTEtvWklodmhOCkFRMEJBZ3NDQVFBd0VBWUxLb1pJaHZoTkFRMEJBZ3dDQVFBd0VBWUxLb1pJaHZoTkFR"
        + "MEJBZzBDQVFBd0VBWUwKS29aSWh2aE5BUTBCQWc0Q0FRQXdFQVlMS29aSWh2aE5BUTBCQWc4Q0FRQXdFQVlMS29aSWh2aE5BUTBCQWhBQwpB"
        + "UUF3RUFZTEtvWklodmhOQVEwQkFoRUNBUW93SHdZTEtvWklodmhOQVEwQkFoSUVFQkVSQWdRQmdBWUFBQUFBCkFBQUFBQUF3RUFZS0tvWklo"
        + "dmhOQVEwQkF3UUNBQUF3RkFZS0tvWklodmhOQVEwQkJBUUdBSkJ1MVFBQU1BOEcKQ2lxR1NJYjRUUUVOQVFVS0FRQXdDZ1lJS29aSXpqMEVB"
        + "d0lEUndBd1JBSWdjREZEZHl1UFRHRVRORm5BU0QzOApDWTNSNmlBREpEVHZBbHZTWDNIekk4a0NJRDZsVm1DWklYUHk4ekpKMWgvMnJ1NjJs"
        + "dlVVWDJJaU1ibVFOUEEwClBzMC8KLS0tLS1FTkQgQ0VSVElGSUNBVEUtLS0tLQotLS0tLUJFR0lOIENFUlRJRklDQVRFLS0tLS0KTUlJQ2x6"
        + "Q0NBajZnQXdJQkFnSVZBTkRvcXRwMTEva3VTUmVZUEhzVVpkRFY4bGxOTUFvR0NDcUdTTTQ5QkFNQwpNR2d4R2pBWUJnTlZCQU1NRVVsdWRH"
        + "VnNJRk5IV0NCU2IyOTBJRU5CTVJvd0dBWURWUVFLREJGSmJuUmxiQ0JECmIzSndiM0poZEdsdmJqRVVNQklHQTFVRUJ3d0xVMkZ1ZEdFZ1Ey"
        + "eGhjbUV4Q3pBSkJnTlZCQWdNQWtOQk1Rc3cKQ1FZRFZRUUdFd0pWVXpBZUZ3MHhPREExTWpFeE1EUTFNRGhhRncwek16QTFNakV4TURRMU1E"
        + "aGFNSEV4SXpBaApCZ05WQkFNTUdrbHVkR1ZzSUZOSFdDQlFRMHNnVUhKdlkyVnpjMjl5SUVOQk1Sb3dHQVlEVlFRS0RCRkpiblJsCmJDQkRi"
        + "M0p3YjNKaGRHbHZiakVVTUJJR0ExVUVCd3dMVTJGdWRHRWdRMnhoY21FeEN6QUpCZ05WQkFnTUFrTkIKTVFzd0NRWURWUVFHRXdKVlV6QlpN"
        + "Qk1HQnlxR1NNNDlBZ0VHQ0NxR1NNNDlBd0VIQTBJQUJMOXErTk1wMklPZwp0ZGwxYmsvdVdaNStUR1FtOGFDaTh6NzhmcytmS0NRM2QrdUR6"
        + "WG5WVEFUMlpoRENpZnlJdUp3dk4zd05CcDlpCkhCU1NNSk1KckJPamdic3dnYmd3SHdZRFZSMGpCQmd3Rm9BVUltVU0xbHFkTkluemc3U1ZV"
        + "cjlRR3prbkJxd3cKVWdZRFZSMGZCRXN3U1RCSG9FV2dRNFpCYUhSMGNITTZMeTlqWlhKMGFXWnBZMkYwWlhNdWRISjFjM1JsWkhObApjblpw"
        + "WTJWekxtbHVkR1ZzTG1OdmJTOUpiblJsYkZOSFdGSnZiM1JEUVM1amNtd3dIUVlEVlIwT0JCWUVGTkRvCnF0cDExL2t1U1JlWVBIc1VaZERW"
        + "OGxsTk1BNEdBMVVkRHdFQi93UUVBd0lCQmpBU0JnTlZIUk1CQWY4RUNEQUcKQVFIL0FnRUFNQW9HQ0NxR1NNNDlCQU1DQTBjQU1FUUNJQy85"
        + "ais4NFQrSHp0Vk8vc09RQldKYlNkKy8ydWV4Swo0K2FBMGpjRkJMY3BBaUEzZGhNckY1Y0Q1MnQ2RnFNdkFJcGo4WGRHbXkyYmVlbGpMSksr"
        + "cHpwY1JBPT0KLS0tLS1FTkQgQ0VSVElGSUNBVEUtLS0tLQotLS0tLUJFR0lOIENFUlRJRklDQVRFLS0tLS0KTUlJQ2pqQ0NBalNnQXdJQkFn"
        + "SVVJbVVNMWxxZE5JbnpnN1NWVXI5UUd6a25CcXd3Q2dZSUtvWkl6ajBFQXdJdwphREVhTUJnR0ExVUVBd3dSU1c1MFpXd2dVMGRZSUZKdmIz"
        + "UWdRMEV4R2pBWUJnTlZCQW9NRVVsdWRHVnNJRU52CmNuQnZjbUYwYVc5dU1SUXdFZ1lEVlFRSERBdFRZVzUwWVNCRGJHRnlZVEVMTUFrR0Ex"
        + "VUVDQXdDUTBFeEN6QUoKQmdOVkJBWVRBbFZUTUI0WERURTRNRFV5TVRFd05ERXhNVm9YRFRNek1EVXlNVEV3TkRFeE1Gb3dhREVhTUJnRwpB"
        + "MVVFQXd3UlNXNTBaV3dnVTBkWUlGSnZiM1FnUTBFeEdqQVlCZ05WQkFvTUVVbHVkR1ZzSUVOdmNuQnZjbUYwCmFXOXVNUlF3RWdZRFZRUUhE"
        + "QXRUWVc1MFlTQkRiR0Z5WVRFTE1Ba0dBMVVFQ0F3Q1EwRXhDekFKQmdOVkJBWVQKQWxWVE1Ga3dFd1lIS29aSXpqMENBUVlJS29aSXpqMERB"
        + "UWNEUWdBRUM2bkV3TURJWVpPai9pUFdzQ3phRUtpNwoxT2lPU0xSRmhXR2pibkJWSmZWbmtZNHUzSWprRFlZTDBNeE80bXFzeVlqbEJhbFRW"
        + "WXhGUDJzSkJLNXpsS09CCnV6Q0J1REFmQmdOVkhTTUVHREFXZ0JRaVpReldXcDAwaWZPRHRKVlN2MUFiT1NjR3JEQlNCZ05WSFI4RVN6QkoK"
        + "TUVlZ1JhQkRoa0ZvZEhSd2N6b3ZMMk5sY25ScFptbGpZWFJsY3k1MGNuVnpkR1ZrYzJWeWRtbGpaWE11YVc1MApaV3d1WTI5dEwwbHVkR1Zz"
        + "VTBkWVVtOXZkRU5CTG1OeWJEQWRCZ05WSFE0RUZnUVVJbVVNMWxxZE5JbnpnN1NWClVyOVFHemtuQnF3d0RnWURWUjBQQVFIL0JBUURBZ0VH"
        + "TUJJR0ExVWRFd0VCL3dRSU1BWUJBZjhDQVFFd0NnWUkKS29aSXpqMEVBd0lEU0FBd1JRSWdRUXMvMDhyeWNkUGF1Q0ZrOFVQUVhDTUFsc2xv"
        + "QmU3TndhUUdUY2RwYTBFQwpJUUNVdDhTR3Z4S21qcGNNL3owV1A5RHZvOGgyazVkdTFpV0RkQmtBbiswaWlBPT0KLS0tLS1FTkQgQ0VSVElG"
        + "SUNBVEUtLS0tLQoA";

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testAttestSgxEnclave(HttpClient httpClient, String clientUri) {

        AttestationClientBuilder attestationBuilder = getAttestationBuilder(httpClient, clientUri);

        AttestationClient client = attestationBuilder.buildClient();

        BinaryData decodedRuntimeData = BinaryData.fromBytes(Base64.getUrlDecoder().decode(RUNTIME_DATA));
        BinaryData decodedOpenEnclaveReport = BinaryData.fromBytes(Base64.getUrlDecoder().decode(OPEN_ENCLAVE_REPORT));
        BinaryData sgxQuote = BinaryData.fromBytes(Arrays.copyOfRange(decodedOpenEnclaveReport.toBytes(), 0x10,
            decodedOpenEnclaveReport.toBytes().length));

        AttestationOptions request = new AttestationOptions(sgxQuote)
            .setRunTimeData(new AttestationData(decodedRuntimeData, AttestationDataInterpretation.BINARY));
        AttestationResult result = client.attestSgxEnclave(request);

        verifyAttestationResult(getTestMode(), clientUri, result, decodedRuntimeData, false);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testAttestSgxEnclaveNoRuntimeData(HttpClient httpClient, String clientUri) {

        AttestationClientBuilder attestationBuilder = getAttestationBuilder(httpClient, clientUri);

        AttestationClient client = attestationBuilder.buildClient();

        BinaryData decodedRuntimeData = BinaryData.fromBytes(Base64.getUrlDecoder().decode(RUNTIME_DATA));
        BinaryData decodedOpenEnclaveReport = BinaryData.fromBytes(Base64.getUrlDecoder().decode(OPEN_ENCLAVE_REPORT));
        BinaryData sgxQuote = BinaryData.fromBytes(Arrays.copyOfRange(decodedOpenEnclaveReport.toBytes(), 0x10,
            decodedOpenEnclaveReport.toBytes().length));

        AttestationResult result = client.attestSgxEnclave(sgxQuote);
        verifyAttestationResult(getTestMode(), clientUri, result, null, false);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testAttestSgxEnclaveRuntimeJson(HttpClient httpClient, String clientUri) {

        AttestationClientBuilder attestationBuilder = getAttestationBuilder(httpClient, clientUri);

        AttestationClient client = attestationBuilder.buildClient();

        BinaryData decodedRuntimeData = BinaryData.fromBytes(Base64.getUrlDecoder().decode(RUNTIME_DATA));
        BinaryData decodedOpenEnclaveReport = BinaryData.fromBytes(Base64.getUrlDecoder().decode(OPEN_ENCLAVE_REPORT));
        BinaryData sgxQuote = BinaryData.fromBytes(Arrays.copyOfRange(decodedOpenEnclaveReport.toBytes(), 0x10,
            decodedOpenEnclaveReport.toBytes().length));

        AttestationOptions request = new AttestationOptions(sgxQuote)
            .setRunTimeData(new AttestationData(decodedRuntimeData, AttestationDataInterpretation.JSON));

        AttestationResult result = client.attestSgxEnclave(request);
        verifyAttestationResult(getTestMode(), clientUri, result, decodedRuntimeData, true);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testAttestSgxEnclaveDraftPolicy(HttpClient httpClient, String clientUri) {

        AttestationClientBuilder attestationBuilder = getAttestationBuilder(httpClient, clientUri);

        AttestationClient client = attestationBuilder.buildClient();

        BinaryData decodedRuntimeData = BinaryData.fromBytes(Base64.getUrlDecoder().decode(RUNTIME_DATA));
        BinaryData decodedOpenEnclaveReport = BinaryData.fromBytes(Base64.getUrlDecoder().decode(OPEN_ENCLAVE_REPORT));
        BinaryData sgxQuote = BinaryData.fromBytes(Arrays.copyOfRange(decodedOpenEnclaveReport.toBytes(), 0x10,
            decodedOpenEnclaveReport.toBytes().length));

        AttestationOptions request = new AttestationOptions(sgxQuote)
            .setDraftPolicyForAttestation("version=1.0; authorizationrules{=> permit();}; issuancerules{};")
            .setRunTimeData(new AttestationData(decodedRuntimeData, AttestationDataInterpretation.JSON));

        Response<AttestationResult> response = client.attestSgxEnclaveWithResponse(request, Context.NONE);
        AttestationResponse<?> attestResponse = assertInstanceOf(AttestationResponse.class, response);

        // When a draft policy is specified, the token is unsecured.
        assertEquals("none", attestResponse.getToken().getAlgorithm());

        verifyAttestationResult(getTestMode(), clientUri, response.getValue(), decodedRuntimeData, true);
    }


    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testAttestSgxEnclaveAsync(HttpClient httpClient, String clientUri) {

        AttestationClientBuilder attestationBuilder = getAttestationBuilder(httpClient, clientUri);
        AttestationAsyncClient client = attestationBuilder.buildAsyncClient();

        BinaryData decodedRuntimeData = BinaryData.fromBytes(Base64.getUrlDecoder().decode(RUNTIME_DATA));
        BinaryData decodedOpenEnclaveReport = BinaryData.fromBytes(Base64.getUrlDecoder().decode(OPEN_ENCLAVE_REPORT));
        BinaryData sgxQuote = BinaryData.fromBytes(Arrays.copyOfRange(decodedOpenEnclaveReport.toBytes(), 0x10,
            decodedOpenEnclaveReport.toBytes().length));

        final AtomicBoolean callbackCalled = new AtomicBoolean(false);
        AttestationOptions request = new AttestationOptions(sgxQuote)
            .setRunTimeData(new AttestationData(decodedRuntimeData, AttestationDataInterpretation.BINARY))
            .setValidationOptions(new AttestationTokenValidationOptions()
                .setValidationCallback((token, signer) -> {
                    callbackCalled.set(true);
                    // Perform minimal validation of the issued SGX token. The
                    // token validation logic will have checked the issuance_time
                    // and expiration_time, but this shows accessing those fields.
                    //
                    // The validation logic also checks the subject of the certificate to verify
                    // that the issuer of the certificate is the expected instance of the service.
                    LOGGER.info("In validation callback, checking token...");
                    LOGGER.info(String.format("     Token issuer: %s", token.getIssuer()));
                    if (!interceptorManager.isPlaybackMode()) {
                        LOGGER.info(String.format("     Token was issued at: %tc", token.getIssuedAt()));
                        LOGGER.info(String.format("     Token expires at: %tc", token.getExpiresOn()));
                        if (!token.getIssuer().equals(clientUri)) {
                            LOGGER.error(String.format("Token issuer %s does not match expected issuer %s",
                                token.getIssuer(), clientUri));
                            throw new RuntimeException(String.format("Issuer Mismatch: found %s, expected %s",
                                token.getIssuer(), clientUri));
                        }
                        LOGGER.info(String.format("Issuer of signing certificate is: %s",
                            signer.getCertificates().get(0).getIssuerDN().getName()));
                    }
                })
                // Only validate time based properties when not in PLAYBACK mode. PLAYBACK mode has these values
                // hard-coded into the session record.
                .setValidateExpiresOn(getTestMode() != TestMode.PLAYBACK));

        StepVerifier.create(client.attestSgxEnclave(request))
            .assertNext(result -> {
                assertTrue(callbackCalled.get());
                verifyAttestationResult(getTestMode(), clientUri, result, decodedRuntimeData, false);
            })
            .expectComplete()
            .verify();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testAttestSgxEnclaveNoRuntimeDataAsync(HttpClient httpClient, String clientUri) {

        AttestationClientBuilder attestationBuilder = getAttestationBuilder(httpClient, clientUri);

        AttestationAsyncClient client = attestationBuilder.buildAsyncClient();

        BinaryData decodedRuntimeData = BinaryData.fromBytes(Base64.getUrlDecoder().decode(RUNTIME_DATA));
        BinaryData decodedOpenEnclaveReport = BinaryData.fromBytes(Base64.getUrlDecoder().decode(OPEN_ENCLAVE_REPORT));
        BinaryData sgxQuote = BinaryData.fromBytes(Arrays.copyOfRange(decodedOpenEnclaveReport.toBytes(), 0x10,
            decodedOpenEnclaveReport.toBytes().length));

        AttestationOptions request = new AttestationOptions(sgxQuote);

        StepVerifier.create(client.attestSgxEnclave(request))
            .assertNext(result -> verifyAttestationResult(getTestMode(), clientUri, result, null, false))
            .expectComplete()
            .verify();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testAttestSgxEnclaveRuntimeJsonAsync(HttpClient httpClient, String clientUri) {

        AttestationClientBuilder attestationBuilder = getAttestationBuilder(httpClient, clientUri);

        Span span = tracer.spanBuilder("AttestWithDraft").startSpan();
        Context contextWithSpan = new Context(PARENT_TRACE_CONTEXT_KEY, io.opentelemetry.context.Context.current());

        AttestationAsyncClient client = attestationBuilder.buildAsyncClient();

        BinaryData decodedRuntimeData = BinaryData.fromBytes(Base64.getUrlDecoder().decode(RUNTIME_DATA));
        BinaryData decodedOpenEnclaveReport = BinaryData.fromBytes(Base64.getUrlDecoder().decode(OPEN_ENCLAVE_REPORT));
        BinaryData sgxQuote = BinaryData.fromBytes(Arrays.copyOfRange(decodedOpenEnclaveReport.toBytes(), 0x10, decodedOpenEnclaveReport.toBytes().length));

        AttestationOptions options = new AttestationOptions(sgxQuote)
            .setRunTimeData(new AttestationData(decodedRuntimeData, AttestationDataInterpretation.JSON));

        StepVerifier.create(client.attestSgxEnclaveWithResponse(options, contextWithSpan))
            .assertNext(result -> verifyAttestationResult(getTestMode(), clientUri, result.getValue(),
                decodedRuntimeData, true))
            .expectComplete()
            .verify();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testAttestSgxEnclaveDraftPolicyAsync(HttpClient httpClient, String clientUri) {
        AttestationClientBuilder attestationBuilder = getAttestationBuilder(httpClient, clientUri);

        AttestationAsyncClient client = attestationBuilder.buildAsyncClient();

        BinaryData decodedRuntimeData = BinaryData.fromBytes(Base64.getUrlDecoder().decode(RUNTIME_DATA));
        BinaryData decodedOpenEnclaveReport = BinaryData.fromBytes(Base64.getUrlDecoder().decode(OPEN_ENCLAVE_REPORT));
        BinaryData sgxQuote = BinaryData.fromBytes(Arrays.copyOfRange(decodedOpenEnclaveReport.toBytes(), 0x10,
            decodedOpenEnclaveReport.toBytes().length));


        Span span = tracer.spanBuilder("AttestWithDraft").startSpan();
        Context contextWithSpan = new Context(PARENT_TRACE_CONTEXT_KEY, io.opentelemetry.context.Context.current());

        AttestationOptions request = new AttestationOptions(sgxQuote)
            .setDraftPolicyForAttestation("version=1.0; authorizationrules{=> permit();}; issuancerules{};")
            .setRunTimeData(new AttestationData(decodedRuntimeData, AttestationDataInterpretation.JSON));

        try {
            StepVerifier.create(client.attestSgxEnclaveWithResponse(request, contextWithSpan))
                .assertNext(response -> {
                    // Make sure that the request included a traceparent header and that the response contains a
                    // traceresponse header.
                    // Note: The recording infrastructure doesn't record traceparent or traceresponse, so we can
                    // only perform this check on live servers.
                    if (getTestMode() != TestMode.PLAYBACK) {
                        HttpHeaders requestHeaders = response.getRequest().getHeaders();
                        assertNotNull(requestHeaders.getValue("traceparent"));
                        HttpHeaders responseHeaders = response.getHeaders();
                        // NB: As of 1-5-2022, MAA doesn't include the standardized traceresponse header, instead
                        // it includes the response in x-ms-request-id.
                        assertNotNull(responseHeaders.getValue("x-ms-request-id"));
                        assertEquals(requestHeaders.getValue("traceparent"), responseHeaders.getValue("x-ms-request-id"));
                    }
                    verifyAttestationResult(getTestMode(), clientUri, response.getValue(), decodedRuntimeData, true);
                })
                .expectComplete()
                .verify();
        } finally {
            span.end();
        }
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testTpmAttestation(HttpClient httpClient, String clientUri) {
        ClientTypes clientType = classifyClient(clientUri);
        // TPM attestation requires that we have an attestation policy set, and we can't set attestation policy on the
        // shared client, so just exit early.
        assumeTrue(clientType != ClientTypes.SHARED, "This test does not work on shared instances.");

        // Set the TPM attestation policy to a default value.
        AttestationAdministrationClient adminClient = getAttestationAdministrationBuilder(httpClient, clientUri)
            .buildClient();
        PolicyResult result = adminClient.setAttestationPolicy(AttestationType.TPM, new AttestationPolicySetOptions()
            .setAttestationPolicy("version=1.0; authorizationrules{=>permit();};issuancerules{};")
            .setAttestationSigner(new AttestationSigningKey(getIsolatedSigningCertificate(), getIsolatedSigningKey())));

        if (result.getPolicyResolution() != PolicyModification.UPDATED) {
            LOGGER.log(LogLevel.VERBOSE,
                () -> "Unexpected resolution setting TPM policy: " + result.getPolicyResolution());
            return;
        }

        // We cannot perform the entire protocol exchange for TPM attestation, but we CAN perform the
        // first leg of the attestation operation.
        //
        // Note that TPM attestation requires an authenticated attestation builder.
        AttestationClient client = getAuthenticatedAttestationBuilder(httpClient, clientUri)
            .buildClient();

        // The initial payload for TPM attestation is a JSON object with a property named "payload",
        // containing an object with a property named "type" whose value is "aikcert".

        String attestInitialPayload = "{\"payload\": { \"type\": \"aikcert\" } }";
        TpmAttestationResult tpmResponse = client.attestTpm(BinaryData.fromString(attestInitialPayload));

        Object deserializedResponse = assertDoesNotThrow(() -> ADAPTER.deserialize(tpmResponse.getTpmResult().toBytes(),
            Object.class, SerializerEncoding.JSON));
        assertInstanceOf(LinkedHashMap.class, deserializedResponse);
        @SuppressWarnings("unchecked")
        LinkedHashMap<String, Object> initialResponse = (LinkedHashMap<String, Object>) deserializedResponse;
        assertTrue(initialResponse.containsKey("payload"));
        assertInstanceOf(LinkedHashMap.class, initialResponse.get("payload"));
        @SuppressWarnings("unchecked")
        LinkedHashMap<String, Object> payload = (LinkedHashMap<String, Object>) initialResponse.get("payload");
        assertTrue(payload.containsKey("challenge"));
        assertTrue(payload.containsKey("service_context"));

    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testTpmAttestationWithResult(HttpClient httpClient, String clientUri) {
        ClientTypes clientType = classifyClient(clientUri);
        // TPM attestation requires that we have an attestation policy set, and we can't set attestation policy on the
        // shared client, so just exit early.
        assumeTrue(clientType != ClientTypes.SHARED, "This test does not work on shared instances.");

        // Set the TPM attestation policy to a default value.

        AttestationAdministrationClient adminClient = getAttestationAdministrationBuilder(httpClient, clientUri)
            .buildClient();
        PolicyResult result = adminClient.setAttestationPolicy(AttestationType.TPM, new AttestationPolicySetOptions()
            .setAttestationPolicy("version=1.0; authorizationrules{=>permit();};issuancerules{};")
            .setAttestationSigner(new AttestationSigningKey(getIsolatedSigningCertificate(), getIsolatedSigningKey())));

        if (result.getPolicyResolution() != PolicyModification.UPDATED) {
            LOGGER.log(LogLevel.VERBOSE,
                () -> "Unexpected resolution setting TPM policy: " + result.getPolicyResolution());
            return;
        }

        // We cannot perform the entire protocol exchange for TPM attestation, but we CAN perform the
        // first leg of the attestation operation.
        //
        // Note that TPM attestation requires an authenticated attestation builder.
        AttestationClientBuilder attestationBuilder = getAuthenticatedAttestationBuilder(httpClient, clientUri);
        AttestationClient client = attestationBuilder.buildClient();

        // BEGIN: com.azure.security.attestation.AttestationClient.attestTpmWithResponse
        // The initial payload for TPM attestation is a JSON object with a property named "payload",
        // containing an object with a property named "type" whose value is "aikcert".

        String attestInitialPayload = "{\"payload\": { \"type\": \"aikcert\" } }";
        Response<TpmAttestationResult> tpmResponse = client.attestTpmWithResponse(BinaryData.fromString(attestInitialPayload), Context.NONE);
        // END: com.azure.security.attestation.AttestationClient.attestTpmWithResponse

        Object deserializedResponse = assertDoesNotThrow(() -> ADAPTER.deserialize(
            tpmResponse.getValue().getTpmResult().toBytes(), Object.class, SerializerEncoding.JSON));
        assertInstanceOf(LinkedHashMap.class, deserializedResponse);
        @SuppressWarnings("unchecked")
        LinkedHashMap<String, Object> initialResponse = (LinkedHashMap<String, Object>) deserializedResponse;
        assertTrue(initialResponse.containsKey("payload"));
        assertInstanceOf(LinkedHashMap.class, initialResponse.get("payload"));
        @SuppressWarnings("unchecked")
        LinkedHashMap<String, Object> payload = (LinkedHashMap<String, Object>) initialResponse.get("payload");
        assertTrue(payload.containsKey("challenge"));
        assertTrue(payload.containsKey("service_context"));
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testTpmAttestationAsync(HttpClient httpClient, String clientUri) {
        ClientTypes clientType = classifyClient(clientUri);
        // TPM attestation requires that we have an attestation policy set, and we can't set attestation policy on the
        // shared client, so just exit early.
        assumeTrue(clientType != ClientTypes.SHARED, "This test does not work on shared instances.");

        // Set the TPM attestation policy to a default value.
        AttestationAdministrationClient adminClient = getAttestationAdministrationBuilder(httpClient, clientUri)
            .buildClient();
        PolicyResult result = adminClient.setAttestationPolicy(AttestationType.TPM, new AttestationPolicySetOptions()
            .setAttestationPolicy("version=1.0; authorizationrules{=>permit();};issuancerules{};")
            .setAttestationSigner(new AttestationSigningKey(getIsolatedSigningCertificate(), getIsolatedSigningKey())));

        if (result.getPolicyResolution() != PolicyModification.UPDATED) {
            LOGGER.log(LogLevel.VERBOSE,
                () -> "Unexpected resolution setting TPM policy: " + result.getPolicyResolution());
            return;
        }

        // We cannot perform the entire protocol exchange for TPM attestation, but we CAN perform the
        // first leg of the attestation operation.
        //
        // Note that TPM attestation requires an authenticated attestation builder.
        AttestationClientBuilder attestationBuilder = getAuthenticatedAttestationBuilder(httpClient, clientUri);
        AttestationAsyncClient client = attestationBuilder.buildAsyncClient();

        // The initial payload for TPM attestation is a JSON object with a property named "payload",
        // containing an object with a property named "type" whose value is "aikcert".

        String attestInitialPayload = "{\"payload\": { \"type\": \"aikcert\" } }";
        StepVerifier.create(client.attestTpm(BinaryData.fromString(attestInitialPayload)))
            .assertNext(tpmResponse -> {
                Object deserializedResponse = assertDoesNotThrow(() -> ADAPTER.deserialize(
                    tpmResponse.getTpmResult().toBytes(), Object.class, SerializerEncoding.JSON));
                assertInstanceOf(LinkedHashMap.class, deserializedResponse);
                @SuppressWarnings("unchecked")
                LinkedHashMap<String, Object> initialResponse = (LinkedHashMap<String, Object>) deserializedResponse;
                assertTrue(initialResponse.containsKey("payload"));
                assertInstanceOf(LinkedHashMap.class, initialResponse.get("payload"));
                @SuppressWarnings("unchecked")
                LinkedHashMap<String, Object> payload = (LinkedHashMap<String, Object>) initialResponse.get("payload");
                assertTrue(payload.containsKey("challenge"));
                assertTrue(payload.containsKey("service_context"));
            })
            .verifyComplete();
    }

    @Test()
    void testAttestationOptions() {
        BinaryData decodedRuntimeData = BinaryData.fromBytes(Base64.getUrlDecoder().decode(RUNTIME_DATA));
        BinaryData decodedOpenEnclaveReport = BinaryData.fromBytes(Base64.getUrlDecoder().decode(OPEN_ENCLAVE_REPORT));

        AttestationOptions request1 = new AttestationOptions(decodedOpenEnclaveReport);
        AttestationOptions request2 = new AttestationOptions(decodedOpenEnclaveReport)
            .setInitTimeData(new AttestationData(decodedRuntimeData, AttestationDataInterpretation.JSON))
            .setInitTimeData(new AttestationData(decodedOpenEnclaveReport, AttestationDataInterpretation.BINARY))
            .setRunTimeData(new AttestationData(BinaryData.fromBytes(new byte[]{1, 2, 3, 4, 5}),
                AttestationDataInterpretation.BINARY));

        assertArrayEquals(decodedOpenEnclaveReport.toBytes(), request2.getInitTimeData().getData().toBytes());
        assertArrayEquals(new byte[]{1, 2, 3, 4, 5}, request2.getRunTimeData().getData().toBytes());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testAttestOpenEnclave(HttpClient httpClient, String clientUri) {

        AttestationClientBuilder attestationBuilder = getAttestationBuilder(httpClient, clientUri);

        AttestationClient client = attestationBuilder.buildClient();

        BinaryData decodedRuntimeData = BinaryData.fromBytes(Base64.getUrlDecoder().decode(RUNTIME_DATA));
        BinaryData decodedOpenEnclaveReport = BinaryData.fromBytes(Base64.getUrlDecoder().decode(OPEN_ENCLAVE_REPORT));

        AttestationOptions request = new AttestationOptions(decodedOpenEnclaveReport)
            .setRunTimeData(new AttestationData(decodedRuntimeData, AttestationDataInterpretation.BINARY));
        AttestationResult result = client.attestOpenEnclave(request);

        verifyAttestationResult(getTestMode(), clientUri, result, decodedRuntimeData, false);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testAttestOpenEnclaveNoRuntimeData(HttpClient httpClient, String clientUri) {

        AttestationClientBuilder attestationBuilder = getAttestationBuilder(httpClient, clientUri);

        AttestationClient client = attestationBuilder.buildClient();

        BinaryData decodedOpenEnclaveReport = BinaryData.fromBytes(Base64.getUrlDecoder().decode(OPEN_ENCLAVE_REPORT));

        AttestationOptions request = new AttestationOptions(decodedOpenEnclaveReport);

        AttestationResult result = client.attestOpenEnclave(request);
        verifyAttestationResult(getTestMode(), clientUri, result, null, false);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testAttestOpenEnclaveRuntimeJson(HttpClient httpClient, String clientUri) {

        AttestationClientBuilder attestationBuilder = getAttestationBuilder(httpClient, clientUri);

        AttestationClient client = attestationBuilder.buildClient();

        BinaryData decodedRuntimeData = BinaryData.fromBytes(Base64.getUrlDecoder().decode(RUNTIME_DATA));
        BinaryData decodedOpenEnclaveReport = BinaryData.fromBytes(Base64.getUrlDecoder().decode(OPEN_ENCLAVE_REPORT));

        AttestationOptions request = new AttestationOptions(decodedOpenEnclaveReport)
            .setRunTimeData(new AttestationData(decodedRuntimeData, AttestationDataInterpretation.JSON));

        AttestationResult result = client.attestOpenEnclave(request);
        verifyAttestationResult(getTestMode(), clientUri, result, decodedRuntimeData, true);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testAttestOpenEnclaveDraftPolicy(HttpClient httpClient, String clientUri) {

        AttestationClientBuilder attestationBuilder = getAttestationBuilder(httpClient, clientUri);

        AttestationClient client = attestationBuilder.buildClient();

        BinaryData decodedRuntimeData = BinaryData.fromBytes(Base64.getUrlDecoder().decode(RUNTIME_DATA));
        BinaryData decodedOpenEnclaveReport = BinaryData.fromBytes(Base64.getUrlDecoder().decode(OPEN_ENCLAVE_REPORT));

        AttestationOptions request = new AttestationOptions(decodedOpenEnclaveReport)
            .setDraftPolicyForAttestation("version=1.0; authorizationrules{=> permit();}; issuancerules{};")
            .setRunTimeData(new AttestationData(decodedRuntimeData, AttestationDataInterpretation.JSON));

        Response<AttestationResult> response = client.attestOpenEnclaveWithResponse(request, Context.NONE);
        AttestationResponse<?> attestResponse = assertInstanceOf(AttestationResponse.class, response);

        // When a draft policy is specified, the token is unsecured.
        assertEquals("none", attestResponse.getToken().getAlgorithm());

        verifyAttestationResult(getTestMode(), clientUri, response.getValue(), decodedRuntimeData, true);
    }


    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testAttestOpenEnclaveAsync(HttpClient httpClient, String clientUri) {

        AttestationClientBuilder attestationBuilder = getAttestationBuilder(httpClient, clientUri);
        AttestationAsyncClient client = attestationBuilder.buildAsyncClient();

        BinaryData decodedRuntimeData = BinaryData.fromBytes(Base64.getUrlDecoder().decode(RUNTIME_DATA));
        BinaryData decodedOpenEnclaveReport = BinaryData.fromBytes(Base64.getUrlDecoder().decode(OPEN_ENCLAVE_REPORT));

        AttestationOptions options = new AttestationOptions(decodedOpenEnclaveReport)
            .setRunTimeData(new AttestationData(decodedRuntimeData, AttestationDataInterpretation.BINARY));

        StepVerifier.create(client.attestOpenEnclave(options))
            .assertNext(result -> verifyAttestationResult(getTestMode(), clientUri, result, decodedRuntimeData, false))
            .expectComplete()
            .verify();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testAttestOpenEnclaveNoRuntimeDataAsync(HttpClient httpClient, String clientUri) {

        AttestationClientBuilder attestationBuilder = getAttestationBuilder(httpClient, clientUri);

        AttestationAsyncClient client = attestationBuilder.buildAsyncClient();

        BinaryData decodedOpenEnclaveReport = BinaryData.fromBytes(Base64.getUrlDecoder().decode(OPEN_ENCLAVE_REPORT));

        StepVerifier.create(client.attestOpenEnclave(decodedOpenEnclaveReport))
            .assertNext(result -> verifyAttestationResult(getTestMode(), clientUri, result, null, false))
            .expectComplete()
            .verify();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testAttestOpenEnclaveRuntimeJsonAsync(HttpClient httpClient, String clientUri) {

        AttestationClientBuilder attestationBuilder = getAttestationBuilder(httpClient, clientUri);

        AttestationAsyncClient client = attestationBuilder.buildAsyncClient();

        BinaryData decodedRuntimeData = BinaryData.fromBytes(Base64.getUrlDecoder().decode(RUNTIME_DATA));
        BinaryData decodedOpenEnclaveReport = BinaryData.fromBytes(Base64.getUrlDecoder().decode(OPEN_ENCLAVE_REPORT));

        AttestationOptions options = new AttestationOptions(decodedOpenEnclaveReport)
            .setRunTimeData(new AttestationData(decodedRuntimeData, AttestationDataInterpretation.JSON));

        StepVerifier.create(client.attestOpenEnclave(options))
            .assertNext(result -> verifyAttestationResult(getTestMode(), clientUri, result, decodedRuntimeData, true))
            .expectComplete()
            .verify();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testAttestOpenEnclaveDraftPolicyAsync(HttpClient httpClient, String clientUri) {

        AttestationClientBuilder attestationBuilder = getAttestationBuilder(httpClient, clientUri);

        AttestationAsyncClient client = attestationBuilder.buildAsyncClient();

        BinaryData decodedRuntimeData = BinaryData.fromBytes(Base64.getUrlDecoder().decode(RUNTIME_DATA));
        BinaryData decodedOpenEnclaveReport = BinaryData.fromBytes(Base64.getUrlDecoder().decode(OPEN_ENCLAVE_REPORT));

        AttestationOptions options = new AttestationOptions(decodedOpenEnclaveReport)
            .setDraftPolicyForAttestation("version=1.0; authorizationrules{=> permit();}; issuancerules{};")
            .setRunTimeData(new AttestationData(decodedRuntimeData, AttestationDataInterpretation.JSON));

        StepVerifier.create(client.attestOpenEnclaveWithResponse(options))
            .assertNext(response -> verifyAttestationResult(getTestMode(), clientUri, response.getValue(),
                decodedRuntimeData, true))
            .expectComplete()
            .verify();

    }


    private static void verifyAttestationResult(TestMode testMode, String clientUri, AttestationResult result,
        BinaryData runtimeData, boolean expectJson) {
        assertNotNull(result.getIssuer());

        // In playback mode, the client URI is bogus and thus cannot be relied on for test purposes.
        if (testMode != TestMode.PLAYBACK) {
            Assertions.assertEquals(clientUri, result.getIssuer());
        }

        assertNotNull(result.getMrEnclave());
        assertNotNull(result.getMrSigner());
        // assertNotNull(result.getSvn()); svn is an int, cannot be null.
        assertNull(result.getNonce());

        if (expectJson) {
            assertInstanceOf(Map.class, result.getRuntimeClaims());
            @SuppressWarnings("unchecked")
            Map<String, Object> runtimeClaims = (Map<String, Object>) result.getRuntimeClaims();
            Map<String, Object> expectedClaims = assertDoesNotThrow(() ->
                ADAPTER.deserialize(runtimeData.toBytes(), Object.class, SerializerEncoding.JSON));
            assertObjectEqual(expectedClaims, runtimeClaims);
        } else if (runtimeData != null) {
            TestUtils.assertArraysEqual(runtimeData.toBytes(), result.getEnclaveHeldData().toBytes());
        }
    }

    static void assertObjectEqual(Map<String, Object> expected, Map<String, Object> actual) {
        expected.forEach((key, o) -> {
            LOGGER.verbose("Key: " + key);
            assertTrue(actual.containsKey(key));
            if (expected.get(key) instanceof Map) {
                assertInstanceOf(Map.class, actual.get(key));
                @SuppressWarnings("unchecked")
                Map<String, Object> expectedInner = (Map<String, Object>) expected.get(key);
                @SuppressWarnings("unchecked")
                Map<String, Object> actualInner = (Map<String, Object>) actual.get(key);
                assertObjectEqual(expectedInner, actualInner);
            } else {
                assertEquals(o, actual.get(key));
            }
        });

    }
}

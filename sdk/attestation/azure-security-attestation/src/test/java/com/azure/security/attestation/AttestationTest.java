// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.attestation;

import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.test.TestMode;
import com.azure.core.util.Context;
import com.azure.security.attestation.models.AttestOpenEnclaveOptions;
import com.azure.security.attestation.models.AttestSgxEnclaveOptions;
import com.azure.security.attestation.models.AttestationResponse;
import com.azure.security.attestation.models.AttestationResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.test.StepVerifier;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AttestationTest extends AttestationClientTestBase {
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";

    private final String runtimeData =
        "CiAgICAgICAgewogI"
            + "CAgICAgICAgICAiandrIiA6IHsKICAgICAgICAgICAgICAgICJrdHkiOiJFQyIsCiAg"
            + "ICAgICAgICAgICAgICAidXNlIjoic2lnIiwKICAgICAgICAgICAgICAgICJjcnYiOiJ"
            + "QLTI1NiIsCiAgICAgICAgICAgICAgICAieCI6IjE4d0hMZUlnVzl3Vk42VkQxVHhncH"
            + "F5MkxzellrTWY2SjhualZBaWJ2aE0iLAogICAgICAgICAgICAgICAgInkiOiJjVjRkU"
            + "zRVYUxNZ1BfNGZZNGo4aXI3Y2wxVFhsRmRBZ2N4NTVvN1RrY1NBIgogICAgICAgICAg"
            + "ICB9CiAgICAgICAgfQogICAgICAgIA";


    private final String openEnclaveReport =
        "AQAAAAIAAADkEQAAAAAAAAMAAg"
        + "AAAAAABQAKAJOacjP3nEyplAoNs5V_Bgc42MPzGo7hPWS_h-3tExJrAAAAABERAwX_g"
        + "AYAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAUAAAAAAAAA"
        + "BwAAAAAAAAC3eSAmGL7LY2do5dkC8o1SQiJzX6-1OeqboHw_wXGhwgAAAAAAAAAAAAA"
        + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAALBpElSroIHE1xsKbdbjAKTcu6UtnfhXCC9QjQP"
        + "ENQaoAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
        + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAB"
        + "AAEAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
        + "AAAAAAAAAAAAAAAAA7RGp65ffwXBToyppkucdBPfsmW5FUZq3EJNq-0j5BB0AAAAAAA"
        + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAADAQAAB4iv_XjOJsrFMrPvIYOBCeMR2q6"
        + "xB08KluTNAtIgpZQUIzLNyy78Gmb5LE77UIVye2sao77dOGiz3wP2f5jhEE5iovgPhy"
        + "6-Qg8JQkqe8XJI6B5ZlWsfq3E7u9EvH7ZZ33MihT7aM-sXca4u92L8OIhpM2cfJguOS"
        + "AS3Q4pR4NdRERAwX_gAYAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
        + "AAAAAAABUAAAAAAAAABwAAAAAAAAA_sKzghp0uMPKOhtcMdmQDpU-7zWWO7ODhuUipF"
        + "VkXQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAjE9XddeWUD6WE393xoqC"
        + "mgBWrI3tcBQLCBsJRJDFe_8AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
        + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
        + "AAAAAAAAAAAAAAAAABAAUAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
        + "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAD9rOmAu-jSSf1BAj_cC0mu7YCnx4QosD"
        + "78yj3sQX81IAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAH5Au8JZ_dpXiLY"
        + "aE1TtyGjGz0dtFZa7eGooRGTQzoJJuR8Xj-zUvyCKE4ABy0pajfE8lOGSUHuJoifisJ"
        + "NAhg4gAAABAgMEBQYHCAkKCwwNDg8QERITFBUWFxgZGhscHR4fBQDIDQAALS0tLS1CR"
        + "UdJTiBDRVJUSUZJQ0FURS0tLS0tCk1JSUVmekNDQkNhZ0F3SUJBZ0lVRk5xSnZZZTU4"
        + "ZXlpUjI2Yzd0L2lxU0pNYnFNd0NnWUlLb1pJemowRUF3SXcKY1RFak1DRUdBMVVFQXd"
        + "3YVNXNTBaV3dnVTBkWUlGQkRTeUJRY205alpYTnpiM0lnUTBFeEdqQVlCZ05WQkFvTQ"
        + "pFVWx1ZEdWc0lFTnZjbkJ2Y21GMGFXOXVNUlF3RWdZRFZRUUhEQXRUWVc1MFlTQkRiR"
        + "0Z5WVRFTE1Ba0dBMVVFCkNBd0NRMEV4Q3pBSkJnTlZCQVlUQWxWVE1CNFhEVEl4TURR"
        + "eU1USXdOVGt6T0ZvWERUSTRNRFF5TVRJd05Ua3oKT0Zvd2NERWlNQ0FHQTFVRUF3d1p"
        + "TVzUwWld3Z1UwZFlJRkJEU3lCRFpYSjBhV1pwWTJGMFpURWFNQmdHQTFVRQpDZ3dSU1"
        + "c1MFpXd2dRMjl5Y0c5eVlYUnBiMjR4RkRBU0JnTlZCQWNNQzFOaGJuUmhJRU5zWVhKa"
        + "E1Rc3dDUVlEClZRUUlEQUpEUVRFTE1Ba0dBMVVFQmhNQ1ZWTXdXVEFUQmdjcWhrak9Q"
        + "UUlCQmdncWhrak9QUU1CQndOQ0FBUTgKU2V1NWV4WCtvMGNkclhkeEtHMGEvQXRzdnV"
        + "lNVNoUFpmOHgwa2czc0xSM2E5TzVHWWYwcW1XSkptL0c4bzZyVgpvbVI2Nmh3cFJXNl"
        + "pqSm9ocXdvT280SUNtekNDQXBjd0h3WURWUjBqQkJnd0ZvQVUwT2lxMm5YWCtTNUpGN"
        + "Wc4CmV4UmwwTlh5V1Uwd1h3WURWUjBmQkZnd1ZqQlVvRktnVUlaT2FIUjBjSE02THk5"
        + "aGNHa3VkSEoxYzNSbFpITmwKY25acFkyVnpMbWx1ZEdWc0xtTnZiUzl6WjNndlkyVnl"
        + "kR2xtYVdOaGRHbHZiaTkyTWk5d1kydGpjbXcvWTJFOQpjSEp2WTJWemMyOXlNQjBHQT"
        + "FVZERnUVdCQlFzbnhWelhVWnhwRkd5YUtXdzhWZmdOZXBjcHpBT0JnTlZIUThCCkFmO"
        + "EVCQU1DQnNBd0RBWURWUjBUQVFIL0JBSXdBRENDQWRRR0NTcUdTSWI0VFFFTkFRU0NB"
        + "Y1V3Z2dIQk1CNEcKQ2lxR1NJYjRUUUVOQVFFRUVEeEI4dUNBTVU0bmw1ZlBFaktxdG8"
        + "wd2dnRmtCZ29xaGtpRytFMEJEUUVDTUlJQgpWREFRQmdzcWhraUcrRTBCRFFFQ0FRSU"
        + "JFVEFRQmdzcWhraUcrRTBCRFFFQ0FnSUJFVEFRQmdzcWhraUcrRTBCCkRRRUNBd0lCQ"
        + "WpBUUJnc3Foa2lHK0UwQkRRRUNCQUlCQkRBUUJnc3Foa2lHK0UwQkRRRUNCUUlCQVRB"
        + "UkJnc3EKaGtpRytFMEJEUUVDQmdJQ0FJQXdFQVlMS29aSWh2aE5BUTBCQWdjQ0FRWXd"
        + "FQVlMS29aSWh2aE5BUTBCQWdnQwpBUUF3RUFZTEtvWklodmhOQVEwQkFna0NBUUF3RU"
        + "FZTEtvWklodmhOQVEwQkFnb0NBUUF3RUFZTEtvWklodmhOCkFRMEJBZ3NDQVFBd0VBW"
        + "UxLb1pJaHZoTkFRMEJBZ3dDQVFBd0VBWUxLb1pJaHZoTkFRMEJBZzBDQVFBd0VBWUwK"
        + "S29aSWh2aE5BUTBCQWc0Q0FRQXdFQVlMS29aSWh2aE5BUTBCQWc4Q0FRQXdFQVlMS29"
        + "aSWh2aE5BUTBCQWhBQwpBUUF3RUFZTEtvWklodmhOQVEwQkFoRUNBUW93SHdZTEtvWk"
        + "lodmhOQVEwQkFoSUVFQkVSQWdRQmdBWUFBQUFBCkFBQUFBQUF3RUFZS0tvWklodmhOQ"
        + "VEwQkF3UUNBQUF3RkFZS0tvWklodmhOQVEwQkJBUUdBSkJ1MVFBQU1BOEcKQ2lxR1NJ"
        + "YjRUUUVOQVFVS0FRQXdDZ1lJS29aSXpqMEVBd0lEUndBd1JBSWdjREZEZHl1UFRHRVR"
        + "ORm5BU0QzOApDWTNSNmlBREpEVHZBbHZTWDNIekk4a0NJRDZsVm1DWklYUHk4ekpKMW"
        + "gvMnJ1NjJsdlVVWDJJaU1ibVFOUEEwClBzMC8KLS0tLS1FTkQgQ0VSVElGSUNBVEUtL"
        + "S0tLQotLS0tLUJFR0lOIENFUlRJRklDQVRFLS0tLS0KTUlJQ2x6Q0NBajZnQXdJQkFn"
        + "SVZBTkRvcXRwMTEva3VTUmVZUEhzVVpkRFY4bGxOTUFvR0NDcUdTTTQ5QkFNQwpNR2d"
        + "4R2pBWUJnTlZCQU1NRVVsdWRHVnNJRk5IV0NCU2IyOTBJRU5CTVJvd0dBWURWUVFLRE"
        + "JGSmJuUmxiQ0JECmIzSndiM0poZEdsdmJqRVVNQklHQTFVRUJ3d0xVMkZ1ZEdFZ1Eye"
        + "GhjbUV4Q3pBSkJnTlZCQWdNQWtOQk1Rc3cKQ1FZRFZRUUdFd0pWVXpBZUZ3MHhPREEx"
        + "TWpFeE1EUTFNRGhhRncwek16QTFNakV4TURRMU1EaGFNSEV4SXpBaApCZ05WQkFNTUd"
        + "rbHVkR1ZzSUZOSFdDQlFRMHNnVUhKdlkyVnpjMjl5SUVOQk1Sb3dHQVlEVlFRS0RCRk"
        + "piblJsCmJDQkRiM0p3YjNKaGRHbHZiakVVTUJJR0ExVUVCd3dMVTJGdWRHRWdRMnhoY"
        + "21FeEN6QUpCZ05WQkFnTUFrTkIKTVFzd0NRWURWUVFHRXdKVlV6QlpNQk1HQnlxR1NN"
        + "NDlBZ0VHQ0NxR1NNNDlBd0VIQTBJQUJMOXErTk1wMklPZwp0ZGwxYmsvdVdaNStUR1F"
        + "tOGFDaTh6NzhmcytmS0NRM2QrdUR6WG5WVEFUMlpoRENpZnlJdUp3dk4zd05CcDlpCk"
        + "hCU1NNSk1KckJPamdic3dnYmd3SHdZRFZSMGpCQmd3Rm9BVUltVU0xbHFkTkluemc3U"
        + "1ZVcjlRR3prbkJxd3cKVWdZRFZSMGZCRXN3U1RCSG9FV2dRNFpCYUhSMGNITTZMeTlq"
        + "WlhKMGFXWnBZMkYwWlhNdWRISjFjM1JsWkhObApjblpwWTJWekxtbHVkR1ZzTG1OdmJ"
        + "TOUpiblJsYkZOSFdGSnZiM1JEUVM1amNtd3dIUVlEVlIwT0JCWUVGTkRvCnF0cDExL2"
        + "t1U1JlWVBIc1VaZERWOGxsTk1BNEdBMVVkRHdFQi93UUVBd0lCQmpBU0JnTlZIUk1CQ"
        + "WY4RUNEQUcKQVFIL0FnRUFNQW9HQ0NxR1NNNDlCQU1DQTBjQU1FUUNJQy85ais4NFQr"
        + "SHp0Vk8vc09RQldKYlNkKy8ydWV4Swo0K2FBMGpjRkJMY3BBaUEzZGhNckY1Y0Q1MnQ"
        + "2RnFNdkFJcGo4WGRHbXkyYmVlbGpMSksrcHpwY1JBPT0KLS0tLS1FTkQgQ0VSVElGSU"
        + "NBVEUtLS0tLQotLS0tLUJFR0lOIENFUlRJRklDQVRFLS0tLS0KTUlJQ2pqQ0NBalNnQ"
        + "XdJQkFnSVVJbVVNMWxxZE5JbnpnN1NWVXI5UUd6a25CcXd3Q2dZSUtvWkl6ajBFQXdJ"
        + "dwphREVhTUJnR0ExVUVBd3dSU1c1MFpXd2dVMGRZSUZKdmIzUWdRMEV4R2pBWUJnTlZ"
        + "CQW9NRVVsdWRHVnNJRU52CmNuQnZjbUYwYVc5dU1SUXdFZ1lEVlFRSERBdFRZVzUwWV"
        + "NCRGJHRnlZVEVMTUFrR0ExVUVDQXdDUTBFeEN6QUoKQmdOVkJBWVRBbFZUTUI0WERUR"
        + "TRNRFV5TVRFd05ERXhNVm9YRFRNek1EVXlNVEV3TkRFeE1Gb3dhREVhTUJnRwpBMVVF"
        + "QXd3UlNXNTBaV3dnVTBkWUlGSnZiM1FnUTBFeEdqQVlCZ05WQkFvTUVVbHVkR1ZzSUV"
        + "OdmNuQnZjbUYwCmFXOXVNUlF3RWdZRFZRUUhEQXRUWVc1MFlTQkRiR0Z5WVRFTE1Ba0"
        + "dBMVVFQ0F3Q1EwRXhDekFKQmdOVkJBWVQKQWxWVE1Ga3dFd1lIS29aSXpqMENBUVlJS"
        + "29aSXpqMERBUWNEUWdBRUM2bkV3TURJWVpPai9pUFdzQ3phRUtpNwoxT2lPU0xSRmhX"
        + "R2pibkJWSmZWbmtZNHUzSWprRFlZTDBNeE80bXFzeVlqbEJhbFRWWXhGUDJzSkJLNXp"
        + "sS09CCnV6Q0J1REFmQmdOVkhTTUVHREFXZ0JRaVpReldXcDAwaWZPRHRKVlN2MUFiT1"
        + "NjR3JEQlNCZ05WSFI4RVN6QkoKTUVlZ1JhQkRoa0ZvZEhSd2N6b3ZMMk5sY25ScFptb"
        + "GpZWFJsY3k1MGNuVnpkR1ZrYzJWeWRtbGpaWE11YVc1MApaV3d1WTI5dEwwbHVkR1Zz"
        + "VTBkWVVtOXZkRU5CTG1OeWJEQWRCZ05WSFE0RUZnUVVJbVVNMWxxZE5JbnpnN1NWClV"
        + "yOVFHemtuQnF3d0RnWURWUjBQQVFIL0JBUURBZ0VHTUJJR0ExVWRFd0VCL3dRSU1BWU"
        + "JBZjhDQVFFd0NnWUkKS29aSXpqMEVBd0lEU0FBd1JRSWdRUXMvMDhyeWNkUGF1Q0ZrO"
        + "FVQUVhDTUFsc2xvQmU3TndhUUdUY2RwYTBFQwpJUUNVdDhTR3Z4S21qcGNNL3owV1A5"
        + "RHZvOGgyazVkdTFpV0RkQmtBbiswaWlBPT0KLS0tLS1FTkQgQ0VSVElGSUNBVEUtLS0"
        + "tLQoA";


    @Test()
    void testAttestSgxEnclaveRequest() {
        byte[] decodedRuntimeData = Base64.getUrlDecoder().decode(runtimeData);
        byte[] decodedOpenEnclaveReport = Base64.getUrlDecoder().decode(openEnclaveReport);

        AttestSgxEnclaveOptions request1 = AttestSgxEnclaveOptions.fromQuote(decodedOpenEnclaveReport);
        AttestSgxEnclaveOptions request2 = AttestSgxEnclaveOptions
            .fromQuote(decodedOpenEnclaveReport)
            .setInitTimeData(decodedRuntimeData)
            .setInitTimeData(decodedOpenEnclaveReport)
            .setRunTimeJson("{ \"xxx\": 123 }".getBytes(StandardCharsets.UTF_8))
            .setRunTimeData(new byte[] { 1, 2, 3, 4, 5});

        assertArrayEquals(decodedOpenEnclaveReport, request2.getInitTimeData());

        // Get Json should throw because we set Data.
        assertThrows(IllegalStateException.class, () -> request2.getInitTimeJson());
        assertThrows(IllegalStateException.class, () -> request2.getRunTimeJson());
        assertArrayEquals(new byte[] { 1, 2, 3, 4, 5}, request2.getRunTimeData());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testAttestSgxEnclave(HttpClient httpClient, String clientUri) {

        AttestationClientBuilder attestationBuilder = getBuilder(httpClient, clientUri);

        AttestationClient client = attestationBuilder.buildAttestationClient();

        byte[] decodedRuntimeData = Base64.getUrlDecoder().decode(runtimeData);
        byte[] decodedOpenEnclaveReport = Base64.getUrlDecoder().decode(openEnclaveReport);
        byte[] sgxQuote = Arrays.copyOfRange(decodedOpenEnclaveReport, 0x10, decodedOpenEnclaveReport.length);
        AttestSgxEnclaveOptions request = AttestSgxEnclaveOptions
            .fromQuote(sgxQuote)
            .setRunTimeData(decodedRuntimeData);
        AttestationResult result = client.attestSgxEnclave(request);

        verifyAttestationResult(clientUri, result, decodedRuntimeData, false);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testAttestSgxEnclaveNoRuntimeData(HttpClient httpClient, String clientUri) {

        AttestationClientBuilder attestationBuilder = getBuilder(httpClient, clientUri);

        AttestationClient client = attestationBuilder.buildAttestationClient();

        byte[] decodedRuntimeData = Base64.getUrlDecoder().decode(runtimeData);
        byte[] decodedOpenEnclaveReport = Base64.getUrlDecoder().decode(openEnclaveReport);
        byte[] sgxQuote = Arrays.copyOfRange(decodedOpenEnclaveReport, 0x10, decodedOpenEnclaveReport.length);

        AttestationResult result = client.attestSgxEnclave(sgxQuote);
        verifyAttestationResult(clientUri, result, null, false);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testAttestSgxEnclaveRuntimeJson(HttpClient httpClient, String clientUri) {

        AttestationClientBuilder attestationBuilder = getBuilder(httpClient, clientUri);

        AttestationClient client = attestationBuilder.buildAttestationClient();

        byte[] decodedRuntimeData = Base64.getUrlDecoder().decode(runtimeData);
        byte[] decodedOpenEnclaveReport = Base64.getUrlDecoder().decode(openEnclaveReport);
        byte[] sgxQuote = Arrays.copyOfRange(decodedOpenEnclaveReport, 0x10, decodedOpenEnclaveReport.length);

        AttestSgxEnclaveOptions request = AttestSgxEnclaveOptions
            .fromQuote(sgxQuote)
            .setRunTimeJson(decodedRuntimeData);

        AttestationResult result = client.attestSgxEnclave(request);
        verifyAttestationResult(clientUri, result, decodedRuntimeData, true);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testAttestSgxEnclaveDraftPolicy(HttpClient httpClient, String clientUri) {

        AttestationClientBuilder attestationBuilder = getBuilder(httpClient, clientUri);

        AttestationClient client = attestationBuilder.buildAttestationClient();

        byte[] decodedRuntimeData = Base64.getUrlDecoder().decode(runtimeData);
        byte[] decodedOpenEnclaveReport = Base64.getUrlDecoder().decode(openEnclaveReport);
        byte[] sgxQuote = Arrays.copyOfRange(decodedOpenEnclaveReport, 0x10, decodedOpenEnclaveReport.length);

        AttestSgxEnclaveOptions request = AttestSgxEnclaveOptions
            .fromQuote(sgxQuote)
            .setDraftPolicyForAttestation("version=1.0; authorizationrules{=> permit();}; issuancerules{};")
            .setRunTimeJson(decodedRuntimeData);

        Response<AttestationResult> response = client.attestSgxEnclaveWithResponse(request, Context.NONE);
        assertTrue(response instanceof AttestationResponse);
        AttestationResponse<AttestationResult> attestResponse = (AttestationResponse<AttestationResult>) response;

        // When a draft policy is specified, the token is unsecured.
        assertTrue(attestResponse.getToken().getAlgorithm() == "none");

        verifyAttestationResult(clientUri, response.getValue(), decodedRuntimeData, true);
    }


    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testAttestSgxEnclaveAsync(HttpClient httpClient, String clientUri) {

        AttestationClientBuilder attestationBuilder = getBuilder(httpClient, clientUri);
        AttestationAsyncClient client = attestationBuilder.buildAttestationAsyncClient();

        byte[] decodedRuntimeData = Base64.getUrlDecoder().decode(runtimeData);
        byte[] decodedSgxQuote = Base64.getUrlDecoder().decode(openEnclaveReport);
        byte[] sgxQuote = Arrays.copyOfRange(decodedSgxQuote, 0x10, decodedSgxQuote.length);

        AttestSgxEnclaveOptions request = AttestSgxEnclaveOptions
            .fromQuote(sgxQuote)
            .setRunTimeData(decodedRuntimeData);

        StepVerifier.create(client.attestSgxEnclave(request))
            .assertNext(result -> {
                verifyAttestationResult(clientUri, result, decodedRuntimeData, false);
            })
            .expectComplete()
            .verify();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testAttestSgxEnclaveNoRuntimeDataAsync(HttpClient httpClient, String clientUri) {

        AttestationClientBuilder attestationBuilder = getBuilder(httpClient, clientUri);

        AttestationAsyncClient client = attestationBuilder.buildAttestationAsyncClient();

        byte[] decodedRuntimeData = Base64.getUrlDecoder().decode(runtimeData);
        byte[] decodedOpenEnclaveReport = Base64.getUrlDecoder().decode(openEnclaveReport);
        byte[] sgxQuote = Arrays.copyOfRange(decodedOpenEnclaveReport, 0x10, decodedOpenEnclaveReport.length);

        AttestSgxEnclaveOptions request = AttestSgxEnclaveOptions
            .fromQuote(sgxQuote);

        StepVerifier.create(client.attestSgxEnclave(request))
            .assertNext(result -> verifyAttestationResult(clientUri, result, null, false))
            .expectComplete()
            .verify();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testAttestSgxEnclaveRuntimeJsonAsync(HttpClient httpClient, String clientUri) {

        AttestationClientBuilder attestationBuilder = getBuilder(httpClient, clientUri);

        AttestationAsyncClient client = attestationBuilder.buildAttestationAsyncClient();

        byte[] decodedRuntimeData = Base64.getUrlDecoder().decode(runtimeData);
        byte[] decodedOpenEnclaveReport = Base64.getUrlDecoder().decode(openEnclaveReport);
        byte[] sgxQuote = Arrays.copyOfRange(decodedOpenEnclaveReport, 0x10, decodedOpenEnclaveReport.length);

        AttestSgxEnclaveOptions options = AttestSgxEnclaveOptions
            .fromQuote(sgxQuote)
            .setRunTimeJson(decodedRuntimeData);

        StepVerifier.create(client.attestSgxEnclave(options))
            .assertNext(result -> verifyAttestationResult(clientUri, result, decodedRuntimeData, true))
            .expectComplete()
            .verify();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testAttestSgxEnclaveDraftPolicyAsync(HttpClient httpClient, String clientUri) {

        AttestationClientBuilder attestationBuilder = getBuilder(httpClient, clientUri);

        AttestationAsyncClient client = attestationBuilder.buildAttestationAsyncClient();

        byte[] decodedRuntimeData = Base64.getUrlDecoder().decode(runtimeData);
        byte[] decodedOpenEnclaveReport = Base64.getUrlDecoder().decode(openEnclaveReport);
        byte[] sgxQuote = Arrays.copyOfRange(decodedOpenEnclaveReport, 0x10, decodedOpenEnclaveReport.length);

        AttestSgxEnclaveOptions request = AttestSgxEnclaveOptions
            .fromQuote(sgxQuote)
            .setDraftPolicyForAttestation("version=1.0; authorizationrules{=> permit();}; issuancerules{};")
            .setRunTimeJson(decodedRuntimeData);

        StepVerifier.create(client.attestSgxEnclaveWithResponse(request))
            .assertNext(response -> {
                assertTrue(response instanceof AttestationResponse);
                AttestationResponse<AttestationResult> attestResponse = (AttestationResponse<AttestationResult>) response;
                verifyAttestationResult(clientUri, response.getValue(), decodedRuntimeData, true);
            })
            .expectComplete()
            .verify();

    }

    @Test()
    void testAttestOpenEnclaveRequest() {
        byte[] decodedRuntimeData = Base64.getUrlDecoder().decode(runtimeData);
        byte[] decodedOpenEnclaveReport = Base64.getUrlDecoder().decode(openEnclaveReport);

        AttestOpenEnclaveOptions request1 = AttestOpenEnclaveOptions.fromReport(decodedOpenEnclaveReport);
        AttestOpenEnclaveOptions request2 = AttestOpenEnclaveOptions
            .fromReport(decodedOpenEnclaveReport)
            .setInitTimeData(decodedRuntimeData)
            .setInitTimeData(decodedOpenEnclaveReport)
            .setRunTimeData(new byte[] { 1, 2, 3, 4, 5});

        assertArrayEquals(decodedOpenEnclaveReport, request2.getInitTimeData());

        // Get Json should throw because we set Data.
        assertThrows(IllegalStateException.class, () -> request2.getInitTimeJson());
        assertThrows(IllegalStateException.class, () -> request2.getRunTimeJson());
        assertArrayEquals(new byte[] { 1, 2, 3, 4, 5}, request2.getRunTimeData());
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testAttestOpenEnclave(HttpClient httpClient, String clientUri) {

        AttestationClientBuilder attestationBuilder = getBuilder(httpClient, clientUri);

        AttestationClient client = attestationBuilder.buildAttestationClient();

        byte[] decodedRuntimeData = Base64.getUrlDecoder().decode(runtimeData);
        byte[] decodedOpenEnclaveReport = Base64.getUrlDecoder().decode(openEnclaveReport);

        AttestOpenEnclaveOptions request = AttestOpenEnclaveOptions
            .fromReport(decodedOpenEnclaveReport)
            .setRunTimeData(decodedRuntimeData);
        AttestationResult result = client.attestOpenEnclave(request);

        verifyAttestationResult(clientUri, result, decodedRuntimeData, false);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testAttestOpenEnclaveNoRuntimeData(HttpClient httpClient, String clientUri) {

        AttestationClientBuilder attestationBuilder = getBuilder(httpClient, clientUri);

        AttestationClient client = attestationBuilder.buildAttestationClient();

        byte[] decodedOpenEnclaveReport = Base64.getUrlDecoder().decode(openEnclaveReport);

        AttestOpenEnclaveOptions request = AttestOpenEnclaveOptions
            .fromReport(decodedOpenEnclaveReport);

        AttestationResult result = client.attestOpenEnclave(request);
        verifyAttestationResult(clientUri, result, null, false);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testAttestOpenEnclaveRuntimeJson(HttpClient httpClient, String clientUri) {

        AttestationClientBuilder attestationBuilder = getBuilder(httpClient, clientUri);

        AttestationClient client = attestationBuilder.buildAttestationClient();

        byte[] decodedRuntimeData = Base64.getUrlDecoder().decode(runtimeData);
        byte[] decodedOpenEnclaveReport = Base64.getUrlDecoder().decode(openEnclaveReport);

        AttestOpenEnclaveOptions request = AttestOpenEnclaveOptions
            .fromReport(decodedOpenEnclaveReport)
            .setRunTimeJson(decodedRuntimeData);

        AttestationResult result = client.attestOpenEnclave(request);
        verifyAttestationResult(clientUri, result, decodedRuntimeData, true);
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testAttestOpenEnclaveDraftPolicy(HttpClient httpClient, String clientUri) {

        AttestationClientBuilder attestationBuilder = getBuilder(httpClient, clientUri);

        AttestationClient client = attestationBuilder.buildAttestationClient();

        byte[] decodedRuntimeData = Base64.getUrlDecoder().decode(runtimeData);
        byte[] decodedOpenEnclaveReport = Base64.getUrlDecoder().decode(openEnclaveReport);

        AttestOpenEnclaveOptions request = AttestOpenEnclaveOptions
            .fromReport(decodedOpenEnclaveReport)
            .setDraftPolicyForAttestation("version=1.0; authorizationrules{=> permit();}; issuancerules{};")
            .setRunTimeJson(decodedRuntimeData);

        Response<AttestationResult> response = client.attestOpenEnclaveWithResponse(request, Context.NONE);
        assertTrue(response instanceof AttestationResponse);
        AttestationResponse<AttestationResult> attestResponse = (AttestationResponse<AttestationResult>) response;

        // When a draft policy is specified, the token is unsecured.
        assertTrue(attestResponse.getToken().getAlgorithm() == "none");

        verifyAttestationResult(clientUri, response.getValue(), decodedRuntimeData, true);
    }


    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testAttestOpenEnclaveAsync(HttpClient httpClient, String clientUri) {

        AttestationClientBuilder attestationBuilder = getBuilder(httpClient, clientUri);
        AttestationAsyncClient client = attestationBuilder.buildAttestationAsyncClient();

        byte[] decodedRuntimeData = Base64.getUrlDecoder().decode(runtimeData);
        byte[] decodedOpenEnclaveReport = Base64.getUrlDecoder().decode(openEnclaveReport);

        AttestOpenEnclaveOptions options = AttestOpenEnclaveOptions
            .fromReport(decodedOpenEnclaveReport)
            .setRunTimeData(decodedRuntimeData);

        StepVerifier.create(client.attestOpenEnclave(options))
            .assertNext(result -> verifyAttestationResult(clientUri, result, decodedRuntimeData, false))
            .expectComplete()
            .verify();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testAttestOpenEnclaveNoRuntimeDataAsync(HttpClient httpClient, String clientUri) {

        AttestationClientBuilder attestationBuilder = getBuilder(httpClient, clientUri);

        AttestationAsyncClient client = attestationBuilder.buildAttestationAsyncClient();

        byte[] decodedOpenEnclaveReport = Base64.getUrlDecoder().decode(openEnclaveReport);

        StepVerifier.create(client.attestOpenEnclave(decodedOpenEnclaveReport))
            .assertNext(result -> verifyAttestationResult(clientUri, result, null, false))
            .expectComplete()
            .verify();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testAttestOpenEnclaveRuntimeJsonAsync(HttpClient httpClient, String clientUri) {

        AttestationClientBuilder attestationBuilder = getBuilder(httpClient, clientUri);

        AttestationAsyncClient client = attestationBuilder.buildAttestationAsyncClient();

        byte[] decodedRuntimeData = Base64.getUrlDecoder().decode(runtimeData);
        byte[] decodedOpenEnclaveReport = Base64.getUrlDecoder().decode(openEnclaveReport);

        AttestOpenEnclaveOptions options = AttestOpenEnclaveOptions
            .fromReport(decodedOpenEnclaveReport)
            .setRunTimeJson(decodedRuntimeData);

        StepVerifier.create(client.attestOpenEnclave(options))
            .assertNext(result -> verifyAttestationResult(clientUri, result, decodedRuntimeData, true))
            .expectComplete()
            .verify();
    }

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void testAttestOpenEnclaveDraftPolicyAsync(HttpClient httpClient, String clientUri) {

        AttestationClientBuilder attestationBuilder = getBuilder(httpClient, clientUri);

        AttestationAsyncClient client = attestationBuilder.buildAttestationAsyncClient();

        byte[] decodedRuntimeData = Base64.getUrlDecoder().decode(runtimeData);
        byte[] decodedOpenEnclaveReport = Base64.getUrlDecoder().decode(openEnclaveReport);

        AttestOpenEnclaveOptions options = AttestOpenEnclaveOptions
            .fromReport(decodedOpenEnclaveReport)
            .setDraftPolicyForAttestation("version=1.0; authorizationrules{=> permit();}; issuancerules{};")
            .setRunTimeJson(decodedRuntimeData);

        StepVerifier.create(client.attestOpenEnclaveWithResponse(options))
            .assertNext(response -> {
                assertTrue(response instanceof AttestationResponse);
                AttestationResponse<AttestationResult> attestResponse = (AttestationResponse<AttestationResult>) response;
                verifyAttestationResult(clientUri, response.getValue(), decodedRuntimeData, true);
            })
            .expectComplete()
            .verify();

    }


    private void verifyAttestationResult(String clientUri, AttestationResult result, byte[] runtimeData, boolean expectJson) {
        assertNotNull(result.getIss());

        // In playback mode, the client URI is bogus and thus cannot be relied on for test purposes.
        if (testContextManager.getTestMode() != TestMode.PLAYBACK) {
            Assertions.assertEquals(clientUri, result.getIss());
        }

        if (expectJson) {
            ObjectMapper mapper = new ObjectMapper();
            assertTrue(result.getRuntimeClaims() instanceof Map);
            @SuppressWarnings("unchecked")
            Map<String, Object> runtimeClaims = (Map<String, Object>) result.getRuntimeClaims();
            @SuppressWarnings("unchecked")
            Map<String, Object> expectedClaims = assertDoesNotThrow(() -> (Map<String, Object>) mapper.readValue(runtimeData, Object.class));
            assertObjectEqual(expectedClaims, runtimeClaims);
        } else if (runtimeData != null) {
            Assertions.assertArrayEquals(runtimeData, result.getEnclaveHeldData());
        }
    }

    void assertObjectEqual(Map<String, Object> expected, Map<String, Object> actual) {
        expected.forEach((key, o) -> {
            logger.verbose("Key: " + key);
            assertTrue(actual.containsKey(key));
            if (expected.get(key) instanceof Map) {
                assertTrue(actual.get(key) instanceof Map);
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


    /**
     * This test cannot be written until the setPolicy APIs are written because it depends on
     * setting attestation policy :(.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void attestTpm() {
    }

    /**
     * This test cannot be written until the setPolicy APIs are written because it depends on
     * setting attestation policy :(.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void attestTpmAsync() {
    }

    /**
     * This test cannot be written until the setPolicy APIs are written because it depends on
     * setting attestation policy :(.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void attestTpmWithResponse() {
    }

    /**
     * This test cannot be written until the setPolicy APIs are written because it depends on
     * setting attestation policy :(.
     */
    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    void attestTpmWithResponseAsync() {
    }
}


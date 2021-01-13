// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.security.attestation;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.test.TestMode;
import com.azure.core.util.Configuration;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.security.attestation.models.JsonWebKeySet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AttestationTest extends AttestationClientTestBase {
    private AttestationClient sharedClient;
    private static final String DISPLAY_NAME_WITH_ARGUMENTS = "{displayName} with [{arguments}]";

    @ParameterizedTest(name = DISPLAY_NAME_WITH_ARGUMENTS)
    @MethodSource("getAttestationClients")
    public void testClassCreation(HttpClient client, String clientUri) {

        AttestationClientBuilder attestationBuilder = getBuilder(client, clientUri);

        Object metadataConfigResponse = attestationBuilder.buildMetadataConfigurationClient().get();

        Assertions.assertTrue(metadataConfigResponse instanceof LinkedHashMap);

        LinkedHashMap<String, Object> metadataConfig = (LinkedHashMap<String, Object>)metadataConfigResponse;

        JsonWebKeySet certs = attestationBuilder.buildSigningCertificatesClient().get();

    }

    private AttestationClientBuilder getBuilder(HttpClient httpClient, String clientUri) {
        return new AttestationClientBuilder().pipeline(getHttpPipeline(httpClient)).instanceUrl(clientUri);
    }

    private static Stream<Arguments> getAttestationClients() {
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
}


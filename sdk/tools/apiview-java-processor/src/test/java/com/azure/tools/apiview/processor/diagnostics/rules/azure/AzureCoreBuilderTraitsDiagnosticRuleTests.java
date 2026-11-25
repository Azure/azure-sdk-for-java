// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.apiview.processor.diagnostics.rules.azure;

import com.azure.tools.apiview.processor.model.ApiListing;
import com.github.javaparser.StaticJavaParser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class AzureCoreBuilderTraitsDiagnosticRuleTests {
    @ParameterizedTest
    @CsvSource(
        value = {
            "|false|1",
            "ServiceClientProtocol.HTTP|false|1",
            "com.azure.core.annotation.ServiceClientProtocol.HTTP|false|1",
            "HTTP|false|1",
            "ServiceClientProtocol.AMQP|false|0",
            "com.azure.core.annotation.ServiceClientProtocol.AMQP|false|0",
            "AMQP|false|0",
            "|true|0",
            "HTTP|true|0" },
        delimiter = '|')
    public void selectsHttpTraitChecks(String protocol, boolean implementsTrait, int diagnosticCount) {
        String annotationProtocol = protocol == null ? "" : ", protocol = " + protocol;
        String implementedTrait = implementsTrait ? " implements HttpTrait<TestClientBuilder>" : "";
        ApiListing listing = new ApiListing();

        new AzureCoreBuilderTraitsDiagnosticRule().scanIndividual(StaticJavaParser
            .parse("package com.azure.test; import static com.azure.core.annotation.ServiceClientProtocol.*;"
                + "@ServiceClientBuilder(serviceClients = TestClient.class" + annotationProtocol + ")"
                + "public class TestClientBuilder" + implementedTrait
                + " { public void httpClient(HttpClient client) {} }"),
            listing);

        assertEquals(diagnosticCount, listing.getDiagnostics().size());
        if (diagnosticCount > 0) {
            assertTrue(listing.getDiagnostics().get(0).getText().contains("HttpTrait"));
        }
    }

    @ParameterizedTest
    @ValueSource(
        strings = { "ServiceClientProtocol.AMQP", "com.azure.core.annotation.ServiceClientProtocol.AMQP", "AMQP" })
    public void selectsAmqpTraitChecks(String protocol) {
        ApiListing listing = new ApiListing();

        new AzureCoreBuilderTraitsDiagnosticRule().scanIndividual(
            StaticJavaParser
                .parse("package com.azure.test; import static com.azure.core.annotation.ServiceClientProtocol.*;"
                    + "@ServiceClientBuilder(serviceClients = TestClient.class, protocol = " + protocol + ")"
                    + "public class TestClientBuilder { public void transportType(AmqpTransportType type) {} }"),
            listing);

        assertEquals(1, listing.getDiagnostics().size());
        assertTrue(listing.getDiagnostics().get(0).getText().contains("AmqpTrait"));
    }
}

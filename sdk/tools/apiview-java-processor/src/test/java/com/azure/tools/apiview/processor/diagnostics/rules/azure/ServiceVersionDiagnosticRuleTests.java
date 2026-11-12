// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.apiview.processor.diagnostics.rules.azure;

import com.azure.tools.apiview.processor.model.APIListing;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ServiceVersionDiagnosticRuleTests {
    @ParameterizedTest
    @CsvSource(
        value = {
            "|1",
            "ServiceClientProtocol.HTTP|1",
            "com.azure.core.annotation.ServiceClientProtocol.HTTP|1",
            "HTTP|1",
            "ServiceClientProtocol.AMQP|0",
            "AMQP|0" },
        delimiter = '|')
    public void checksMissingServiceVersionForHttpBuilders(String annotationProtocol, int diagnosticCount) {
        ServiceVersionDiagnosticRule rule = new ServiceVersionDiagnosticRule();
        APIListing listing = new APIListing();
        String protocol = annotationProtocol == null ? "" : ", protocol = " + annotationProtocol;
        CompilationUnit builder = StaticJavaParser
            .parse("package com.azure.test;" + "@ServiceClientBuilder(serviceClients = TestClient.class" + protocol
                + ")" + "public class TestClientBuilder {}");
        builder.setStorage(Paths.get("TestClientBuilder.java"));

        rule.scanIndividual(builder, listing);
        rule.scanFinal(listing);

        assertEquals(diagnosticCount, listing.getDiagnostics().size());
        if (diagnosticCount > 0) {
            assertEquals("ServiceVersion type is missing.", listing.getDiagnostics().get(0).getText());
        }
    }
}

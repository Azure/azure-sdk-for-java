// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.apiview.processor.diagnostics.rules;

import com.azure.tools.apiview.processor.model.APIListing;
import com.github.javaparser.StaticJavaParser;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RequiredBuilderMethodsDiagnosticRuleTests {
    @ParameterizedTest
    @CsvSource(
        value = {
            "http||2",
            "http|ServiceClientProtocol.HTTP|2",
            "amqp|ServiceClientProtocol.AMQP|2",
            "http|com.azure.core.annotation.ServiceClientProtocol.HTTP|2",
            "http|HTTP|2",
            "amqp|AMQP|2",
            "http|ServiceClientProtocol.AMQP|0",
            "amqp|ServiceClientProtocol.HTTP|0",
            "amqp||0" },
        delimiter = '|')
    public void selectsProtocolSpecificChecks(String ruleProtocol, String annotationProtocol, int diagnosticCount) {
        RequiredBuilderMethodsDiagnosticRule rule = new RequiredBuilderMethodsDiagnosticRule(ruleProtocol)
            .addMethod("configured", new RequiredBuilderMethodsDiagnosticRule.ExactTypeNameCheckFunction("Expected"))
            .addMethod("required", method -> Optional.empty());
        APIListing listing = new APIListing();
        String protocol = annotationProtocol == null ? "" : ", protocol = " + annotationProtocol;

        rule.scanIndividual(
            StaticJavaParser.parse("package com.azure.test;" + "@ServiceClientBuilder(serviceClients = TestClient.class"
                + protocol + ")" + "public class TestClientBuilder { public void configured(Wrong value) {} }"),
            listing);

        assertEquals(diagnosticCount, listing.getDiagnostics().size());
        if (diagnosticCount > 0) {
            assertTrue(listing.getDiagnostics().get(0).getText().contains("Incorrect type"));
            assertTrue(listing.getDiagnostics().get(1).getText().contains("[required]"));
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = { true, false })
    public void checksMissingMethodsIndependentlyForEachBuilder(boolean sameCompilationUnit) {
        RequiredBuilderMethodsDiagnosticRule rule
            = new RequiredBuilderMethodsDiagnosticRule(null).addMethod("required", method -> Optional.empty());
        APIListing listing = new APIListing();
        String first = "@ServiceClientBuilder(serviceClients = FirstClient.class)"
            + "public class FirstClientBuilder { public void required() {} }";
        String second
            = "@ServiceClientBuilder(serviceClients = SecondClient.class)" + "public class SecondClientBuilder {}";

        if (sameCompilationUnit) {
            rule.scanIndividual(StaticJavaParser.parse("package com.azure.test;" + first + second), listing);
        } else {
            rule.scanIndividual(StaticJavaParser.parse("package com.azure.test;" + first), listing);
            rule.scanIndividual(StaticJavaParser.parse("package com.azure.test;" + second), listing);
        }

        assertEquals(1, listing.getDiagnostics().size());
        assertTrue(listing.getDiagnostics().get(0).getText().contains("[required]"));
    }
}

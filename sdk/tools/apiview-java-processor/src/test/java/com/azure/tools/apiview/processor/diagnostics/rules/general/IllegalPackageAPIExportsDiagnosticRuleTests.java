// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.apiview.processor.diagnostics.rules.general;

import com.azure.tools.apiview.processor.model.APIListing;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class IllegalPackageAPIExportsDiagnosticRuleTests {
    @ParameterizedTest
    @CsvSource(
        value = {
            "public io.netty.buffer.ByteBuf getBuffer() { return null; }|1",
            "protected void setBuffer(io.netty.buffer.ByteBuf buffer) {}|1",
            "public TestClient(io.netty.buffer.ByteBuf buffer) {}|1",
            "public java.util.List<io.netty.buffer.ByteBuf> getBuffers() { return null; }|1",
            "public com.azure.test.implementation.InternalModel getModel() { return null; }|1",
            "public java.nio.ByteBuffer getBuffer() { return null; }|0",
            "private io.netty.buffer.ByteBuf getBuffer() { return null; }|0" },
        delimiter = '|')
    public void checksExportedTypes(String member, int diagnosticCount) {
        IllegalPackageAPIExportsDiagnosticRule rule
            = new IllegalPackageAPIExportsDiagnosticRule("implementation", "netty");
        APIListing listing = new APIListing();
        CompilationUnit compilationUnit
            = StaticJavaParser.parse("package com.azure.test; public class TestClient {" + member + "}");
        compilationUnit.setStorage(Paths.get("TestClient.java"));

        rule.scanIndividual(compilationUnit, listing);

        assertEquals(diagnosticCount, listing.getDiagnostics().size());
    }

    @Test
    public void checksImportedTypes() {
        IllegalPackageAPIExportsDiagnosticRule rule = new IllegalPackageAPIExportsDiagnosticRule("netty");
        APIListing listing = new APIListing();
        listing.getTypeToPackageNameMap().put("ByteBuf", "io.netty.buffer");
        CompilationUnit compilationUnit = StaticJavaParser.parse("package com.azure.test;"
            + "import io.netty.buffer.ByteBuf; public class TestClient { public ByteBuf getBuffer() { return null; } }");
        compilationUnit.setStorage(Paths.get("TestClient.java"));

        rule.scanIndividual(compilationUnit, listing);

        assertEquals(1, listing.getDiagnostics().size());
    }
}

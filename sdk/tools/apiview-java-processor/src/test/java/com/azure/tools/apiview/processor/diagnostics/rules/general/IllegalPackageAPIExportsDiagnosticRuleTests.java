// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.apiview.processor.diagnostics.rules.general;

import com.azure.tools.apiview.processor.model.ApiListing;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

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
            "public io.netty.buffer.ByteBuf[][] getBuffers() { return null; }|1",
            "protected void setBuffers(io.netty.buffer.ByteBuf[] buffers) {}|1",
            "public TestClient(io.netty.buffer.ByteBuf[] buffers) {}|1",
            "public java.util.List<? extends io.netty.buffer.ByteBuf> getBuffers() { return null; }|1",
            "public void setBuffers(java.util.List<? super io.netty.buffer.ByteBuf> buffers) {}|1",
            "public java.util.List<java.util.List<? extends io.netty.buffer.ByteBuf[]>> getBuffers() { return null; }|1",
            "public java.util.List<? extends java.nio.ByteBuffer[]> getBuffers() { return null; }|0",
            "public java.util.List<?> getBuffers() { return null; }|0",
            "public com.azure.test.implementation.InternalModel getModel() { return null; }|1",
            "public java.nio.ByteBuffer getBuffer() { return null; }|0",
            "private io.netty.buffer.ByteBuf[] getBuffers() { return null; }|0",
            "private io.netty.buffer.ByteBuf getBuffer() { return null; }|0" },
        delimiter = '|')
    public void checksExportedTypes(String member, int diagnosticCount) {
        IllegalPackageApiExportsDiagnosticRule rule
            = new IllegalPackageApiExportsDiagnosticRule("implementation", "netty");
        ApiListing listing = new ApiListing();
        CompilationUnit compilationUnit
            = StaticJavaParser.parse("package com.azure.test; public class TestClient {" + member + "}");
        compilationUnit.setStorage(Paths.get("TestClient.java"));

        rule.scanIndividual(compilationUnit, listing);

        assertEquals(diagnosticCount, listing.getDiagnostics().size());
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
            "public ByteBuf getBuffer() { return null; }",
            "public ByteBuf[] getBuffers() { return null; }",
            "public java.util.List<? extends ByteBuf> getBuffers() { return null; }",
            "public void setBuffers(java.util.List<? super ByteBuf[]> buffers) {}" })
    public void checksImportedTypes(String member) {
        IllegalPackageApiExportsDiagnosticRule rule = new IllegalPackageApiExportsDiagnosticRule("netty");
        ApiListing listing = new ApiListing();
        listing.getTypeToPackageNameMap().put("ByteBuf", "io.netty.buffer");
        CompilationUnit compilationUnit = StaticJavaParser.parse(
            "package com.azure.test;" + "import io.netty.buffer.ByteBuf; public class TestClient {" + member + "}");
        compilationUnit.setStorage(Paths.get("TestClient.java"));

        rule.scanIndividual(compilationUnit, listing);

        assertEquals(1, listing.getDiagnostics().size());
    }
}

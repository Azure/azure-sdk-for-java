// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.apiview.processor.analysers;

import com.azure.tools.apiview.processor.model.ApiListing;
import com.azure.tools.apiview.processor.model.MarkdownRenderer;
import com.azure.tools.apiview.processor.model.ReviewLine;
import com.azure.tools.apiview.processor.model.TokenKind;
import com.azure.tools.apiview.processor.model.maven.Pom;
import com.github.javaparser.ast.type.ArrayType;
import com.github.javaparser.ast.type.PrimitiveType;
import com.github.javaparser.ast.type.Type;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class JavaASTAnalyserTests {
    @ParameterizedTest
    @CsvSource(
        value = {
            "WidgetBuilder,tokenCredential,true",
            "WidgetBuilder,setEndpoint,true",
            "WidgetClient,isHasValue,true",
            "WidgetClient,setHasValue,true",
            "WidgetBuilder,credential,false",
            "WidgetBuilder,endpoint,false",
            "WidgetClient,setEndpoint,false",
            "WidgetClient,tokenCredential,false",
            "WidgetBuilder,tokenCredentialProvider,false",
            "WidgetBuilderFactory,setEndpoint,false",
            "WidgetClient,hasValue,false",
            "WidgetClient,getHasValue,false" })
    public void reportsMethodNamingDiagnostics(String className, String methodName, boolean illegalName,
        @TempDir Path tempDir) throws IOException {
        Path sourceFile = tempDir.resolve(className + ".java");
        String source
            = "package com.azure.test; public class " + className + " { public void " + methodName + "() {} }";
        Files.write(sourceFile, source.getBytes(StandardCharsets.UTF_8));
        ApiListing listing = new ApiListing();
        listing.setPackageName("com.azure:test");
        listing.setMavenPom(mock(Pom.class));

        new JavaASTAnalyser(listing).analyse(Collections.singletonList(sourceFile));

        assertEquals(illegalName,
            listing.getDiagnostics()
                .stream()
                .anyMatch(diagnostic -> diagnostic.getText().contains("is using an illegal method name")));
    }

    @ParameterizedTest
    @CsvSource({ "com.azure.test,true,false", "com.azure.InvalidPackage,false,true", "com.azure.test,false,false" })
    public void reportsImportAndPackageDiagnostics(String packageName, boolean prohibitedImport, boolean invalidPackage,
        @TempDir Path tempDir) throws IOException {
        Path sourceFile = tempDir.resolve("TestClient.java");
        String source = "package " + packageName + ";"
            + (prohibitedImport ? "import com.sun.example.InternalType;" : "") + "public class TestClient {}";
        Files.write(sourceFile, source.getBytes(StandardCharsets.UTF_8));
        ApiListing listing = new ApiListing();
        listing.setPackageName("com.azure:test");
        listing.setMavenPom(mock(Pom.class));

        new JavaASTAnalyser(listing).analyse(Collections.singletonList(sourceFile));

        assertEquals(prohibitedImport,
            listing.getDiagnostics()
                .stream()
                .anyMatch(diagnostic -> diagnostic.getText().contains("Do not add dependencies")));
        assertEquals(invalidPackage,
            listing.getDiagnostics()
                .stream()
                .anyMatch(diagnostic -> diagnostic.getText().contains("Package name match")));
    }

    @Test
    public void keepsTypeSpacingLocalDuringOverlappingRendering() throws Exception {
        CountDownLatch entered = new CountDownLatch(1);
        CountDownLatch release = new CountDownLatch(1);
        ArrayType pausedArray = new ArrayType(PrimitiveType.intType()) {
            @Override
            public Type getComponentType() {
                entered.countDown();
                try {
                    if (!release.await(5, TimeUnit.SECONDS)) {
                        throw new AssertionError("Timed out waiting to resume array rendering.");
                    }
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                    throw new AssertionError(exception);
                }
                return super.getComponentType();
            }
        };
        Method visitType
            = JavaASTAnalyser.class.getDeclaredMethod("visitClassType", Type.class, ReviewLine.class, TokenKind.class);
        visitType.setAccessible(true);
        ApiListing first = new ApiListing();
        ApiListing second = new ApiListing();
        first.setPackageName("io.clientcore:first");
        second.setPackageName("io.clientcore:second");
        ReviewLine primitiveLine = new ReviewLine(second);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> rendering = executor.submit(() -> {
            visitType.invoke(new JavaASTAnalyser(first), pausedArray, new ReviewLine(first), TokenKind.TYPE_NAME);
            return null;
        });
        try {
            assertTrue(entered.await(5, TimeUnit.SECONDS));
            visitType.invoke(new JavaASTAnalyser(second), PrimitiveType.intType(), primitiveLine, TokenKind.TYPE_NAME);
            assertTrue(primitiveLine.getTokens().get(0).hasSuffixSpace(),
                "Another analyser's array rendering must not suppress the space after this primitive type.");
        } finally {
            release.countDown();
            try {
                rendering.get(5, TimeUnit.SECONDS);
            } finally {
                executor.shutdownNow();
            }
        }
    }

    @Test
    public void rendersServiceVersionReturnHintAfterMethodSignature(@TempDir Path tempDir) throws IOException {
        Path sourceFile = tempDir.resolve("TestServiceVersion.java");
        Files.write(sourceFile,
            String
                .join(System.lineSeparator(), "package com.azure.test;",
                    "public enum TestServiceVersion implements ServiceVersion {", "    V1;",
                    "    public static TestServiceVersion getLatest() {", "        return V1;", "    }", "}")
                .getBytes(StandardCharsets.UTF_8));

        ApiListing listing = new ApiListing();
        listing.setPackageName("io.clientcore:test");
        new JavaASTAnalyser(listing).analyse(Collections.singletonList(sourceFile));
        String markdown = MarkdownRenderer.render(listing);

        assertTrue(markdown.contains("public static TestServiceVersion getLatest() // returns V1"));
        assertFalse(markdown.contains("getLatest(// returns V1"));
    }
}

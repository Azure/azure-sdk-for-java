package com.azure.tools.apiview.processor.model.maven;

import com.azure.tools.apiview.processor.Main;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class FilenameParserTest {

    @ParameterizedTest
    @MethodSource("getValidFilenames")
    public void testValidFilenames(String filename, String artifactId, String packageVersion) {
        Map<String, String> filenameParts = Pom.parseFilename(filename);
        assertEquals(filenameParts.get("artifactId"), artifactId);
        assertEquals(filenameParts.get("version"), packageVersion);
    }

    @ParameterizedTest
    @MethodSource("getInvalidFilenames")
    public void testInvalidFilenames(String filename) {
        assertThrows(IllegalArgumentException.class, () -> Pom.parseFilename(filename));
    }

    private static Stream<Arguments> getValidFilenames() {
        return Stream.of(
                Arguments.of("azure-core-v2-1.0.0-sources.jar", "azure-core-v2", "1.0.0"),
                Arguments.of("azure-core-v2-1.0.0-beta.1-sources.jar", "azure-core-v2", "1.0.0-beta.1"),
                Arguments.of("azure-core-1.0.0-sources.jar", "azure-core", "1.0.0"),
                Arguments.of("azure-core-v500-1.0.0-sources.jar", "azure-core-v500", "1.0.0"),
                Arguments.of("azure-core-v2-1232-1.0.0-sources.jar", "azure-core-v2-1232", "1.0.0"),
                Arguments.of("azure-core-1.0.0-beta.1-sources.jar", "azure-core", "1.0.0-beta.1"),
                Arguments.of("azure-storage-blob-1.0.0-sources.jar", "azure-storage-blob", "1.0.0"),
                Arguments.of("azure-c1o1r1e-1.0.0-sources.jar", "azure-c1o1r1e", "1.0.0"),
                Arguments.of("azure-core-123.456.789-sources.jar", "azure-core", "123.456.789"),
                Arguments.of("azure-core-1.0.0-preview.1-sources.jar", "azure-core", "1.0.0-preview.1"),
                Arguments.of("azure-core-1.0.0-SNAPSHOT-sources.jar", "azure-core", "1.0.0-SNAPSHOT"),
                Arguments.of("azure-core-1.0.0-rc1-sources.jar", "azure-core", "1.0.0-rc1")
        );
    }

    private static Stream<String> getInvalidFilenames() {
        return Stream.of(
                "azure-core-v2-1.0-sources.jar",
                "1.0-sources.jar",
                "azure-core-v2-sources.jar",
                "azure-core-1.0.0.jar");
    }
}
package com.azure.openrewrite.migration;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.SourceSpecs;
import org.openrewrite.test.TypeValidation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.openrewrite.java.Assertions.java;

public class FullSampleMigrationTest implements RewriteTest {

    static final String GOLDEN_IMAGE = "v2";
    static final String ORIGINAL_IMAGE = "v1";

    static Stream<Path> sampleDirectories() throws IOException {
        List<Path> packageDirectories = packageDirectories().collect(Collectors.toList());
        List<Path> sampleDirectories = new ArrayList<>();
        for (Path packageDirectory : packageDirectories) {
            sampleDirectories.addAll(Files
                .list(packageDirectory)
                .filter(Files::isDirectory)
                .collect(Collectors.toList()));
        }
        return sampleDirectories.stream();
    }

    static Stream<Path> packageDirectories() throws IOException {
        return Files.list(Paths.get("src/test/resources/migrationExamples"))
            .filter(Files::isDirectory);
    }

    static Stream<Path> gatherAllRegularFiles(Path sampleDir) throws IOException {
        return Files.walk(sampleDir)
            .filter(Files::isRegularFile).collect(Collectors.toList()).stream();
    }

    @Override
    public void defaults(RecipeSpec spec) {
        spec.recipeFromResources("com.azure.openrewrite.migrateToVNext")
            .typeValidationOptions(TypeValidation.none());
    }


    @ParameterizedTest
    @MethodSource("sampleDirectories")
    public void testGoldenImage(Path sampleDir) throws Exception {

        Map<String, String> fileMap = new HashMap<String, String>();

        Path unmigratedDir = sampleDir.resolve(ORIGINAL_IMAGE);
        gatherAllRegularFiles(unmigratedDir).forEach(file -> {
            String unmigratedFileString = file.toString();
            String migratedFileString = unmigratedFileString.replace(ORIGINAL_IMAGE, GOLDEN_IMAGE);
            if (Files.exists(Paths.get(migratedFileString))) {
                fileMap.put(unmigratedFileString, migratedFileString);
            } else {
                throw new RuntimeException("Invalid Sample Migration Structure. Migrated file does not exist: " + migratedFileString);
            }
        });

        assertFullMigration(fileMap);
    }

    public void assertFullMigration(Map<String,String> fileMap) throws IOException {
        List<SourceSpecs> sourceSpecs = new ArrayList<SourceSpecs>();
        for (Map.Entry<String,String> entry : fileMap.entrySet()) {
            String before = Files.readAllLines(Paths.get(entry.getKey()))
                .stream()
                .reduce("", (a, b) -> a + b + "\n");

            String after = Files.readAllLines(Paths.get(entry.getValue()))
                .stream()
                .reduce("", (a, b) -> a + b + "\n");
            sourceSpecs.add(java(before, after));
        }
        rewriteRun(
            sourceSpecs.toArray(new SourceSpecs[sourceSpecs.size()])
        );
    }

}

package com.azure.openrewrite.migration;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.condition.DisabledIf;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.SourceSpecs;
import org.openrewrite.test.TypeValidation;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
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

@DisplayNameGeneration(CustomDisplayNameGenerator.class)
public class FullSampleMigrationTest implements RewriteTest {

    static final String GOLDEN_IMAGE = "v2";
    static final String ORIGINAL_IMAGE = "v1";
    static final String RECIPE_NAME = "com.azure.openrewrite.migrateToVNext";
    static final String[] DISABLED_DIRS = {
        "src/test/resources/migrationExamples/azure-storage-blob/"
    };

    static boolean isDisabledDir(Path dir) {
        for (String disabledDir : DISABLED_DIRS) {
            if (dir.startsWith(Paths.get(disabledDir))) {
                return true;
            }
        }
        return false;
    }

    static Stream<String> sampleDirectories() throws IOException {
        List<Path> packageDirectories = packageDirectories().collect(Collectors.toList());
        List<String> sampleDirectories = new ArrayList<>();
        for (Path packageDirectory : packageDirectories) {
            sampleDirectories.addAll(Files
                .list(packageDirectory)
                .filter(Files::isDirectory)
                .map(path -> path.toString())
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
        spec.recipeFromResources(RECIPE_NAME)
            .typeValidationOptions(TypeValidation.none());
    }

    @ParameterizedTest(name="{0}")
    @Execution(ExecutionMode.CONCURRENT)
    @MethodSource("sampleDirectories")
    public void testGoldenImage(String sampleDirString) throws Exception {
        System.out.printf("Sample: %s\nActive Recipe: %s", sampleDirString, RECIPE_NAME);

        Path sampleDir = Paths.get(sampleDirString);

        Assumptions.assumeFalse(isDisabledDir(sampleDir));
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

        assertFullMigration(fileMap, sampleDirString);
    }

    public void assertFullMigration(Map<String,String> fileMap, String name) throws IOException {
        List<SourceSpecs> sourceSpecs = new ArrayList<SourceSpecs>();
        for (Map.Entry<String,String> entry : fileMap.entrySet()) {

            String before = Files.readAllLines(Paths.get(entry.getKey()))
                .stream()
                .reduce("", (a, b) -> a + b + "\n");

            String after = Files.readAllLines(Paths.get(entry.getValue()))
                .stream()
                .reduce("", (a, b) -> a + b + "\n");
            if (!before.equals(after)) {
                sourceSpecs.add(java(before, after));
            }
        }
        if (sourceSpecs.isEmpty()) {
            Assumptions.abort("Migration samples are identical. No migration detected.");
        }

        try  {
            rewriteRun(
                sourceSpecs.toArray(new SourceSpecs[sourceSpecs.size()])
            );
        } catch (AssertionError e) {
            String message = e.getMessage();
            throw new AssertionError("Migration failed for sample directory: " + name + "\n" + e.getLocalizedMessage());
        }

    }
}

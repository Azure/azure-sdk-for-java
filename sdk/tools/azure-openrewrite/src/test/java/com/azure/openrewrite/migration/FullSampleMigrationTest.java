package com.azure.openrewrite.migration;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.openrewrite.test.RecipeSpec;
import org.openrewrite.test.RewriteTest;
import org.openrewrite.test.TypeValidation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

import static org.openrewrite.java.Assertions.java;

public class FullSampleMigrationTest extends FullMigrationTestBase {

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
        //runTransformationScript(inputDir, getTempDir());
        //compareWithGoldenImages(getTempDir(), goldenImagesDir);
    }

    @ParameterizedTest
    @MethodSource("sampleDirectories")
    void testMigrationOfFullSample(Path migrationExamplesDir) throws IOException {
        String unmigratedExample = Files.readAllLines(migrationExamplesDir
            .resolve("SimpleBlobClientUsage.java"))
            .stream()
            .reduce("", (a, b) -> a + b + "\n");
        String migratedExample = Files.readAllLines(migrationExamplesDir
            .resolve("SimpleBlobClientUsage.java"))
            .stream()
            .reduce("", (a, b) -> a + b + "\n");
        rewriteRun(
            spec -> spec.typeValidationOptions(TypeValidation.none()),
            java(unmigratedExample, migratedExample)
        );
    }

}

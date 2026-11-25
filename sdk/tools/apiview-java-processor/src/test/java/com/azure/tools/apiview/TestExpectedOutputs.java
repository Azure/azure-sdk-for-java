// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.apiview;

import com.azure.tools.apiview.processor.Main;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.*;
import org.junit.jupiter.params.provider.*;

import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.io.*;
import java.util.stream.Stream;
import java.util.zip.ZipError;

import static org.junit.jupiter.api.Assertions.*;

public class TestExpectedOutputs {
    // This is a flag to add a '-DELETE' suffix to the generated output file if the expected output file is missing.
    // By default we do, but if we want to regenerate a bunch of expected outputs, we can see this to false to save
    // having to rename.
    private static final boolean ADD_DELETE_PREFIX_FOR_MISSING_EXPECTED_OUTPUTS = true;

    private static final String ROOT = "src/test/resources/";
    private static final File TEMP_DIR = new File(ROOT + "temp");

    private static Stream<String> provideFileNames() {
        // This is a stream of local files or URLs to download the file from (or a combination of both). For each value,
        // if it starts with http, it'll try downloading the file into a temp location. If it sees no http, it'll look
        // for the file in the inputs directory. To prevent clogging up the repository with source jars, it is
        // preferable to download the source jars from known-good places.
        return Stream.of(""
        //            "https://repo1.maven.org/maven2/com/azure/azure-core/1.48.0/azure-core-1.48.0-sources.jar"
        //                "https://repo1.maven.org/maven2/com/azure/azure-communication-chat/1.5.0/azure-communication-chat-1.5.0-sources.jar",
        //                "https://repo1.maven.org/maven2/com/azure/azure-security-keyvault-keys/4.8.2/azure-security-keyvault-keys-4.8.2-sources.jar",
        //                "https://repo1.maven.org/maven2/com/azure/azure-data-appconfiguration/1.6.0/azure-data-appconfiguration-1.6.0-sources.jar",
        //                "https://repo1.maven.org/maven2/com/azure/azure-messaging-eventhubs/5.18.2/azure-messaging-eventhubs-5.18.2-sources.jar",
        //                "https://repo1.maven.org/maven2/com/azure/azure-messaging-servicebus/7.15.2/azure-messaging-servicebus-7.15.2-sources.jar",
        //                "https://repo1.maven.org/maven2/com/azure/azure-identity/1.12.0/azure-identity-1.12.0-sources.jar",
        //                "https://repo1.maven.org/maven2/com/azure/azure-storage-blob/12.25.3/azure-storage-blob-12.25.3-sources.jar",
        //                "https://repo1.maven.org/maven2/com/azure/azure-cosmos/4.57.0/azure-cosmos-4.57.0-sources.jar"
        );
    }

    @BeforeAll
    public static void setup() {
        TEMP_DIR.mkdirs();
    }

    @ParameterizedTest
    @ValueSource(strings = { "json", "markdown" })
    public void testCorruptSourcesJarPreservesOutput(String renderer, @TempDir Path directory) throws IOException {
        Path sourcesJar = directory.resolve("library-1.0.0-sources.jar");
        Files.write(sourcesJar, "not a JAR".getBytes(StandardCharsets.UTF_8));
        Path outputDirectory = Files.createDirectory(directory.resolve("output"));
        Path output = outputDirectory.resolve("library-1.0.0-sources." + ("markdown".equals(renderer) ? "md" : "json"));
        byte[] previousOutput = "previous API snapshot".getBytes(StandardCharsets.UTF_8);
        Files.write(output, previousOutput);

        Throwable failure = assertThrows(Throwable.class, () -> Main
            .main(new String[] { "--renderer=" + renderer, sourcesJar.toString(), outputDirectory.toString() }));
        // Java 8's ZIP filesystem throws ZipError for malformed archives.
        assertTrue(failure instanceof IllegalStateException || failure instanceof ZipError, failure::toString);

        assertArrayEquals(previousOutput, Files.readAllBytes(output));
    }

    @ParameterizedTest
    @MethodSource("provideFileNames")
    public void testGeneratedJson(String filenameOrUrl) {
        if (filenameOrUrl.isEmpty()) {
            return;
        }

        String filename;
        Path inputFile;

        if (filenameOrUrl.startsWith("http")) {
            // download the file if it isn't local...
            filename = filenameOrUrl.substring(filenameOrUrl.lastIndexOf('/') + 1);

            // download the file and save it to the temp directory
            String destinationFile = TEMP_DIR + "/" + filename;
            downloadFile(filenameOrUrl, destinationFile);

            // strip off the file extension
            filename = filename.substring(0, filename.lastIndexOf('.'));
            inputFile = Paths.get(destinationFile);
        } else {
            // the file is local, so we can go straight to it
            filename = filenameOrUrl;
            inputFile = Paths.get(ROOT + "inputs/" + filename + ".jar");
        }

        final Path expectedOutputFile = Paths.get(ROOT + "expected-outputs/" + filename + ".json");

        // Run the processor, receiving the name of the JSON file that was generated (we ignore the gzipped value in index 1)
        final Path actualOutputFile = Main.run(inputFile.toFile(), TEMP_DIR)[0].toPath();

        if (expectedOutputFile.toFile().exists()) {
            try {
                // Compare the temporary file to the expected-outputs file
                assertArrayEquals(Files.readAllBytes(expectedOutputFile), Files.readAllBytes(actualOutputFile));
            } catch (IOException e) {
                fail("Failed to compare the actual output file with the expected output file.", e);
            }
        } else {
            // the test was never going to pass as we don't have an expected file to compare against.
            try {
                final String outputFile = ROOT + "expected-outputs/" + filename
                    + (ADD_DELETE_PREFIX_FOR_MISSING_EXPECTED_OUTPUTS ? "-DELETE" : "") + ".json";
                final Path suggestedOutputFile = Paths.get(outputFile);

                if (!suggestedOutputFile.toFile().getParentFile().exists()) {
                    suggestedOutputFile.toFile().getParentFile().mkdirs();
                }
                Files.move(actualOutputFile, suggestedOutputFile, StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException e) {
                // unable to move file
                e.printStackTrace();
            }
            if (ADD_DELETE_PREFIX_FOR_MISSING_EXPECTED_OUTPUTS) {
                fail("Could not find expected output file for test. The output from the sources.jar was added into the "
                    + "/expected-outputs directory, but with a '-DELETE' filename suffix. Please review and rename if it "
                    + "should be included in the test suite.");
            } else {
                fail("Could not find expected output file for test. The output from the sources.jar was added into the "
                    + "/expected-outputs directory without any -DELETE suffix. It will be used next time the test is run!");

            }
        }
    }

    @AfterAll
    public static void tearDown() throws IOException {
        // Delete the temporary file
        if (TEMP_DIR.exists()) {
            // delete everything in TEMP_DIR and then delete the directory
            Files.walk(TEMP_DIR.toPath()).map(Path::toFile).forEach(File::delete);

            try {
                Files.delete(TEMP_DIR.toPath());
            } catch (NoSuchFileException e) {
                // ignore
            }
        }
    }

    private static void downloadFile(String urlStr, String destinationFile) {
        File file = new File(destinationFile);
        if (file.exists()) {
            return;
        }
        try (InputStream in = new URL(urlStr).openStream()) {
            Files.copy(in, file.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            fail("Failed to download and / or copy the given URL to the given destination { url: " + urlStr
                + ", destination: " + destinationFile + " }");
        }
    }
}

// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Fail.fail;

/**
 * Ratchet that stops new tests from creating databases outside the sanctioned helpers.
 * <p>
 * Databases created with an arbitrary id cannot be attributed to a run, so when a CI job is
 * cancelled or times out they leak permanently on the long lived shared accounts. Tests must use
 * {@code TestSuiteBase.createTestDatabase(...)} or {@link CosmosDatabaseForTest#generateId(String)}
 * so cleanup can find them.
 * <p>
 * The check is a ratchet rather than a hard ban: {@code test-resource-hygiene-baseline.properties}
 * records the violations that existed when this was introduced. Adding a violation to a file - or
 * introducing a new offending file - fails. Removing violations and lowering the baseline is always
 * welcome.
 */
public class TestResourceHygieneTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestResourceHygieneTest.class);
    private static final String BASELINE_RESOURCE = "test-resource-hygiene-baseline.properties";
    private static final Path TEST_SOURCE_ROOT = Paths.get("src", "test", "java");

    /**
     * Files that own the sanctioned helpers, or whose entire purpose is exercising the database
     * management APIs. They are not scanned at all.
     */
    private static final List<String> EXCLUDED_FILES = java.util.Arrays.asList(
        "com/azure/cosmos/rx/TestSuiteBase.java",
        "com/azure/cosmos/CosmosDatabaseForTest.java",
        "com/azure/cosmos/CosmosTestResourceJanitor.java",
        "com/azure/cosmos/CosmosTestAccountJanitor.java",
        "com/azure/cosmos/CosmosTestResourceRegistry.java",
        "com/azure/cosmos/TestResourceHygieneTest.java");

    /**
     * A method declaration: optional modifiers, a return type, a name, a parameter list, and an opening
     * brace at end of line. The modifiers are optional so package-private declarations are recognized
     * too; what actually distinguishes a declaration from a call is the {@code <type> <name>(} shape,
     * which a call ({@code client.createDatabase(} or {@code = createDatabase(}) never has.
     */
    private static final Pattern METHOD_DECLARATION = Pattern.compile(
        "^(?:(?:public|protected|private|static|final|abstract|default|synchronized)\\s+)*"
            + "[\\w.<>\\[\\],\\s]+\\s+\\w+\\s*\\([^;]*\\)\\s*\\{$");

    private static final Pattern DATABASE_CREATION = Pattern.compile(
        "(?:\\.\\s*createDatabase(?:IfNotExists)?\\s*\\()"
            + "|(?:(?<![\\w.])create(?:Sync)?Database(?:IfNotExists)?\\s*\\()");

    @Test(groups = {"unit"})
    public void newTestsMustUseSanctionedDatabaseHelpers() throws IOException {
        // Failing rather than skipping: a silent skip would turn the ratchet into a permanent no-op if the
        // working directory ever changed, and nobody would notice.
        assertThat(Files.isDirectory(TEST_SOURCE_ROOT))
            .withFailMessage("Test source root %s not found - run this test from the module directory",
                TEST_SOURCE_ROOT.toAbsolutePath())
            .isTrue();

        ScanResult scanResult = scan();
        assertThat(scanResult.filesScanned)
            .withFailMessage("Scanned %d files - the hygiene scan is not looking at the test sources",
                scanResult.filesScanned)
            .isGreaterThan(100);

        Map<String, Integer> actual = scanResult.violations;
        Map<String, Integer> baseline = loadBaseline();

        List<String> regressions = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : actual.entrySet()) {
            int allowed = baseline.getOrDefault(entry.getKey(), 0);
            if (entry.getValue() > allowed) {
                regressions.add(String.format(
                    "%s: %d direct database creation call(s), baseline allows %d",
                    entry.getKey(), entry.getValue(), allowed));
            }
        }

        if (!regressions.isEmpty()) {
            StringBuilder message = new StringBuilder()
                .append("New direct database creation detected. Use TestSuiteBase.createTestDatabase(...) so the")
                .append(" database id carries the run id and CI cleanup can delete it - see")
                .append(" sdk/cosmos/AGENTS.md.");
            for (String regression : regressions) {
                message.append(System.lineSeparator()).append("  - ").append(regression);
            }

            fail(message.toString());
        }

        List<String> staleBaselineEntries = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : baseline.entrySet()) {
            int current = actual.getOrDefault(entry.getKey(), 0);
            if (current < entry.getValue()) {
                staleBaselineEntries.add(String.format(
                    "%s: baseline allows %d but only %d remain", entry.getKey(), entry.getValue(), current));
            }
        }

        // Not a failure - lowering the baseline is a manual follow up, but surface it so the ratchet
        // actually tightens over time instead of drifting.
        if (!staleBaselineEntries.isEmpty()) {
            LOGGER.warn("Test resource hygiene baseline can be lowered:{}{}",
                System.lineSeparator(),
                String.join(System.lineSeparator(), staleBaselineEntries));
        }
    }

    /**
     * Guards the guard: if the regex stops matching, the ratchet silently passes forever.
     */
    @Test(groups = {"unit"})
    public void scannerDetectsDirectDatabaseCreation() {
        assertThat(countViolations("client.createDatabase(props).block();")).isEqualTo(1);
        assertThat(countViolations("client.createDatabaseIfNotExists(dbId).block();")).isEqualTo(1);
        assertThat(countViolations("database = createDatabase(client, dbId);")).isEqualTo(1);
        assertThat(countViolations("db = createSyncDatabase(client, dbId);")).isEqualTo(1);

        assertThat(countViolations("database = createTestDatabase(client);")).isZero();
        assertThat(countViolations("String id = CosmosDatabaseForTest.generateId(\"x\");")).isZero();
        assertThat(countViolations("// client.createDatabase(props) is not allowed")).isZero();
        // A "//" inside a string literal must not hide a violation later on the line.
        assertThat(countViolations("log(\"see http://x\"); client.createDatabase(props);")).isEqualTo(1);
        assertThat(countViolations("container = createCollection(database, def, options);")).isZero();

        // Declarations and interface implementations define helpers rather than call them.
        assertThat(countViolations(
            "public Mono<CosmosDatabaseResponse> createDatabase(CosmosDatabaseProperties def) {")).isZero();
        assertThat(countViolations(
            "static protected CosmosAsyncDatabase createDatabase(CosmosAsyncClient c, String id) {")).isZero();
        // Package-private declarations have no modifier at all.
        assertThat(countViolations(
            "CosmosAsyncDatabase createDatabase(CosmosAsyncClient c, String id) {")).isZero();
        // ... but a call that merely happens to sit on a line ending in "{" is still counted.
        assertThat(countViolations("if (x) { client.createDatabase(props); }")).isEqualTo(1);
    }

    private static ScanResult scan() throws IOException {
        Map<String, Integer> violations = new TreeMap<>();
        int filesScanned = 0;
        try (Stream<Path> files = Files.walk(TEST_SOURCE_ROOT)) {
            List<Path> javaFiles = files
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().endsWith(".java"))
                .collect(java.util.stream.Collectors.toList());

            for (Path file : javaFiles) {
                String relative = normalize(TEST_SOURCE_ROOT.relativize(file).toString());
                if (EXCLUDED_FILES.contains(relative)) {
                    continue;
                }

                filesScanned++;
                int count = 0;
                for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                    count += countViolations(line);
                }

                if (count > 0) {
                    violations.put(relative, count);
                }
            }
        }

        return new ScanResult(violations, filesScanned);
    }

    private static final class ScanResult {
        private final Map<String, Integer> violations;
        private final int filesScanned;

        private ScanResult(Map<String, Integer> violations, int filesScanned) {
            this.violations = violations;
            this.filesScanned = filesScanned;
        }
    }

    private static int countViolations(String line) {
        String code = stripComment(line);
        if (code.isEmpty()) {
            return 0;
        }

        // Skip method declarations - those define or implement a helper rather than call one. A call site
        // never both opens a body and ends the line with ") {".
        if (METHOD_DECLARATION.matcher(code).matches()) {
            return 0;
        }

        int count = 0;
        Matcher matcher = DATABASE_CREATION.matcher(code);
        while (matcher.find()) {
            count++;
        }

        return count;
    }

    private static String stripComment(String line) {
        String trimmed = line.trim();
        if (trimmed.startsWith("//") || trimmed.startsWith("*") || trimmed.startsWith("/*")) {
            return "";
        }

        // Only strip a trailing comment when the "//" is not inside a string literal, otherwise the rest of
        // the line - which may contain a real violation - is silently dropped.
        boolean inString = false;
        for (int i = 0; i < trimmed.length() - 1; i++) {
            char c = trimmed.charAt(i);
            if (c == '\\') {
                i++;
            } else if (c == '"') {
                inString = !inString;
            } else if (!inString && c == '/' && trimmed.charAt(i + 1) == '/') {
                return trimmed.substring(0, i);
            }
        }

        return trimmed;
    }

    private static String normalize(String path) {
        return path.replace('\\', '/');
    }

    private static Map<String, Integer> loadBaseline() throws IOException {
        Properties properties = new Properties();
        try (InputStream stream =
                 TestResourceHygieneTest.class.getClassLoader().getResourceAsStream(BASELINE_RESOURCE)) {

            if (stream == null) {
                throw new IOException("Missing baseline resource " + BASELINE_RESOURCE);
            }

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                properties.load(reader);
            }
        }

        Map<String, Integer> baseline = new TreeMap<>();
        for (String name : properties.stringPropertyNames()) {
            baseline.put(normalize(name), Integer.parseInt(properties.getProperty(name).trim()));
        }

        return baseline;
    }
}

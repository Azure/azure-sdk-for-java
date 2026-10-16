// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.linting.extensions.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.ConfigurationLoader;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.PropertiesExpander;
import com.puppycrawl.tools.checkstyle.TreeWalker;
import com.puppycrawl.tools.checkstyle.XMLLogger;
import com.puppycrawl.tools.checkstyle.api.AuditEvent;
import com.puppycrawl.tools.checkstyle.api.AuditListener;
import com.puppycrawl.tools.checkstyle.api.AutomaticBean;
import com.puppycrawl.tools.checkstyle.api.Configuration;
import com.puppycrawl.tools.checkstyle.api.SeverityLevel;
import com.puppycrawl.tools.checkstyle.filters.SuppressionFilter;
import io.clientcore.linting.extensions.checkstyle.filters.SdkCheckFilter;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsolidatedChecksTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void publicApiPoliciesShareImportAndSignatureAnalysis() throws Exception {
        DefaultConfiguration check = check(PublicApiTypeCheck.class);
        check.addProperty("allowedDependencyPrefixes", "com.azure.");
        List<AuditEvent> events = inspect(configuration(false, check),
            source("Api.java", "package com.azure;", "import com.azure.implementation.Internal;",
                "import org.example.External;", "import java.util.List;",
                "public class Api<A extends Internal> extends Internal {",
                "    public Internal getInternal() { return null; }",
                "    public List<List<External>> getExternal(List<External> value) { return null; }",
                "    private External hidden() { return null; }", "}"));
        assertEquals(3, countMessagesContaining(events, "is in an implementation package"));
        assertEquals(2, countMessagesContaining(events, "external dependency"));
        assertEquals(5, events.size());
        assertTrue(events.stream().allMatch(event -> PublicApiTypeCheck.class.getName().equals(event.getSourceName())));
        assertTrue(events.stream().allMatch(event -> event.getModuleId() == null));
    }

    @Test
    void publicApiScopeDoesNotLeakOutOfNestedClasses() throws Exception {
        DefaultConfiguration check = check(PublicApiTypeCheck.class);
        check.addProperty("allowedDependencyPrefixes", "com.azure.");
        List<AuditEvent> events = inspect(configuration(false, check),
            source("Api.java", "package com.azure;", "import org.example.External;", "public class Api {",
                "    private class Hidden { public External hidden() { return null; } }",
                "    public External visible() { return null; }", "}"));
        assertEquals(1, events.size());
        assertEquals(PublicApiTypeCheck.class.getName(), events.get(0).getSourceName());
        assertNull(events.get(0).getModuleId());
    }

    @Test
    void publicApiImportsAreResetBetweenFiles() throws Exception {
        DefaultConfiguration check = check(PublicApiTypeCheck.class);
        check.addProperty("allowedDependencyPrefixes", "com.azure.");
        List<AuditEvent> events = inspect(configuration(false, check),
            source("First.java", "package com.azure;", "import org.example.External;",
                "public class First { public External get() { return null; } }"),
            source("Second.java", "package com.azure;",
                "public class Second { public External get() { return null; } }"));
        assertEquals(1, events.size());
        assertTrue(events.get(0).getFileName().endsWith("First.java"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "clientcore", "azure-core" })
    void exceptionPoliciesRetainTheirLoggingGeneration(String loggingApi) throws Exception {
        DefaultConfiguration check = check(ExceptionHandlingCheck.class);
        check.addProperty("loggingApi", loggingApi);
        List<AuditEvent> events = inspect(configuration(false, check), source("Exceptions.java",
            "public class Exceptions {", "    public void run() {",
            "        try { work(); } catch (Exception ex) { throw new RuntimeException(\"lost\"); }",
            "        logger.throwableAtWarning().addKeyValue(\"key\", \"value\").log(\"lost\", RuntimeException::new);",
            "    }", "}"));
        assertEquals(1,
            countMessagesContaining(events, String.format(ExceptionHandlingCheck.UNUSED_CAUGHT_EXCEPTION_ERROR, "ex")));
        assertTrue(
            events.stream().allMatch(event -> ExceptionHandlingCheck.class.getName().equals(event.getSourceName())));
        assertTrue(events.stream().allMatch(event -> event.getModuleId() == null));
        if ("clientcore".equals(loggingApi)) {
            assertEquals(1, countMessagesContaining(events, ExceptionHandlingCheck.RAW_EXCEPTION_ERROR));
            assertEquals(1,
                countMessagesContaining(events, ExceptionHandlingCheck.CREATED_EXCEPTION_NOT_THROWN_MESSAGE));
            assertEquals(0, countMessagesContaining(events, ExceptionHandlingCheck.THROW_LOGGER_EXCEPTION_MESSAGE));
            assertEquals(3, events.size());
        } else {
            assertEquals(1, countMessagesContaining(events, ExceptionHandlingCheck.THROW_LOGGER_EXCEPTION_MESSAGE));
            assertEquals(0, countMessagesContaining(events, ExceptionHandlingCheck.RAW_EXCEPTION_ERROR));
            assertEquals(0,
                countMessagesContaining(events, ExceptionHandlingCheck.CREATED_EXCEPTION_NOT_THROWN_MESSAGE));
            assertEquals(2, events.size());
        }
    }

    @Test
    void clientCoreValidationExceptionsAndThrownLoggingChainsAreAllowed() throws Exception {
        List<AuditEvent> events = inspect(configuration(false, check(ExceptionHandlingCheck.class)),
            source("Exceptions.java", "public class Exceptions {",
                "    public void first() { throw new IllegalArgumentException(); }",
                "    public void second() { throw new IllegalStateException(); }",
                "    public void third() { throw new NullPointerException(); }",
                "    public void fourth() { throw new UnsupportedOperationException(); }",
                "    public void logged() { throw (RuntimeException) logger.throwableAtError()",
                "        .addKeyValue(\"key\", \"value\").log(\"message\", RuntimeException::new); }", "}"));
        assertTrue(events.isEmpty());
    }

    @Test
    void azureCoreStaticAndConstructorExemptionsDoNotLeak() throws Exception {
        DefaultConfiguration check = check(ExceptionHandlingCheck.class);
        check.addProperty("loggingApi", "azure-core");
        List<AuditEvent> events = inspect(configuration(false, check),
            source("Exceptions.java", "public class Exceptions {", "    Exceptions() { throw new RuntimeException(); }",
                "    static void skip() { throw new RuntimeException(); }",
                "    static class Nested { void skip() { throw new RuntimeException(); } }",
                "    void checked() { throw new RuntimeException(); }",
                "    void logged() { throw logger.logExceptionAsError(new RuntimeException()); }", "}"));
        assertEquals(1, events.size());
        assertEquals(ExceptionHandlingCheck.class.getName(), events.get(0).getSourceName());
        assertEquals(5, events.get(0).getLine());
    }

    @Test
    void causeAliasesAndNestedScopesRemainIndependent() throws Exception {
        DefaultConfiguration check = check(ExceptionHandlingCheck.class);
        check.addProperty("checkThrownExceptions", "false");
        List<AuditEvent> events = inspect(configuration(false, check),
            source("Exceptions.java", "public class Exceptions {", "    void run(boolean flag) {",
                "        try { work(); } catch (Exception outer) {",
                "            class Nested { void run() { throw new RuntimeException(); } }",
                "            try { work(); } catch (Exception inner) {",
                "                throw new RuntimeException(inner, outer);", "            }",
                "            Throwable wrapped = new RuntimeException(outer);",
                "            if (flag) { throw wrapped; }", "            throw new RuntimeException();", "        }",
                "    }", "}"));
        assertEquals(1, events.size());
        assertEquals(10, events.get(0).getLine());
        assertEquals(ExceptionHandlingCheck.class.getName(), events.get(0).getSourceName());
    }

    @Test
    void outerCauseAliasesAssignedInsideNestedCatchesAreRetained() throws Exception {
        DefaultConfiguration check = check(ExceptionHandlingCheck.class);
        check.addProperty("checkThrownExceptions", "false");
        List<AuditEvent> events = inspect(configuration(false, check),
            source("Exceptions.java", "public class Exceptions { void run() {",
                "    try { work(); } catch (Exception outer) {", "        RuntimeException wrapped;",
                "        try { throw new RuntimeException(outer); } catch (Exception inner) {",
                "            wrapped = new RuntimeException(outer);", "        }", "        throw wrapped;", "    }",
                "} }"));
        assertTrue(events.isEmpty());
    }

    @Test
    void manyThrowsDoNotGrowTheCatchAliasCollection() throws Exception {
        DefaultConfiguration check = check(ExceptionHandlingCheck.class);
        check.addProperty("checkThrownExceptions", "false");
        StringBuilder text = new StringBuilder(
            "public class Exceptions { void run(boolean flag) { " + "try { work(); } catch (Exception ex) {\n");
        for (int i = 0; i < 512; i++) {
            text.append("if (flag) { throw new RuntimeException(); }\n");
        }
        text.append("} } }");
        List<AuditEvent> events = inspect(configuration(false, check), source("Exceptions.java", text.toString()));
        assertEquals(512, events.size());
        assertEquals(512,
            countMessagesContaining(events, String.format(ExceptionHandlingCheck.UNUSED_CAUGHT_EXCEPTION_ERROR, "ex")));
    }

    @Test
    void formattedMessagesAreCheckedAfterIntermediateLoggingCalls() throws Exception {
        List<AuditEvent> events = inspect(configuration(false, check(StringFormattedExceptionMessageCheck.class)),
            source("Exceptions.java", "public class Exceptions { void run() {",
                "    throw logger.throwableAtError().addKeyValue(\"key\", \"value\")",
                "        .log(String.format(\"message %s\", value), RuntimeException::new);", "} }"));
        assertEquals(1, events.size());
        assertEquals(3, events.get(0).getLine());
    }

    @Test
    void unrelatedLogArgumentsDoNotBecomeThrowableLoggingChains() throws Exception {
        List<AuditEvent> events = inspect(
            configuration(false, check(ExceptionHandlingCheck.class),
                check(StringFormattedExceptionMessageCheck.class)),
            source("Other.java", "public class Other { void run() {",
                "    other.log(logger.throwableAtError(), String.format(\"value %s\", value));",
                "    other.throwableAtError();", "} }"));
        assertTrue(events.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(
        strings = { "PublicApiTypeCheck", "ExceptionHandlingCheck", "PublicApiTypeCheck|ExceptionHandlingCheck" })
    void ruleNameSuppressionsApplyToMergedRules(String checks) throws Exception {
        Path suppressionFile = temporaryDirectory.resolve("suppressions.xml");
        Files.write(suppressionFile,
            Arrays.asList("<?xml version=\"1.0\"?>",
                "<!DOCTYPE suppressions PUBLIC \"-//Checkstyle//DTD SuppressionFilter Configuration 1.2//EN\" "
                    + "\"https://checkstyle.org/dtds/suppressions_1_2.dtd\">",
                "<suppressions>", "<suppress checks=\"" + checks + "\" files=\".*\"/>", "</suppressions>"),
            StandardCharsets.UTF_8);
        DefaultConfiguration configuration
            = configuration(false, check(PublicApiTypeCheck.class), check(ExceptionHandlingCheck.class));
        DefaultConfiguration filter = check(SuppressionFilter.class);
        filter.addProperty("file", suppressionFile.toString());
        configuration.addChild(filter);
        List<AuditEvent> events = inspect(configuration,
            source("Policies.java", "package com.azure;", "import org.example.implementation.Internal;",
                "public class Policies {", "    public Internal get() { return null; }",
                "    void run() { try { work(); } catch (Exception ex) { throw new RuntimeException(); } }", "}"));
        assertEquals(checks.contains("PublicApiTypeCheck") ? 0 : 2,
            events.stream().filter(event -> PublicApiTypeCheck.class.getName().equals(event.getSourceName())).count());
        assertEquals(checks.contains("ExceptionHandlingCheck") ? 0 : 2,
            events.stream()
                .filter(event -> ExceptionHandlingCheck.class.getName().equals(event.getSourceName()))
                .count());
    }

    @Test
    void implementationPathFilteringAppliesToMergedApiRule() throws Exception {
        List<AuditEvent> events = inspect(configuration(true, check(PublicApiTypeCheck.class)),
            source("src/main/java/com/azure/implementation/Api.java", "package com.azure;",
                "import org.example.implementation.Internal;",
                "public class Api { public Internal get() { return null; } }"));
        assertTrue(events.isEmpty());
    }

    @Test
    void xmlReportsContainTheMergedRuleName() throws Exception {
        DefaultConfiguration check = check(PublicApiTypeCheck.class);
        check.addProperty("checkExternalDependencies", "false");
        List<AuditEvent> events = inspect(configuration(false, check),
            source("Api.java", "package com.azure;", "import com.azure.implementation.Internal;",
                "public class Api { public Internal get() { return null; } }"));
        assertEquals(1, events.size());
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        XMLLogger logger = new XMLLogger(output, AutomaticBean.OutputStreamOptions.NONE);
        AuditEvent event = events.get(0);
        logger.auditStarted(event);
        logger.fileStarted(event);
        logger.addError(event);
        logger.fileFinished(event);
        logger.auditFinished(event);
        String xml = new String(output.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(xml.contains("source=\"" + PublicApiTypeCheck.class.getName() + "\""));
        assertNull(event.getModuleId());
    }

    @Test
    void diagnosticsRetainColumnsSeverityExplicitIdsAndCustomMessages() throws Exception {
        DefaultConfiguration check = check(PublicApiTypeCheck.class);
        check.addProperty("checkExternalDependencies", "false");
        check.addProperty("severity", "warning");
        check.addProperty("id", "public-types");
        String message = String.format(PublicApiTypeCheck.RETURN_TYPE_ERROR, "Internal");
        check.addMessage(message, "custom diagnostic");
        List<AuditEvent> events = inspect(configuration(false, check),
            source("Api.java", "package com.azure;", "import com.azure.implementation.Internal;", "public class Api {",
                "\tpublic Internal get() { return null; }", "}"));
        assertEquals(1, events.size());
        AuditEvent event = events.get(0);
        assertEquals(16, event.getColumn());
        assertEquals(SeverityLevel.WARNING, event.getSeverityLevel());
        assertEquals("public-types", event.getModuleId());
        assertEquals("custom diagnostic", event.getMessage());
        assertEquals(PublicApiTypeCheck.class.getName(), event.getSourceName());
    }

    @ParameterizedTest
    @ValueSource(strings = { "test", "test-shared", "samples" })
    void inapplicableProductionPoliciesAreSkippedBeforeReporting(String directory) throws Exception {
        DefaultConfiguration logging = check(GoodLoggingCheck.class);
        logging.addProperty("fullyQualifiedLoggerName", "com.azure.core.util.logging.ClientLogger");
        logging.addProperty("simpleClassName", "ClientLogger");
        logging.addProperty("loggerName", "logger");
        List<AuditEvent> events = inspect(
            configuration(false, check(PublicApiTypeCheck.class), check(ExceptionHandlingCheck.class),
                check(ServiceClientCheck.class), check(ServiceClientBuilderCheck.class),
                check(ServiceInterfaceCheck.class), check(ImmutableClassCheck.class),
                check(EnforceFinalFieldsCheck.class), check(SerializableMethodsCheck.class),
                check(HttpPipelinePolicyCheck.class), logging),
            source("src/" + directory + "/java/Api.java", "package com.azure;", "import org.example.External;",
                "@Immutable @ServiceClient public class Api {", "    public int value = 1;",
                "    public External get() { return null; }",
                "    public void setValue(int value) { this.value = value; }",
                "    void run() { System.out.println(\"ignored\"); throw new RuntimeException(); }", "}",
                "class WrongClientBuilder {}", "@ServiceInterface(name = \"\") interface Service {}",
                "class Model implements JsonSerializable<Model> {}", "class Policy implements HttpPipelinePolicy {}"));
        assertTrue(events.isEmpty());
    }

    @Test
    void stepVerifierDiagnosticsSurviveTheConfiguredSdkFilter() throws Exception {
        List<AuditEvent> events = inspect(configuration(true, check(StepVerifierCheck.class)),
            source("src/test/java/MyTest.java", "public class MyTest { void test() {",
                "    StepVerifier.setDefaultTimeout(Duration.ofSeconds(1));", "} }"));
        assertEquals(1, events.size());
        assertEquals(StepVerifierCheck.class.getName(), events.get(0).getSourceName());
    }

    @Test
    void stepVerifierImportsAccumulateWithinAFileButResetBetweenFiles() throws Exception {
        List<AuditEvent> events = inspect(configuration(false, check(StepVerifierCheck.class)),
            source("First.java", "import static reactor.test.StepVerifier.setDefaultTimeout;",
                "import static java.util.Collections.emptyList;", "public class First { void test() {",
                "    setDefaultTimeout(Duration.ofSeconds(1));", "} }"),
            source("Second.java", "public class Second { void test() {",
                "    setDefaultTimeout(Duration.ofSeconds(1));", "} }"));
        assertEquals(1, events.size());
        assertTrue(events.get(0).getFileName().endsWith("First.java"));
    }

    @Test
    void loggingApiMustBeExplicitlySupported() {
        ExceptionHandlingCheck check = new ExceptionHandlingCheck();
        assertThrows(IllegalArgumentException.class, () -> check.setLoggingApi("other"));
        assertThrows(IllegalArgumentException.class, () -> check.setLoggingApi(null));
        assertFalse(SdkCheckFilter.isSampleOrTestFile(null));
        assertTrue(SdkCheckFilter.isSampleOrTestFile("C:\\repo\\src\\test\\java\\Test.java"));
    }

    @Test
    void synchronousServiceSignaturesAcceptAllSupportedContextParameters() throws Exception {
        List<AuditEvent> events = inspect(configuration(false, check(ServiceClientCheck.class)),
            source("ApiClient.java", "@ServiceClient public class ApiClient {",
                "    @ServiceMethod(returns = ReturnType.SINGLE)",
                "    public Response<String> firstWithResponse(Context context) { return null; }",
                "    @ServiceMethod(returns = ReturnType.SINGLE)",
                "    public Response<String> secondWithResponse(RequestOptions options) { return null; }",
                "    @ServiceMethod(returns = ReturnType.SINGLE)",
                "    public Response<String> thirdWithResponse(RequestContext context) { return null; }",
                "    @ServiceMethod(returns = ReturnType.SINGLE)",
                "    public Response<String> missingWithResponse() { return null; }", "}"));
        assertEquals(1, events.size());
        assertEquals(8, events.get(0).getLine());
        assertTrue(events.get(0).getMessage().contains("must have"));
    }

    @Test
    void asynchronousServiceSignaturesRejectContextEvenWhenAnOptionsBagIsPresent() throws Exception {
        List<AuditEvent> events = inspect(configuration(false, check(ServiceClientCheck.class)),
            source("ApiAsyncClient.java", "@ServiceClient(isAsync = true) public class ApiAsyncClient {",
                "    @ServiceMethod(returns = ReturnType.SINGLE)",
                "    public Mono<String> first(Context context, RequestOptions options) { return null; }",
                "    @ServiceMethod(returns = ReturnType.SINGLE)",
                "    public Mono<Response<String>> secondWithResponse(Context context) { return null; }",
                "    @ServiceMethod(returns = ReturnType.SINGLE)", "    public Mono<String> third() { return null; }",
                "}"));
        assertEquals(2, events.size());
        assertTrue(events.stream().allMatch(event -> event.getMessage().contains("must not have")));
    }

    @Test
    void serviceInterfaceNamesRetainStringLiteralContents() throws Exception {
        List<AuditEvent> events = inspect(configuration(false, check(ServiceInterfaceCheck.class)),
            source("Services.java", "@ServiceInterface(name = \"\") interface Empty {}",
                "@ServiceInterface(name = \"service\") interface Named {}",
                "@ServiceInterface(name = \"service\\\"quoted\") interface Quoted {}"));
        assertEquals(1, events.size());
        assertEquals(1, events.get(0).getLine());
    }

    @ParameterizedTest
    @ValueSource(strings = { "track2", "clientcore", "vnext" })
    void realConfigurationsLoadTheConsolidatedRules(String profile) throws Exception {
        Path root = Paths.get("").toAbsolutePath();
        while (root != null && !Files.isDirectory(root.resolve("eng/lintingconfigs/checkstyle"))) {
            root = root.getParent();
        }
        assertTrue(root != null, "Cannot locate the repository's Checkstyle configurations");
        Path configPath = root.resolve("eng/lintingconfigs/checkstyle/" + profile + "/checkstyle.xml");
        Properties properties = new Properties();
        properties.setProperty("samedir", temporaryDirectory.toString());
        Configuration configuration = ConfigurationLoader.loadConfiguration(configPath.toString(),
            new PropertiesExpander(properties), ConfigurationLoader.IgnoredModulesOptions.EXECUTE);
        assertTrue(inspect(configuration).isEmpty());
        Configuration walker = Arrays.stream(configuration.getChildren())
            .filter(child -> "TreeWalker".equals(child.getName()))
            .findFirst()
            .get();
        assertEquals(1,
            Arrays.stream(walker.getChildren())
                .filter(child -> PublicApiTypeCheck.class.getName().equals(child.getName()))
                .count());
        assertEquals(1,
            Arrays.stream(walker.getChildren())
                .filter(child -> ExceptionHandlingCheck.class.getName().equals(child.getName()))
                .count());
    }

    private static long countMessagesContaining(List<AuditEvent> events, String message) {
        return events.stream().filter(event -> event.getMessage().contains(message)).count();
    }

    private static DefaultConfiguration check(Class<?> type) {
        return new DefaultConfiguration(type.getName());
    }

    private static DefaultConfiguration configuration(boolean sdkFilter, DefaultConfiguration... checks) {
        DefaultConfiguration configuration = check(Checker.class);
        if (sdkFilter) {
            configuration.addChild(check(SdkCheckFilter.class));
        }
        DefaultConfiguration walker = check(TreeWalker.class);
        for (DefaultConfiguration check : checks) {
            walker.addChild(check);
        }
        configuration.addChild(walker);
        return configuration;
    }

    private Path source(String name, String... lines) throws Exception {
        Path file = temporaryDirectory.resolve(name);
        Files.createDirectories(file.getParent());
        Files.write(file, Arrays.asList(lines), StandardCharsets.UTF_8);
        return file;
    }

    private static List<AuditEvent> inspect(Configuration configuration, Path... files) throws Exception {
        List<AuditEvent> events = new ArrayList<>();
        Checker checker = new Checker();
        checker.setModuleClassLoader(ConsolidatedChecksTest.class.getClassLoader());
        checker.addListener(new AuditListener() {
            @Override
            public void auditStarted(AuditEvent event) {
            }

            @Override
            public void auditFinished(AuditEvent event) {
            }

            @Override
            public void fileStarted(AuditEvent event) {
            }

            @Override
            public void fileFinished(AuditEvent event) {
            }

            @Override
            public void addError(AuditEvent event) {
                events.add(event);
            }

            @Override
            public void addException(AuditEvent event, Throwable throwable) {
                throw new AssertionError(throwable);
            }
        });
        try {
            checker.configure(configuration);
            checker.process(Arrays.stream(files).map(Path::toFile).collect(Collectors.toList()));
        } finally {
            checker.destroy();
        }
        return events;
    }
}

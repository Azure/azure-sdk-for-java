// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.javadoc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.Duration;
import java.util.Collections;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;

class DocumentationClassifierTests {
    private static final String SOURCE = "package example;\npublic class Example {\n"
        + "    /** Gets the name. */\n    public String getName() { return \"name\"; }\n}\n";

    @ParameterizedTest(name = "{0}")
    @MethodSource("documentationEdits")
    void acceptsDocumentationWithoutTrackingAttachment(String name, String before, String after) {
        assertEquals("ast-equivalent", new DocumentationClassifier().compare(before, after), name);
    }

    static Stream<Arguments> documentationEdits() {
        return Stream.of(
            Arguments.of("prose", SOURCE, SOURCE.replace("Gets the name.", "Gets the resource name.")),
            Arguments.of("multiline", SOURCE,
                SOURCE.replace("Gets the name.", "\n     * Gets the name.\n     * More.\n     ")),
            Arguments.of("standard tags", SOURCE, SOURCE.replace("Gets the name.", "Gets the name.\n"
                + "     * @return the {@code String} name\n     * @see java.lang.String")),
            Arguments.of("documentation-only tags", SOURCE,
                SOURCE.replace("Gets the name.", "{@summary Gets the name.} @hidden")),
            Arguments.of("type", "/** Old. */ class Example {}", "/** New. */ class Example {}"),
            Arguments.of("field", "class Example { /** Old. */ int value; }",
                "class Example { /** New. */ int value; }"),
            Arguments.of("constructor", "class Example { /** Old. */ Example() {} }",
                "class Example { /** New. */ Example() {} }"),
            Arguments.of("enum", "enum Example { /** Old. */ VALUE }",
                "enum Example { /** New. */ VALUE }"),
            Arguments.of("annotation member", "@interface Example { /** Old. */ String value(); }",
                "@interface Example { /** New. */ String value(); }"),
            Arguments.of("non-ASCII prose", SOURCE, SOURCE.replace("Gets the name.", "Gets the caf\u00e9 name.")),
            Arguments.of("snippet marker", SOURCE, SOURCE.replace("Gets the name.",
                "Gets the name. <!-- src_embed example.name -->")),
            Arguments.of("orphan documentation", "class Example { void f() { /** Old. */ return; } }",
                "class Example { void f() { /** New. */ return; } }"),
            Arguments.of("new documentation", "class Example {}", "/** Added. */ class Example {}"),
            Arguments.of("removed documentation", "/** Removed. */ class Example {}", "class Example {}"),
            Arguments.of("moved documentation", "class Example { /** Text. */ int a; int b; }",
                "class Example { int a; /** Text. */ int b; }"),
            Arguments.of("Javadoc becomes block", SOURCE, SOURCE.replace("/**", "/*")),
            Arguments.of("block becomes Javadoc", SOURCE.replace("/**", "/*"), SOURCE),
            Arguments.of("Javadoc detached by whitespace", "class Example {\n /** Text. */\n int value;\n}",
                "class Example {\n /** Text. */\n\n int value;\n}"),
            Arguments.of("overloads", "class Example { /** First. */ void f() {}"
                + " /** Second. */ void f(int x) {} }", "class Example {\n /** Revised first. */ void f() {}\n"
                + " /** Revised second. */ void f(int x) {} }"),
            Arguments.of("nested declaration", "class Example { class Inner { /** Old. */ int value; } }",
                "class Example {\n    class Inner {\n        /** New. */ int value;\n    }\n}"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("formattingAndCommentEdits")
    void acceptsFormattingAndOrdinaryComments(String name, String before, String after) {
        assertEquals("ast-equivalent", new DocumentationClassifier().compare(before, after), name);
    }

    static Stream<Arguments> formattingAndCommentEdits() {
        String plain = "class Example { int value = 1; }";
        return Stream.of(
            Arguments.of("space between code tokens", SOURCE, SOURCE.replace("return \"", "return  \"")),
            Arguments.of("indentation", SOURCE, SOURCE.replace("    ", "\t")),
            Arguments.of("line endings", SOURCE, SOURCE.replace("\n", "\r\n")),
            Arguments.of("carriage return lines", SOURCE, SOURCE.replace("\n", "\r")),
            Arguments.of("trailing whitespace", SOURCE, SOURCE.replace(";\n", ";  \n")),
            Arguments.of("leading and trailing whitespace", SOURCE, "\n" + SOURCE + "\n"),
            Arguments.of("no Javadoc required", plain, "class Example {\n    int value = 1;\n}"),
            Arguments.of("ordinary line prose", SOURCE + "// old\n", SOURCE + "// revised\n"),
            Arguments.of("ordinary block prose", plain.replace("int value", "int/* old */ value"),
                plain.replace("int value", "int/* revised */ value")),
            Arguments.of("add block prose", plain, plain.replace("int value", "int/* counts requests */ value")),
            Arguments.of("remove block prose", plain.replace("int value", "int/* counts requests */ value"), plain),
            Arguments.of("add line prose", plain, plain.replace("int value", "// Counts requests.\nint value")),
            Arguments.of("remove line prose", plain.replace("int value", "// Counts requests.\nint value"), plain),
            Arguments.of("multiline prose", plain.replace("int value", "/* First explanation. */int value"),
                plain.replace("int value", "/* More detail.\n * On another line.\n */int value")),
            Arguments.of("TODO prose", plain.replace("int value", "/* TODO: explain */int value"),
                plain.replace("int value", "/* TODO: explain the units */int value")),
            Arguments.of("comment before Javadoc", SOURCE,
                SOURCE.replace("public class", "/* New prose. */public class")),
            Arguments.of("Javadoc and formatting", SOURCE,
                SOURCE.replace("Gets the name.", "Gets the resource name.").replace("    ", "\t")),
            Arguments.of("Javadoc and prose", SOURCE,
                SOURCE.replace("Gets the name.", "Gets the resource name.")
                    .replace("return ", "/* Explain. */return ")),
            Arguments.of("directive-shaped string", "class Example { String value = \"@generate GEN-BEGIN\"; }",
                "class Example {  String value = \"@generate GEN-BEGIN\"; }"),
            Arguments.of("directive-shaped character", "class Example { char value = '#'; }",
                "class Example {  char value = '#'; }"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("syntaxNormalizations")
    void labelsPrinterNormalizationAsAstEquivalence(String name, String before, String after) {
        assertEquals("ast-equivalent", new DocumentationClassifier().compare(before, after), name);
    }

    static Stream<Arguments> syntaxNormalizations() {
        return Stream.of(
            Arguments.of("array field", "class Example { int[] value; }", "class Example { int value[]; }"),
            Arguments.of("array parameter", "class Example { void f(int[] value) {} }",
                "class Example { void f(int value[]) {} }"),
            Arguments.of("array return", "class Example { int[] f() { return null; } }",
                "class Example { int f()[] { return null; } }"),
            Arguments.of("empty declarations", "class Example {}", "; class Example {} ;"),
            Arguments.of("normalization and documentation", "/** Old. */ class Example { int[] value; }",
                "/** New. */ class Example { int value[]; }"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("codeEdits")
    void rejectsChangedCode(String name, String before, String after) {
        assertEquals("non-documentation-change", new DocumentationClassifier().compare(before, after), name);
    }

    static Stream<Arguments> codeEdits() {
        String documented = SOURCE.replace("Gets the name.", "Gets the resource name.");
        return Stream.of(
            Arguments.of("literal", SOURCE, documented.replace("\"name\"", "\"other\"")),
            Arguments.of("annotation", SOURCE, documented.replace("public String", "@Deprecated public String")),
            Arguments.of("modifier", SOURCE, documented.replace("public String", "private String")),
            Arguments.of("method name", SOURCE, documented.replace("getName()", "getResourceName()")),
            Arguments.of("import", SOURCE,
                documented.replace("package example;\n", "package example;\nimport java.util.List;\n")),
            Arguments.of("import order", "import java.util.List; import java.util.Set; class Example {}",
                "import java.util.Set; import java.util.List; class Example {}"),
            Arguments.of("string resembling comment",
                SOURCE.replace("\"name\"", "\"/* old */\""), documented.replace("\"name\"", "\"/* new */\"")),
            Arguments.of("annotation value", "@A(\"old\") class Example { /** Old. */ int value; }",
                "@A(\"new\") class Example { /** New. */ int value; }"),
            Arguments.of("comment terminator", SOURCE,
                SOURCE.replace("Gets the name.", "Text. */ int value; /** More.")),
            Arguments.of("string whitespace", SOURCE, SOURCE.replace("\"name\"", "\" name \"")),
            Arguments.of("character whitespace", "class Example { char c = ' '; }",
                "class Example { char c = '\\t'; }"),
            Arguments.of("commented out code", "class Example { int first; int second; }",
                "class Example { /* int first; */ int second; }"),
            Arguments.of("line ending exposes code", "class Example {\n // explanation int first;\n int second;\n}",
                "class Example {\n // explanation\n int first;\n int second;\n}"),
            Arguments.of("line ending hides code", "class Example {\n // explanation\n int first;\n int second;\n}",
                "class Example {\n // explanation int first;\n int second;\n}"),
            Arguments.of("valid operator token merging", "class Example { void f(int a, int b) { a = a + +b; } }",
                "class Example { void f(int a, int b) { a = a++ + b; } }"),
            Arguments.of("prefix and postfix", "class Example { int f(int x) { return ++x; } }",
                "class Example { int f(int x) { return x++; } }"),
            Arguments.of("Javadoc cannot hide a later code edit", SOURCE,
                documented.replace("\"name\"", "\"other\"")));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("compilerSensitiveEdits")
    void keepsUnicodeProtectionAndDelegatesDeprecation(String name, String before, String after, String reason) {
        assertEquals(reason, new DocumentationClassifier().compare(before, after), name);
    }

    static Stream<Arguments> compilerSensitiveEdits() {
        String deprecated = SOURCE.replace("Gets the name.", "@deprecated Old.");
        String plain = "class Example { int f() {\n int x = 1;\n // ordinary\n return x;\n } }";
        return Stream.of(
            Arguments.of("Unicode escape added", SOURCE, SOURCE.replace("Gets the name.", "\\u000a Not supported."),
                "unsupported-unicode-escape"),
            Arguments.of("Unicode escape removed", SOURCE.replace("Gets the name.", "\\u0041"), SOURCE,
                "unsupported-unicode-escape"),
            Arguments.of("Unicode exposes executable code", plain,
                plain.replace("// ordinary", "// ordinary \\u000a x=2;"),
                "unsupported-unicode-escape"),
            Arguments.of("deprecation added", SOURCE, deprecated, "ast-equivalent"),
            Arguments.of("deprecation removed", deprecated, SOURCE, "ast-equivalent"),
            Arguments.of("deprecation explanation", deprecated, deprecated.replace("Old.", "New."),
                "ast-equivalent"),
            Arguments.of("unchanged deprecation and formatting", deprecated, deprecated.replace("    ", "\t"),
                "ast-equivalent"),
            Arguments.of("deprecation moved", "class Example { /** @deprecated Old. */ int first; int second; }",
                "class Example { int first; /** @deprecated Old. */ int second; }", "ast-equivalent"),
            Arguments.of("deprecation-like literal",
                "class Example { String value = \"@deprecated\"; }",
                "class Example {  String value = \"@deprecated\"; }", "ast-equivalent"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("staticDirectiveEdits")
    void leavesKnownStaticAndDocumentationDirectivesToExistingChecks(String name, String before, String after) {
        assertEquals("ast-equivalent", new DocumentationClassifier().compare(before, after), name);
    }

    static Stream<Arguments> staticDirectiveEdits() {
        String plain = "class Example {\n int value;\n}";
        String marked = "class Example {\n // BEGIN: example\n int value;\n // END: example\n}";
        String suppressed = "class Example {\n int value; // NOSONAR\n}";
        return Stream.of(
            Arguments.of("snippet renamed", marked, marked.replace("BEGIN: example", "BEGIN: another")),
            Arguments.of("snippet removed", marked, plain),
            Arguments.of("snippet added", plain, marked),
            Arguments.of("snippet moved", marked,
                "class Example {\n int value;\n // BEGIN: example\n // END: example\n}"),
            Arguments.of("suppression changed lines", suppressed, suppressed.replace("; //", ";\n //")),
            Arguments.of("formatter changed", plain.replace("int value;", "// @formatter:off\n int value;"),
                plain.replace("int value;", "// @formatter:on\n int value;")),
            Arguments.of("inspection added", plain, plain.replace("int value;", "// noinspection unused\n int value;")),
            Arguments.of("block suppression", plain, plain.replace("int value;", "/* CHECKSTYLE:OFF */int value;")),
            Arguments.of("spelling configuration", plain,
                plain.replace("int value;", "// cspell:ignore value\n int value;")),
            Arguments.of("editor folding", plain,
                plain.replace("int value;", "// <editor-fold desc=\"value\">\n int value;")),
            Arguments.of("snippet HTML", plain,
                plain.replace("int value;", "/* <!-- src_embed example.value --> */int value;")),
            Arguments.of("generated-origin banner", plain, "// Code generated by Example. DO NOT EDIT.\n" + plain),
            Arguments.of("localization suppression", plain,
                plain.replace("int value;", "// $NON-NLS-1$\n int value;")));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("unclassifiedDirectiveEdits")
    void delegatesCommentDirectivesToValidationAndPathRules(String name, String before, String after) {
        assertEquals("ast-equivalent", new DocumentationClassifier().compare(before, after), name);
    }

    static Stream<Arguments> unclassifiedDirectiveEdits() {
        String plain = "class Example {\n int value;\n}";
        String marked = plain.replace("int value;", "// GEN-BEGIN: value\n int value;\n // GEN-END: value");
        return Stream.of(
            Arguments.of("generation marker added", plain, marked),
            Arguments.of("unchanged generation marker", marked, marked.replace("int value", "int  value")),
            Arguments.of("unknown directive", plain,
                plain.replace("int value;", "// custom-generator: enabled\n int value;")),
            Arguments.of("multiline directive", plain,
                plain.replace("int value;", "/*\n * custom-generator\n: enabled\n */ int value;")),
            Arguments.of("annotation-style directive", plain,
                plain.replace("int value;", "// @generate value\n int value;")),
            Arguments.of("preprocessor directive", plain, plain.replace("int value;", "// #if feature\n int value;")),
            Arguments.of("custom block tag", SOURCE, SOURCE.replace("Gets the name.", "@custom generate something")),
            Arguments.of("custom inline tag", SOURCE, SOURCE.replace("Gets the name.", "{@custom generate}")),
            Arguments.of("unknown HTML directive", plain,
                plain.replace("int value;", "/* <!-- custom generate --> */int value;")),
            Arguments.of("generated region", plain, plain.replace("int value;", "// <generated>\n int value;")),
            Arguments.of("unclassified region consumer", plain,
                plain.replace("int value;", "// tag::value[]\n int value;")),
            Arguments.of("known marker cannot mask generation", plain,
                plain.replace("int value;", "// @formatter:off @generate value\n int value;")),
            Arguments.of("Javadoc generation marker", SOURCE, SOURCE.replace("Gets the name.", "GEN-BEGIN: value")));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidSources")
    void rejectsUnsuccessfulAndUnsupportedParses(String name, String before, String after) {
        assertEquals("parse-error", new DocumentationClassifier().compare(before, after), name);
    }

    static Stream<Arguments> invalidSources() {
        return Stream.of(
            Arguments.of("malformed Java", SOURCE, SOURCE.replace("return \"name\";", "return ; broken")),
            Arguments.of("partial parse", "class Broken {", SOURCE),
            Arguments.of("invalid operator merging", "class Example { int f(int x) { return x + +1; } }",
                "class Example { int f(int x) { return x ++1; } }"),
            Arguments.of("identifier token merging", "class Example { int a; }", "class Example { inta; }"),
            Arguments.of("text block", SOURCE, SOURCE.replace("\"name\"", "\"\"\"\nname\n\"\"\"")),
            Arguments.of("record", "class Example {}", "record Example() {}"));
    }

    @Test
    void requiresAnActualSourceChange() {
        assertEquals("no-source-edit", new DocumentationClassifier().compare(SOURCE, SOURCE));
        assertFalse(DocumentationClassifier.isEligible("no-source-edit"));
        assertFalse(DocumentationClassifier.isEligible("whitespace-only"));
    }

    @Test
    void comparesCodeAfterDifferentCommentEdits() {
        DocumentationClassifier classifier = new DocumentationClassifier();
        assertEquals("ast-equivalent", classifier.compare(SOURCE,
            SOURCE.replace("Gets the name.", "@generate value")));
        assertEquals("ast-equivalent",
            classifier.compare(SOURCE, SOURCE.replace("Gets the name.", "New description.")));
        assertEquals("non-documentation-change",
            classifier.compare(SOURCE, SOURCE.replace("\"name\"", "\"other\"")));
    }

    @Test
    void parsesLongCommentWhitespaceAndBlankLines() {
        String spaces = String.join("", Collections.nCopies(64 * 1024, " "));
        String before = "/*" + spaces + String.join("", Collections.nCopies(64 * 1024, "\n"))
            + "*/ class Example {}";
        assertTimeoutPreemptively(Duration.ofSeconds(5), () ->
            assertEquals("ast-equivalent", new DocumentationClassifier().compare(before, before + "\n")));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("existingDocumentationMarkers")
    void acceptsExistingJavadocReferencesAndSnippetMarkers(String name, String before, String after) {
        assertEquals("ast-equivalent", new DocumentationClassifier().compare(before, after), name);
    }

    static Stream<Arguments> existingDocumentationMarkers() {
        return Stream.of(
            Arguments.of("method reference", SOURCE,
                SOURCE.replace("Gets the name.", "See {@link #getName()} for the name.")),
            Arguments.of("query option", SOURCE,
                SOURCE.replace("Gets the name.", "Use $filter to select matching resources.")),
            Arguments.of("snippet closing marker", SOURCE,
                SOURCE.replace("Gets the name.", "<!-- src_embed example.name -->Code.<!-- end example.name -->")),
            Arguments.of("annotated deprecation explanation",
                SOURCE.replace("public String", "@Deprecated public String")
                    .replace("Gets the name.", "@deprecated Old explanation."),
                SOURCE.replace("public String", "@Deprecated public String")
                    .replace("Gets the name.", "@deprecated New explanation.")));
    }
}

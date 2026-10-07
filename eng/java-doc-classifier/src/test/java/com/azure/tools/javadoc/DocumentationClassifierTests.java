// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.javadoc;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class DocumentationClassifierTests {
    private static final String SOURCE = "package example;\npublic class Example {\n"
        + "    /** Gets the name. */\n    public String getName() { return \"name\"; }\n}\n";

    @ParameterizedTest(name = "{0}")
    @MethodSource("documentationEdits")
    void acceptsSupportedDocumentation(String name, String before, String after) {
        assertEquals("javadoc-only", new DocumentationClassifier().compare(before, after), name);
    }

    static Stream<Arguments> documentationEdits() {
        return Stream.of(
            Arguments.of("prose", SOURCE, SOURCE.replace("Gets the name.", "Gets the resource name.")),
            Arguments.of("multiline", SOURCE,
                SOURCE.replace("Gets the name.", "\n     * Gets the name.\n     * More.\n     ")),
            Arguments.of("standard tags", SOURCE, SOURCE.replace("Gets the name.", "Gets the name.\n"
                + "     * @return the {@code String} name\n     * @see java.lang.String")),
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
                "Gets the name. <!-- src_embed example.name -->")));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("formattingAndCommentEdits")
    void acceptsFormattingAndOrdinaryComments(String name, String before, String after, String expected) {
        assertEquals(expected, new DocumentationClassifier().compare(before, after), name);
    }

    static Stream<Arguments> formattingAndCommentEdits() {
        String plain = "class Example { int value = 1; }";
        String directive = "class Example {\n    // @formatter:off\n    int value;\n}";
        String deprecated = "class Example {\n    /**\n     * @deprecated Old API.\n     */\n    int value;\n}";
        return Stream.of(
            Arguments.of("space between code tokens", SOURCE, SOURCE.replace("return \"", "return  \""),
                "whitespace-only"),
            Arguments.of("indentation", SOURCE, SOURCE.replace("    ", "\t"), "whitespace-only"),
            Arguments.of("line endings", SOURCE, SOURCE.replace("\n", "\r\n"), "whitespace-only"),
            Arguments.of("carriage return lines", SOURCE, SOURCE.replace("\n", "\r"), "whitespace-only"),
            Arguments.of("trailing whitespace", SOURCE, SOURCE.replace(";\n", ";  \n"), "whitespace-only"),
            Arguments.of("leading and trailing whitespace", SOURCE, "\n" + SOURCE + "\n", "whitespace-only"),
            Arguments.of("no Javadoc required", plain, "class Example {\n    int value = 1;\n}", "whitespace-only"),
            Arguments.of("ordinary line prose", SOURCE + "// old\n", SOURCE + "// revised\n", "ordinary-comment-only"),
            Arguments.of("ordinary block prose", plain.replace("int value", "int/* old */ value"),
                plain.replace("int value", "int/* revised */ value"), "ordinary-comment-only"),
            Arguments.of("add block prose", plain, plain.replace("int value", "int/* counts requests */ value"),
                "ordinary-comment-only"),
            Arguments.of("remove block prose", plain.replace("int value", "int/* counts requests */ value"), plain,
                "ordinary-comment-only"),
            Arguments.of("add line prose", plain, plain.replace("int value", "// Counts requests.\nint value"),
                "non-code-only"),
            Arguments.of("remove line prose", plain.replace("int value", "// Counts requests.\nint value"), plain,
                "non-code-only"),
            Arguments.of("multiline prose", plain.replace("int value", "/* First explanation. */int value"),
                plain.replace("int value", "/* More detail.\n * On another line.\n */int value"),
                "ordinary-comment-only"),
            Arguments.of("TODO prose", plain.replace("int value", "/* TODO: explain */int value"),
                plain.replace("int value", "/* TODO: explain the units */int value"), "ordinary-comment-only"),
            Arguments.of("comment before Javadoc", SOURCE,
                SOURCE.replace("public class", "/* New prose. */public class"),
                "ordinary-comment-only"),
            Arguments.of("Javadoc and formatting", SOURCE,
                SOURCE.replace("Gets the name.", "Gets the resource name.").replace("    ", "\t"), "non-code-only"),
            Arguments.of("Javadoc and prose", SOURCE,
                SOURCE.replace("Gets the name.", "Gets the resource name.").replace("return ", "/* Explain. */return "),
                "non-code-only"),
            Arguments.of("unchanged directive", directive, directive.replace("int value", "int  value"),
                "whitespace-only"),
            Arguments.of("unchanged directive line endings", directive, directive.replace("\n", "\r\n"),
                "whitespace-only"),
            Arguments.of("unchanged deprecation line endings", deprecated, deprecated.replace("\n", "\r\n"),
                "whitespace-only"),
            Arguments.of("overload declaration anchors", "class Example { /** First. */ void f() {}"
                + " /** Second. */ void f(int x) {} }", "class Example {\n /** Revised first. */ void f() {}\n"
                + " /** Revised second. */ void f(int x) {} }", "non-code-only"),
            Arguments.of("nested declaration anchors", "class Example { class Inner { /** Old. */ int value; } }",
                "class Example {\n    class Inner {\n        /** New. */ int value;\n    }\n}", "non-code-only"));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("nonDocumentationEdits")
    void rejectsOtherChanges(String name, String before, String after) {
        String result = new DocumentationClassifier().compare(before, after);
        assertFalse(Arrays.asList("javadoc-only", "ordinary-comment-only", "whitespace-only", "non-code-only")
            .contains(result), name + ": " + result);
    }

    static Stream<Arguments> nonDocumentationEdits() {
        String documented = SOURCE.replace("Gets the name.", "Gets the resource name.");
        return Stream.of(
            Arguments.of("literal", SOURCE, documented.replace("\"name\"", "\"other\"")),
            Arguments.of("annotation", SOURCE, documented.replace("public String", "@Deprecated public String")),
            Arguments.of("modifier", SOURCE, documented.replace("public String", "private String")),
            Arguments.of("method name", SOURCE, documented.replace("getName()", "getResourceName()")),
            Arguments.of("import", SOURCE, "import java.util.List;\n" + documented),
            Arguments.of("string resembling comment",
                SOURCE.replace("\"name\"", "\"/* old */\""), documented.replace("\"name\"", "\"/* new */\"")),
            Arguments.of("deprecation added", SOURCE, SOURCE.replace("Gets the name.", "@deprecated Old.")),
            Arguments.of("deprecation removed", SOURCE.replace("Gets the name.", "@deprecated Old."), SOURCE),
            Arguments.of("deprecation explanation", SOURCE.replace("Gets the name.", "@deprecated Old."),
                SOURCE.replace("Gets the name.", "@deprecated New.")),
            Arguments.of("custom block tag", SOURCE, SOURCE.replace("Gets the name.", "@custom generate something")),
            Arguments.of("custom inline tag", SOURCE, SOURCE.replace("Gets the name.", "{@custom generate}")),
            Arguments.of("orphan documentation", "class Example { void f() { /** Old. */ return; } }",
                "class Example { void f() { /** New. */ return; } }"),
            Arguments.of("new documentation", "class Example {}", "/** Added. */ class Example {}"),
            Arguments.of("removed documentation", "/** Removed. */ class Example {}", "class Example {}"),
            Arguments.of("moved documentation", "class Example { /** Text. */ int a; int b; }",
                "class Example { int a; /** Text. */ int b; }"),
            Arguments.of("malformed Java", SOURCE, documented.replace("return \"name\";", "return ; broken")),
            Arguments.of("comment terminator", SOURCE,
                SOURCE.replace("Gets the name.", "Text. */ int value; /** More.")),
            Arguments.of("text block", SOURCE, documented.replace("\"name\"", "\"\"\"\nname\n\"\"\"")),
            Arguments.of("record", "record Example() {}", "/** New. */ record Example() {}"),
            Arguments.of("Unicode escape", SOURCE, SOURCE.replace("Gets the name.", "\\u000a Not supported.")),
            Arguments.of("Unicode escape before", SOURCE.replace("Gets the name.", "\\u0041"), SOURCE),
            Arguments.of("annotation value", "@A(\"old\") class Example { /** Old. */ int value; }",
                "@A(\"new\") class Example { /** New. */ int value; }"),
            Arguments.of("string whitespace", SOURCE, SOURCE.replace("\"name\"", "\" name \"")),
            Arguments.of("character whitespace", "class Example { char c = ' '; }",
                "class Example { char c = '\\t'; }"),
            Arguments.of("commented out code", "class Example { int first; int second; }",
                "class Example { /* int first; */ int second; }"),
            Arguments.of("line ending exposes code", "class Example {\n // explanation int first;\n int second;\n}",
                "class Example {\n // explanation\n int first;\n int second;\n}"),
            Arguments.of("line ending hides code", "class Example {\n // explanation\n int first;\n int second;\n}",
                "class Example {\n // explanation int first;\n int second;\n}"),
            Arguments.of("operator token merging", "class Example { int f(int x) { return x + +1; } }",
                "class Example { int f(int x) { return x ++1; } }"),
            Arguments.of("valid operator token merging", "class Example { void f(int a, int b) { a = a + +b; } }",
                "class Example { void f(int a, int b) { a = a++ + b; } }"),
            Arguments.of("identifier token merging", "class Example { int a; }", "class Example { inta; }"),
            Arguments.of("Javadoc becomes block", SOURCE, SOURCE.replace("/**", "/*")),
            Arguments.of("block becomes Javadoc", SOURCE.replace("/**", "/*"), SOURCE),
            Arguments.of("Javadoc detached by whitespace", "class Example {\n /** Text. */\n int value;\n}",
                "class Example {\n /** Text. */\n\n int value;\n}"),
            Arguments.of("deprecation moved", "class Example { /** @deprecated Old. */ int first; int second; }",
                "class Example { int first; /** @deprecated Old. */ int second; }"),
            Arguments.of("unchanged", SOURCE, SOURCE));
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("directiveEdits")
    void preservesToolDirectives(String name, String before, String after) {
        assertEquals("unsupported-comment-directive", new DocumentationClassifier().compare(before, after), name);
    }

    static Stream<Arguments> directiveEdits() {
        String plain = "class Example {\n int value;\n}";
        String marked = "class Example {\n // BEGIN: example\n int value;\n // END: example\n}";
        String suppressed = "class Example {\n int value; // NOSONAR\n}";
        return Stream.of(
            Arguments.of("snippet renamed", marked, marked.replace("BEGIN: example", "BEGIN: another")),
            Arguments.of("snippet removed", marked, plain),
            Arguments.of("snippet added", plain, marked),
            Arguments.of("directive moved", marked,
                "class Example {\n int value;\n // BEGIN: example\n // END: example\n}"),
            Arguments.of("suppression changed lines", suppressed, suppressed.replace("; //", ";\n //")),
            Arguments.of("formatter changed", plain.replace("int value;", "// @formatter:off\n int value;"),
                plain.replace("int value;", "// @formatter:on\n int value;")),
            Arguments.of("inspection added", plain, plain.replace("int value;", "// noinspection unused\n int value;")),
            Arguments.of("block directive", plain, plain.replace("int value;", "/* CHECKSTYLE:OFF */int value;")),
            Arguments.of("unknown directive", plain, plain.replace("int value;", "// custom-generator: enabled\n"
                + " int value;")),
            Arguments.of("annotation-style directive", plain, plain.replace("int value;", "// @generate value\n"
                + " int value;")),
            Arguments.of("preprocessor directive", plain, plain.replace("int value;", "// #if feature\n int value;")),
            Arguments.of("editor directive", plain, plain.replace("int value;", "// <editor-fold desc=\"value\">\n"
                + " int value;")),
            Arguments.of("HTML injection marker", plain, plain.replace("int value;",
                "/* <!-- src_embed example.value --> */int value;")),
            Arguments.of("generated banner", plain, "// Code generated by Example. DO NOT EDIT.\n" + plain),
            Arguments.of("HTML directive", plain,
                plain.replace("int value;", "/* <!-- custom generate --> */int value;")),
            Arguments.of("dollar directive", plain, plain.replace("int value;", "// $NON-NLS-1$\n int value;")));
    }

    @Test
    void doesNotLetEarlierEligibleDocumentationHideLaterCodeChanges() {
        assertEquals("non-documentation-change", new DocumentationClassifier().compare(SOURCE,
            SOURCE.replace("Gets the name.", "New documentation.").replace("\"name\"", "\"other\"")));
    }

    @Test
    void requiresAnActualSourceChange() {
        assertEquals("no-source-edit", new DocumentationClassifier().compare(SOURCE, SOURCE));
    }
}

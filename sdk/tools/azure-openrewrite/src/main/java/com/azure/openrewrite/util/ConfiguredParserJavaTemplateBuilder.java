package com.azure.openrewrite.util;

import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;

public class ConfiguredParserJavaTemplateBuilder {

    private JavaParser.Builder parser;


    /**
     * Creates a new instance of {@link ConfiguredParserJavaTemplateBuilder} with the default parser.
     *
     * @return A new instance of {@link ConfiguredParserJavaTemplateBuilder} with the default parser.
     */
    public static ConfiguredParserJavaTemplateBuilder defaultBuilder() {
        return new ConfiguredParserJavaTemplateBuilder(
            JavaParser.fromJavaVersion()
                .classpath(JavaParser.runtimeClasspath())
        );
    }

    /**
     * Creates a new instance of {@link ConfiguredParserJavaTemplateBuilder} with the specified parser.
     *
     * @param parser The JavaParser to use for creating JavaTemplate instances.
     */
    public ConfiguredParserJavaTemplateBuilder(JavaParser.Builder parser) {
        this.parser = parser;
    }

    /**
     * Sets the JavaParser to use for creating JavaTemplate instances.
     *
     * @param parser The JavaParser to use.
     * @return The current instance of {@link ConfiguredParserJavaTemplateBuilder}.
     */
    public JavaTemplate.Builder getJavaTemplateBuilder(String code) {
        return JavaTemplate.builder(code)
            .javaParser(parser);
    }
}

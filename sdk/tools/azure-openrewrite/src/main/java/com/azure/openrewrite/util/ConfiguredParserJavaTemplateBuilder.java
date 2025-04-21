package com.azure.openrewrite.util;

import org.openrewrite.java.JavaParser;
import org.openrewrite.java.JavaTemplate;

public class ConfiguredParserJavaTemplateBuilder {

    private JavaParser.Builder parser;

    public static ConfiguredParserJavaTemplateBuilder defaultBuilder() {
        return new ConfiguredParserJavaTemplateBuilder(
            JavaParser.fromJavaVersion()
                .classpath(JavaParser.runtimeClasspath())
        );
    }

    public ConfiguredParserJavaTemplateBuilder(JavaParser.Builder parser) {
        this.parser = parser;
    }

    public JavaTemplate.Builder getJavaTemplateBuilder(String code) {
        return JavaTemplate.builder(code)
            .javaParser(parser);
    }
}
